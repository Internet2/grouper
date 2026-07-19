---
title: "Grouper provisioning mock services"
space: GrIntDev
pageId: 48792562
version: 2
lastUpdated: 2026-07-12T06:45:28.541Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792562/Grouper+provisioning+mock+services
---

If there is a service that can be easily implemented (mocked) to test the provisioning DAO, implement in the Grouper Mock Services framework.

These services should be secure like the service being mocked is secure. This should require a secret in the config. Also, the services shouldn't do much anyways, and work on mock tables with test data and not enabled by default.

## Configure

grouper.hibernate.properties

```
grouper.is.mockServices = true
```

grouper.properties

```
############################################
## Mock services
############################################

# If requests and responses should be logged
# {valueType: "boolean", defaultValue: "false"}
grouper.mock.services.logRequestsResponses = true
```

log4j.properties

```
log4j.logger.edu.internet2.middleware.grouper.j2ee.MockServiceServlet = DEBUG
```

## Unit tests

Example is: GrouperAzureProvisionerTest.testGroupCreateThenDownloadUuid()

Configure a tomcat in the grouper.properties

Note: make sure grouper.hibernate.properties: grouper.is.daemon = false

```
# if grouper should run tests that require tomcat
# {valueType: "boolean", defaultValue: "false"}
junit.test.tomcat = true

# command to start tomcat for unit tests, 
# e.g. cmd /c C:/mchyzer/tomcatJunit/bin/startup.bat
# e.g. bash /some/where/tomcat/startup.sh
# set to "none" and you have 10 seconds to start it on prompt
# {valueType: "string"}
junit.test.tomcat.startCommand = 

# command to stop tomcat for unit tests, 
# e.g. cmd /c C:/mchyzer/tomcatJunit/bin/shutdown.bat
# e.g. bash /some/where/tomcat/shutdown.sh
# set to "none" and you have 10 seconds to stop it on prompt
# {valueType: "string"}
junit.test.tomcat.stopCommand = 

# http port to look for to see if tomcat has started, e.g. 8500
# {valueType: "integer", defaultValue: "8080"}
junit.test.tomcat.port = 8080

# ip address for tomcat
# {valueType: "string", defaultValue: "0.0.0.0"}
junit.test.tomcat.ipAddress = 0.0.0.0

# domain name for tomcat
# {valueType: "string", defaultValue: "localhost"}
junit.test.tomcat.domainName = localhost

# if should wait for tomcat start command to return (unix?) or spawn a thread (windows?)
# {valueType: "boolean"}
junit.test.tomcat.waitForProcessReturn = 

# if this is ssl
# {valueType: "boolean", defaultValue: "false"}
junit.test.tomcat.ssl = 

```

In unit test start with this:

```
    if (!tomcatRunTests()) {
      return;
    }
```

Configure anything needed in the database

```
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.clientId").value("myClient").store();
```

Get the endpoint from the config file

```
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.resourceEndpoint").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/azure/").store();

```

Download Tomcat 8.5 and unzip

(windows) Edit tomcat/bin/startup.bat and shutdown.bat

```
FROM
@echo off
rem Licensed to the Apache Software Foundation (ASF) under one or more

TO
@echo off
set "CATALINA_HOME=C:\mchyzer\tomcatJunit"
rem Licensed to the Apache Software Foundation (ASF) under one or more

```

(unix/mac) Edit tomcat/bin/startup.sh and shutdown.sh

```
FROM
#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one or more

TO
#!/bin/sh
CATALINA_HOME=/path/to/tomcat
# Licensed to the Apache Software Foundation (ASF) under one or more
```

Edit the server.xml to have the right ports and put the Context for grouper ui webapp dir there (make sure Eclipse or another process puts the libs, classfiles, and configs there, e.g.

```
<?xml version="1.0" encoding="UTF-8"?>
<Server port="8405" shutdown="SHUTDOWN">
  <Listener className="org.apache.catalina.startup.VersionLoggerListener" />

  <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />

  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />

  <GlobalNamingResources>
    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>

  <Service name="Catalina">

    <Connector port="8400" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />

    <Engine name="Catalina" defaultHost="localhost">

      <Realm className="org.apache.catalina.realm.LockOutRealm">

        <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
               resourceName="UserDatabase"/>
      </Realm>

      <Host name="localhost"  appBase="webapps"
            unpackWARs="true" autoDeploy="true">

        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="localhost_access_log" suffix=".txt"
               pattern="%h %l %u %t &quot;%r&quot; %s %b" />

        <Context docBase="C:\git\grouper_prod\grouper-ui\webapp" path="/grouper" reloadable="false"/>
      </Host>
    </Engine>
  </Service>
</Server>

```

## Handler

Implement the MockServicesHandler

```
public class AzureMockServiceHandler implements MockServiceHandler {

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!mockTablesThere) {
      ensureAzureMockTables();
    }
    mockTablesThere = true;
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }

    if (StringUtils.equals("GET", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("POST", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("auth".equals(mockServiceRequest.getPostMockNamePaths()[0])) {
        postAuth(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    
    throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
  }

```

## Register the handler

Ideally provisioners will be in the Grouper API and not introduce new libraries (or will reduce the number)

If not in Grouper API, put fully qualified class name string

```
public class MockServiceServlet extends HttpServlet {

  /**
   * add handlers here to handle requests
   */
  private static final Map<String, String> urlToHandler = GrouperUtil.toMap(
      "azure", AzureMockServiceHandler.class.getName());
  

```

## URL to use

The grouper url with a suffix

[http://localhost:8400/grouper/mockServices/azure/groups](http://localhost:8400/grouper/mockServices/azure/groups)

"azure" is the mock service, everything after that is like it is going to the real service

## Handler a request

```
  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);
    
    if (!StringUtils.equals("application/json", mockServiceRequest.getHttpServletRequest().getContentType())) {
      throw new RuntimeException("Content type must be application/json");
    }
    
    List<GrouperAzureGroup> grouperAzureGroups = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class);
    
    //  {
    //    "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#groups",
    //    "value": [
    //      {
    //        "id": "11111111-2222-3333-4444-555555555555",
    //        "mail": "group1@example.com",
    //        "mailEnabled": true,
    //        "mailNickname": "ContosoGroup1",
    //        "securityEnabled": true
    //      }
    //    ]
    //  }
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    resultNode.put("@odata.context", "https://graph.microsoft.com/v1.0/$metadata#groups");
    
    Set<String> fieldsToRetrieve = null;
    String fieldsToRetrieveString = mockServiceRequest.getHttpServletRequest().getParameter("$select");
    if (!StringUtils.isBlank(fieldsToRetrieveString)) {
      fieldsToRetrieve = GrouperUtil.toSet(GrouperUtil.split(fieldsToRetrieveString, ","));
    }
    
    for (GrouperAzureGroup grouperAzureGroup : grouperAzureGroups) {
      valueNode.add(grouperAzureGroup.toJson(fieldsToRetrieve));
    }
    
    resultNode.set("value", valueNode);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

```

## DDL

Mock services should create their own tables on demand. See Azure for an example:

```
  private static boolean mockTablesThere = false;
  
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!mockTablesThere) {
      ensureAzureMockTables();
    }
    mockTablesThere = true;
  ...
  }
    
  public static void ensureAzureMockTables() {
    GrouperAzureGroup.createTableAzureGroup();
    GrouperAzureAuth.createTableAzureAuth();
  }

  public static void createTableAzureGroup() {

    final String tableName = "mock_azure_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "256", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type_mail_enabled", Types.VARCHAR, "1", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type_mail_enabled_sec", Types.VARCHAR, "1", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type_security", Types.VARCHAR, "1", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type_unified", Types.VARCHAR, "1", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "mail_enabled", Types.VARCHAR, "1", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "mail_nickname", Types.VARCHAR, "64", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "security_enabled", Types.VARCHAR, "1", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "visibility", Types.VARCHAR, "32", false, true);
          
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_azure_group_disp_idx", false, "display_name");
        }
        
      });
    }
  }
```

## Hibernate

Map a class to a table that starts with "mock_" and the services to mock, e.g. "mock_azure_group"

```
<hibernate-mapping package="edu.internet2.middleware.grouper.app.azure" default-lazy="false">
  <class name="GrouperAzureGroup" table="mock_azure_group">

    <meta attribute="implement-equals">true</meta>
    <meta attribute="session-method">Hib3DAO.getSession();</meta>

    <id name="id" column="id">
      <generator class="assigned" />
    </id>

    <property name="description" column="description" />
    <property name="displayName" column="display_name" />
    <property name="groupTypeMailEnabledDb" column="group_type_mail_enabled" />
    <property name="groupTypeMailEnabledSecurityDb" column="group_type_mail_enabled_sec" />
    <property name="groupTypeSecurityDb" column="group_type_security" />
    <property name="groupTypeUnifiedDb" column="group_type_unified" />
    <property name="mailEnabledDb" column="mail_enabled" />
    <property name="mailNickname" column="mail_nickname" />
    <property name="securityEnabledDb" column="security_enabled" />
    <property name="visibilityDb" column="visibility" />

  </class>
</hibernate-mapping>

```

Register in Hib3DAO.java (conditionally if running the mock services)

```
      if (GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.mockServices", false)) {
        addClass(configuration, GrouperAzureGroup.class);
        addClass(configuration, GrouperAzureAuth.class);
      }

```
