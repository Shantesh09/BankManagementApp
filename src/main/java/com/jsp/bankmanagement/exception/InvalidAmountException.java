package com.jsp.bankmanagement.exception;

public class InvalidAmountException extends RuntimeException 
{
	public InvalidAmountException(String message)
	{
		super(message);
	}
}
