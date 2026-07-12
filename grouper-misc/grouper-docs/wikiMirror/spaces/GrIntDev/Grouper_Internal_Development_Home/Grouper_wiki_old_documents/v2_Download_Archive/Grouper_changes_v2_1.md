---
title: "Grouper changes v2.1"
space: GrIntDev
pageId: 48793439
version: 25
lastUpdated: 2026-07-12T07:01:42.476Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793439/Grouper+changes+v2.1
---

> [http://www.youtube.com/watch?v=dE67V38Jes0](http://www.youtube.com/watch?v=dE67V38Jes0) This topic is discussed in the ["Grouper Minor Upgrade" training video](http://www.youtube.com/watch?v=dE67V38Jes0).

 This document lists instructions for people with existing groups installations on how to upgrade to newer versions of grouper (or grouper related products). If you notice something missing please let us know. The instructions are in descending order based on date/release. You will find instructions below for Grouper, Grouper-ws, Grouper-ui, etc. It is assumed if you are running grouper-ui that you will perform both the grouper upgrade notes, and the grouper-ui upgrade notes. It is understood that you will get the new source/javadoc/etc files, this document addresses configurations, jars, etc. Note that for major upgrades, you should [follow the upgrade steps](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793429/v2.1+Upgrade+Instructions+from+v1.6). For minor upgrades, that instructions should be sufficient.

 

#### Grouper

 

- v2.1.2: Update the grouperClient.jar
- v2.1.2: Merge grouper.properties with grouper.example.properties, add db.log.driver.mismatch
- v2.1.0: Merge sources.xml with sources.example.xml. Add the entity source, and if you havent added max page results and an inclause jdbc query, merge that part too.
- v2.1.0: Merge grouper.properties with grouper.example.properties
- v2.1.0: Upgrade the following jars: vt-ldap.jar, hibernate.jar, cglib.jar (REMOVED), commons-collections.jar, javassist.jar, slf4j-api.jar, asm.jar, asm-util.jar, asm-attrs.jar (REMOVED), hibernate-jpa-2.0-api.jar, mysql-connector-java-bin.jar (in jdbcSamples), slf4j-log4j12
- v2.1.0: Merge the grouper.hibernate.properties with the grouper.hibernate.example.properties, update the cache 
  ```
  
  FROM:
  hibernate.cache.provider_class  = org.hibernate.cache.EhCacheProvider
  
  
  
  TO:
  hibernate.cache.region.factory_class = net.sf.ehcache.hibernate.EhCacheRegionFactoryhibernate.cache.region.factory_class = net.sf.ehcache.hibernate.EhCacheRegionFactory
  hibernate.cache.region.factory_class = net.sf.ehcache.hibernate.EhCacheRegionFactory
  
  ```
- v2.1.0: Merge grouper-loader.properties with grouper-loader.example.properties. 
  
  - The change log no longer has flattened permissions. Remove the option changeLog.includeFlattenedPermissions and add changeLog.includeRolesWithPermissionChanges. See [GRP-611](https://bugs.internet2.edu/jira/browse/GRP-611). 
    ```
    
    # Should the change log include roles that have had permission changes?
    changeLog.includeRolesWithPermissionChanges = false
    
    ```
- v2.1.0: Merge ehcache.xml with ehcache.example.xml. Add the following cache setting for point in time audit. 
  ```
  
  <cache  name="edu.internet2.middleware.grouper.pit.PITField"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITFieldDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITAttributeAssign"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITAttributeAssignAction"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITAttributeAssignValue"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITAttributeAssignValueView"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITAttributeDef"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITAttributeDefName"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITGroup"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITGroupSet"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITMember"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITMembership"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITMembershipView"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITPermissionAllView"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITRoleSet"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.pit.PITStem"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeAssignActionDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeAssignActionSetDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeAssignDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeAssignValueDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeDefDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeDefNameDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeDefNameSetDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITGroupDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITGroupSetDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITMemberDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITMembershipDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITRoleSetDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITStemDAO.FindById"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeAssignActionDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeAssignActionSetDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeAssignDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeAssignValueDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeDefDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeDefNameDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITAttributeDefNameSetDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITGroupDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITGroupSetDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITMemberDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITMembershipDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITRoleSetDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITStemDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
    <cache  name="edu.internet2.middleware.grouper.internal.dao.hib3.Hib3PITFieldDAO.FindBySourceIdActive"
            maxElementsInMemory="1000"
            eternal="false"
            timeToIdleSeconds="30"
            timeToLiveSeconds="120"
            overflowToDisk="false"
    />
  
  ```

 

#### Grouper UI

 

- v2.1.2: Merge the nav.properties, add in the session timeout text, paging text
- v2.1.2: Merge the build.properties.template with build.properties, remove duplicate logout.link.show
- v2.1.0: Merge media.properties
- v2.1.0: Merge nav.properties 
  
  - There are many other updates in the nav.properties file. Mostly to define the text in all the new screens in the lite UI. Be sure to merge your copy.

 

#### Grouper WS

 

- v2.1.1: For grouper developers merge the grouper-ws.properties with grouper-ws.example.properties, including the default rest format of json
- v2.1.1 Merge the web.xml, this part

 
```

<filter>
    <!-- logging filter -->
    <filter-name>Grouper logging filter</filter-name>
    <filter-class>edu.internet2.middleware.grouper.ws.j2ee.ServletFilterLogger</filter-class>
  </filter>

  <!-- filter-mapping>
    <filter-name>Grouper logging filter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping -->

```

 

- If you are upgrading to 2.1.0 (it is fixed in 2.1.1+), and you use SOAP web services and the wsdl from server, then build with this [axis2.xml](http://www.internet2.edu/grouper/release/2.1.1/axis2.xml) instead of the one there in webapp/WEB-INF/conf.
- v2.1.0: Merge grouper-ws.properties with grouper-ws.example.properties 
  
  - Add decorator attributes:

 
```

# if there are attribute names that need to be sent to the SubjectDecorator
# for subsequent dynamic lookup (configured in SubjectFinder), comma separated
ws.subject.attributes.for.decorator =

```

 

- - Add a default diagnostics minutes since last success: 
    ```
    
    #this is 52 hours... 48 for 2 days, and 4 more for the job to run.  So if the warehouse is down for updates,
    #then the daily job will not give an error
    ws.diagnostic.defaultMinutesSinceLastSuccess = 3120
    
    #change log can only for 30 minutes of failing before diagnostics fails
    ws.diagnostic.defaultMinutesChangeLog = 30
    
    ```
  - Update the WS version for testing purposes 
    ```
    
    150c150
    < ws.testing.version=v2_1_000
    ---
    > ws.testing.version=v2_0_000
    
    ```

 

- - Update all the axis and rampart jars. Merge or replace the new axis2.xml config file
- (2.1.1+) Update the grouper-ws.properties (merge with example):

 
```

# cache the decision to allow a user to user web services, so it doesnt have to be calculated each time
# defaults to 5 minutes:
ws.client.user.group.cache.minutes = 5

```

 df

 

#### Subject API

 

- v2.1.1: Add this for ldap or jndi sources in the sources.xml  
   <init-param>  
   <param-name>SubjectID_formatToLowerCase</param-name>  
   <param-value>false</param-value>  
   </init-param>

 

#### Grouper Client

 

- v2.1.2: Merge grouper.client.properties with grouper.client.example.properties.
- v2.1.2: Merge grouper.client.usage.txt with grouper.client.usage.example.txt.
- v2.1.0: Merge grouper.client.properties with grouper.client.example.properties.
- v2.1.0: Merge grouper.client.usage.txt with grouper.client.usage.example.txt.
