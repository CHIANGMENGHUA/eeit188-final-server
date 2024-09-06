package com.ispan.eeit188_final.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ispan.eeit188_final.model.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
}