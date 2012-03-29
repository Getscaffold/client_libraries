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
	public static String signUrl(String url, String key) {
		Pair<String, Map<String, String>> response = getParams(url);
		String server = response.first;
		Map<String,String> params = response.second;
		params.put("signature", generateSignature(params, key));
		return constructUrl(server, params);
	}

	  // Concatenate the server and params array into a single url
	public static String constructUrl(String server, Map<String,String> params) {
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
	public static String generateSignature(Map<String,String> requestParams, String key) {
		String rawSignature = "";
		
		for (String param : requestParams.keySet()) {
			if (!PARAMS_TO_IGNORE.contains(param)){
				rawSignature += param + requestParams.get(param);
			}
		}
		
		String hash = null;
		try {
			// Do some mumbo jumbo to generate the Hmac-sha1 hash
		  Mac mac = Mac.getInstance("HmacSHA1");
      SecretKeySpec secret = new SecretKeySpec(key.getBytes(),"HmacSHA1");
      mac.init(secret);
      byte[] digest = mac.doFinal(rawSignature.getBytes());
      BigInteger bigIntDigest = new BigInteger(1, digest);
      hash = bigIntDigest.toString(16);
      if (hash.length() % 2 != 0) {
        hash = "0" + hash;
      }
      
		} catch (NoSuchAlgorithmException nsae) {
		  // Won't happen, because the algo exists.
		} catch (InvalidKeyException ike) {
		  // Won't happen, because the algo exists.
		}

		if (true) {
			System.out.println("RAW: " + rawSignature);
			System.out.println("HASH: " + hash);
		}
		return hash;
	}

	// Parses the url, returning the server name and an array of params
	private static Pair<String, Map<String, String>> getParams(String url) {
		String[] urlParts = url.split("?");
		String server = urlParts[0];
		String[] parameters = urlParts[1].split("&");
		Map<String, String> paramMap = new TreeMap<String, String>();
		for(String parameter : parameters) {
			String[] paramPieces = parameter.split("=");
			try {
				paramMap.put(paramPieces[0], URLDecoder.decode(paramPieces[1], "utf-8"));
			} catch (UnsupportedEncodingException e) {
				// Won't ever happen since UTF-8 is supported
			}
		}

		paramMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

		return new Pair<String, Map<String, String>>(server, paramMap);
	}
}
