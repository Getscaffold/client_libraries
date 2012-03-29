package com.getscaffold;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.internal.Pair;

public class UrlSigning {
	// signs a url with the specified key
	public static String sign_url(String url, String key) {
		Pair<String, Map<String, String>> response = get_params(url);
		String server = response.first;
		Map<String,String> params = response.second;
		params.put("signature", generate_signature(params, key));
		return construct_url(server, params);
	}

	  // Concatenate the server and params array into a single url
	public static String construct_url(String server, Map<String,String> params) {
	    String url = server + "?";
	   
	    int pos = 0;
	    for (String key : params.keySet()) {
	    	if (pos > 0) {
	        url += "&";
	      }
	    	try {
	    		url += key + "=" + URLEncoder.encode(params.get(key), "utf-8");
	    	} catch (UnsupportedEncodingException e) {
	    		// Won't ever happen since UTF-8 is supported
	    	}
	    	pos++;
	    } 
	    return url;
	}

	private static List PARAMS_TO_IGNORE = 
			Arrays.asList(new String[]{"action", "controller", "signature"}); 
	// generates the signature, given the key and params
	public static String generate_signature(Map<String,String> request_params, String key) {
		String raw_signature = key;
		
		for (String param : request_params.keySet()) {
			if (!PARAMS_TO_IGNORE.contains(param)){
				raw_signature += param + request_params.get(param);
			}
		}
		
		String hash = null;
		try {
		  Mac mac = Mac.getInstance("HmacSHA1");
      SecretKeySpec secret = new SecretKeySpec(" ".getBytes(),"HmacSHA1");
      mac.init(secret);
      byte[] digest = mac.doFinal(raw_signature.getBytes());
      BigInteger big_int_digest = new BigInteger(1, digest);
      hash = big_int_digest.toString(16);
      if (hash.length() % 2 != 0) {
          hash = "0" + hash;
      }
      
      hash = new String(digest);
		} catch (NoSuchAlgorithmException nsae) {
		  // Won't happen, because the algo exists.
		} catch (InvalidKeyException ike) {
		  // Won't happen, because the algo exists.
		}

		if (true) {
			System.out.println("RAW: " + raw_signature);
			System.out.println("HASH: " + hash);
		}
		return hash;
	}

	// Parses the url, returning the server name and an array of params
	private static Pair<String, Map<String, String>> get_params(String url) {
		String[] url_parts = url.split("?");
		String server = url_parts[0];
		String[] parameters = url_parts[1].split("&");
		Map<String, String> param_map = new TreeMap<String, String>();
		for(String parameter : parameters) {
			String[] param_pieces = parameter.split("=");
			try {
				param_map.put(param_pieces[0], URLDecoder.decode(param_pieces[1], "utf-8"));
			} catch (UnsupportedEncodingException e) {
				// Won't ever happen since UTF-8 is supported
			}
		}

		param_map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

		return new Pair<String, Map<String, String>>(server, param_map);
	}
}
