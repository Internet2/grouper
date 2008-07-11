The files in this directory consist of the jars and project object model (pom)
files needed to build and/or run ldappc, but which are not contained in the
Maven central repository.

They can be broadly divided into three groups:

1) Internet2 jars (grouper, ldappc, signet, subject)
	These jars could be deployed to the central repository, but there has
	been no policy decision made to pursue this with the Maven repository
	people.

2) Overlooked jars (invoker, jamon, oracle poms)
	These are jars which, although freely distributable, have not been
	deployed to the central repository by their owners.  In the case
	of the Oracle poms, the jars are not freely redistributable, but the
	poms are. The Oracle 11 poms have simply not yet been deployed to
	central. The Oracle jars are not included in this directory.

3) Sun jars (mail, smtp, jta)
	These jars are licensed by Sun under a restrictive license that prevents
	them from being included in the central repository.  These jars are
	legal to include in our software, but we had to agree to the Sun license
	to do it. If we moved to later versions of these jars, Sun has a more
	lenient license and its own repository that we could reference.

If you need to add a jar to this repository you must create a pom file for it.
Use the existing pom files as a template. The essential elements are the
groupId, the artifactId, and the version. These elements define the artifact
jar you are adding. Then run

	mvn deploy:deploy-file -Durl=file://i2mi-repository -Dfile=myjar.jar -DpomFile=mypom.pom

where myjar.jar is your jar and mypom.pom is your created pom file.  Once you
have done this, the jar will be available for reference in the pom.xml file
for this project and thus by the project itself.

If you have downloaded an Oracle jar and want to use it in ldappc and the pom
already exists in central or in this repository, you do not need to deploy the
jar to this repository; you just need to install it to your local repository.
Run

	mvn install:install-file -Dfile=myjar.jar -DpomFile=mypom.pom

where myjar.jar is the Oracle jar to be installed, and mypom.pom is the path
to the desired Oracle pom file.
