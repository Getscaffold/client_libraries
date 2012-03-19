require 'url_signing'

module Scaffold
  class Api
    VERSION = 1.0
    DEFAULT_SERVER = "https://api.getscaffold.com"
    BASE_URI = "/v1/"

    attr_accessor :token
    attr_accessor :service_id
    attr_accessor :api_key
    attr_accessor :server

    def initialize(service_id, api_key, server = nil)
      raise ArgumentError, "Must provide service_id and api_key" unless service_id && api_key
      self.service_id = service_id
      self.api_key = api_key
      self.server = server
      if ping
        self.token = get_token
      end
    end

    ##
    # Sends a ping to the remote server
    def ping
      return_code, response = send_command("ping")
      return_code == "200"
    end

    ##
    # Requests a token that can be used to make api calls
    def get_token
      return_code, response = send_command("get_token", api_key)
      return response["token"] if return_code == "200"
      raise ArgumentError, response["error"]
    end

    ##
    # Verifies that the signature mechanism is valid
    def verify_signature
      return_code, response = send_command("verify_signature", token)
      return_code == "200"
    end

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
    def submit_background_check_request(first_name, last_name, dob, ssn, email,
        callback_url, optional_params = {})
      return_code, response = send_command("background_check/submit_request", token, 
        {:first_name => first_name, :last_name => last_name, :dob => dob, 
         :ssn => ssn, :email => email, :callback_url => callback_url
        }.merge(optional_params))
      return response["request_id"] if return_code == "200"
      raise ArgumentError, response["error"]
    end

    ##
    # Sends a postcard with a code to the specified mailing address
    #
    # Returns request_id
    def mailing_address_send_code(first_name, last_name, address, city, state,
        zip, email, address2 = nil)
      return_code, response = send_command("mailing_address/send_code", token, 
        {:first_name => first_name, :last_name => last_name, 
          :address => address, :address2 => address2, :city => city, 
          :state => state, :zip => zip, :email => email})
      return response["request_id"] if return_code == "200"
      raise ArgumentError, response["error"]
    end

    ##
    # Checks the provided mailing address code/request_id.
    def mailing_address_check_code(code, request_id)
      return_code, response = send_command("mailing_address/check_code", token, 
        {:code => code, :request_id => request_id})
      return return_code == "200"
      raise ArgumentError, response["error"]
    end

    ##
    # Sends a code to the specified phone number.
    #
    # Returns request_id
    def phone_number_send_code(phone_number, email, type = "sms")
      return_code, response = send_command("phone_number/send_code", token, 
        {:phone_number => phone_number, :email => email})
      return response["request_id"] if return_code == "200"
      raise ArgumentError, response["error"]
    end

    ##
    # Checks the provided phone number code/request_id.
    def phone_number_check_code(code, request_id)
      return_code, response = send_command("phone_number/check_code", token, 
        {:code => code, :request_id => request_id})
      return return_code == "200"
      raise ArgumentError, response["error"]
    end

    ##
    # Submits a professional license request.
    #
    # Returns request_id
    def professional_license_submit_request(first_name, last_name, state,
        license_type, license_number, callback_url, email)
      return_code, response = send_command("license/submit_request", token, 
        {:first_name => first_name, :last_name => last_name, :state => state,
         :license_type => license_type, :license_number => license_number,
         :callback_url => callback_url, :email => email})
      return response["request_id"] if return_code == "200"
      raise ArgumentError, response["error"]
    end

  private
    def send_command(command, signed_with = nil, params = {})
      if signed_with
        params[:service_id] = self.service_id
        params[:timestamp] = Time.now.to_i
        params[:signature] = 
          UrlSigning.generate_signature(params, signed_with)
      end
      url = UrlSigning.construct_url(DEFAULT_SERVER + BASE_URI + command, params)
      return send_request(url)
    end

    def send_request(url)
      uri = URI.parse(url)
      http = Net::HTTP.new(uri.host, uri.port)
      http.use_ssl = true if (uri.scheme == 'https')
      resp, output = http.get(uri.path + "?" + uri.query)
      return resp.code, JSON.parse(output)
    end
  end
end
