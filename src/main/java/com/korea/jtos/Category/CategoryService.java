package com.korea.jtos.Category;

import com.korea.jtos.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category getCategory(Integer id) {
        Optional<Category> category = this.categoryRepository.findById(id);
        if (category.isPresent()) {
            return category.get();
        } else {
            throw new DataNotFoundException("category not found");
        }
    }

    public void create(String name) {
        Category c = new Category();
        c.setName(name); // 제목을 설정하는 메서드 사용
        this.categoryRepository.save(c);
    }

    public void modify (Category category, String name){
        category.setName(name);
        this.categoryRepository.save(category);
    }

    public void delete(Category category) {
        this.categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
