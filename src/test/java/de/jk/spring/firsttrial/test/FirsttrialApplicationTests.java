package de.jk.spring.firsttrial.test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import de.jk.spring.firsttrial.FirsttrialApplication;
import de.jk.spring.firsttrial.entity.CustomerEntity;
import de.jk.spring.firsttrial.functionalservice.CustomerService;
import de.jk.spring.firsttrial.repository.CustomerRepository;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

//@RunWith(SpringRunner.class)
@TestMethodOrder(Alphanumeric.class)
@SpringBootTest(classes = FirsttrialApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class FirsttrialApplicationTests {
	 private static long identifier;
	 
	 @Autowired
	 CustomerRepository repository;
	 
	 @Autowired
	 CustomerService service;
	    
     @LocalServerPort
     private int port = 8080;
     
 	private Logger logger = LoggerFactory.getLogger(this.getClass());

 	//Test JPA Repository
 	@Test
	public void test0testRepositoryFindById() {
 			Optional<CustomerEntity> emp = repository.findById(2L);
 			assertThat(emp.get().getId(), Matchers.equalTo(2L));
 	}
 	
 	//Test Internal Service
 	@Test
	public void test0testServiceFindById() {
 			CustomerEntity customer = service.getCustomerById(2L);
 			assertThat(customer.getId(), Matchers.equalTo(2L));
 	}

 	
 	//Tests External Rest Controller
 	@Test
	public void test1GetCustomerList() {

		get("http://localhost:8080/customers")
				.then()
				.assertThat()
				.statusCode(200)
				.body("size()", is(3));
	}

	@Test
	public void test2GetCustomerById() {

		get("http://localhost:8080/customers/2")
				.then()
				.assertThat()
				.statusCode(200)
				.body("lastName", Matchers.equalTo("Vu"));
	}

	@Test
	public void test3GetCustomerByLastNameLike() {

		get("http://localhost:8080/customers/name/a")
				.then()
				.assertThat()
				.statusCode(200)
				.body("size()", is(2))
				.body("lastName", hasItem("Gupta"))
				.body("lastName", hasItem("America"));
	}
	
	
	@Test
    public void test4PostEndpoint() {
    	try {	
    		CustomerTestEntity customer = new CustomerTestEntity(-1L, "Harry", "Potter","harry.potter@hogwarts.com");
    		ValidatableResponse response = given().contentType(ContentType.JSON).body(customer)
	                .when().post("/customers")
	                .then()
		                .log()
	                	.body()
		                .statusCode(HttpStatus.OK.value())
	                	.body("id", notNullValue())
	                	.body("lastName", Matchers.equalTo(customer.getlastName()));
	        FirsttrialApplicationTests.identifier = Long.parseLong(response.extract().body().
	        		jsonPath().get("id").toString());
	        System.out.println("Identifier: " + FirsttrialApplicationTests.identifier);
	        assertEquals(true,true);
    	}catch (Exception e) {
    		logger.error("Error method testPostEndpoint. " + e.getStackTrace());
    		assertEquals(true,false);
		}
    }
	
    
	 @Test
	    public void test5PutEndpoint() {
	    	try {
	    		CustomerTestEntity customer = new CustomerTestEntity(FirsttrialApplicationTests.identifier, "Harry", "Potter1","harry.potter1@hogwarts.com");
	    		ValidatableResponse response = given().contentType(ContentType.JSON).body(customer)
		                .when().post("/customers")
		                .then()
		                	.log()
		                	.body()
		                	.statusCode(HttpStatus.OK.value())
		                	//.body("id", is(book.id)) 
		                	//-> this doesn't work for long or double values. Need to use JSON Path after
		                	.body("lastName", Matchers.equalTo(customer.getlastName()));
		        Long id = Long.parseLong(response.extract().body().
		        		jsonPath().get("id").toString());
		        assertEquals(id, (int)FirsttrialApplicationTests.identifier);
	    	}catch (Exception e) {
	    		logger.error("Error method testPutEndpoint. " + e.getStackTrace());
	    		assertEquals(true,false);
			}
	    }

	@Test
    public void test6DeleteEndpoint() {
    	try {
	        given()
	                .when().delete("/customers/"+ FirsttrialApplicationTests.identifier)
	                .then().statusCode(HttpStatus.NO_CONTENT.value());
    	}catch (Exception e) {
    		logger.error("Error method testDeleteEndpoint. " + e.getStackTrace());
    		assertEquals(true,false);
		}
    }
	
	/*
	 * Input Validations (Mandatory, Format, Length)
	 * */
	@Test
    public void test7PostEndpoint_WithMandatoryValidation() {
    	try {
    		CustomerTestEntity customer = new CustomerTestEntity(2L, null, null, null);
	        given().contentType(ContentType.JSON).body(customer)
	                .when().post("/customers")
	                .then()
	                	.log()
	                	.body()
	                	.statusCode(HttpStatus.BAD_REQUEST.value())
	                	.body("errorList.size()", is(3))
	                	.body("errorList.parameter", hasItems("firstName","lastName", "email"))
	        			.body("errorList.code[0]", equalTo("NotNull"))
	        			.body("errorList.code[1]", equalTo("NotNull"))
	        			.body("errorList.code[2]", equalTo("NotNull"));
    	}catch (Exception e) {
    		logger.error("Error method testPostEndpoint. " + e.getStackTrace());
    		assertEquals(true,false);
		}
    }
	
	@Test
    public void test8PostEndpoint_WithLengthValidation() {
    	try {
    		CustomerTestEntity customer = new CustomerTestEntity(2L, 
    				"12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", 
    				"12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", 
    				"12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890012345678901234567890012345678901234567890012345678901234567890012345678901234567890012345678901234567890012345678901234567890012345678901234567890"
    				);
	        given().contentType(ContentType.JSON).body(customer)
	                .when().post("/customers")
	                .then()
	                	.log()
	                	.body()
	                	.statusCode(HttpStatus.BAD_REQUEST.value())
	                	.body("errorList.size()", is(4))
	                	.body("errorList.parameter", hasItems("firstName","lastName", "email"))
	        			.body("errorList.code", hasItems("Size", "Email"));
    	}catch (Exception e) {
    		logger.error("Error method testPostEndpoint. " + e.getStackTrace());
    		assertEquals(true,false);
		}
    }
	
	@Test
    public void test9PostEndpoint_WithMailFormatValidation() {
    	try {
    		CustomerTestEntity customer = new CustomerTestEntity(-1L, "Harry", "Potter","@hogwarts.com");
	        given().contentType(ContentType.JSON).body(customer)
	                .when().post("/customers")
	                .then()
	                	.log()
	                	.body()
	                	.statusCode(HttpStatus.BAD_REQUEST.value())
	                	.body("errorList.size()", is(1))
	                	.body("errorList.parameter", hasItems("email"))
	        			.body("errorList.code[0]", equalTo("Email"));
    	}catch (Exception e) {
    		logger.error("Error method testPostEndpoint. " + e.getStackTrace());
    		assertEquals(true,false);
		}
    }
}
