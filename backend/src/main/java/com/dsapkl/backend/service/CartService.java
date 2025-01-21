package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Cart;
import com.dsapkl.backend.entity.CartItem;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.CartItemRepository;
import com.dsapkl.backend.repository.CartRepository;
import com.dsapkl.backend.repository.ItemRepository;
import com.dsapkl.backend.repository.MemberRepository;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.repository.query.CartQueryRepository;
import com.dsapkl.backend.dto.CartItemDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService {
    private final CartRepository cartRepository;
    private final CartQueryRepository cartQueryRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public Cart findCart(Long memberId) {
        return cartRepository.findByMemberId(memberId).orElse(null);
    }

    @Transactional(readOnly = true)
    public CartItem findCartItem(Long cartItemId) {
        return cartItemRepository.findById(cartItemId).orElse(null);
    }

    /**
     * 장바구니 조회
     */
    @Transactional(readOnly = true)
    public List<CartQueryDto> findCartItems(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId).orElseThrow(EntityNotFoundException::new);  // () -> new EntityNotFoundException()
        List<CartQueryDto> cartQueryDtos = cartQueryRepository.findCartQueryDtos(cart.getId());
        return cartQueryDtos;
    }


    /**
     * 장바구니 담기(추가)
     */
    public Long addCart(CartItemDto cartItemDto, String email) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        Cart cart = cartRepository.findByMemberId(member.getId())
            .orElseGet(() -> Cart.createCart(member));
        
        Item item = itemRepository.findById(cartItemDto.getItemId())
            .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
            .orElse(null);

        if (cartItem == null) {
            cartItem = CartItem.createCartItem(cartItemDto.getCount(), cart, item);
            return cartItemRepository.save(cartItem).getId();
        }

        cartItem.changeCount(cartItemDto.getCount());
        return cartItem.getId();
    }

    /**
     * 장바구니 삭제
     */
    public void deleteCartItem(Long itemId) {
        CartItem findCartItem = cartItemRepository.findById(itemId).orElse(null);
        cartItemRepository.delete(findCartItem);
    }

    public int getCartItemCount(String email) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        return cartRepository.findByMemberId(member.getId())
            .orElse(Cart.createCart(member))
            .getCartItems().size();
    }

    public boolean isItemInCart(Long memberId, Long itemId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElse(null);
        
        if (cart == null) {
            return false;
        }
        
        return cartItemRepository.findByCartIdAndItemId(cart.getId(), itemId)
                .isPresent();
    }

}
