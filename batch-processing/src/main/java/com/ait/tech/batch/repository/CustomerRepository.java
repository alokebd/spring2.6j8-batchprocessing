package com.ait.tech.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ait.tech.batch.entity.Customer;

public interface CustomerRepository  extends JpaRepository<Customer,Integer> {
}
