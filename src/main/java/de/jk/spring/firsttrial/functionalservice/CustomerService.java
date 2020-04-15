package de.jk.spring.firsttrial.functionalservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import de.jk.spring.firsttrial.entity.CustomerEntity;
import de.jk.spring.firsttrial.exception.RecordNotFoundException;
import de.jk.spring.firsttrial.repository.CustomerRepository;

/**
 * @author jkirchner
 * Functional Interface
 */
@Service
public class CustomerService {

	@Autowired
    CustomerRepository repository;
     
    public List<CustomerEntity> getAllCustomers(Integer pageNo, Integer pageSize, String sortBy)
    {

    	Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
    	 
        Page<CustomerEntity> pagedResult = repository.findAll(paging);
         
        if(pagedResult.hasContent()) {
            return pagedResult.getContent();
        } else {
            return new ArrayList<CustomerEntity>();
        }
    	
    	/*
    	List<CustomerEntity> customerList = repository.findAll();
         
        if(customerList.size() > 0) {
            return customerList;
        } else {
            return new ArrayList<CustomerEntity>();
        }
        */
    }
    
    public CustomerEntity getCustomerById(Long id) throws RecordNotFoundException 
    {
        Optional<CustomerEntity> customer = repository.findById(id);
         
        if(customer.isPresent()) {
            return customer.get();
        } else {
            throw new RecordNotFoundException("No customer record exist for given id");
        }
    }

    
    public List<CustomerEntity> getCustomersByNameLike(Optional<String> name)
    {
    	if( name.isPresent()){
        	List<CustomerEntity> customers = repository.findByLastNameLike(name.get()); 
        	return customers;              
    	} else {
        	return new ArrayList<CustomerEntity>();
        }
    }


    public CustomerEntity createOrUpdateCustomer(CustomerEntity entity) throws RecordNotFoundException 
    {
        Optional<CustomerEntity> customer = repository.findById(entity.getId());
         
        if(customer.isPresent()) 
        {
            CustomerEntity newEntity = customer.get();
            newEntity.setEmail(entity.getEmail());
            newEntity.setFirstName(entity.getFirstName());
            newEntity.setLastName(entity.getLastName());
 
            newEntity = repository.save(newEntity);
             
            return newEntity;
        } else {
        	entity.setId(null);
            entity = repository.save(entity);
            return entity;
        }
    } 
     
    public void deleteCustomerById(Long id) throws RecordNotFoundException 
    {
        Optional<CustomerEntity> employee = repository.findById(id);
         
        if(employee.isPresent()) 
        {
            repository.deleteById(id);
        } else {
            throw new RecordNotFoundException("No employee record exist for given id");
        }
    } 
    
}
