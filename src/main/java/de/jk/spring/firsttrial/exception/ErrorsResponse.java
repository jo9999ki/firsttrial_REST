package de.jk.spring.firsttrial.exception;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
//Swagger
@ApiModel(description = "List of errors - by default each method might provides 1..n errors in on response")
public class ErrorsResponse {
	//Swagger
	@ApiModelProperty(required = true, dataType = "ErrorResponse", notes  = "List of errors")
	private List<ErrorResponse> errorList = new ArrayList<>();

	public List<ErrorResponse> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<ErrorResponse> errorList) {
		this.errorList = errorList;
	}
	
	
	
}
