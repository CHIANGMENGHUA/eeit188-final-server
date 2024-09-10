package com.ispan.eeit188_final.repository.specification;

import java.util.UUID;
import java.util.function.Function;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.data.jpa.domain.Specification;

import com.ispan.eeit188_final.dto.HouseDTO;
import com.ispan.eeit188_final.model.House;
import com.ispan.eeit188_final.model.Postulate;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public class HouseSpecification {
    // 房源名稱
    public static Specification<House> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty()) {
                return cb.conjunction(); // 如果 name 為 null 或空字串，返回無條件過濾
            }
            return cb.like(root.get("name"), "%" + name + "%"); // 使用 % 符號進行模糊搜尋
        };
    }

    // 類型
    public static Specification<House> hasCategory(String category) {
        return (root, query, cb) -> category == null ? cb.conjunction()
                : cb.equal(root.get("category"), category);
    }

    // 國家
    public static Specification<House> hasCountry(String country) {
        return (root, query, cb) -> country == null ? cb.conjunction()
                : cb.equal(root.get("country"), country);
    }

    // 縣市
    public static Specification<House> hasCity(String city) {
        return (root, query, cb) -> city == null ? cb.conjunction()
                : cb.equal(root.get("city"), city);
    }

    // 區域
    public static Specification<House> hasRegion(String region) {
        return (root, query, cb) -> region == null ? cb.conjunction()
                : cb.equal(root.get("region"), region);
    }

    // 價格區間 (最小, 最大)
    public static Specification<House> hasPriceBetween(Integer minPrice, Integer maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return cb.conjunction();
            } else if (minPrice == null) {
                return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            } else if (maxPrice == null) {
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            } else {
                return cb.between(root.get("price"), minPrice, maxPrice);
            }
        };
    }

    // 經緯度區間 (最小經度, 最大經度, 最小緯度, 最大緯度)
    public static Specification<House> isWithinLocation(Double minLatitudeX, Double maxLatitudeX,
            Double minLongitudeY, Double maxLongitudeY) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 處理最小緯度
            Optional.ofNullable(minLatitudeX)
                    .ifPresent(val -> predicates.add(cb.greaterThanOrEqualTo(root.get("latitudeX"), val)));
            // 處理最大緯度
            Optional.ofNullable(maxLatitudeX)
                    .ifPresent(val -> predicates.add(cb.lessThanOrEqualTo(root.get("latitudeX"), val)));
            // 處理最小經度
            Optional.ofNullable(minLongitudeY)
                    .ifPresent(val -> predicates.add(cb.greaterThanOrEqualTo(root.get("longitudeY"), val)));
            // 處理最大經度
            Optional.ofNullable(maxLongitudeY)
                    .ifPresent(val -> predicates.add(cb.lessThanOrEqualTo(root.get("longitudeY"), val)));
            // 如果沒有條件，則返回一個總是為真的條件
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // 客廳
    public static Specification<House> hasLivingDiningRoom(Short livingDiningRoom) {
        return (root, query, cb) -> livingDiningRoom == null ? cb.conjunction()
                : cb.equal(root.get("livingDiningRoom"), livingDiningRoom);
    }

    // 房間
    public static Specification<House> hasBedroom(Short bedroom) {
        return (root, query, cb) -> bedroom == null ? cb.conjunction()
                : cb.equal(root.get("bedroom"), bedroom);
    }

    // 衛生間 (馬桶)
    public static Specification<House> hasRestroom(Short restroom) {
        return (root, query, criteriaBuilder) -> restroom == null ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("restroom"), restroom);
    }

    // 淋浴間
    public static Specification<House> hasBathroom(Short bathroom) {
        return (root, query, criteriaBuilder) -> bathroom == null ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("bathroom"), bathroom);
    }

    // 廚房
    public static Specification<House> hasKitchen(Boolean kitchen) {
        return (root, query, criteriaBuilder) -> kitchen == null ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("kitchen"), kitchen);
    }

    // 陽台
    public static Specification<House> hasBalcony(Boolean balcony) {
        return (root, query, cb) -> balcony == null ? cb.conjunction()
                : cb.equal(root.get("balcony"), balcony);
    }

    // 是否上架
    public static Specification<House> isShown(Boolean show) {
        return (root, query, cb) -> show == null ? cb.conjunction()
                : cb.equal(root.get("show"), show);
    }

    // 擁有者ID
    public static Specification<House> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? cb.conjunction()
                : cb.equal(root.get("userId"), userId);
    }

    // 附加設施 (只對單個設施查詢)
    public static Specification<House> hasPostulateId(UUID postulateId) {
        return (root, query, cb) -> {
            if (postulateId == null) {
                return cb.conjunction(); // 如果沒有提供 ID，返回一個總是為真的條件
            }
            Join<House, Postulate> postulatesJoin = root.join("postulates");
            return cb.equal(postulatesJoin.get("id"), postulateId);
        };
    }

    // 附加設施 (其中一個即符合)
    public static Specification<House> hasPostulateIds(List<UUID> postulateIds) {
        return (root, query, cb) -> {
            if (postulateIds == null || postulateIds.isEmpty()) {
                return cb.conjunction(); // 如果沒有提供 ID 列表，返回一個總是為真的條件
            }
            Join<House, Postulate> postulatesJoin = root.join("postulates");
            return postulatesJoin.get("id").in(postulateIds);
        };
    }

    // 附加設施 (必須符合)
    public static Specification<House> hasAllPostulateIds(List<UUID> postulateIds) {
        return (root, query, cb) -> {
            if (postulateIds == null || postulateIds.isEmpty()) {
                return cb.conjunction(); // 如果沒有提供 ID 列表，返回一個總是為真的條件
            }
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<House> subqueryRoot = subquery.from(House.class);
            Join<House, Postulate> subqueryPostulates = subqueryRoot.join("postulates");
            subquery.select(subqueryRoot.get("id"))
                    .where(subqueryPostulates.get("id").in(postulateIds))
                    .groupBy(subqueryRoot.get("id"))
                    .having(cb.equal(cb.count(subqueryPostulates.get("id")),
                            postulateIds.size()));

            return root.get("id").in(subquery);
        };
    }

    // 多條件查詢
    public static Specification<House> filterHouses(HouseDTO dto) {
        // 條件查詢
        Specification<House> spec = Specification.where(null);
        // // 房源基本資料
        spec = addIfNotNull(spec, dto.getName(), HouseSpecification::hasName);
        spec = addIfNotNull(spec, dto.getCategory(), HouseSpecification::hasCategory);
        spec = addIfNotNull(spec, dto.getCountry(), HouseSpecification::hasCountry);
        spec = addIfNotNull(spec, dto.getCity(), HouseSpecification::hasCity);
        spec = addIfNotNull(spec, dto.getRegion(), HouseSpecification::hasRegion);
        // // 房源基本設施
        spec = addIfNotNull(spec, dto.getLivingDiningRoom(), HouseSpecification::hasLivingDiningRoom);
        spec = addIfNotNull(spec, dto.getBedroom(), HouseSpecification::hasBedroom);
        spec = addIfNotNull(spec, dto.getRestroom(), HouseSpecification::hasRestroom);
        spec = addIfNotNull(spec, dto.getBathroom(), HouseSpecification::hasBathroom);
        // // 常態設施
        spec = addIfNotNull(spec, dto.getKitchen(), HouseSpecification::hasKitchen);
        spec = addIfNotNull(spec, dto.getBalcony(), HouseSpecification::hasBalcony);
        // // 刊登顯示
        spec = addIfNotNull(spec, dto.getShow(), HouseSpecification::isShown);
        // // 擁有者ID
        spec = addIfNotNull(spec, dto.getUserId(), HouseSpecification::hasUserId);
        // 經緯度區間
        spec = spec.and(HouseSpecification.isWithinLocation(
                dto.getMinLatitudeX(), dto.getMaxLatitudeX(),
                dto.getMinLongitudeY(), dto.getMaxLongitudeY()));
        // 價錢區間 (可以則一輸入或min,max都輸入)
        if (dto.getMinPrice() != null || dto.getMaxPrice() != null) {
            spec = spec.and(HouseSpecification.hasPriceBetween(
                    dto.getMinPrice(), dto.getMaxPrice()));
        }
        // 附加設施過濾
        UUID postulateId = dto.getPostulateId();
        List<UUID> postulateIds = dto.getPostulateIds();
        Boolean matchAllPostulates = Optional.ofNullable(dto.getMatchAllPostulates()).orElse(false);
        if (postulateId != null) {
            // 當有單一設施 ID 時
            spec = spec.and(HouseSpecification.hasPostulateId(postulateId));
        } else if (postulateIds != null && !postulateIds.isEmpty()) {
            // 當有多個設施 ID 時
            if (postulateIds.size() == 1) {
                // 如果只有一個 postulateId，直接處理為單一設施過濾
                postulateId = postulateIds.get(0);
                spec = spec.and(HouseSpecification.hasPostulateId(postulateId));
            } else {
                // 根據 matchAllPostulates 決定是匹配所有還是任意一個
                if (matchAllPostulates) {
                    spec = spec.and(HouseSpecification.hasAllPostulateIds(postulateIds));
                } else {
                    spec = spec.and(HouseSpecification.hasPostulateIds(postulateIds));
                }
            }
        }
        return spec;
    }

    // 輔助方法檢查加入條件
    private static <T> Specification<House> addIfNotNull(Specification<House> spec, T value,
            Function<T, Specification<House>> func) {
        return value != null ? spec.and(func.apply(value)) : spec;
    }
}
