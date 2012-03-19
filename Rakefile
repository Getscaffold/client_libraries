require 'rubygems'
require 'rake'
require 'echoe'

Echoe.new('scaffold-api', '0.1.6') do |p|
  p.description = "Client library for accessing the Scaffold API."
  p.url = "http://github.com/dana11235/scaffold-api"
  p.author = "Dana Levine"
  p.email = "dana@getscaffold.com"
  p.ignore_pattern = ["tmp/*","script/*"]
  p.development_dependencies = []
end

Dir["#{File.dirname(__FILE__)}/tasks/*.rake"].sort.each { |ext| load ext }
