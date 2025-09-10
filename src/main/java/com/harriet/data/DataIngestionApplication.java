package com.harriet.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

@SpringBootApplication
@Slf4j
public class DataIngestionApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataIngestionApplication.class);
    private final Job ingestionJob;
    private final JobLauncher jobLauncher;
    public DataIngestionApplication(Job ingestionJob, JobLauncher jobLauncher) {
        this.ingestionJob = ingestionJob;
        this.jobLauncher = jobLauncher;
    }
	public static void main(String[] args) {
		SpringApplication.run(DataIngestionApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        if (args.length !=1) {
            logger.error("Expecting 1 argument as path to file to ingest, but got "+args.length);
            return;
        }
        String filePath = args[0];

        logger.info("Starting ingestion job ingesting file at {}...", filePath);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("filePath", filePath)
                .addLong("time",System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(ingestionJob, jobParameters);
        logger.info("Job started with ID {}, at status {}", execution.getJobId(), execution.getStatus());
    }
}
