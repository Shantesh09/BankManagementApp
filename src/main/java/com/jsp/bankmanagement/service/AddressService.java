package com.jsp.bankmanagement.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jsp.bankmanagement.dto.ResponseStructure;
import com.jsp.bankmanagement.entity.Address;
import com.jsp.bankmanagement.entity.Bank;
import com.jsp.bankmanagement.exception.IdNotFoundException;
import com.jsp.bankmanagement.exception.NoRecordsAvailableException;
import com.jsp.bankmanagement.repository.AddressRepository;

@Service
public class AddressService 
{
	@Autowired
	public AddressRepository addressRepository;
	
	public ResponseStructure<Address> getById(Integer id)
	{
		Optional<Address> opt = addressRepository.findById(id);
		
		ResponseStructure<Address> res = new ResponseStructure<>();
		
		if(opt.isEmpty())
		{
			throw new IdNotFoundException("Address record with Id : "+id+" does not exist");
		}
		else
		{
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Address record with Id : "+id+" fetched successfully");
			res.setData(opt.get());
			return res;
		}
	}
	
	
	public ResponseStructure<String> updateAddress(Integer addressId, Map<String, Object> data)
	{
	    ResponseStructure<String> res = new ResponseStructure<>();

	    Optional<Address> opt = addressRepository.findById(addressId);

	    if(opt.isPresent())
	    {
	        Address address = opt.get();

	        for(Map.Entry<String, Object> entry : data.entrySet())
	        {
	            String key = entry.getKey();
	            Object value = entry.getValue();

	            switch(key)
	            {
	                case "street":
	                    address.setStreet((String)value);
	                    break;

	                case "city":
	                    address.setCity((String)value);
	                    break;

	                case "state":
	                    address.setState((String)value);
	                    break;

	                case "pincode":
	                    address.setPincode((Integer)value);
	                    break;
	            }
	        }

	        addressRepository.save(address);

	        res.setStatusCode(HttpStatus.OK.value());
	        res.setMessage("Address Updated Successfully");
	        res.setData("Success");

	        return res;
	    }
	    else
	    {
	        throw new IdNotFoundException("Address with Id " + addressId + " does not exist in DB");
	    }
	}
	
	
	public ResponseStructure<Address> getAddressByBank(Integer bankId)
	{
	    Optional<Address> opt = addressRepository.findByBankId(bankId);

	    ResponseStructure<Address> res = new ResponseStructure<>();

	    if(opt.isEmpty())
	    {
	    	throw new NoRecordsAvailableException("Address record with bank id : "+bankId+" does not exist");
	    	
	        
	    }
	    else
	    {
	    	res.setStatusCode(HttpStatus.OK.value());
	        res.setMessage("Address record with bank id : "+bankId+" fetched successfully");
	        res.setData(opt.get());

	        return res;
	    }
	}
	
	
	public ResponseStructure<List<Address>> getByCity(String city)
	{
		List<Address> addresses = addressRepository.findByCity(city);
		
		ResponseStructure<List<Address>> res = new ResponseStructure<>();
		
		if(addresses.isEmpty())
		{
			throw new NoRecordsAvailableException("No address records by City : "+city+" found");
		}
		else
		{
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Address record with City : "+city+" fetched successfully");
			res.setData(addresses);
			return res;
		}
	}
	
	
	public ResponseStructure<Address> getByCityAndStreet(String city,String street)
	{
		Optional<Address> opt = addressRepository.findByCityAndStreet(city, street);
		
		ResponseStructure<Address> res = new ResponseStructure<>();
		
		if(opt.isEmpty())
		{
			throw new IdNotFoundException("Address record with City : "+city+" and Street : "+street+ " does not exist");
		}
		else
		{
			res.setStatusCode(HttpStatus.OK.value());
			res.setMessage("Address record with City : "+city+" and Strret : "+street+ " fetched successfully");
			res.setData(opt.get());
			return res;
		}
	}

}
