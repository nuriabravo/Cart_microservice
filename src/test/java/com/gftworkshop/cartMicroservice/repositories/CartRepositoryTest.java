package com.gftworkshop.cartMicroservice.repositories;

import com.gftworkshop.cartMicroservice.model.Cart;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class CartRepositoryTest {

    private CartRepository cartRepository;

    @Before
    public void setUp() {
        cartRepository = mock(CartRepository.class);
    }

    @Test
    public void testIdentifyAbandonedCarts() {
        LocalDate thresholdDate = LocalDate.now();

        List<Cart> expectedCarts = List.of(new Cart(), new Cart());

        when(cartRepository.identifyAbandonedCarts(thresholdDate)).thenReturn(expectedCarts);

        List<Cart> actualCarts = cartRepository.identifyAbandonedCarts(thresholdDate);

        assertEquals(expectedCarts, actualCarts);
        verify(cartRepository, times(1)).identifyAbandonedCarts(thresholdDate);
    }


    @Test
    public void testFindByUserId() {

        Long userId = 123L;
        Cart expectedCart = new Cart();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(expectedCart));

        Optional<Cart> actualCartOptional = cartRepository.findByUserId(userId);

        assertTrue(actualCartOptional.isPresent());
        assertEquals(expectedCart, actualCartOptional.get());
        verify(cartRepository, times(1)).findByUserId(userId);
    }

    @Test
    public void testFindByUserIdNotFound() {

        Long userId = 123L;

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Optional<Cart> actualCartOptional = cartRepository.findByUserId(userId);

        assertTrue(actualCartOptional.isEmpty());
        verify(cartRepository, times(1)).findByUserId(userId);
    }
}
