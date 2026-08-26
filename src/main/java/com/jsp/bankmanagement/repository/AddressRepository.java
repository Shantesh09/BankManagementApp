package com.jsp.bankmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.bankmanagement.entity.Address;
import com.jsp.bankmanagement.entity.Bank;

public interface AddressRepository extends JpaRepository<Address, Integer>
{
	//to check pincode is unique or not
	boolean existsByPincode(Integer pincode);
	
	//to fetch address by bank id
	Optional<Address> findByBankId(Integer bankId);
	
	//to fetch address by city
	List<Address> findByCity(String city);
	
	//to fech address by city and street
	Optional<Address> findByCityAndStreet(String city, String street);
}
