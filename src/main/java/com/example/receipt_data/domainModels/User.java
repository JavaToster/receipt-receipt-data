package com.example.receipt_data.domainModels;

import com.example.receipt_data.models.Receipt;
import lombok.Data;

import java.util.List;

@Data
public class User {
    private long telegramId;
    private String username;
    private String recoveryEmail;
    private List<Receipt> receipts;
}
