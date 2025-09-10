package com.harriet.data.batch;

import com.harriet.data.model.MeterReading;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class Nem12MeterReadingProcessor implements ItemProcessor<String, List<MeterReading>> {
    private static final Logger logger = LoggerFactory.getLogger(Nem12MeterReadingProcessor.class);
    private String currentNmi = null; // spring batch will create only one processor instance shared among all process calls,
    //hence nmi value persists thru process calls until nmi is reset again by the next 200 record
    @Override
    public List<MeterReading> process(String line) throws Exception {
        if(line==null || line.isEmpty()){
            return null;
        }
        String[] parts = line.split(",");
        if (parts.length ==0) {return null;}
        List<MeterReading> meterReadings = new ArrayList<>();
        switch (parts[0]) {
            case "200":
                currentNmi = parts[1];
                break;
            case "300":
                if (currentNmi==null){
                    throw new IllegalStateException("300 record encountered before 200 record, nmi is not set");
                }
                LocalDate recordDate = LocalDate.parse(parts[1], DateTimeFormatter.BASIC_ISO_DATE);
                // get meter reading from 3 to element before quality flag A/E/V
                for (int i=2;i<parts.length;i++) {
                    String reading = parts[i].trim();
                    if (List.of("A","E","V").contains(reading)){
                        logger.info("Quality Flag encountered, aka finised processing all readings from  300 record dated {} of nmi {}", parts[1], currentNmi);
                        if (meterReadings.size()!=48){
                            logger.warn("done reading a 300 record, but accumulated {} readings instead of the expected 48 readings per day", meterReadings.size());
                        }
                        break;
                    }
                    if(reading.isEmpty()|| !reading.matches("\\d+(\\.\\d+)?")){
                        logger.warn("encountered non-numeric reading for 300 record dated {} of nmi {}", parts[1], currentNmi);
                        continue;
                    }
                    int intervalsPassed = i-2; //first record at index 2, is at midnight, zero interval has passed
                    LocalDateTime timestamp = recordDate.atTime(LocalTime.MIDNIGHT.plusMinutes(intervalsPassed*30));
                    MeterReading meterReading = new MeterReading();
//                    UUID id = UUID.randomUUID();
//                    meterReading.setId(id);
                    meterReading.setNmi(currentNmi);
                    meterReading.setTimestamp(timestamp);
                    meterReading.setConsumption(new BigDecimal(reading));
                    meterReadings.add(meterReading);

                }
                break;
            default:
                //ignore 100, 500, 900 records
                break;
        }
        return meterReadings;
    }

}
