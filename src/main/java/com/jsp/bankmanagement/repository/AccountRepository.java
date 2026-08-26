package com.jsp.bankmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.bankmanagement.dto.AccountType;
import com.jsp.bankmanagement.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Integer>
{
	//to check if the account is present or not
	Boolean existsByBankId(Integer id);
	
	//to check account no. is unique or not
	Boolean existsByAccountNumber(Long accountNumber);
	
	//to find account by account number
	Optional<Account> findByAccountNumber(Long accountNumber);
	
	//to find account by bank
	List<Account> findByBankId(Integer bankId);
	
	//to find account by account type
	List<Account> findByAccountType(AccountType accountType);
	
	//to find account with balance greater than a value
	List<Account> findByBalanceGreaterThan(Double value);
	
}
