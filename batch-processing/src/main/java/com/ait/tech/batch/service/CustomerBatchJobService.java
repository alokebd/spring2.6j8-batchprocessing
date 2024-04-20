package com.ait.tech.batch.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.ait.tech.batch.repository.CustomerRepository;

@Service
public class CustomerBatchJobService {
	
	 @Autowired
	 private JobLauncher jobLauncher;
	 @Autowired
	 private Job importCustomersJob;
	 @Autowired
	 private JobExplorer jobExplorer;
	 @Autowired
	 private CustomerRepository customerRepository;
	 
	public ResponseEntity<String> importCsvToDBJob() {
		 JobParameters jobParameters = new JobParametersBuilder()
	                .addLong("startAt", System.currentTimeMillis())
	                .toJobParameters();   
	     try {
	          JobExecution jobExecution=jobLauncher.run(importCustomersJob, jobParameters);
	        	
	        	BatchStatus batchStatus =jobExecution.getStatus();
	        	while (batchStatus.isRunning()) {
	        		System.out.println("Still running...");
	        		Thread.sleep(5000L);
	        	}
	        } 
	        catch (JobExecutionAlreadyRunningException | JobRestartException | 
	        		JobInstanceAlreadyCompleteException | JobParametersInvalidException  |
	        		InterruptedException e) {
	            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
	 
	        } 
	        return ResponseEntity.ok().body("Batch job has been invoked");
	 }
	
	
	public ResponseEntity<Map<String, Object>> getJobStatus() {
		Map<String, Object> response = new HashMap<>();
		List<JobInstance> instances = jobExplorer.getJobInstances("importCustomersJob", 0, 1);
		if (instances.isEmpty()) {
            response.put("message", "No job instance found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
		
		List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(instances.get(0));
		if (jobExecutions.isEmpty()) {
            response.put("message", "No job execution found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
		
		JobExecution lastJobExecution = jobExecutions.get(0);
		for (JobExecution jobExecution : jobExecutions) {
            if (jobExecution.getCreateTime().after(lastJobExecution.getCreateTime())) {
                lastJobExecution = jobExecution;
            }
        }
		BatchStatus batchStatus = lastJobExecution.getStatus();
        response.put("status", batchStatus.toString());
        Collection<StepExecution> stepExecutions = lastJobExecution.getStepExecutions();
        for (StepExecution stepExecution : stepExecutions) {
            response.put("readCount", stepExecution.getReadCount());
            response.put("writeCount", stepExecution.getWriteCount());
            response.put("commitCount", stepExecution.getCommitCount());
            response.put("skipCount", stepExecution.getSkipCount());
            response.put("rollbackCount", stepExecution.getRollbackCount());
            response.put("customerInDB", customerRepository.count());
            int progress = (int) (((double) stepExecution.getReadCount() / 100000) * 100);
            response.put("progress", progress + "%");
        }
        
		return ResponseEntity.ok().body(response);
	}

}
