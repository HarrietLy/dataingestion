package com.harriet.data.batch;

import com.harriet.data.model.MeterReading;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableBatchProcessing
//@RequiredArgsConstructor
public class JobConfig {

//    @Bean
//    @StepScope
//    public ItemReader<String> reader(@org.springframework.beans.factory.annotation.Value("#{jobParameters['filePath']}") String filePath) {
//        return new Nem12MeterReadingReader(filePath);
//    }

    @Bean
    public ItemProcessor<String, List<MeterReading>> processor() {
        return new Nem12MeterReadingProcessor();
    }

    @Bean
    public ItemWriter<List<MeterReading>> writer(DataSource dataSource) {
        return new Nem12MeterReadingWriter(dataSource);
    }

    @Bean
    public Step nem12Step(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          ItemReader<String> reader,
                          ItemProcessor<String, List<MeterReading>> processor,
                          ItemWriter<List<MeterReading>> writer) {
        return new StepBuilder("nem12Step", jobRepository)
                .<String, List<MeterReading>>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .build();
    }

    @Bean
    public Job nem12Job(JobRepository jobRepository, Step nem12Step) {
        return new JobBuilder("nem12Job", jobRepository)
                .start(nem12Step)
                .build();
    }
}
