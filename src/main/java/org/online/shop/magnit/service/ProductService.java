package org.online.shop.magnit.service;

import lombok.RequiredArgsConstructor;
import org.online.shop.magnit.dto.*;
import org.online.shop.magnit.model.*;
import org.online.shop.magnit.repository.*;
import org.online.shop.magnit.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(Long categoryId, String name) {
        return products.search(categoryId, name == null ? "" : name.trim()).stream().map(ProductResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) { return ProductResponse.from(get(id)); }
    private Product get(Long id) {
        return products.findById(id).orElseThrow(() -> ApiException.notFound("Товар не найден"));
    }
    public ProductResponse create(ProductRequest r) {
        var product = new Product();
        apply(product, r);
        return ProductResponse.from(products.save(product));
    }
    public ProductResponse update(Long id, ProductRequest r) {
        var product = products.findLockedById(id).orElseThrow(() -> ApiException.notFound("Товар не найден"));
        apply(product, r);
        return ProductResponse.from(product);
    }
    public void delete(Long id) { products.delete(get(id)); products.flush(); }
    private void apply(Product p, ProductRequest r) {
        p.setName(r.name().trim()); p.setDescription(r.description()); p.setPrice(r.price());
        p.setStockQuantity(r.stockQuantity());
        p.setCategory(categories.findById(r.categoryId()).orElseThrow(() -> ApiException.notFound("Категория не найдена")));
    }
}
