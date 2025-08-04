package com.example.receipt_data.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ReceiptsDTO {
    private List<ReceiptDTO> receipts;
}
