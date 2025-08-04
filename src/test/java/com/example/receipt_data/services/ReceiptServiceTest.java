package com.example.receipt_data.services;

import com.example.receipt_data.DTO.ReceiptDTO;
import com.example.receipt_data.QRCodeUtil.QRCodeDecoder;
import com.example.receipt_data.forExceptions.exceptions.EntityIsExistException;
import com.example.receipt_data.models.Receipt;
import com.example.receipt_data.repositories.ReceiptRepository;
import com.example.receipt_data.util.Convertor;
import com.example.receipt_data.util.Sorter;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private QRCodeDecoder qrCodeDecoder;
    @Mock
    private Convertor convertor;
    @Mock
    private Sorter sorter;
    @InjectMocks
    private ReceiptService receiptService;

    private final long TELEGRAM_ID = 123456789L;
    private final long RECEIPT_ID = 1L;

    @Test
    void save_shouldThrowEntityIsExistException() {
        ReceiptDTO receiptDTO = new ReceiptDTO();
        receiptDTO.setQrRawData("some");

        when(receiptRepository.existsByQrRawData("some")).thenReturn(true);

        assertThrows(EntityIsExistException.class, () -> receiptService.save(receiptDTO, TELEGRAM_ID));

    }

    @Test
    void save_shouldNotThrowAnException(){
        ReceiptDTO receiptDTO = new ReceiptDTO();
        receiptDTO.setQrRawData("some");

        Receipt receipt = new Receipt();
        receipt.setQrRawData("some");

        when(receiptRepository.existsByQrRawData("some")).thenReturn(false);
        when(convertor.convertToReceipt(receiptDTO)).thenReturn(receipt);
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(invocation -> {
            Receipt receipt1 = invocation.getArgument(0);
            receipt1.setId(1L);
            return receipt1;
        });

        receiptService.save(receiptDTO, TELEGRAM_ID);

        assertEquals(1L, receiptDTO.getId());
        verify(receiptRepository).save(receipt);
        verify(receiptRepository).existsByQrRawData("some");
    }

    @Test
    void get_shouldNotThrowAnException() {

        Receipt receipt = new Receipt();
        receipt.setId(RECEIPT_ID);

        ReceiptDTO receiptDTO = new ReceiptDTO();
        receiptDTO.setId(RECEIPT_ID);

        when(receiptRepository.findById(RECEIPT_ID)).thenReturn(Optional.of(receipt));
        when(convertor.convertToReceiptDTO(receipt)).thenReturn(receiptDTO);

        ReceiptDTO actual = receiptService.get(RECEIPT_ID);
        assertEquals(RECEIPT_ID, actual.getId());
        verify(receiptRepository).findById(RECEIPT_ID);
        verify(convertor).convertToReceiptDTO(receipt);
    }

    @Test
    void get_shouldThrowAnEntityNotFoundException(){
        when(receiptRepository.findById(RECEIPT_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> receiptService.get(RECEIPT_ID));
    }

    @Test
    void findByUserTelegramIdAndSortByCreationDate_ShouldReturnSortedDTOs() {
        // given

        Receipt receipt1 = new Receipt();
        receipt1.setId(1L);
        Receipt receipt2 = new Receipt();
        receipt2.setId(2L);

        List<Receipt> unsortedReceipts = List.of(receipt2, receipt1); // допустим, не по порядку
        List<Receipt> sortedReceipts = List.of(receipt1, receipt2);   // сортировщик вернёт так

        ReceiptDTO dto1 = new ReceiptDTO();
        dto1.setId(1L);
        ReceiptDTO dto2 = new ReceiptDTO();
        dto2.setId(2L);
        List<ReceiptDTO> expectedDTOs = List.of(dto1, dto2);

        // mock behavior
        when(receiptRepository.findAllByOwnerId(TELEGRAM_ID)).thenReturn(unsortedReceipts);
        when(sorter.sortByCreationDate(unsortedReceipts)).thenReturn(sortedReceipts);
        when(convertor.convertToReceiptDTO(sortedReceipts)).thenReturn(expectedDTOs);

        // when
        List<ReceiptDTO> actualDTOs = receiptService.findByUserTelegramIdAndSortByCreationDate(TELEGRAM_ID);

        // then
        assertEquals(2, actualDTOs.size());
        assertEquals(1L, actualDTOs.get(0).getId());
        assertEquals(2L, actualDTOs.get(1).getId());

        verify(receiptRepository).findAllByOwnerId(TELEGRAM_ID);
        verify(sorter).sortByCreationDate(unsortedReceipts);
        verify(convertor).convertToReceiptDTO(sortedReceipts);
    }

    @Test
    void findByMonth_ShouldReturnReceiptsForGivenMonth() {
        long telegramId = 12345L;
        int month = 7; // Июль

        Receipt julyReceipt = new Receipt();
        julyReceipt.setId(1L);
        julyReceipt.setCreationDate(LocalDateTime.of(2025, 7, 10, 12, 0));

        Receipt juneReceipt = new Receipt();
        juneReceipt.setId(2L);
        juneReceipt.setCreationDate(LocalDateTime.of(2025, 6, 15, 12, 0));

        List<Receipt> allReceipts = List.of(julyReceipt, juneReceipt);
        List<Receipt> filteredReceipts = List.of(julyReceipt); // только июль

        ReceiptDTO dto = new ReceiptDTO();
        dto.setId(1L);

        // mock
        when(receiptRepository.findAllByOwnerId(telegramId)).thenReturn(allReceipts);
        when(convertor.convertToReceiptDTO(filteredReceipts)).thenReturn(List.of(dto));

        // when
        List<ReceiptDTO> result = receiptService.findByMonth(telegramId, month);

        // then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(receiptRepository).findAllByOwnerId(telegramId);
        verify(convertor).convertToReceiptDTO(filteredReceipts);
    }

    @Test
    void decode_ShouldReturnDecodedReceiptDTO() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        String qrRawData = "t=20250721T1230&s=300.00&fn=111&i=222&fp=333&n=1";

        ReceiptDTO expectedDTO = new ReceiptDTO();
        expectedDTO.setQrRawData(qrRawData);
        expectedDTO.setSum(300.00);
        expectedDTO.setFn("111");
        expectedDTO.setI("222");
        expectedDTO.setFp("333");
        expectedDTO.setN(1);
        expectedDTO.setCreationDate(LocalDateTime.of(2025, 7, 21, 12, 30));

        when(qrCodeDecoder.readQRFromReceipt(mockFile)).thenReturn(qrRawData);

        // when
        ReceiptDTO actual = receiptService.decode(mockFile);

        // then
        assertEquals(expectedDTO.getQrRawData(), actual.getQrRawData());
        assertEquals(expectedDTO.getSum(), actual.getSum());
        assertEquals(expectedDTO.getFn(), actual.getFn());
        assertEquals(expectedDTO.getFp(), actual.getFp());
        assertEquals(expectedDTO.getI(), actual.getI());
        assertEquals(expectedDTO.getN(), actual.getN());
        assertEquals(expectedDTO.getCreationDate(), actual.getCreationDate());

        verify(qrCodeDecoder).readQRFromReceipt(mockFile);
    }

}