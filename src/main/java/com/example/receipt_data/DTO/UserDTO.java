package com.example.receipt_data.DTO;

import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private long telegramId;
    private String username;
    private String recoveryEmail;
    private List<ReceiptDTO> receipts;
}
