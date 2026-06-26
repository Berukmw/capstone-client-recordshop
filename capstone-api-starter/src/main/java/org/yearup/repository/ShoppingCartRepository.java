package org.yearup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.yearup.models.CartItem;

import java.util.List;

@Repository
public interface ShoppingCartRepository extends JpaRepository<CartItem, Integer>
{
    List<CartItem> findByUserId(int userId);

    CartItem findByUserIdAndProductId(int userId, int productId);
   // hibernate throws "cannot reliably process remove" when deleting
   // this is what was causing the 500 on delete /cart
    @Transactional
    void deleteByUserId(int userId);
    // finds the exact row for "this user, this product" so we can delete just that one
    @Transactional
    void deleteByUserIdAndProductId(int userId, int productId);
}



