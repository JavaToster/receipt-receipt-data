package com.example.receipt_data.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class ReceiptDTO {
    private long id;
    private double sum;
    private LocalDateTime creationDate;
    private String fn;
    private String i;
    private String fp;
    private int n;
    private String qrRawData;
}
