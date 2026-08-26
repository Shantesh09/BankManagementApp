package com.jsp.bankmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jsp.bankmanagement.dto.ResponseStructure;
import com.jsp.bankmanagement.entity.Bank;
import com.jsp.bankmanagement.service.BankService;

@RequestMapping("/api/bank")
@RestController
public class BankController 
{
	@Autowired
	public BankService bankService;
	
	@PostMapping("/save")
	public ResponseEntity<ResponseStructure<Bank>> saveBank(@RequestBody Bank bank)
	{
		return new ResponseEntity<>(bankService.saveBank(bank), HttpStatus.CREATED);
	}
	
	@GetMapping("/all")
	public ResponseEntity<ResponseStructure<List<Bank>>> getAllBanks()
	{
		return new ResponseEntity<>(bankService.getAllBanks(), HttpStatus.OK);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ResponseStructure<Bank>> getById(@PathVariable Integer id)
	{
		return new ResponseEntity<>(bankService.getById(id), HttpStatus.OK);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseStructure<String>> deleteBank(@PathVariable Integer id)
	{
		return new ResponseEntity<>(bankService.deleteBank(id),HttpStatus.OK);
	}
	
	@GetMapping("/page/{pageNumber}/{pageSize}/sort/{fieldName}")
	public ResponseEntity<ResponseStructure<Page<Bank>>> getByPaginationAndSorting(@PathVariable Integer pageNumber,@PathVariable Integer pageSize,@PathVariable String fieldName)
	{
		return new ResponseEntity<>(bankService.getByPaginationAndSorting(pageNumber, pageSize, fieldName), HttpStatus.OK);
	}
	
	@GetMapping("/ifsc/{ifsc}")
	public ResponseEntity<ResponseStructure<Bank>> getByIfsc(@PathVariable String ifsc)
	{
		return new ResponseEntity<>(bankService.getByIfsc(ifsc), HttpStatus.OK);
	}
	
	@GetMapping("/addressId/{addressId}")
	public ResponseEntity<ResponseStructure<Bank>> getByPincode(@PathVariable Integer addressId)
	{
	    return new ResponseEntity<>(bankService.getByAddressId(addressId), HttpStatus.OK);
	}
	
	@GetMapping("/city/{city}")
	public ResponseEntity<ResponseStructure<List<Bank>>> getByCity(@PathVariable String city)
	{
		return new ResponseEntity<>(bankService.getByCity(city), HttpStatus.OK);
	}
	
	@GetMapping("/contact/{contact}")
	public ResponseEntity<ResponseStructure<Bank>> getByContact(@PathVariable Long contact)
	{
		return new ResponseEntity<>(bankService.getByContact(contact), HttpStatus.OK);
	}
	
}
