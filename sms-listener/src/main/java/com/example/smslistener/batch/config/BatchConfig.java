package com.example.smslistener.batch.config;

import com.example.smslistener.batch.SmsItemProcessor;
import com.example.smslistener.batch.SmsItemWriter;
import com.example.smslistener.batch.listener.SmsSkipListener;
import com.example.smslistener.model.SmsRequest;
import com.example.smslistener.repository.SmsRequestRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

@Configuration
public class BatchConfig {

    public static final int CHUNK_SIZE = 15;

    @Autowired private JobRepository jobRepository;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private SmsRequestRepository smsRequestRepository;
    @Autowired private SmsItemProcessor smsItemProcessor;
    @Autowired private SmsItemWriter smsItemWriter;
    @Autowired private SmsSkipListener smsSkipListener;

    @Bean
    @StepScope
    public RepositoryItemReader<SmsRequest> smsItemReader(
            @Value("#{jobParameters['batchId']}") String batchId) {
        return new RepositoryItemReaderBuilder<SmsRequest>()
                .name("smsItemReader")
                .repository(smsRequestRepository)
                .methodName("findByBatchId")
                .arguments(Collections.singletonList(batchId))
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public Step smsValidationStep() {
        return new StepBuilder("smsValidationStep", jobRepository)
                .<SmsRequest, SmsRequest>chunk(CHUNK_SIZE)
                .reader(smsItemReader(null))
                .processor(smsItemProcessor)
                .writer(smsItemWriter)
                .transactionManager(transactionManager)
                .faultTolerant()
                .skipLimit(50)
                .skip(Exception.class)
                .listener(smsSkipListener)
                .build();
    }

    @Bean
    public Job smsProcessingJob() {
        return new JobBuilder("smsProcessingJob", jobRepository)
                .start(smsValidationStep())
                .build();
    }
}