package com.jsp.bankmanagement.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseStructure<T> 
{
	private int statusCode;
	private String message;
	private T data;
}
