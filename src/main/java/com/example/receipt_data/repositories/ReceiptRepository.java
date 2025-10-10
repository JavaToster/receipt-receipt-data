package com.example.receipt_data.repositories;

import com.example.receipt_data.DTO.statistics.Top3RatingDTO;
import com.example.receipt_data.models.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    boolean existsByQrRawData(String rawData);

    List<Receipt> findAllByOwnerId(long ownerId);

    @Query(
            value = """
                    SELECT owner_id, cnt 
                    FROM (
                    SELECT owner_id, COUNT(*) AS cnt 
                    FROM Receipt 
                    GROUP BY owner_id 
                    ORDER BY cnt DESC
                    ) as top3
                    """,
            nativeQuery = true
    )
    List<Top3RatingDTO> findTop3UserIdAndReceiptCount();

    long countByOwnerId(long ownerId);
}
