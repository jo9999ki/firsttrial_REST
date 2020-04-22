package de.jk.spring.firsttrial.exception;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
 
@SuppressWarnings({"unchecked","rawtypes"})
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private String TRACE_ID = "trace-id";

	@ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        logger.error("Unspecified exception occured: error message: " + ex.getLocalizedMessage() +  " stacktrace: " + ex.getStackTrace() + " cause: " + ex.getCause());
        List<ErrorResponse> errors = new ArrayList<>();
        errors.add(new ErrorResponse("50000", "Server Error", null, details));
        return new ResponseEntity(errors, getHeadersWithTraceid(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
 
    @ExceptionHandler(RecordNotFoundException.class)
    public final ResponseEntity<Object> handleUserNotFoundException(RecordNotFoundException ex, WebRequest request) {
    	ErrorsResponse errors = new ErrorsResponse();
    	List<String> details = new ArrayList<>();
        logger.error("Record not found: error message: " + ex.getLocalizedMessage() +  " stacktrace: " + ex.getStackTrace() + " cause: " + ex.getCause());
        details.add(ex.getLocalizedMessage());
        //ErrorResponse error = new ErrorResponse("40001", "Record Not Found", null, details);
        errors.getErrorList().add(new ErrorResponse("40001", "Record Not Found", null, details));
        return new ResponseEntity(errors, getHeadersWithTraceid(), HttpStatus.NOT_FOUND);
    }
 
    @Override //Override Method in ResponseEntityExceptionHandler
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {    
    	ErrorsResponse errors = new ErrorsResponse();
	    ex.getBindingResult().getAllErrors().forEach((error) -> {
	        String fieldName = ((FieldError) error).getField();
	        String errorMessage = error.getDefaultMessage();
	        String code = error.getCode();
	        logger.error("Input validation error: error code: " + code + " error message: " + errorMessage + " field: " + fieldName);
	        errors.getErrorList().add(new ErrorResponse(code, errorMessage, fieldName, null));
	    });
	    return new ResponseEntity(errors, getHeadersWithTraceid(), HttpStatus.BAD_REQUEST);
	}
    
    private HttpHeaders getHeadersWithTraceid() {
    	HttpHeaders headers = new HttpHeaders();
    	headers.add(TRACE_ID, MDC.get("X-B3-TraceId"));
    	return headers;
    }

}
