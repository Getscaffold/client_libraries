# coding: utf-8
# Client Library for interacting with the Scaffold API
#
# Author::    Dana Levine (mailto:dana@getscaffold.com
# Copyright:: Copyright (c) 2012 Speakergram, Inc

from time import time
import cgi
import hmac
import hashlib
import json
import urllib2

class api:
  VERSION = 1.0
  DEFAULT_SERVER = "https://api.getscaffold.com"
  BASE_URI = "/v1/"

  token = None
  service_id = None
  api_key = None
  server = None

  def __init__(self, service_id, api_key, server = None):
    """
    Constructs a new API object. If not specified, server defaults to
    production api server.
    """
    if service_id is None or api_key is None:
      raise ValueError("Must provide service_id and api_key")
    self.service_id = service_id
    self.api_key = api_key
    if server == None:
      self.server = api.DEFAULT_SERVER
    else:
      self.server = server

    if self.ping():
      self.token = self.get_token()

  def ping(self):
    """ Sends a ping to the remote server """
    return_code, response = self.__send_command("ping")
    return return_code == 200

  def get_token(self):
    """ Requests a token that can be used to make api calls """
    return_code, response = self.__send_command("get_token", self.api_key)
    if return_code == 200:
      return str(response["token"])
    else:
      raise ValueError(response["error"])

  def verify_signature(self):
    """ Verifies that the signature mechanism is valid """
    return_code, response = self.__send_command("verify_signature", self.token)
    return return_code == 200

  def submit_background_check_request(self, first_name, last_name, dob, ssn, email,
      callback_url, optional_params = dict()):
    """
    Submits a background check request.
    Optional params is a hash of additional parameters (defaults in parens): 
      fail_on_misdemeanor (true) – failure if a misdemeanor is found
      fail_on_felony (true) – failure if a felony is found
      fail_on_dui (true) – failure if a DUI or related offense is found
      fail_on_sex_offense (true) – failure if a sex offense is found
      fail_on_terrorist (true) – failure if the subject appears on the OFAC terrorist list
      check_type (electronic) - determines which type of check is run [electronic, recent, all]
    returns @request_id
    """
    return_code, response = self.__send_command("background_check/submit_request",
      self.token, dict(dict(first_name=first_name, last_name=last_name, dob=dob, 
      ssn=ssn, email=email, callback_url=callback_url), **optional_params))
    if return_code == 200:
      return response["request_id"]
    else:
      raise ValueError(response["error"])

  def check_background_check_result(request_id): 
    """
    Checks the result of a background check
   
    returns @results (status, ssn_valid, background_check_passed, request_id, ext_user_id, signature)
    """
      return_code, response = self.__send_command("background_check/check_result",
        self.token, dict(request_id=>request_id))
      if return_code == "200":
        return response
      else:
        raise ValueError(response["error"])

  def mailing_address_send_code(self, first_name, last_name, address, city, state,
      zip, email, address2 = None):
    """ 
    Sends a postcard with a code to the specified mailing address
    returns @request_id
    """
    return_code, response = self.__send_command("mailing_address/send_code", self.token, 
      dict(first_name=first_name, last_name=last_name, 
        address=address, address2=address2, city=city, 
        state=state, zip=zip, email=email))
    if return_code == 200:
      return response["request_id"]
    else:
      raise ValueError(response["error"])

  def mailing_address_check_code(self, code, request_id):
    """ Checks the provided mailing address code/request_id. """
    return_code, response = self.__send_command("mailing_address/check_code",
      self.token, dict(code=code, request_id=request_id))
    return return_code == 200

  def phone_number_send_code(self, phone_number, email, type = "sms"):
    """
    Sends a code to the specified phone number.
    Valid values for type are "sms" and "voice"
    returns @request_id
    """
    return_code, response = self.__send_command("phone_number/send_code", self.token, 
      dict(phone_number=phone_number, email=email))
    if return_code == 200:
      return response["request_id"]
    else:
      raise ValueError(response["error"])

  def phone_number_check_code(self, code, request_id):
    """ Checks the provided phone number code/request_id. """
    return_code, response = self.__send_command("phone_number/check_code", self.token, 
      dict(code=code, request_id=request_id))
    return return_code == 200

  def professional_license_submit_request(self, first_name, last_name, state,
      license_type, license_number, callback_url, email):
    """ 
    Submits a professional license request.
    returns @request_id
    """
    return_code, response = self.__send_command("license/submit_request", 
      self.token, 
      dict(first_name=first_name, last_name=last_name, state=state,
       license_type=license_type, license_number=license_number,
       callback_url=callback_url, email=email))
    if return_code == 200:
      return response["request_id"]
    else:
      raise ValueError(response["error"])

  def __send_command(self, command, signed_with = None, params = dict()):
    if signed_with is not None:
      params["service_id"] = self.service_id
      params["timestamp"] = int(time())
      params["signature"] = url_signing.generate_signature(params, signed_with)
    url = url_signing.construct_url(self.server + self.BASE_URI + command,
                                    params)
    return self.__send_request(url)

  def __send_request(self, url):
    try:
      resp = urllib2.urlopen(url)
      data = resp.read()
      code = resp.code
    except urllib2.HTTPError, error:
      data = error.read()
      code = error.code
    return code, json.loads(data)

class url_signing:
  @staticmethod
  def sign_url(url, key):
    # signs a url with the specified key
    server, params = __get_params(url)
    params["signature"] = generate_signature(params, key)
    return construct_url(server, params)

  PARAMS_TO_IGNORE = ["action", "controller", "signature"]

  @staticmethod 
  def generate_signature(request_params, sig_key):
    # generates the signature, given the key and params
    raw_signature = ""
    for key in sorted(request_params.keys()):
      if key not in url_signing.PARAMS_TO_IGNORE:
        raw_signature += key + str(request_params[key])
    return hmac.new(sig_key, raw_signature, hashlib.sha1).hexdigest()

  @staticmethod
  def construct_url(server, params):
    # Concatenate the server and params array into a single url
    url = server + "?"
    for idx, item in enumerate(params.items()):
      if idx > 0:
        url += "&"
      url += item[0] + "=" + cgi.escape(str(item[1]))
    return url 

  def __get_params(url):
    # Parses the url, returning the server name and an array of params
    server, query_string = url.split("?")
    parameters = query_string.split("&")
    params = dict()
    for parameter in parameters:
      if len(parameter) > 0:
        key, value = parameter.split("=")
        params[key] = __unescape(value)

    # Set the timestamp to the current time
    params["timestamp"] = int(time())
    return server, params

  def __unescape(string):
    string = string.replace("&lt;", "<")
    string = string.replace("&gt;", ">")
    # this has to be last:
    string = string.replace("&amp;", "&")
    return string
