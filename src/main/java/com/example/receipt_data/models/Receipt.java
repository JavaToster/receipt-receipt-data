package com.example.receipt_data.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Entity
@Table(name = "Receipt")
public class Receipt {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "sum")
    private double sum;
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
    @Column(name = "fn")
    private long fn;
    @Column(name = "fp")
    private long fp;
    @Column(name = "i")
    private long i;
    @Column(name = "n")
    private int n;
    @Column(name = "qr_raw_data")
    private String qrRawData;
    @Column(name = "owner_id")
    private long ownerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Receipt receipt = (Receipt) o;
        return Objects.equals(qrRawData, receipt.qrRawData);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(qrRawData);
    }
}
