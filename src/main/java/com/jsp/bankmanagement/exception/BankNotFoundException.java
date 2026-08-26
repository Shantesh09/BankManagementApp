package com.jsp.bankmanagement.exception;

public class BankNotFoundException extends RuntimeException 
{
	public BankNotFoundException(String message)
	{
		super(message);
	}
}
