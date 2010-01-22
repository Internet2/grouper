The conf directory contains configuration files, both properties and XML
files, needed to run Ldappc.  These files need to be in the classpath in
order to run Ldappc.

The files consist of Grouper configuration files, Signet configuration files,
Subject API configuration files, and Ldappc configuration files.  The Subject
API is shared between Grouper and Signet.  Refer to the appropriate
documentation for configuration information.

Shared configuration:
	log4j.properties

Grouper configuration:
	ehcache.xml
	grouper.ehcache.xml
	grouper.hibernate.properties
	grouper.properties

Signet configuration:
	hibernate.cfg.xml
	signet.properties
	signetResApp.properties
	subjectSources.xml

Subject API configuration:
	sources.xml

Ldappc configuration:
	ldappc.properties
	ldappc.xml
