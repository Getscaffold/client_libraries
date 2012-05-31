# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = "scaffold-api"
  s.version = "0.1.8"

  s.required_rubygems_version = Gem::Requirement.new(">= 1.2") if s.respond_to? :required_rubygems_version=
  s.authors = ["Dana Levine"]
  s.date = "2012-05-31"
  s.description = "Client library for accessing the Scaffold API."
  s.email = "dana@getscaffold.com"
  s.extra_rdoc_files = ["README.rdoc", "lib/scaffold-api.rb", "lib/url_signing.rb"]
  s.files = ["Manifest", "README.rdoc", "Rake", "Rakefile", "lib/scaffold-api.rb", "lib/url_signing.rb", "scaffold-api.gemspec"]
  s.homepage = "http://github.com/dana11235/scaffold-api"
  s.rdoc_options = ["--line-numbers", "--inline-source", "--title", "Scaffold-api", "--main", "README.markdown"]
  s.require_paths = ["lib"]
  s.rubyforge_project = "scaffold-api"
  s.rubygems_version = "1.8.10"
  s.summary = "Client library for accessing the Scaffold API."

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
    else
    end
  else
  end
end
