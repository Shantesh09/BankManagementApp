package com.jsp.bankmanagement.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jsp.bankmanagement.dto.ResponseStructure;

@RestControllerAdvice
public class GlobalExceptionHandler 
{
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ResponseStructure<String>> handleDIVE(DataIntegrityViolationException exception)
	{
		ResponseStructure<String> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.CONFLICT.value());
		res.setMessage(exception.getMessage());
		res.setData("Failure");
		
		return new ResponseEntity<>(res, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(InvalidDetailsException.class)
	public ResponseEntity<ResponseStructure<String>> handleICE(InvalidDetailsException exception)
	{
		ResponseStructure<String> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.BAD_REQUEST.value());
		res.setMessage(exception.getMessage());
		res.setData("Failure");
		
		return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(AddressRequiredException.class)
	public ResponseEntity<ResponseStructure<String>> handleARE(AddressRequiredException exception)
	{
	    ResponseStructure<String> res = new ResponseStructure<>();
	    res.setStatusCode(HttpStatus.BAD_REQUEST.value());
	    res.setMessage(exception.getMessage());
	    res.setData("Failure");

	    return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
	}
	
	
	@ExceptionHandler(NoRecordsAvailableException.class)
	public ResponseEntity<ResponseStructure<String>> handleNRAE(NoRecordsAvailableException exception)
	{
		ResponseStructure<String> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.NOT_FOUND.value());
		res.setMessage(exception.getMessage());
		res.setData("Failure");
		
		return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(IdNotFoundException.class)
	public ResponseEntity<ResponseStructure<String>> handleINFE(IdNotFoundException exception)
	{
		ResponseStructure<String> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.NOT_FOUND.value());
		res.setMessage(exception.getMessage());
		res.setData("Failure");
		
		return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(BankDeletionException.class)
	public ResponseEntity<ResponseStructure<String>> handleBDE(BankDeletionException exception)
	{
		ResponseStructure<String> res = new ResponseStructure<>();
		res.setStatusCode(HttpStatus.CONFLICT.value());
		res.setMessage(exception.getMessage());
		res.setData("Failure");
		
		return new ResponseEntity<>(res, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(BankNotFoundException.class)
	public ResponseEntity<ResponseStructure<String>> handleBNFE(BankNotFoundException exception)
	{
		ResponseStructure<String> res = new ResponseStructure<>();
	    res.setStatusCode(HttpStatus.NOT_FOUND.value());
	    res.setMessage(exception.getMessage());
	    res.setData("Failure");

	    return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(NoMinimumBalanceException.class)
	public ResponseEntity<ResponseStructure<String>> handleNMBE(NoMinimumBalanceException exception)
	{
		ResponseStructure<String> res = new ResponseStructure<>();
	    res.setStatusCode(HttpStatus.BAD_REQUEST.value());
	    res.setMessage(exception.getMessage());
	    res.setData("Failure");

	    return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(InvalidAmountException.class)
	public ResponseEntity<ResponseStructure<String>> handleIAE(InvalidAmountException exception)
	{
		ResponseStructure<String> res = new ResponseStructure<>();
	    res.setStatusCode(HttpStatus.BAD_REQUEST.value());
	    res.setMessage(exception.getMessage());
	    res.setData("Failure");

	    return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
	}
}
