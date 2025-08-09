package com.example.receipt_data.DTO.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Top3RatingDTO {
    private long owner_id;
    private long cnt;
}
