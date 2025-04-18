
package com.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class StepExecutionListenerJobConfig {

    /*
     StepExecutionListener 실습
     - Step 실행 전/후에 콜백을 통해 로그 출력 및 상태 변경 가능
     - afterStep()에서 ExitStatus 조작도 가능
    */
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job stepExecutionListenerJob() {
        return new JobBuilder("stepExecutionListenerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(stepWithListener())
                .build();
    }

    @Bean
    public Step stepWithListener() {
        return new StepBuilder("stepWithListener", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("🔧 Step 내부 실행 중...");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(new StepExecutionListenerSupport() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        System.out.println("🟡 [BeforeStep] " + stepExecution.getStepName());
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        System.out.println("🟢 [AfterStep] " + stepExecution.getStepName());
                        return ExitStatus.COMPLETED; // 또는 커스텀 상태 반환 가능
                    }
                })
                .build();
    }
}