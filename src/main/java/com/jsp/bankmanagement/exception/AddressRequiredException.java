package com.jsp.bankmanagement.exception;

public class AddressRequiredException extends RuntimeException
{
    public AddressRequiredException(String message)
    {
        super(message);
    }
}
