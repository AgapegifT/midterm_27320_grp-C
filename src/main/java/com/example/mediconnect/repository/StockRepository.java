package com.example.mediconnect.repository;

import com.example.mediconnect.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
}
