package com.harriet.data.batch;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@StepScope
@Component
public class Nem12MeterReadingReader implements ItemReader<String>, ItemStream {

    private final FlatFileItemReader<String> delegate = new FlatFileItemReader<>();
    private String buffer = null;

    public Nem12MeterReadingReader(@Value("#{jobParameters['filePath']}") String filePath) {
        Resource resource;
        if (filePath.startsWith("classpath:")) {
            resource = new ClassPathResource(filePath.substring("classpath:".length()));
        } else {
            resource = new FileSystemResource(filePath);
        }
        delegate.setResource(resource);
        delegate.setLineMapper((line, lineNumber) -> line); // simpler lambda
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            delegate.afterPropertiesSet(); // MUST call this here
            delegate.open(executionContext);
        } catch (Exception e) {
            throw new ItemStreamException("Failed to open delegate reader", e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        delegate.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        delegate.close();
    }

    @Override
    public String read() throws Exception {
        String line;
        while ((line = delegate.read()) != null) {
            if (buffer != null) {
                buffer = buffer.concat(line.trim());
                if (is300recordCompleted(line)) {
                    String completed = buffer;
                    buffer = null;
                    return completed;
                }
            } else if (line.startsWith("300,")) {
                if (is300recordCompleted(line)) {
                    return line;
                } else {
                    buffer = line;
                }
            } else {
                return line;
            }
        }
        return null;
    }

    private boolean is300recordCompleted(String line) {
        return line.contains(",A") || line.contains(",E") || line.contains(",V");
    }
}