package com.dsapkl.backend.repository;


import com.dsapkl.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);

    List<CartItem> findAllByCartId(Long cartId);
}
