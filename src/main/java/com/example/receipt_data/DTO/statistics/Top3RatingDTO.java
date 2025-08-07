package com.example.receipt_data.DTO.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Top3RatingDTO {
    private final long owner_id;
    private final long cnt;
}
