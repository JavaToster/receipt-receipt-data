package com.example.receipt_data.DTO.reports;

import com.example.receipt_data.DTO.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportOfUserDTO {
    private UserDTO userDTO;
    private long receiptsCount;
}
