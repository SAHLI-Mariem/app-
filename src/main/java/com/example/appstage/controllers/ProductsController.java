package com.example.appstage.controllers;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ScatteringByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import com.example.appstage.models.Product;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.example.appstage.models.ProductDto;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import com.example.appstage.services.ProductsRepository;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/products")
public class ProductsController {
    @Autowired
    private ProductsRepository repo;
    private Date createAt;
    private String storageFileName;
    private ProductDto productDto;
    private Product product;

    @GetMapping({"", "/"})
    public String showProductList(Model model) {
        List<Product> products = repo.findAll();
        model.addAttribute("products", products);
        return "products/index";

    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        //ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", new ProductDto());
        // model.addAttribute("productDto", ProductDto);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto,
                                BindingResult result) {

        if (productDto.getImageFile().isEmpty()) {
            result.addError((new FieldError("productDto", "imagFile", "the image file is required")));
        }

        if (result.hasErrors()) {
            return "products/CreateProduct";
        }
        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try (InputStream inputStream = image.getInputStream()) {

                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);

            }
        } catch (Exception ex) {
            System.out.println("Exception : " + ex.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setImageFileName(productDto.getImageFile().getOriginalFilename());
        product.setCreatedAt(createdAt);
        MultipartFile imageFile = productDto.getImageFile();
        String fileName = imageFile.getOriginalFilename();
        product.setImageFileName(fileName);

        product.setCreatedAt(new Date());

        repo.save(product);

        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);
            model.addAttribute("product", product);
            ProductDto productDto = new ProductDto();
            product.setName(product.getName());
            product.setBrand(product.getBrand());
            product.setCategory(product.getCategory());
            product.setPrice(product.getPrice());
            product.setDescription(product.getDescription());

            model.addAttribute("productDto", productDto);

        } catch (Exception ex) {
            System.out.println("Exception : " + ex.getMessage());
            return "redirect:/products";
        }


        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(
            Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto ProductDto, BindingResult result) throws IOException {

        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product ", product);

            if (result.hasErrors()) {
                return "products/EditProduct";
            }
        } catch (Exception ex) {
            System.out.println("Exception " + ex.getMessage());
        }
        if (!productDto.getImageFile().isEmpty()) {
            String uploadDir = "public/images/";
            Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
            try {
                Files.delete(oldImagePath);
            } catch (Exception ex) {
                System.out.println("Exception :" + ex.getMessage());
            }
            MultipartFile image = productDto.getImageFile();
            Date createdAt = new Date();
            String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                System.out.println("Exception: " + ex.getMessage());
            }

            product.setImageFileName(storageFileName);
        }

        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());

        repo.save(product);

        return "redirect:/products";
    }

    //@GetMapping("/delete")
    @DeleteMapping("/delete")
    public String deleteProduct (
        @RequestParam int id){


        try {
            Product product = repo.findById(id).get();
            Path imagePath = Paths.get("public/images/" + product.getImageFileName());
            try {
                Files.delete(imagePath);
            } catch (Exception ex) {
                System.out.println("Exception :" + ex.getMessage());
            }
            repo.delete(product);

        }

        catch (Exception ex ){
            System.out.println("Exception :"+ ex.getMessage());
        }
        return "redirect:/products";
    }
}


