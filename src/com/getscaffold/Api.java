package com.getscaffold;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;

class Api {
  private String token = null;
  private String serviceId;
  private String apiKey;
  private String server;
  public static final String DEFAULT_SERVER = 
    "https://api.getscaffold.com";
  public static final String VERSION = "1.0";
  public static final String BASE_URL = "/v1/";

  public Api(String serviceId, String apiKey, String server) throws IOException {
    this.serviceId = serviceId;
    this.apiKey = apiKey;
    this.server = server;

    if (ping()) {
      getToken();
    }
  }

  public Api(String serviceId, String apiKey) throws IOException {
    new Api(serviceId, apiKey, DEFAULT_SERVER);
  }
  
  public boolean ping() throws IOException {
    ApiResponse response = sendCommand("ping", null, new TreeMap<String,String>());
    return response.getCode() == 200;
  }

  public String getToken() throws IOException {
  	if (token == null) {
  		ApiResponse response = sendCommand("get_token", apiKey, new TreeMap<String,String>());
  		if (response.getCode() == 200) {
  			this.token = response.getBody().getToken();
  		} else {
  			throw new IOException(response.getBody().getError());
  		}
  	}
  	return token;
  }
  
  // Verifies that the signature mechanism is valid
  public boolean verifySignature() throws IOException {
    ApiResponse response = sendCommand("verify_signature", token, new TreeMap<String,String>());
    return (response.getCode() == 200);
  }
  
  /**
   * Submits a background check request
   * optional params is a hash of additional parameters (defaults in parens): 
   *   fail_on_misdemeanor (true) Ð failure if a misdemeanor is found
   *   fail_on_felony  (true) Ð failure if a felony is found
   *   fail_on_dui (true) Ð failure if a DUI or related offense is found
   *   fail_on_sex_offense (true) Ð failure if a sex offense is found
   *   fail_on_terrorist  (true) Ð failure if the subject appears on the OFAC terrorist list
   *   check_type (electronic) - determines which type of check is run
   *     [electronic, recent, all]
   **/
  public String submitBackgroundCheckRequest(String firstName, String lastName, 
  		String dob, String ssn, String email, String callbackUrl, 
  		Map<String,String> optionalParams) throws IOException {
  	Map<String,String> params = new TreeMap<String,String>();
  	params.put("first_name", firstName);
  	params.put("last_name", lastName);
  	params.put("dob", dob);
  	params.put("ssn", ssn);
  	params.put("email", email);
  	params.put("callback_url", callbackUrl);
  	if (optionalParams != null) { 
  		params.putAll(optionalParams);
  	}
  	
    ApiResponse response = 
    		sendCommand("background_check/submit_request", token, params); 
    if (response.getCode() == 200) {
      return response.getBody().getRequestId();
    } else {
      throw new IOException(response.getBody().getError());
    }
  }

  /**
   * Sends a postcard with a code to the specified mailing address
   *
   * Returns request_id
   **/
  public String mailingAddressSendCode(String firstName, String lastName, 
  		String address, String city, String state, String zip, String email, String address2) 
  throws IOException {
  	Map<String,String> params = new TreeMap<String,String>();
  	params.put("first_name", firstName);
  	params.put("last_name", lastName);
  	params.put("address", address);
  	params.put("city", city);
  	params.put("state", state);
  	params.put("zip", zip);
  	params.put("email", email);
  	if (address2 != null) {
  		params.put("address2", address2);
  	}
    ApiResponse response = sendCommand("mailing_address/send_code", token, params);
    if (response.getCode() == 200) {
      return response.getBody().getRequestId();
    } else {
    	throw new IOException(response.getBody().getError());
    }
  }

  /**
   * Checks the provided mailing address code/request_id.
   **/
  public boolean mailing_address_check_code(String code, String requestId) 
  		throws IOException {
  	Map<String,String> params = new TreeMap<String,String>();
  	params.put("code", code);
  	params.put("request_id", requestId);
    ApiResponse response = sendCommand("mailing_address/check_code", token, params);
    if (response.getCode() == 200) {
      return true;
    } else {
      throw new IOException(response.getBody().getError());
    }
  }

  /**
   * Sends a code to the specified phone number.
   *
   * Returns request_id
   **/
  public String phoneNumberSendCode(String phoneNumber, String email, String type) 
  		throws IOException {
  	Map<String,String> params = new TreeMap<String,String>();
  	params.put("phone_number", phoneNumber);
  	params.put("email", email);
  	params.put("type", type == null ? "sms" : type);
  	ApiResponse response = sendCommand("phone_number/send_code", token, params);
    if (response.getCode() == 200) {
      return response.getBody().getRequestId();
    } else {
    	throw new IOException(response.getBody().getError());
    }
  }

  /**
   * Checks the provided phone number code/request_id.
   **/
  public boolean phoneNumberCheckCode(String code, String requestId) 
  		throws IOException {
  	Map<String,String> params = new TreeMap<String,String>();
  	params.put("code", code);
  	params.put("request_id", requestId);
  	ApiResponse response = sendCommand("phone_number/check_code", token, params);
    if (response.getCode() == 200) {
    	return true;
    } else {
    	throw new IOException(response.getBody().getError());
    }
  }

  /**
   * Submits a professional license request.
   *
   * @return requestId
   **/
  public String professionalLicenseSubmitRequest(String firstName, String lastName, 
  		String state, String licenseType, String licenseNumber, String callbackUrl, 
  		String email) throws IOException {
  	Map<String,String> params = new TreeMap<String,String>();
  	params.put("first_name", firstName);
  	params.put("last_name", lastName);
  	params.put("state", state);
  	params.put("license_number", licenseNumber);
  	params.put("license_type", licenseType);
  	params.put("callback_url", callbackUrl);
  	params.put("email", email);
    ApiResponse response = sendCommand("license/submit_request", token, params);
    if (response.getCode() == 200) {
      return response.getBody().getRequestId();
    } else {
    	throw new IOException(response.getBody().getError());
    }
  }
  
  /** Private Methods **/
  
  private ApiResponse sendCommand(String command, String signedWith, Map<String,String> params) 
  		throws IOException {
  	if (signedWith != null) {
  		params.put("service_id", serviceId);
  		params.put("timestamp", String.valueOf(System.currentTimeMillis()));
  		params.put("signature", 
  				UrlSigning.generateSignature(params, signedWith));
  	}
  	String url = 
  			UrlSigning.constructUrl(server + BASE_URL  + command, params);
  	return send_request(new URL(url));
	  }

  private ApiResponse send_request(URL url) throws IOException {
  	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
  	InputStream is;
  	if (conn.getResponseCode() == 200){
  		is = conn.getInputStream();
  	} else {
  		is = conn.getErrorStream();
  	}
  	BufferedReader rd = new BufferedReader(new InputStreamReader(is));
  	StringBuffer sb = new StringBuffer();
  	String line;
  	while ((line = rd.readLine()) != null) {
  		sb.append(line);
  	}
  	rd.close();
  	String result = sb.toString();

  	return new ApiResponse(conn.getResponseCode(), 
  			new Gson().fromJson(result, ApiResponse.ApiResponseBody.class));
  }
}