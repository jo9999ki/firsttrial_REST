package de.jk.spring.firsttrial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.jk.spring.firsttrial.entity.CustomerEntity;

@Repository
public interface CustomerRepository  extends PagingAndSortingRepository<CustomerEntity, Long> {

	 //Names entity and parameter based on bean class and bean parameter name
	 @Query("SELECT cus FROM CustomerEntity cus WHERE cus.lastName LIKE %:lastName%")
	 public List<CustomerEntity> findByLastNameLike(@Param("lastName") String lastName);
	 
}
