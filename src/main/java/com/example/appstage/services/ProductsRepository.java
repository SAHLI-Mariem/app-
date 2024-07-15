package com.example.appstage.services;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.appstage.models.Product;

public interface ProductsRepository extends JpaRepository<Product, Integer> {

}