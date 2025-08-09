package com.example.receipt_data.DTO.receipt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

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
