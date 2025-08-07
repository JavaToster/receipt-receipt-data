package com.example.receipt_data.DTO.statistics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DailyStatisticDTO {
    private final List<StatisticDTO> statistic = new ArrayList<>();

    public void addStatistic(StatisticDTO statisticDTO){
        this.statistic.add(statisticDTO);
    }

}
