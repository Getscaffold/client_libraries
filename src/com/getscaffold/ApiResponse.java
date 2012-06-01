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
		private String request_id;
		private String token;
		private String error;
		private String status;
		private String ssn_valid;
		private String background_check_passed;
		private String ext_user_id;
		private String signature;

		public String getRequestId() {
			return request_id;
		}

		public void setRequestId(String requestId) {
			this.request_id = requestId;
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

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getSsnValid() {
			return ssn_valid;
		}

		public void setSsnValid(String ssn_valid) {
			this.ssn_valid = ssn_valid;
		}

		public String getBackgroundCheckPassed() {
			return background_check_passed;
		}

		public void setBackgroundCheckPassed(String background_check_passed) {
			this.background_check_passed = background_check_passed;
		}

		public String getExtUserId() {
			return ext_user_id;
		}

		public void setExtUserId(String ext_user_id) {
			this.ext_user_id = ext_user_id;
		}

		public String getSignature() {
			return signature;
		}

		public void setSignature(String signature) {
			this.signature = signature;
		}
	}

}
