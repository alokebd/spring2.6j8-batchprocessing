package com.ait.tech.batch.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ait.tech.batch.service.CustomerBatchJobService;

@RestController
@RequestMapping("/jobs")
public class CustomerBatchJobController {

	@Autowired
	CustomerBatchJobService customerBatchJobService;

    @PostMapping("/start")
    public ResponseEntity<String> startJob() {
    	return customerBatchJobService.importCsvToDBJob();
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getJobStatus() {
    	return customerBatchJobService.getJobStatus();
    }
}
