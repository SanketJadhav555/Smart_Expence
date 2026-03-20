package com.tracker.expensetracker.service;

import com.tracker.expensetracker.model.Category;
import com.tracker.expensetracker.model.TransactionType;
import com.tracker.expensetracker.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Get all categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByTypeAscNameAsc();
    }

    // Get categories by type
    public List<Category> getCategoriesByType(TransactionType type) {
        return categoryRepository.findByTypeOrderByNameAsc(type);
    }

    // Get category by ID
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    // Create category
    public Category createCategory(Category category) {
        if (categoryRepository.existsByNameAndType(category.getName(), category.getType())) {
            throw new RuntimeException("Category '" + category.getName() + "' already exists for type " + category.getType());
        }
        return categoryRepository.save(category);
    }

    // Update category
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);
        category.setName(categoryDetails.getName());
        category.setType(categoryDetails.getType());
        category.setIcon(categoryDetails.getIcon());
        category.setColor(categoryDetails.getColor());
        return categoryRepository.save(category);
    }

    // Delete category
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
