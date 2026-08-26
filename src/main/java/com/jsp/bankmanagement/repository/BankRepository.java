package com.jsp.bankmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.bankmanagement.entity.Bank;

public interface BankRepository extends JpaRepository<Bank, Integer>
{
	//to check ifsc is unique or not
	boolean existsByIfsc(String ifsc);
	
	//to check contact is unique or not
	boolean existsByContact(Long contact);
	
	//fetch bank by ifsc code
	Optional<Bank> findByIfsc(String ifsc);
	
	//fetch bank by address
	Optional<Bank> findByAddressId(Integer addressId);
	
	//fetch bank by contact
	Optional<Bank> findByContact(Long contact);
	
	//fetch bank by city
	List<Bank> findByAddressCity(String city);
}
