package com.harriet.data.batch;

import com.harriet.data.model.MeterReading;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import javax.sql.DataSource;
import java.util.ArrayList;
import org.springframework.lang.NonNull;
import java.util.List;
import java.util.stream.Collectors;

public class Nem12MeterReadingWriter implements ItemWriter<List<MeterReading>> {

    private final JdbcBatchItemWriter<MeterReading> delegate;

    public Nem12MeterReadingWriter(DataSource datasource){
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
        // Flatten Chunk<List<MeterReading>> into Chunk<MeterReading>
        Chunk<MeterReading> flatChunk = new Chunk<>(
                chunk.getItems().stream()
                        .filter(list -> list != null)
                        .flatMap(List::stream)
                        .toList()
        );

        if (!flatChunk.isEmpty()) {
            delegate.write(flatChunk);
        }

    }

}
