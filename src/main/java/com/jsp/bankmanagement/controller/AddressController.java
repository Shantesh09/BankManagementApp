package com.jsp.bankmanagement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jsp.bankmanagement.dto.ResponseStructure;
import com.jsp.bankmanagement.entity.Address;
import com.jsp.bankmanagement.service.AddressService;

@RequestMapping("/api/address")
@RestController
public class AddressController 
{
	@Autowired AddressService addressService;
	
	@GetMapping("/{id}")
	public ResponseEntity<ResponseStructure<Address>> getById(@PathVariable Integer id)
	{
		return new ResponseEntity<>(addressService.getById(id), HttpStatus.OK);
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity<ResponseStructure<String>> updateAddress(@PathVariable Integer id, @RequestBody Map<String, Object> data)
	{
	    return new ResponseEntity<>(addressService.updateAddress(id, data), HttpStatus.OK);
	}
	
	@GetMapping("/bank/{bankId}")
	public ResponseEntity<ResponseStructure<Address>> getAddressByBank(@PathVariable Integer bankId)
	{
	    return new ResponseEntity<>(addressService.getAddressByBank(bankId), HttpStatus.OK);
	}
	
	@GetMapping("/city/{city}")
	public ResponseEntity<ResponseStructure<List<Address>>> getByCity(@PathVariable String city)
	{
		return new ResponseEntity<>(addressService.getByCity(city), HttpStatus.OK);
	}
	
	@GetMapping("/city/{city}/street/{street}")
	public ResponseEntity<ResponseStructure<Address>> getByCityAndStreet(@PathVariable String city, @PathVariable String street)
	{
		return new ResponseEntity<>(addressService.getByCityAndStreet(city, street), HttpStatus.OK);
	}
}
