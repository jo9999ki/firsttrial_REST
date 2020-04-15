package de.jk.spring.firsttrial.exception;

import java.time.Instant;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
 
@XmlRootElement(name = "error")
//Swagger
@ApiModel(description = "Standard error format")
public class ErrorResponse 
{
    public ErrorResponse(String code, String message, String parameter, List<String> details) {
        super();
        this.code = code;
        this.parameter = parameter;
        this.message = message;
        this.details = details;
    }
 
    //Timestamp error occured
    @ApiModelProperty(required = true, dataType = "String", notes  = "timestamp (UTC), when the error occured, JSONZ format", position = 1, example = "2020-04-02T08:09:18.687701800Z") //Swagger
    private String timestamp = Instant.now().toString();
    
    //Unique error code
    @ApiModelProperty(required = true, dataType = "String", notes  = "unique code for single error type", position = 2, example = "Size, NotNull") //Swagger
    private String code;
    
    //General error message about nature of error
    @ApiModelProperty(required = true, dataType = "String", notes  = "localized user readable message", position = 3, example = "muss zwischen 0 und 255 liegen") //Swagger
    private String message;
 
    //Input parameter, which caused the error
    @ApiModelProperty(required = false, dataType = "String", notes  = "optional, e.g. if validation of input parameter fails", position = 4, example = "lastName") //Swagger
    private String parameter;
    
    //Specific errors in API request processing
    @ApiModelProperty(required = false, dataType = "String", notes  = "optional, e.g. if more details can be posted", position = 5) //Swagger
    private List<String> details;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public List<String> getDetails() {
		return details;
	}

	public void setDetails(List<String> details) {
		this.details = details;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	
    
}