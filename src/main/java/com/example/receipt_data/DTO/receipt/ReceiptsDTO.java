package com.example.receipt_data.DTO.receipt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptsDTO {
    private List<ReceiptDTO> receipts;
}
