<?php
include 'url_signing.php';

class Api {
  private $token;
  private $service_id;
  private $api_key;
  private $server;
  const DEFAULT_SERVER = "https://api.getscaffold.com";
  const VERSION = "1.0";
  const BASE_URL = "/v1/";

  function __construct($service_id, $api_key, $server = self::DEFAULT_SERVER) {
    $this->service_id = $service_id;
    $this->api_key = $api_key;
    $this->server = $server;

    if ($this->ping()) {
      $this->token = $this->get_token();
    }
  }

  ##
  # Sends a ping to the remote server
  function ping() {
    $response = $this->send_command("ping");
    return $response["code"] == 200;
  }

  ##
  # Requests a token that can be used to make api calls
  function get_token() {
    $response = $this->send_command("get_token", $this->api_key);
    if ($response["code"] == 200) {
      return $response["body"]["token"];
    } else {
      throw new InvalidArgumentException($response["body"]["error"]);
    }
  }

  ##
  # Verifies that the signature mechanism is valid
  function verify_signature() {
    $response = $this->send_command("verify_signature", $this->token);
    return $response["code"] == 200;
  }

  ##
  # Submits a background check request
  # optional params is a hash of additional parameters (defaults in parens): 
  #   fail_on_misdemeanor (true) – failure if a misdemeanor is found
  #   fail_on_felony  (true) – failure if a felony is found
  #   fail_on_dui (true) – failure if a DUI or related offense is found
  #   fail_on_sex_offense (true) – failure if a sex offense is found
  #   fail_on_terrorist  (true) – failure if the subject appears on the OFAC terrorist list
  #   check_type (electronic) - determines which type of check is run
  #     [electronic, recent, all]
  function submit_background_check_request($first_name, $last_name, $dob, $ssn,
      $email, $callback_url, $optional_params = array()) {
    $response = $this->send_command("background_check/submit_request", $this->token, 
      array_merge(array("first_name" => $first_name, "last_name" => $last_name, 
        "dob" => $dob, "ssn" => $ssn, "email" => $email, 
        "callback_url" => $callback_url), $optional_params));
    if ($response["code"] == 200) {
      return $response["body"]["request_id"];
    } else {
      throw new InvalidArgumentException($response["body"]["error"]);
    }
  }

  ##
  # Sends a postcard with a code to the specified mailing address
  #
  # Returns request_id
  function mailing_address_send_code($first_name, $last_name, $address, $city, 
      $state, $zip, $email, $address2 = NULL) {
    $response = $this->send_command("mailing_address/send_code", $this->token, 
      array("first_name" => $first_name, "last_name" => $last_name, 
        "address" => $address, "address2" => $address2, "city" => $city, 
        "state" => $state, "zip" => $zip, "email" => $email));
    if ($response["code"] == 200) {
      return $response["body"]["request_id"];
    } else {
      throw new InvalidArgumentException($response["body"]["error"]);
    }
  }

  ##
  # Checks the provided mailing address code/request_id.
  function mailing_address_check_code($code, $request_id) {
    $response = $this->send_command("mailing_address/check_code", $this->token, 
      array("code" => $code, "request_id" => $request_id));
    return ($response["code"] == 200);
  }

  ##
  # Sends a code to the specified phone number.
  #
  # Returns request_id
  function phone_number_send_code($phone_number, $email, $type = "sms") {
    $response = $this->send_command("phone_number/send_code", $this->token, 
      array("phone_number" => $phone_number, "email" => $email));
    if ($response["code"] == 200) {
      return $response["body"]["request_id"];
    } else {
      throw new InvalidArgumentException($response["body"]["error"]);
    }
  }

  ##
  # Checks the provided phone number code/request_id.
  function phone_number_check_code($code, $request_id) {
    $response = $this->send_command("phone_number/check_code", $this->token, 
      array("code" => $code, "request_id" => $request_id));
    return ($response["code"] == 200);
  }

  ##
  # Submits a professional license request.
  #
  # Returns request_id
  function professional_license_submit_request($first_name, $last_name, $state,
      $license_type, $license_number, $callback_url, $email) {
    $response = $this->send_command("license/submit_request", $this->token, 
      array("first_name" => $first_name, "last_name" => $last_name, "state" => $state,
       "license_type" => $license_type, "license_number" => $license_number,
       "callback_url" => $callback_url, "email" => $email));
    if ($response["code"] == 200) {
      return $response["body"]["request_id"];
    } else {
      throw new InvalidArgumentException($response["body"]["error"]);
    }
  }


  function print_me() {
    echo "SID: ".$this->service_id." API: ".$this->api_key." SVR: ".$this->server."\n";
  }

  private function send_command($command, $signed_with = NULL, $params = array()) {
    if ($signed_with != NULL) {
      $params["service_id"] = $this->service_id;
      $params["timestamp"] = time();
      $params["signature"] = 
        UrlSigning::generate_signature($params, $signed_with);
    }
    $url = 
      UrlSigning::construct_url($this->server . self::BASE_URL . $command, $params);
    return $this->send_request($url);
  }

  private function send_request($url) {
    $request = new HttpRequest($url, HttpRequest::METH_GET);
    $request->send();
    return array("code" => $request->getResponseCode(), 
                 "body" => json_decode($request->getResponseBody(), true));
  } 
}
?>
