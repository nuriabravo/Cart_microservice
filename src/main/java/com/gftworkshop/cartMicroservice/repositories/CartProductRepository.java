package com.gftworkshop.cartMicroservice.repositories;

import com.gftworkshop.cartMicroservice.model.CartProduct;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE CartProduct cp SET cp.quantity = :quantity WHERE cp.id = :id")
    int updateQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    Optional<CartProduct> findByCartIdAndProductId(Long cartId, Long productId);

    void removeAllByCartId(Long cartId);
}
