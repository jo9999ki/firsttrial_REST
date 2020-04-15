package de.jk.spring.firsttrial.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
 
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecordNotFoundException extends RuntimeException 
{
 	private static final long serialVersionUID = -8560227185169702912L;

	public RecordNotFoundException(String exception) {
        super(exception);
    }
}