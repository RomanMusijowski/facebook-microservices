package com.mic.eventservice.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResourceExistException extends RuntimeException {

    public ResourceExistException(String message) {
        super(message);
    }
}
