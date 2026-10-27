package org.online.shop.magnit.service;

import lombok.RequiredArgsConstructor;
import org.online.shop.magnit.dto.CategoryRequest;
import org.online.shop.magnit.model.Category;
import org.online.shop.magnit.repository.CategoryRepository;
import org.online.shop.magnit.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categories;
    @Transactional(readOnly = true)
    public List<Category> findAll(String name) {
        return categories.findByNameContainingIgnoreCaseOrderById(name == null ? "" : name.trim());
    }
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categories.findById(id).orElseThrow(() -> ApiException.notFound("Категория не найдена"));
    }
    public Category create(CategoryRequest r) {
        var c = new Category(); c.setName(r.name().trim()); return categories.save(c);
    }
    public Category update(Long id, CategoryRequest r) {
        var c = findById(id); c.setName(r.name().trim()); return c;
    }
    public void delete(Long id) { categories.delete(findById(id)); categories.flush(); }
}
