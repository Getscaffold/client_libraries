Scaffold Client Library Instructions
===

Building the Libraries
---

### Ruby
To build the GEM, do the following:
`rake manifest`
`rake build_gemspec`


### Java
To build the JAR, do the following:
* Right click on the project in Eclipse, and choose Export
* Export output folders
* Uncheck everything other than .classpath and Manifest
* Save the JAR somewhere outside of the current project

Generating the Documentation
---

### PHP
`phpdoc run -f scaffold-api.php -t phpdoc`

### Ruby
`rdoc --op rdoc -main lib/scaffold-api.rb`

### Java
`javadoc -d javadoc -sourcepath src -classpath gson-2.1.jar -verbose
com.getscaffold`

Setting up the Libraries
---

### Ruby
Insert the following line into your Gemfile
`gem "scaffold-api", :git => "git@github.com:Getscaffold/client_libraries.git"`


### Java
Add scaffold-api.jar and gson-2.1.jar to your classpath

You should then be able to be able

### PHP
Copy the code into your project
You will need to install pecl and pecl_http, since the 
`wget http://pear.php.net/go-pear.phar`
`php -d detect_unicode=0 go-pear.phar`
`pecl install pecl_http`
