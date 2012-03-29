package com.getscaffold;

public class ApiResponse {
	private int code;
	private ApiResponseBody body;
	
	public ApiResponse(int code, ApiResponseBody body) {
		this.code = code;
		this.body = body;
	}
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
	public ApiResponseBody getBody() {
		return body;
	}

	public void setBody(ApiResponseBody body) {
		this.body = body;
	}

	public class ApiResponseBody{
		private String requestId;
		private String token;
		private String error;

		public String getRequestId() {
			return requestId;
		}

		public void setRequestId(String requestId) {
			this.requestId = requestId;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
	}

}
