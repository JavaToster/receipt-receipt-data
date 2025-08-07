package com.example.receipt_data.util;

import com.example.receipt_data.DTO.receipt.ReceiptDTO;
import com.example.receipt_data.DTO.user.UserDTO;
import com.example.receipt_data.domainModels.User;
import com.example.receipt_data.models.Receipt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;


@ExtendWith(MockitoExtension.class)
class ConvertorTest {

    @InjectMocks
    private Convertor convertor;

    @Mock
    private ModelMapper modelMapper;

    @Test
    void convertToReceipt_ShouldMapReceiptDTOToReceipt() {
        ReceiptDTO dto = new ReceiptDTO();
        dto.setId(1L);

        Receipt receipt = new Receipt();
        receipt.setId(1L);

        when(modelMapper.map(dto, Receipt.class)).thenReturn(receipt);

        Receipt result = convertor.convertToReceipt(dto);

        assertEquals(1L, result.getId());
        verify(modelMapper).map(dto, Receipt.class);
    }

    @Test
    void convertToReceiptDTO_ShouldMapReceiptToReceiptDTO() {
        Receipt receipt = new Receipt();
        receipt.setId(2L);

        ReceiptDTO dto = new ReceiptDTO();
        dto.setId(2L);

        when(modelMapper.map(receipt, ReceiptDTO.class)).thenReturn(dto);

        ReceiptDTO result = convertor.convertToReceiptDTO(receipt);

        assertEquals(2L, result.getId());
        verify(modelMapper).map(receipt, ReceiptDTO.class);
    }

    @Test
    void convertToReceiptDTOList_ShouldMapListOfReceiptsToDTOs() {
        Receipt receipt1 = new Receipt(); receipt1.setQrRawData("1");
        Receipt receipt2 = new Receipt(); receipt2.setQrRawData("2");

        ReceiptDTO dto1 = new ReceiptDTO(); dto1.setQrRawData("1");
        ReceiptDTO dto2 = new ReceiptDTO(); dto2.setQrRawData("2");

        when(modelMapper.map(eq(receipt1), eq(ReceiptDTO.class))).thenReturn(dto1);
        when(modelMapper.map(eq(receipt2), eq(ReceiptDTO.class))).thenReturn(dto2);


        List<ReceiptDTO> result = convertor.convertToReceiptDTO(List.of(receipt1, receipt2));

        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getQrRawData());
        assertEquals("2", result.get(1).getQrRawData());

        verify(modelMapper).map(receipt1, ReceiptDTO.class);
        verify(modelMapper).map(receipt2, ReceiptDTO.class);
    }

    @Test
    void convertToUser_ShouldMapUserDTOToUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setTelegramId(123L);

        User user = new User();
        user.setTelegramId(123L);

        when(modelMapper.map(userDTO, User.class)).thenReturn(user);

        User result = convertor.convertToUser(userDTO);

        assertEquals(123L, result.getTelegramId());
        verify(modelMapper).map(userDTO, User.class);
    }
}
