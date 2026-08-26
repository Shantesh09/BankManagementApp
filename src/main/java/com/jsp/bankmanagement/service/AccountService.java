package com.jsp.bankmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jsp.bankmanagement.dto.AccountType;
import com.jsp.bankmanagement.dto.ResponseStructure;
import com.jsp.bankmanagement.entity.Account;
import com.jsp.bankmanagement.exception.BankNotFoundException;
import com.jsp.bankmanagement.exception.IdNotFoundException;
import com.jsp.bankmanagement.exception.InvalidAmountException;
import com.jsp.bankmanagement.exception.InvalidDetailsException;
import com.jsp.bankmanagement.exception.NoMinimumBalanceException;
import com.jsp.bankmanagement.exception.NoRecordsAvailableException;
import com.jsp.bankmanagement.repository.AccountRepository;
import com.jsp.bankmanagement.repository.BankRepository;

@Service
public class AccountService 
{
	@Autowired
	public AccountRepository accountRepository;
	
	@Autowired
	public BankRepository bankRepository;
	
	public ResponseStructure<Account> saveAccount(Account account)
	{
	    ResponseStructure<Account> res = new ResponseStructure<>();

	    if (accountRepository.existsByAccountNumber(account.getAccountNumber()))
	    {
	        throw new DataIntegrityViolationException("Account number already exists");
	    }

		if (account.getBank() == null || !bankRepository.existsById(account.getBank().getId()))
	    {
	        throw new BankNotFoundException("Bank does not exist");
	    }

	    if ((account.getAccountType() == AccountType.CURRENT || account.getAccountType() == AccountType.SAVINGS) && account.getBalance() < 5000)
	    {
	        throw new NoMinimumBalanceException("Balance should be greater than or equal to 5000");
	    }

	    Account savedAccount = accountRepository.save(account);

	    res.setStatusCode(HttpStatus.CREATED.value());
	    res.setMessage("Account created successfully");
	    res.setData(savedAccount);

	    return res;
	}
	
	
	public ResponseStructure<List<Account>> getAllAccounts()
	{
		List<Account> accounts = accountRepository.findAll();
		
		ResponseStructure<List<Account>> res = new ResponseStructure<>();
		
		if(accounts.isEmpty())
		{
			throw new NoRecordsAvailableException("No account recods found");
		}
		else
		{
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Account records fetched successfully");
			res.setData(accounts);
			return res;
		}
	}
	
	
	public ResponseStructure<Account> getById(Integer id)
	{
		Optional<Account> opt = accountRepository.findById(id);
		
		ResponseStructure<Account> res = new ResponseStructure<>();
		
		if(opt.isEmpty())
		{
			throw new IdNotFoundException("Account record with Id : "+id+" does not exist");
		}
		else
		{
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Account record with Id : "+id+" fetched successfully");
			res.setData(opt.get());
			return res;
		}
	}
	
	
	public ResponseStructure<String> deleteAccount(Integer id)
	{
		Optional<Account> opt = accountRepository.findById(id);
		
		ResponseStructure<String> res = new ResponseStructure<>();
		
		if(opt.isEmpty())
		{
			throw new IdNotFoundException("Account record with Id : "+id+" does not exist");
		}
		else
		{
			accountRepository.delete(opt.get());
			
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Account record with Id : "+id+" deleted successfully");
			res.setData("Success");
			return res;
		}
	}
	
	
	public ResponseStructure<Account> depositAmount(Long accountNumber, Double amount)
	{
		Optional<Account> opt = accountRepository.findByAccountNumber(accountNumber);
		
		ResponseStructure<Account> res = new ResponseStructure<>();
		
		if(opt.isEmpty())
		{
			throw new NoRecordsAvailableException("Account record with Account Number :"+accountNumber+" does not exist ");
		}
		
		if(amount <= 0)
		{
			throw new InvalidAmountException("Amount must be greater than 0");
		}
		
		Account account = opt.get();
		account.setBalance(account.getBalance() + amount);
		Account updatedAccount = accountRepository.save(account);
		
	    res.setStatusCode(HttpStatus.OK.value());
	    res.setMessage("Amount deposited successfully");
	    res.setData(updatedAccount);
	    return res;
		
	}
	
	
	public ResponseStructure<Account> withdrawAmount(Long accountNumber, Double amount)
	{
	    Optional<Account> opt = accountRepository.findByAccountNumber(accountNumber);

	    ResponseStructure<Account> res = new ResponseStructure<>();

	    if(opt.isEmpty())
	    {
	        throw new NoRecordsAvailableException(
	            "Account record with Account Number : "+accountNumber+" does not exist");
	    }

	    if(amount <= 0)
	    {
	        throw new InvalidAmountException("Withdrawal amount must be greater than 0");
	    }

	    Account account = opt.get();

	    if(account.getBalance() < amount)
	    {
	        throw new InvalidAmountException("Insufficient balance");
	    }

	    Double updatedAmount = account.getBalance() - amount;

	    if((account.getAccountType() == AccountType.CURRENT || account.getAccountType() == AccountType.SAVINGS) && updatedAmount < 5000)
	    {
	        throw new NoMinimumBalanceException("Minimum balance of 5000 must be maintained");
	    }

	    account.setBalance(updatedAmount);

	    Account updatedAccount = accountRepository.save(account);

	    res.setStatusCode(HttpStatus.OK.value());
	    res.setMessage("Amount withdrawn successfully");
	    res.setData(updatedAccount);

	    return res;
	}
	
	
	public ResponseStructure<String> transferAmount(Long senderAccountNumber, Long receiverAccountNumber,  Double amount)
	{
	    ResponseStructure<String> res = new ResponseStructure<>();

	    if(amount <= 0)
	    {
	        throw new InvalidAmountException("Transfer amount must be greater than 0");
	    }

	    Optional<Account> senderOpt = accountRepository.findByAccountNumber(senderAccountNumber);

	    Optional<Account> receiverOpt = accountRepository.findByAccountNumber(receiverAccountNumber);

	    if(senderOpt.isEmpty())
	    {
	        throw new NoRecordsAvailableException("Sender account with Account Number : "+senderAccountNumber+ " does not exist");
	    }

	    if(receiverOpt.isEmpty())
	    {
	        throw new NoRecordsAvailableException("Receiver account with Account Number : "+receiverAccountNumber+" does not exist");
	    }

	    // 3. Sender and Receiver must not be same
	    if(senderAccountNumber.equals(receiverAccountNumber))
	    {
	        throw new InvalidDetailsException("Sender and Receiver accounts cannot be the same");
	    }

	    Account sender = senderOpt.get();
	    Account receiver = receiverOpt.get();

	    if(sender.getBalance() < amount)
	    {
	        throw new InvalidAmountException("Insufficient balance in sender account");
	    }

	    Double senderUpdatedBalance = sender.getBalance() - amount;

	    if((sender.getAccountType() == AccountType.CURRENT || sender.getAccountType() == AccountType.SAVINGS) && senderUpdatedBalance < 5000)
	    {
	        throw new NoMinimumBalanceException("Minimum balance of 5000 must be maintained in sender account");
	    }

	    sender.setBalance(senderUpdatedBalance);

	    receiver.setBalance(receiver.getBalance() + amount);

	    accountRepository.save(sender);
	    accountRepository.save(receiver);

	    res.setStatusCode(HttpStatus.OK.value());
	    res.setMessage("Amount transferred successfully");
	    res.setData("Success");
	    return res;
	}
	
	
	public ResponseStructure<List<Account>> getByBank(Integer bankId)
	{
	    List<Account> accounts = accountRepository.findByBankId(bankId);
	    
	    ResponseStructure<List<Account>> res = new ResponseStructure<>();

	    if(accounts.isEmpty())
	    {
	        throw new NoRecordsAvailableException("No accounts found for Bank Id : " + bankId);
	    }
	    
	    res.setStatusCode(HttpStatus.OK.value());
	    res.setMessage("Accounts fetched successfully");
	    res.setData(accounts);
	    return res;
	}
	
	
	public ResponseStructure<List<Account>> getByAccountType(AccountType accountType)
	{
		List<Account> accounts = accountRepository.findByAccountType(accountType);
		
		ResponseStructure<List<Account>> res = new ResponseStructure<>();
		
		if(accounts.isEmpty())
		{
			throw new NoRecordsAvailableException("Account records with Account Type :"+accountType+"Does not exist");
		}
		 
	    res.setStatusCode(HttpStatus.OK.value());
	    res.setMessage("Account records with Account Type :"+accountType+" fetched successfully");
	    res.setData(accounts);
	    return res;
	}
	
	
	public ResponseStructure<List<Account>> getByBalanceGreaterThan(Double value)
	{
		List<Account> accounts = accountRepository.findByBalanceGreaterThan(value);
	
		ResponseStructure<List<Account>> res = new ResponseStructure<>();
		
		if(accounts.isEmpty())
		{
			throw new NoRecordsAvailableException("Account records with balance greater than : "+value+"Does not exist");
		}
		
		res.setStatusCode(HttpStatus.OK.value());
	    res.setMessage("Account records with balance greater than : "+value+" fetched successfully");
	    res.setData(accounts);
	    return res;
	}
	
	public ResponseStructure<Account> getByAccountNumber(Long accountNumber)
	{
	    Optional<Account> opt = accountRepository.findByAccountNumber(accountNumber);

	    ResponseStructure<Account> res = new ResponseStructure<>();

	    if(opt.isEmpty())
	    {
	        throw new NoRecordsAvailableException("Account record with Account Number : "+accountNumber+" does not exist");
	    }
	    
	    res.setStatusCode(HttpStatus.OK.value());
	    res.setMessage("Account record with Account Number : "+accountNumber+" fetched successfully");
	    res.setData(opt.get());
	    return res;
	}
	
	
	public ResponseStructure<List<Account>> getBySorting(String fieldName)
	{
		List<Account> accounts = accountRepository.findAll(Sort.by(fieldName).ascending());
		
		ResponseStructure<List<Account>> res = new ResponseStructure<>();
		
		if(accounts.isEmpty())
		{
			throw new NoRecordsAvailableException("No Account records found");
		}
		
		res.setStatusCode(HttpStatus.OK.value());
	    res.setMessage("accounts fetched successfully and sorted by "+fieldName+" in ascending order");
	    res.setData(accounts);
	    return res;
	}
}
