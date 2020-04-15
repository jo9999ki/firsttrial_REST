package de.jk.spring.firsttrial.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import de.jk.spring.firsttrial.entity.CustomerEntity;
import de.jk.spring.firsttrial.repository.CustomerRepository;

@Component
public class HealthCheck implements HealthIndicator {
  
	@Autowired
    CustomerRepository repository;
    @Override
    public Health health() {
        int errorCode = check(); // perform some specific health check
        if (errorCode != 0) {
            return Health.down()
              .withDetail("No customer record found in database", errorCode).build();
        }
        return Health.up().build();
    }
     
    public int check() {
    	Pageable paging = PageRequest.of(0, 10,Sort.by("id"));
    	Page<CustomerEntity> pagedResult = repository.findAll(paging);
        
        if(pagedResult.hasContent()) {
            return 0;
        } else {
            return 1;
        }
    }
}