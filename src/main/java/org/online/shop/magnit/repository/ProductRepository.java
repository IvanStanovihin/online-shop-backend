package org.online.shop.magnit.repository;

import org.online.shop.magnit.model.Product;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p left join fetch p.category where (:categoryId is null or p.category.id = :categoryId) and lower(p.name) like lower(concat('%', :name, '%')) order by p.id")
    List<Product> search(Long categoryId, String name);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findLockedById(Long id);
}
