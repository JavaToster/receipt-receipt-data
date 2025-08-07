package com.example.receipt_data.services;

import com.example.receipt_data.DTO.statistics.DailyStatisticDTO;
import com.example.receipt_data.DTO.receipt.ReceiptDTO;
import com.example.receipt_data.DTO.statistics.StatisticDTO;
import com.example.receipt_data.DTO.statistics.Top3RatingDTO;
import com.example.receipt_data.DTO.user.UserDTO;
import com.example.receipt_data.QRCodeUtil.QRCodeDecoder;
import com.example.receipt_data.clients.UserClient;
import com.example.receipt_data.forExceptions.exceptions.EntityIsExistException;
import com.example.receipt_data.forExceptions.exceptions.ReadingQRCodeDataException;
import com.example.receipt_data.models.Receipt;
import com.example.receipt_data.repositories.ReceiptRepository;
import com.example.receipt_data.util.Convertor;
import com.example.receipt_data.util.Sorter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptService {
    private static final DateTimeFormatter FORMATTER_FOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");
    private static final DateTimeFormatter FORMATTER_FOR_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final ReceiptRepository receiptRepository;
    private final QRCodeDecoder qrCodeDecoder;
    private final Convertor convertor;
    private final Sorter sorter;
    private final UserClient userClient;

    public ReceiptDTO decode(MultipartFile file){
        String valueOfQRCode;
        try{
            valueOfQRCode = qrCodeDecoder.readQRFromReceipt(file);
        }catch (Exception e){
            throw new ReadingQRCodeDataException("Could not read QRCode data, please try send again");
        }

        return decodeQRCodeData(valueOfQRCode);

    }

    @Transactional
    public void save(ReceiptDTO receiptDTO, long telegramIdOfOwner) {
        if(isExist(receiptDTO)){
            throw new EntityIsExistException("Receipt is exist");
        }
        Receipt receipt = convertor.convertToReceipt(receiptDTO);
        receipt.setOwnerId(telegramIdOfOwner);
        receiptRepository.save(receipt);
        receiptDTO.setId(receipt.getId());
    }

    //    t=20250630T1702&s=1057.00&fn=7380440801524581&i=659&fp=1350420190&n=1
    private ReceiptDTO decodeQRCodeData(String query) {
        LocalDateTime time = null;
        double sumOfReceipt = 0;
        String fn = "";
        String i = "";
        String fp = "";
        int n = 0;
        if (query == null || query.isEmpty()) {
            throw new NullPointerException();
        }

        int start = 0;
        int length = query.length();

        while (start < length) {
            // Находим конец пары
            int end = query.indexOf('&', start);
            if (end == -1) end = length;

            // Ищем разделитель ключа и значения
            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                // Пара без значения
                continue;
            } else {
                // Извлекаем ключ и значение
                String key = query.substring(start, separator);
                String value = query.substring(separator+1, end);
                if (key.equals("s")) {
                    sumOfReceipt = Double.parseDouble(value);
                }else if(key.equals("t")) {
                    try {
                        time = parseDateTime(value);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else if(key.equals("fn")){
                    fn = value;
                }else if(key.equals("fp")){
                    fp = value;
                }else if(key.equals("i")){
                    i = value;
                }else if(key.equals("n")){
                    n = Integer.parseInt(value);
                }
                start = end + 1; // Переход к следующей паре
            }
        }
        ReceiptDTO receipt = new ReceiptDTO();
        receipt.setSum(sumOfReceipt);
        receipt.setFp(fp);
        receipt.setFn(fn);
        receipt.setI(i);
        receipt.setCreationDate(time);
        receipt.setN(n);
        receipt.setQrRawData(query);
        return receipt;
    }

    private LocalDateTime parseDateTime(String time) {
        LocalDateTime creationTime;
        try{
            creationTime = LocalDateTime.parse(time, FORMATTER_FOR_TIMESTAMP);
        }catch (DateTimeParseException e){
            creationTime = LocalDate.parse(time.split("T")[0], FORMATTER_FOR_DATE).atStartOfDay();
        }
        return creationTime;
    }

    public ReceiptDTO get(long id){
        Receipt receipt = receiptRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Receipt with this id not found, please try sending another id"));
        return convertor.convertToReceiptDTO(receipt);
    }

    public List<ReceiptDTO> findByUserTelegramIdAndSortByCreationDate(long telegramId){
        List<Receipt> receipts = receiptRepository.findAllByOwnerId(telegramId);
        List<Receipt> sortedReceiptsByCreationDate = sorter.sortByCreationDate(receipts);
        return convertor.convertToReceiptDTO(sortedReceiptsByCreationDate);
    }

    public List<ReceiptDTO> findByMonth(long telegramId, int month) {
        List<Receipt> receipts = receiptRepository.findAllByOwnerId(telegramId);
        List<Receipt> boughtReceiptsInMonth = filterByMonth(receipts, month);
        return convertor.convertToReceiptDTO(boughtReceiptsInMonth);
    }

    private List<Receipt> filterByMonth(List<Receipt> allReceipts, int monthNumber){
        return allReceipts.stream().filter(receipt -> receipt.getCreationDate().getMonth().getValue() == monthNumber).toList();
    }

    private boolean isExist(ReceiptDTO receipt){
        return receiptRepository.existsByQrRawData(receipt.getQrRawData());
    }

    //TODO dont forget remove Thread.sleep()
    @Cacheable(value = "daily-statistic")
    public DailyStatisticDTO getDailyStatistic() {
        try {
            Thread.sleep(1000*3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<Top3RatingDTO> maxReceiptsUsers = receiptRepository.findTop3ReceiptsCount();
        DailyStatisticDTO dailyStatisticDTO = new DailyStatisticDTO();
        for (Top3RatingDTO top: maxReceiptsUsers){
            UserDTO userDTO = userClient.getUser(top.getOwner_id());
            StatisticDTO statisticDTO = new StatisticDTO(userDTO.getUsername(), top.getCnt());
            dailyStatisticDTO.addStatistic(statisticDTO);
        }
        return dailyStatisticDTO;
    }
}

