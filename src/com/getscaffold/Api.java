package com.getscaffold;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;

class Api {
  private String token;
  private String service_id;
  private String api_key;
  private String server;
  public static final String DEFAULT_SERVER = 
    "https://api.getscaffold.com";
  public static final String VERSION = "1.0";
  public static final String BASE_URL = "/v1/";

  public Api(String service_id, String api_key, String server) throws IOException {
    this.service_id = service_id;
    this.api_key = api_key;
    this.server = server;

    if (this.ping()) {
      this.token = get_token();
      System.out.println("TOKEN: " + this.token);
    }
  }

  public Api(String service_id, String api_key) throws IOException {
    new Api(service_id, api_key, DEFAULT_SERVER);
  }
  
  public boolean ping() throws IOException {
    ApiResponse response = send_command("ping", null, new TreeMap<String,String>());
    return response.getCode() == 200;
  }

  public String get_token() throws IOException {
    ApiResponse response = send_command("get_token", api_key, new TreeMap<String,String>());
    if (response.getCode() == 200) {
      return response.getBody().getToken();
    } else {
      return null;
      //throw new InvalidArgumentException($response["body"]["error"]);
    }
  }
  
  private ApiResponse send_command(String command, String signed_with, Map<String,String> params) 
  		throws IOException {
  	if (signed_with != null) {
  		params.put("service_id", service_id);
  		params.put("timestamp", String.valueOf(System.currentTimeMillis()));
  		params.put("signature", 
  				UrlSigning.generate_signature(params, signed_with));
  	}
  	String url = 
  			UrlSigning.construct_url(server + BASE_URL  + command, params);
  	return send_request(url);
	  }

  private ApiResponse send_request(String url) throws IOException {
  	URL url_obj = new URL(url);
  	HttpURLConnection conn = (HttpURLConnection) url_obj.openConnection();
  	BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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
  
  public static void main(String[] args) {
  	 try {
  	   Api api = new Api("SCAFFOLD", "ABC", "http://localhost:3000");
  	 } catch (Exception e){
  		 throw new RuntimeException(e);
  	 }
  }
}