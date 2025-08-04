package com.example.receipt_data.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.example.receipt_data.models.Receipt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SorterTest {

    @InjectMocks
    private Sorter sorter;

    @Test
    void sortByCreationDate_ShouldSortReceiptsByCreationDateAscending() {
        Receipt r1 = new Receipt();
        r1.setCreationDate(LocalDateTime.of(2025, 7, 20, 10, 0));

        Receipt r2 = new Receipt();
        r2.setCreationDate(LocalDateTime.of(2025, 7, 18, 10, 0));

        Receipt r3 = new Receipt();
        r3.setCreationDate(LocalDateTime.of(2025, 7, 19, 10, 0));

        List<Receipt> unsorted = List.of(r1, r2, r3);
        List<Receipt> sorted = sorter.sortByCreationDate(unsorted);

        assertEquals(3, sorted.size());
        assertEquals(r2.getCreationDate(), sorted.get(0).getCreationDate());
        assertEquals(r3.getCreationDate(), sorted.get(1).getCreationDate());
        assertEquals(r1.getCreationDate(), sorted.get(2).getCreationDate());
    }
}
