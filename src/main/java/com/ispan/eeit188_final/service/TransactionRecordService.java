package com.ispan.eeit188_final.service;

import com.ispan.eeit188_final.dto.TranscationRecordDTO;
import com.ispan.eeit188_final.model.House;
import com.ispan.eeit188_final.model.TransactionRecord;
import com.ispan.eeit188_final.model.User;
import com.ispan.eeit188_final.repository.HouseRepository;
import com.ispan.eeit188_final.repository.TransactionRecordRepository;
import com.ispan.eeit188_final.repository.UserRepository;
import com.ispan.eeit188_final.repository.specification.TranscationRecordSpecification;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.ispan.eeit188_final.dto.TranscationRecordDTO;
import com.ispan.eeit188_final.model.House;
import com.ispan.eeit188_final.model.TransactionRecord;
import com.ispan.eeit188_final.model.User;
import com.ispan.eeit188_final.repository.HouseRepository;
import com.ispan.eeit188_final.repository.TransactionRecordRepository;
import com.ispan.eeit188_final.repository.UserRepository;
import com.ispan.eeit188_final.repository.specification.TranscationRecordSpecification;

@Service
public class TransactionRecordService {
    // 預設值
    private static final Integer PAGEABLE_DEFAULT_PAGE = 0;
    private static final Integer PAGEABLE_DEFAULT_LIMIT = 1000;

    private static final Integer COMMISSION = 5; // 平台抽成比例，單位(%)

    @Autowired
    private TransactionRecordRepository transactionRecordRepo;

    @Autowired
    private HouseRepository houseRepo;

    @Autowired
    private UserRepository userRepo;

    // Create a new transaction record
    public TransactionRecord create(TranscationRecordDTO dto) {
        if (dto.getUserId() != null && dto.getHouseId() != null) {
            Optional<House> findHouse = houseRepo.findById(dto.getHouseId());
            Optional<User> findUser = userRepo.findById(dto.getUserId());
            if (findHouse.isPresent() && findUser.isPresent()) {
                // System.out.println(dto.getCreatedAt());
                TransactionRecord create = TransactionRecord.builder()
                        .house(findHouse.get())
                        .user(findUser.get())
                        .cashFlow(dto.getCashFlow())
                        .deal(dto.getDeal())
                        .platformIncome(dto.getPlatformIncome())
                        .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt()
                                : new Timestamp(System.currentTimeMillis()))
                        .build();
                return transactionRecordRepo.save(create);
            }
        }
        return null;
    }

    // public TransactionRecord modify(UUID id, TranscationRecordDTO dto) {
    // if (id != null) {
    // Optional<TransactionRecord> find = transactionRecordRepo.findById(id);
    // if (find.isPresent()) {
    // TransactionRecord modify = find.get();
    // }
    // }
    // return null;
    // }

    public Boolean delete(UUID id) {
        if (id != null) {
            Optional<TransactionRecord> find = transactionRecordRepo.findById(id);
            if (find.isPresent()) {
                transactionRecordRepo.deleteById(id);
                return true;
            }
        }
        return false;
    }

    public TransactionRecord findById(UUID id) {
        if (id != null) {
            Optional<TransactionRecord> find = transactionRecordRepo.findById(id);
            if (find.isPresent()) {
                return find.get();
            }
        }
        return null;
    }

    public Page<TransactionRecord> findAll(TranscationRecordDTO dto) {
        // 頁數 限制 排序
        Integer page = Optional.ofNullable(dto.getPage()).orElse(PAGEABLE_DEFAULT_PAGE);
        Integer limit = Optional.ofNullable(dto.getLimit()).orElse(PAGEABLE_DEFAULT_LIMIT);
        Boolean dir = Optional.ofNullable(dto.getDir()).orElse(false);
        String order = Optional.ofNullable(dto.getOrder()).orElse(null);
        // 是否排序
        Sort sort = (order != null) ? Sort.by(dir ? Direction.DESC : Direction.ASC, order) : Sort.unsorted();
        return transactionRecordRepo.findAll(PageRequest.of(page, limit, sort));
    }

    public List<Object[]> getAllTransactionRecords() {
        return transactionRecordRepo.findAllTransactionRecords();

    }

    // 條件查詢
    public Page<TransactionRecord> find(TranscationRecordDTO dto) {
        // 頁數 限制 排序
        Integer page = Optional.ofNullable(dto.getPage()).orElse(PAGEABLE_DEFAULT_PAGE);
        Integer limit = Optional.ofNullable(dto.getLimit()).orElse(PAGEABLE_DEFAULT_LIMIT);
        Boolean dir = Optional.ofNullable(dto.getDir()).orElse(false);
        String order = Optional.ofNullable(dto.getOrder()).orElse(null);
        // 是否排序
        Sort sort = (order != null) ? Sort.by(dir ? Direction.DESC : Direction.ASC, order) : Sort.unsorted();
        return transactionRecordRepo.findAll(TranscationRecordSpecification.filterTranscationRecords(dto),
                PageRequest.of(page, limit, sort));
    }

    public TransactionRecord modify(UUID id, TranscationRecordDTO dto) {
        if (id != null) {
            Optional<TransactionRecord> existingRecord = transactionRecordRepo.findById(id);
            if (existingRecord.isPresent()) {
                TransactionRecord recordToUpdate = existingRecord.get();
                // 更新屬性
                if (dto.getCashFlow() != null) {
                    recordToUpdate.setCashFlow(dto.getCashFlow());
                    if (dto.getPlatformIncome() == null) {
                        recordToUpdate.setPlatformIncome(dto.getCashFlow() * COMMISSION / 100);
                    }
                }
                if (dto.getDeal() != null) {
                    recordToUpdate.setDeal(dto.getDeal());
                }
                if (dto.getPlatformIncome() != null) {
                    recordToUpdate.setPlatformIncome(dto.getPlatformIncome());
                }

                return transactionRecordRepo.save(recordToUpdate); // 保存更新後的記錄
            }
        }
        return null; // 若未找到記錄，返回 null
    }

}
