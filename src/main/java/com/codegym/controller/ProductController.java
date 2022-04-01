package com.codegym.controller;

import com.codegym.exception.NotFoundException;
import com.codegym.model.Category;
import com.codegym.model.Product;
import com.codegym.model.ProductForm;
import com.codegym.service.category.ICategoryService;
import com.codegym.service.product.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Controller
public class ProductController {
    @Autowired
    private IProductService productService;

    @Autowired
    private ICategoryService categoryService;

    @Value("${file-upload}")
    private String uploadPath;

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView notFoundPage() {
        return new ModelAndView("/error-404");
    }

    @ModelAttribute("categories")
    public Iterable<Category> categories(){
        return categoryService.findAll();
    }


    @GetMapping("/products/list")
    public ModelAndView showListProduct(@RequestParam (name = "q") Optional<String> q,@PageableDefault (value = 3) Pageable pageable ){
        ModelAndView modelAndView = new ModelAndView("/product/list");
//        Iterable<Product> products = productService.findAll();
        Page<Product> products = productService.findAll(pageable);
        if (q.isPresent()) {
            modelAndView.addObject("q", q.get());
//            products = productService.findProductByNameContaining(q.get(), firstPageWithFiveElements);
            products = productService.findProductByNameContaining(q.get(), pageable);
        }
        modelAndView.addObject("products", products);
        return modelAndView;
    }

    @GetMapping("/products/create")
    public ModelAndView showCreateForm(){
        ModelAndView modelAndView = new ModelAndView("/product/create");
        modelAndView.addObject("product", new ProductForm());
        return modelAndView;
    }

    @PostMapping("/products/create")
    public ModelAndView createProduct(@Valid @ModelAttribute("product") ProductForm productForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("/product/create");
        }
        MultipartFile multipartFile = productForm.getImage();
        String fileName = multipartFile.getOriginalFilename();
        long currentTime = System.currentTimeMillis();
        fileName = currentTime + fileName;
        try {
            FileCopyUtils.copy(multipartFile.getBytes(), new File(uploadPath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Product product = new Product(productForm.getId(), productForm.getName(), productForm.getPrice(), productForm.getDescription(), fileName, productForm.getCategory());
        productService.save(product);
        return new ModelAndView("redirect:/products/list");
    }

    @GetMapping("/products/edit/{id}")
    public ModelAndView showEditForm(@PathVariable Long id) throws NotFoundException {
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()){
            throw new NotFoundException();
        }
        return new ModelAndView("/product/edit", "product", productOptional.get());
    }

    @PostMapping("/products/edit/{id}")
    public ModelAndView editProduct(@PathVariable Long id, @Valid @ModelAttribute("product") ProductForm productForm, BindingResult bindingResult) throws NotFoundException {
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()) {
            throw new NotFoundException();
        }
        if (bindingResult.hasErrors()){
            return new ModelAndView("/product/edit");
        }
        Product oldProduct = productOptional.get();
        MultipartFile multipartFile = productForm.getImage();
        if (multipartFile.getSize() != 0) {
            String fileName = multipartFile.getOriginalFilename();
            long currentTime = System.currentTimeMillis();
            fileName = currentTime + fileName;
            try {
                FileCopyUtils.copy(multipartFile.getBytes(), new File(uploadPath + fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
            oldProduct.setImage(fileName);
        }
        oldProduct.setName(productForm.getName());
        oldProduct.setPrice(productForm.getPrice());
        oldProduct.setDescription(productForm.getDescription());
        oldProduct.setCategory(productForm.getCategory());
        productService.save(oldProduct);
        return new ModelAndView("redirect:/products/list");
    }

    @GetMapping("/products/delete/{id}")
    public ModelAndView showDeleteForm(@PathVariable Long id) throws NotFoundException {
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()) {
            throw new NotFoundException();
        }
        return new ModelAndView("/product/delete", "product", productOptional.get());
    }

    @PostMapping("/products/delete/{id}")
    public ModelAndView deleteProduct(@PathVariable Long id) throws NotFoundException {
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()) {
            throw new NotFoundException();
        }
        productService.remove(id);
        return new ModelAndView("redirect:/products/list");
    }

    @GetMapping("/products/{id}")
    public ModelAndView viewDetailProduct(@PathVariable Long id) throws NotFoundException {
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()) {
            throw new NotFoundException();
        }
        Product product = productOptional.get();
        return new ModelAndView("/product/view", "product", product);
    }
}
