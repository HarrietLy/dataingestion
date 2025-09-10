package com.harriet.data.batch;

import com.harriet.data.model.MeterReading;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Nem12MeterReadingWriter implements ItemWriter<List<MeterReading>> {

    private final JdbcBatchItemWriter<MeterReading> delegate;
    private final JdbcTemplate jdbcTemplate;
    private final static Logger logger = LoggerFactory.getLogger(Nem12MeterReadingWriter.class);

    public Nem12MeterReadingWriter(DataSource datasource){
        this.jdbcTemplate = new JdbcTemplate(datasource);

        delegate = new JdbcBatchItemWriter<>();
        delegate.setDataSource(datasource);
        delegate.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        delegate.setSql("""
                INSERT
                INTO meter_readings ( nmi, timestamp, consumption)
                VALUES (:nmi, :timestamp, :consumption)
                """);
        try{
            delegate.afterPropertiesSet();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    @Override
    public void write(@NonNull Chunk<? extends List<MeterReading>> chunk) throws Exception {
        // Flatten Chunk<List<MeterReading>> into List<MeterReading>
        List<MeterReading> flatList = chunk.getItems().stream()
                .filter(list -> list != null)
                .flatMap(List::stream)
                .toList();

        List<MeterReading> filtered = flatList.stream()
                .filter(reading -> !exists(reading)) //filter out duplicates
                .toList();

        if (!filtered.isEmpty()) {
            delegate.write(new Chunk<>(filtered));
            logger.info("written {} meter reading records", filtered.size());
        }

    }

    private boolean exists(MeterReading reading) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM meter_readings WHERE nmi = ? AND timestamp = ?",
                Long.class,
                reading.getNmi(),
                Timestamp.valueOf(reading.getTimestamp())
        );
        return count != null && count > 0;
    }

}
