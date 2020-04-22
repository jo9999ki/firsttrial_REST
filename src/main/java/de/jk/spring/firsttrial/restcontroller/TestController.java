package de.jk.spring.firsttrial.restcontroller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import de.jk.spring.firsttrial.entity.CustomerEntity;
import de.jk.spring.firsttrial.exception.ErrorsResponse;
import de.jk.spring.firsttrial.exception.RecordNotFoundException;
import de.jk.spring.firsttrial.functionalservice.CustomerService;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
 
@RestController
@RequestMapping("/test")
//Swagger
@Api(value="TestController for Distributed Tracing Test")
public class TestController 
{

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired private RestTemplate restTemplate; 
	
  	@Bean public RestTemplate getRestTemplate() { 
  		  return new RestTemplate(); 
  	} 

  	@RequestMapping("/test") public ResponseEntity home() { 
  	  logger.info("you called test"); 
  	  return new ResponseEntity("Hello World", HttpStatus.OK); 

  	} 

  	@RequestMapping("/calltest") public String callHome() { 
  	  logger.info("calling test"); 
  	  return restTemplate.getForObject("http://localhost:8080/test/test", String.class); 

  	}

}