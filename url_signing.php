<?php

define("PRINT_URL", false);

class UrlSigning {
  # signs a url with the specified key
  static function sign_url($url, $key) {
    $array = UrlSigning::get_params($url);
    $server = $array[0];
    $params = $array[1];
    $params["signature"] = UrlSigning::generate_signature($params, $key);
    return UrlSigning::construct_url($server, $params);
  }

  # Concatenate the server and params array into a single url
  static function construct_url($server, $params) {
    $url = $server . "?";
   
    foreach(array_keys($params) as $key=>$value) {
      if ($key > 0) {
        $url .= "&";
      }
      $url .= $value . "=" . urlencode($params[$value]);
    } 
    return $url;
  }

  private static $PARAMS_TO_IGNORE = array("action", "controller", "signature"); 
  # generates the signature, given the key and params
  static function generate_signature($request_params, $key) {
    $raw_signature = "";
    ksort($request_params);
    foreach($request_params as $key => $value) {
      if (!array_key_exists($key, UrlSigning::$PARAMS_TO_IGNORE)) {
        $raw_signature .= $key . $value;
      }
    }
    $hash = hash_hmac("sha1", $raw_signature, $key);
    if (PRINT_URL) {
      echo "RAW: " . $raw_signature . "\n";
      echo "HASH: " . $hash . "\n";
    }
    return $hash;
  }

  # Parses the url, returning the server name and an array of params
  private static function get_params($url) {
    $server = strtok($url, "?");
    $query_string = strtok("?");
    $parameters = explode("&", $query_string);
    $params = array();
    foreach($parameters as $parameter) {
      if (strlen($parameter) > 0) {
        $key = strtok($parameter, "=");
        $value = strtok("=");
        $params[$key] = urldecode($value);
      }
    }
    # Set the timestamp to the current time
    $params["timestamp"] = time();
    return array($server, $params);
  }
}

?>
