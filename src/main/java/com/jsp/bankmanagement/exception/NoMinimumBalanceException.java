package com.jsp.bankmanagement.exception;

public class NoMinimumBalanceException extends RuntimeException 
{
	public NoMinimumBalanceException(String message)
	{
		super(message);
	}
}
