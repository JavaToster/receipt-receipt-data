package com.example.receipt_data.repositories;

import com.example.receipt_data.models.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    boolean existsByQrRawData(String rawData);
    List<Receipt> findAllByOwnerId(long ownerId);
}
