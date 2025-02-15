package com.ispan.eeit188_final.service;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ispan.eeit188_final.dto.PaymentDTO;
import com.ispan.eeit188_final.dto.TicketDTO;
import com.ispan.eeit188_final.dto.TranscationRecordDTO;
import com.ispan.eeit188_final.model.Coupon;
import com.ispan.eeit188_final.model.House;
import com.ispan.eeit188_final.model.Ticket;
import com.ispan.eeit188_final.model.TransactionRecord;
import com.ispan.eeit188_final.model.User;
import com.ispan.eeit188_final.repository.CouponRepository;
import com.ispan.eeit188_final.repository.HouseRepository;
import com.ispan.eeit188_final.repository.UserRepository;

import ecpay.payment.integration.AllInOne;
import ecpay.payment.integration.domain.AioCheckOutALL;

@Service
public class PaymentService {

    @Value("${eeit188final.payment.platform-commission}")
    private Double platformCommission; // 0 ~ 100 (%)

    @Autowired
    private HouseRepository houseRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CouponRepository couponRepo;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TransactionRecordService transactionRecordService;

    public String createOrder(PaymentDTO paymentDTO) throws UnsupportedEncodingException {
        // 檢查 User 和 House ID 是否有效
        if (paymentDTO.getUserId() == null || paymentDTO.getHouseId() == null) {
            throw new IllegalArgumentException("Invalid User or House ID.");
        }

        Optional<House> findHouse = houseRepo.findById(paymentDTO.getHouseId());
        Optional<User> findUser = userRepo.findById(paymentDTO.getUserId());

        if (!findHouse.isPresent()) {
            throw new NoSuchElementException("找不到房源");
        }

        if (!findUser.isPresent()) {
            throw new NoSuchElementException("找不到使用者");
        }

        // 確認日期範圍是否有效
        Timestamp[] dateRange = paymentDTO.getDateRange();
        if (dateRange == null || dateRange.length != 2) {
            throw new IllegalArgumentException("非法的日期區間");
        }

        Timestamp start = dateRange[0];
        Timestamp end = dateRange[1];

        // 確認開始日期必須早於或等於結束日期
        if (start.after(end)) {
            throw new IllegalArgumentException("起始日期不能晚於結束日期");
        }

        // 計算訂房的金額和天數

        // 將 Timestamp 轉換為 LocalDate
        LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // 計算訂房的金額和天數
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1; // +1 以包含結束日
        if (daysBetween <= 0) {
            throw new IllegalArgumentException("非法的日期區間，起始日期不能晚於結束日期");
        }

        // 檢查房屋在指定日期範圍內是否可用
        Boolean isAvailable = ticketService.isHouseAvailable(findHouse.get(), start, end);
        if (!isAvailable) {
            throw new IllegalStateException("目前房源不可預定");
        }

		// 抓取house的price，如果是null則設為1000
		Integer currentAmount = (findHouse.get().getPrice() != null ? findHouse.get().getPrice() : 1000) * (int) daysBetween;

        // 計算平台抽成
        Integer platformIncome = (int) (currentAmount * (platformCommission != null ? platformCommission : 0));
        Integer totalAmountWithCommission = currentAmount + platformIncome;

        // 應用優惠券折扣
        if (paymentDTO.getCouponId() != null) {
            Optional<Coupon> findCoupon = couponRepo.findById(paymentDTO.getCouponId());
            if (findCoupon.isPresent()) {
                Coupon coupon = findCoupon.get();

                // 檢查是固定金額折扣還是百分比折扣
                if (coupon.getDiscountRate() != null && coupon.getDiscountRate() > 0) {
                    // 應用百分比折扣
                    totalAmountWithCommission -= (int) (totalAmountWithCommission * coupon.getDiscountRate());
                } else if (coupon.getDiscount() != null && coupon.getDiscount() > 0) {
                    // 應用固定金額折扣
                    totalAmountWithCommission -= coupon.getDiscount();
                }

                // 確保金額不會低於0
                totalAmountWithCommission = Math.max(totalAmountWithCommission, 0);
            }
        }

        Integer finalAmount = totalAmountWithCommission;

        // 如果沒有指定入住人數，或人數<0，帶入0
        Integer people = paymentDTO.getPeople() == null || paymentDTO.getPeople() <= 0 ? 0 : paymentDTO.getPeople();

        // 抓取createdAt，如果是null會繼續照一般流程走
        Timestamp createdAt = paymentDTO.getCreatedAt();
        
        // 建立 交易紀錄
        TranscationRecordDTO transcationRecordDTO = createTransactionRecord(findHouse.get(), findUser.get(),
                finalAmount, platformIncome, createdAt); // 假資料用的createdAt如果是null會繼續照一般流程走
        TransactionRecord transactionRecord = transactionRecordService.create(transcationRecordDTO);

        // 建立 票券
        TicketDTO ticketDTO = createTicket(findHouse.get(), findUser.get(), transactionRecord, start, end, people);
        Ticket ticket = ticketService.create(ticketDTO);

        // 檢查是否成功創建交易和票券
        if (transactionRecord == null || ticket == null) {
            throw new IllegalStateException("無法建立交易紀錄或票券");
        }

        // 使用 UUID 生成唯一的訂單號
        String uniqueId = generateUniqueTradeNo();

        // 調用綠界支付 API
        String ecpayPostForm = generateECPayForm(uniqueId, finalAmount, findHouse.get().getName());

        // 返回自動提交表單的 HTML
        return createAutoSubmitForm(ecpayPostForm);
    }

    // 設置交易紀錄
    private TranscationRecordDTO createTransactionRecord(House house, User user, Integer cashFlow,
            Integer platformIncome) {
        return TranscationRecordDTO.builder()
                .houseId(house.getId())
                .userId(user.getId())
                .deal("確認付款中")
                .cashFlow(cashFlow)
                .platformIncome(platformIncome)
                .build();
    }

	// 設置交易紀錄(假資料)，如果沒有自訂createdAt，則呼叫一般的method
	private TranscationRecordDTO createTransactionRecord(House house, User user, Integer cashFlow,
			Integer platformIncome, Timestamp createdAt) {
		if(createdAt!=null) {
			return TranscationRecordDTO.builder()
	                .houseId(house.getId())
	                .userId(user.getId())
	                .deal("確認付款中")
	                .cashFlow(cashFlow)
	                .platformIncome(platformIncome)
	                .createdAt(createdAt)
	                .build();
		}
		return createTransactionRecord(house, user, cashFlow, platformIncome);
    }
    
    // 設置票券
    private TicketDTO createTicket(House house, User user, TransactionRecord transactionRecord, Timestamp start,
            Timestamp end, Integer people) {
        return TicketDTO.builder()
                .startedAt(start)
                .endedAt(end)
                .houseId(house.getId())
                .userId(user.getId())
                .transactionRecordId(transactionRecord.getId())
                .people(people)
                .build();
    }

    // 生成唯一的訂單號
    private String generateUniqueTradeNo() {
        String prefix = "EEIT188NOMAD";
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return prefix + uuid.substring(0, 20 - prefix.length());
    }

    // 生成綠界支付表單
    private String generateECPayForm(String tradeNo, Integer amount, String houseName)
            throws UnsupportedEncodingException {
        AllInOne allInOne = new AllInOne("");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String timestampAsString = formatter.format(timestamp.toLocalDateTime());

        AioCheckOutALL aio = new AioCheckOutALL();
        aio.setMerchantTradeNo(tradeNo);
        aio.setMerchantTradeDate(timestampAsString);
        aio.setTotalAmount(amount.toString());
        aio.setTradeDesc("Payment for Nomad: 預訂房源[" + houseName + "]");
        aio.setItemName("Nomad: 預訂房源[" + houseName + "]");
        aio.setReturnURL("https://yourwebsite.com/return");

        return allInOne.aioCheckOut(aio, null);
    }

    // 生成自動提交表單的 HTML
    private String createAutoSubmitForm(String ecpayPostForm) {
        return "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>NOMAD支付</title>" +
                "</head>" +
                "<body onload='document.forms[\"ecpayForm\"].submit()'>" +
                "<div style=\"text-align: center; width: 100%;\">" +
                "<p>即將跳轉至綠界支付，請稍候...</p>" +
                "</div>" +
                ecpayPostForm +
                "</body>" +
                "</html>";
    }

}
