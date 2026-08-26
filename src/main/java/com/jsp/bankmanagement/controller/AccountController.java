package com.jsp.bankmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jsp.bankmanagement.dto.AccountType;
import com.jsp.bankmanagement.dto.ResponseStructure;
import com.jsp.bankmanagement.entity.Account;
import com.jsp.bankmanagement.service.AccountService;

@RequestMapping("/api/account")
@RestController
public class AccountController 
{
	@Autowired
	public AccountService accountService;
	
	@PostMapping("/save")
	public ResponseEntity<ResponseStructure<Account>> saveAccount(@RequestBody Account account)
	{
		return new ResponseEntity<>(accountService.saveAccount(account), HttpStatus.CREATED);
	}
	
	@GetMapping("/all")
	public ResponseEntity<ResponseStructure<List<Account>>> getAllAccounts()
	{
		return new ResponseEntity<>(accountService.getAllAccounts(), HttpStatus.OK);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ResponseStructure<Account>> getById(@PathVariable Integer id)
	{
		return new ResponseEntity<>(accountService.getById(id), HttpStatus.OK);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseStructure<String>> deleteAccount(@PathVariable Integer id)
	{
		return new ResponseEntity<>(accountService.deleteAccount(id), HttpStatus.OK);
	}
	
	@PatchMapping("/deposit/{accountNumber}/{amount}")
	public ResponseEntity<ResponseStructure<Account>> depositAmount(@PathVariable Long accountNumber, @PathVariable Double amount)
	{
		return new ResponseEntity<>(accountService.depositAmount(accountNumber, amount), HttpStatus.OK);
	}
	
	@PatchMapping("/withdraw/{accountNumber}/{amount}")
	public ResponseEntity<ResponseStructure<Account>> withdrawAmount(@PathVariable Long accountNumber, @PathVariable Double amount)
	{
	    return new ResponseEntity<>(accountService.withdrawAmount(accountNumber, amount), HttpStatus.OK);
	}
	
	@PatchMapping("/transfer/{senderAccountNumber}/{receiverAccountNumber}/{amount}")
	public ResponseEntity<ResponseStructure<String>> transferAmount(@PathVariable Long senderAccountNumber, @PathVariable Long receiverAccountNumber, @PathVariable Double amount)
	{
	    return new ResponseEntity<>(accountService.transferAmount(senderAccountNumber, receiverAccountNumber, amount), HttpStatus.OK);
	}
	
	@GetMapping("/bank/{bankId}")
	public ResponseEntity<ResponseStructure<List<Account>>> getByBank(@PathVariable Integer bankId)
	{
	    return new ResponseEntity<>(accountService.getByBank(bankId), HttpStatus.OK);
	}
	
	@GetMapping("/accountType/{accountType}")
	public ResponseEntity<ResponseStructure<List<Account>>> getByAccountType(@PathVariable AccountType accountType)
	{
	    return new ResponseEntity<>(accountService.getByAccountType(accountType), HttpStatus.OK);
	}
	
	@GetMapping("/balanceGreaterThan/{value}")
	public ResponseEntity<ResponseStructure<List<Account>>> getByBalanceGreaterThan(@PathVariable Double value)
	{
	    return new ResponseEntity<>(accountService.getByBalanceGreaterThan(value), HttpStatus.OK);
	}
	
	@GetMapping("/accountNumber/{accountNumber}")
	public ResponseEntity<ResponseStructure<Account>> getByAccountNumber(@PathVariable Long accountNumber)
	{
	    return new ResponseEntity<>(accountService.getByAccountNumber(accountNumber), HttpStatus.OK);
	}
	
	@GetMapping("/sort/{fieldName}")
	public ResponseEntity<ResponseStructure<List<Account>>> getBySorting(@PathVariable String fieldName)
	{
	    return new ResponseEntity<>(accountService.getBySorting(fieldName), HttpStatus.OK);
	}
}
