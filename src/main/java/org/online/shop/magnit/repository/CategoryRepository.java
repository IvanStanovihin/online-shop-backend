package org.online.shop.magnit.repository;

import org.online.shop.magnit.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNameContainingIgnoreCaseOrderById(String name);
}
