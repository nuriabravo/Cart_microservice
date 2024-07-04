package com.gftworkshop.cartMicroservice.repositories;

import com.gftworkshop.cartMicroservice.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c WHERE c.updatedAt < :thresholdDate")
    List<Cart> identifyAbandonedCarts(@Param("thresholdDate") LocalDate thresholdDate);

    @Query("SELECT c FROM Cart c WHERE c.userId = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);
}
