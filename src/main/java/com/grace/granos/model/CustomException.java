package com.grace.granos.model;

import org.springframework.stereotype.Component;

@Component
public class CustomException extends Exception {
	private static final long serialVersionUID = 1L; // Add serialVersionUID
	private int status;
	public CustomException() {
		
	}
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public CustomException(String message, int status) {
        super(message);
        this.status = status;
    }

    // Constructor that also takes in the original exception (cause)
    public CustomException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
