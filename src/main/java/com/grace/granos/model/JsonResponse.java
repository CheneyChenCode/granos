package com.grace.granos.model;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAlias;
@Component
public class JsonResponse {
		private int status;
		private String message;
	 	@JsonAlias("data")
	    private Object data;

	    public JsonResponse() {
			super();
		}
		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}

		public JsonResponse(Object data) {
	        this.data = data;
	    }
}
