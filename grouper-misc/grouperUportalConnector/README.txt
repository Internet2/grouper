====
    Copyright 2010 University of Chicago

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

Greetings:

This software requires the grouper web services client library https://spaces.internet2.edu/display/GrouperWG/Grouper+Client 
It must be downloaded and installed into a maven repository that can be seen by the builders of this project and the uPortal project.

1) To build this project from the source use maven.
	mvn clean install 

2) To use the binary file (grouperUportal-version.jar) in uPortal 3+ (may work with earlier versions)
modify 2 files and add 1 file to the uPortal source code.  

modify uportal-impl/src/main/resources/properties/groups/compositeGroupServices.xml

	<service>
		<name>grouper</name>
		<service_factory>org.jasig.portal.groups.ReferenceIndividualGroupServiceFactory</service_factory>
		<entity_store_factory>edu.interntet.middleware.grouper.uportal.GrouperEntityStoreFactory</entity_store_factory>
		<group_store_factory>edu.interntet.middleware.grouper.uportal.GrouperEntityGroupStoreFactory</group_store_factory>
		<entity_searcher_factory>edu.interntet.middleware.grouper.uportal.GrouperEntitySearcherFactory</entity_searcher_factory>
		<internally_managed>false</internally_managed>
		<caching_enabled>true</caching_enabled>
	</service>
	

modify uportal-impl/pom.xml 

	<dependency>
		<groupId>edu.internet2.middleware</groupId>
		<artifactId>grouperUportal</artifactId>
		<version>1.0-SNAPSHOT</version>
	</dependency>
	
	<dependency>
		<groupId>edu.internet2.middleware</groupId>
		<artifactId>grouperClient</artifactId>
		<version>1.5.0</version>
	</dependency>

add uportal-impl/src/main/resources/grouper.client.properties

	grouperClient.webService.url = https://yourHostname/web/servicesRest/
	grouperClient.webService.login = somebody
	grouperClient.webService.password = secret
	

Extra Configuration:

When this library is used in combination with other group services in the portal it may be desirable 
to filter calls to the grouper web service to speed up the login process. to do that, modify 1 file.

modify	src/main/resources/grouper.lib.properties