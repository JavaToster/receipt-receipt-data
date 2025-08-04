package com.example.receipt_data.util;
import com.example.receipt_data.models.Receipt;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@Scope("prototype")
public class Sorter {
    public List<Receipt> sortByCreationDate(List<Receipt> receipts) {
        return receipts.stream().sorted(Comparator.comparing(Receipt::getCreationDate)).toList();
    }
}

