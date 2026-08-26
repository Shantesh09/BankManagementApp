package com.jsp.bankmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jsp.bankmanagement.dto.ResponseStructure;
import com.jsp.bankmanagement.entity.Account;
import com.jsp.bankmanagement.entity.Bank;
import com.jsp.bankmanagement.exception.AddressRequiredException;
import com.jsp.bankmanagement.exception.BankDeletionException;
import com.jsp.bankmanagement.exception.IdNotFoundException;
import com.jsp.bankmanagement.exception.InvalidDetailsException;
import com.jsp.bankmanagement.exception.NoRecordsAvailableException;
import com.jsp.bankmanagement.repository.AccountRepository;
import com.jsp.bankmanagement.repository.AddressRepository;
import com.jsp.bankmanagement.repository.BankRepository;

@Service
public class BankService 
{
	@Autowired
	public BankRepository bankRepository;
	
	@Autowired
	public AddressRepository addressRepository;
	
	@Autowired
	public AccountRepository accountRepository;
	
	public ResponseStructure<Bank> saveBank(Bank bank)
	{
		ResponseStructure<Bank> res = new ResponseStructure<>();
		
		if(bankRepository.existsByIfsc(bank.getIfsc()))
		{
			throw new DataIntegrityViolationException("IFSC code already exists");
		}
		
		if (bank.getContact() == null || bank.getContact().toString().length() != 10) 
		{
		    throw new InvalidDetailsException("Contact number must be exactly 10 digits");
		}
		
		if(bankRepository.existsByContact(bank.getContact()))
		{
			throw new DataIntegrityViolationException("Contact already exists");
		}
		
		if (bank.getAddress() == null) {
		    throw new AddressRequiredException("Address is required to save Bank");
		}
		
		if (bank.getAddress().getPincode() == null || bank.getAddress().getPincode().toString().length() != 6) 
		{
		    throw new InvalidDetailsException("Pincode must be exactly 6 digits");
		}
		
		if(addressRepository.existsByPincode(bank.getAddress().getPincode()))
		{
			throw new DataIntegrityViolationException("Pincode already exists");
		}
		
		res.setStatusCode(HttpStatus.CREATED.value());
		res.setMessage("Bank created successfully");
		res.setData(bankRepository.save(bank));
		return res;
	}
	
	
	public ResponseStructure<List<Bank>> getAllBanks()
	{
		List<Bank> banks = bankRepository.findAll();
		
		ResponseStructure<List<Bank>> res = new ResponseStructure<>();
		
		if(banks.isEmpty())
		{
			throw new NoRecordsAvailableException("No bank records available");
		}
		else
		{
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("All record fetched successfully");
			res.setData(banks);
			return res;
		}
	}
	
	
	public ResponseStructure<Bank> getById(Integer id)
	{
		Optional<Bank> opt = bankRepository.findById(id);
		
		ResponseStructure<Bank> res = new ResponseStructure<>();
		
		if(opt.isEmpty())
		{
			throw new IdNotFoundException("Bank record with Id : "+id+" not found");
		}
		else
		{
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Bank record fetched successfully");
			res.setData(opt.get());
			return res;
		}
	}
	
	public ResponseStructure<String> deleteBank(Integer id)
	{
	    Optional<Bank> opt = bankRepository.findById(id);

	    ResponseStructure<String> res = new ResponseStructure<>();

	    if(opt.isEmpty())
	    {
	        throw new IdNotFoundException("Bank record with Id : " + id + " not found");
	    }

	    if(accountRepository.existsByBankId(id))
	    {
	        throw new BankDeletionException("Bank cannot be deleted because accounts are associated with it");
	    }

	    bankRepository.delete(opt.get());

	    res.setStatusCode(HttpStatus.OK.value());
	    res.setMessage("Bank record deleted successfully");
	    res.setData("Success");

	    return res;
	}
	
	
	public ResponseStructure<Page<Bank>>  getByPaginationAndSorting(Integer pageNumber, Integer pageSize, String fieldName)
	{
		Page<Bank> page = bankRepository.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(fieldName).ascending()));
		
		ResponseStructure<Page<Bank>> res = new ResponseStructure<>();
		
		if(page.isEmpty())
		{
			throw new NoRecordsAvailableException("Page is empty");
		}
		else
		{
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Banks retrived successfully for the requested page and sorted by "+fieldName);
			res.setData(page);
			return res;
		}
	}
	
	
	public ResponseStructure<Bank> getByIfsc(String ifsc)
	{
		Optional<Bank> opt = bankRepository.findByIfsc(ifsc);
		
		ResponseStructure<Bank> res =  new ResponseStructure<>();
		
		if(opt.isEmpty())
		{
			throw new NoRecordsAvailableException("Bank record with Ifsc : "+ifsc+" does not exist");
		}
		else
		{
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Bank record with Ifsc : "+ifsc+" fetched successfully");
			res.setData(opt.get());
			return res;
		}
	}
	
	
	public ResponseStructure<Bank> getByAddressId(Integer addressId)
	{
	    Optional<Bank> opt = bankRepository.findByAddressId(addressId);

	    ResponseStructure<Bank> res = new ResponseStructure<>();

	    if(opt.isEmpty())
	    {
	    	throw new NoRecordsAvailableException("Bank record with address pincode " +addressId+ " does nor exist");
	    }
	    else
	    {
	    	res.setStatusCode(HttpStatus.OK.value());
	        res.setMessage("Bank record with address id " + addressId+ " fetched successfully");
	        res.setData(opt.get());
	        return res;
	    }
	}
	
	
	public ResponseStructure<List<Bank>> getByCity(String city)
	{
		List<Bank> banks = bankRepository.findByAddressCity(city);
		
		ResponseStructure<List<Bank>> res = new ResponseStructure<>();
		
		if(banks.isEmpty())
		{
			throw new NoRecordsAvailableException("Bank record with City : "+city+" does not exist");
		}
		else
		{
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Bank record with City : "+city+" fetched successfully");
			res.setData(banks);
			return res;
		}
	}
	
	
	public ResponseStructure<Bank> getByContact(Long contact)
	{
		Optional<Bank> opt = bankRepository.findByContact(contact);
		
		ResponseStructure<Bank> res = new ResponseStructure<>();
		
		if(opt.isEmpty())
		{
			throw new NoRecordsAvailableException("Bank record with Contact : "+contact+" does not exist");
		}
		else
		{
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Bank record with Contact : "+contact+" fetched successfully");
			res.setData(opt.get());
			return res;
		}
	}
}
