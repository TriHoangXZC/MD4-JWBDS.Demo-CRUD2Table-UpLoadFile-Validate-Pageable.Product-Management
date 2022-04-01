package com.codegym.controller;

import com.codegym.exception.NotFoundException;
import com.codegym.model.Category;
import com.codegym.model.Product;
import com.codegym.service.category.ICategoryService;
import com.codegym.service.product.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Optional;

@Controller
public class CategoryController {
    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IProductService productService;

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView notFoundPage() {
        return new ModelAndView("/error-404");
    }

    @GetMapping("/categories/list")
    public ModelAndView showListCategory() {
        ModelAndView modelAndView = new ModelAndView("/category/list");
        Iterable<Category> categories = categoryService.findAll();
        modelAndView.addObject("categories", categories);
        return modelAndView;
    }

    @GetMapping("/categories/create")
    public ModelAndView showFormCategory() {
        ModelAndView modelAndView = new ModelAndView("/category/create");
        modelAndView.addObject("category", new Category());
        return modelAndView;
    }

    @PostMapping("/categories/create")
    public ModelAndView createCategory(@Valid @ModelAttribute("category") Category category, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("/category/create");
        }
        ModelAndView modelAndView = new ModelAndView("redirect:/categories/list");
        categoryService.save(category);
        return modelAndView;
    }

    @GetMapping("/categories/edit/{id}")
    public ModelAndView showFormEdit(@PathVariable Long id) throws NotFoundException {
        Optional<Category> categoryOptional = categoryService.findById(id);
        if (!categoryOptional.isPresent()) {
            throw new NotFoundException();
        }
        return new ModelAndView("/category/edit", "category", categoryOptional.get());
    }

    @PostMapping("/categories/edit/{id}")
    public ModelAndView editCategory(@PathVariable Long id,@Valid @ModelAttribute("category") Category category, BindingResult bindingResult) throws NotFoundException {
        Optional<Category> categoryOptional = categoryService.findById(id);
        if (!categoryOptional.isPresent()) {
            throw new NotFoundException();
        }
        if (bindingResult.hasErrors()) {
            return new ModelAndView("/category/edit");
        }
        Category oldCategory = categoryOptional.get();
        oldCategory.setName(category.getName());
        categoryService.save(oldCategory);
        return new ModelAndView("redirect:/categories/list");
    }

    @GetMapping("/categories/{id}")
    public ModelAndView findAllProductByCategory(@PathVariable Long id) throws NotFoundException {
        Optional<Category> categoryOptional = categoryService.findById(id);
        if (!categoryOptional.isPresent()) {
            throw new NotFoundException();
        }
        Category category = categoryOptional.get();
        Iterable<Product> products = productService.findAllByCategory(category);
        return new ModelAndView("/category/view", "products", products);
    }

    @GetMapping("/categories/delete/{id}")
    public ModelAndView showDeleteForm(@PathVariable Long id) throws NotFoundException {
        Optional<Category> categoryOptional = categoryService.findById(id);
        if (!categoryOptional.isPresent()) {
            throw new NotFoundException();
        }
        return new ModelAndView("/category/delete", "category", categoryOptional.get());
    }

    @PostMapping("/categories/delete/{id}")
    public ModelAndView deleteCategory(@PathVariable Long id) throws NotFoundException {
        Optional<Category> categoryOptional = categoryService.findById(id);
        if (!categoryOptional.isPresent()) {
            throw new NotFoundException();
        }
        categoryService.deleteAllProductByCategory(id);
        return new ModelAndView("redirect:/categories/list");
    }
}
