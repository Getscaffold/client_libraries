module UrlSigning
  DIGEST  = OpenSSL::Digest::Digest.new('sha1')

  # Number of seconds signed request can be used for
  SIGNATURE_VALID_TIME = 300

  # signs a url with the specified key
  def self.sign_url(url, key)
    server, params = get_params(url)
    params[:signature] = generate_signature(params, key)
    return construct_url(server, params)
  end

  PARAMS_TO_IGNORE = ["action", "controller", "signature"] 
  # generates the signature, given the key and params
  def self.generate_signature(request_params, key)
    raw_signature = ""
    request_params.keys.sort.each do |request_param|
      unless PARAMS_TO_IGNORE.include? request_param.to_s
        raw_signature << request_param.to_s << 
          request_params[request_param].to_s
      end 
    end
    return OpenSSL::HMAC.hexdigest(DIGEST, key, raw_signature)
  end

  private
    # Parses the url, returning the server name and an array of params
    def self.get_params(url)
      server, query_string = url.split "?"
      parameters = query_string.split "&"
      params = {}
      parameters.each do |parameter|
        if !parameter.empty?
          key, value = parameter.split "="
          params[key.to_sym] = CGI::unescape(value)
        end
      end

      # Set the timestamp to the current time
      params[:timestamp] = Time.now.to_i

      return server, params
    end

    # Concatenate the server and params array into a single url
    def self.construct_url(server, params)
      url = server + "?"
      params.keys.each_with_index do |param, index|
        url << "&" unless index == 0
        url << "#{param.to_s}=#{CGI::escape(params[param].to_s)}"
      end
      return url 
    end
end
