- checkout morphString: cvs -d:pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi co grouper-misc/morphString
- copy conf/morphString.example.properties to conf/morphString.properties
- put in an encrypt.key in conf/morphString.properties
- run the tests
- build the dist target
- run the command line [all should return same morphed string] (easy way):

  C:\mchyzer\isc\dev\grouper\morphString>java -jar dist\morphString.jar
  Enter the location of morphString.properties: conf\morphString.properties
  Enter the string to encrypt:
  The encrypted string is: c5f1d2082f272eff77ee6e734f5aef7d

- run from command line with no masking:

  C:\mchyzer\isc\dev\grouper\morphString>java -jar dist\morphString.jar dontMask
  Enter the location of morphString.properties: conf\morphString.properties
  Enter the string to encrypt: abc
  The encrypted string is: c5f1d2082f272eff77ee6e734f5aef7d

- run from command line with property file in classpath:

  C:\mchyzer\isc\dev\grouper\morphString>java  -cp conf;dist\morphString.jar edu.internet2.middleware.morphString.Encrypt
  Enter the string to encrypt:
  The encrypted string is: c5f1d2082f272eff77ee6e734f5aef7d
  
