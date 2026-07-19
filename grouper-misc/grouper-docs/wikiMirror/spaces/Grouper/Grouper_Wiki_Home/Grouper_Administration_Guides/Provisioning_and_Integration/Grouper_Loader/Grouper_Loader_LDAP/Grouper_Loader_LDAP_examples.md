---
title: "Grouper Loader LDAP examples"
space: Grouper
pageId: 28554595
version: 17
lastUpdated: 2026-07-01T05:40:05.918Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554595/Grouper+Loader+LDAP+examples
---

[Main Grouper Loader page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545200/Grouper+Loader)

### Common setup

- Setup an LDAP source, here is one I googled from the Internet (public unauthenticated). Put this in the sources.xml in all groupers (WS, UI, loader, whatever)
- subject.properties config
  
  
  ```
  #########################################
  ## Configuration for source id: cmuDirectory
  ## Source configName: cmuDirectory
  #########################################
  subjectApi.source.cmuDirectory.id = cmuDirectory
  
  # this is a friendly name for the source
  subjectApi.source.cmuDirectory.name = CMU Directory
  
  # type is not used all that much.  Can have multiple types, comma separate.  Can be person, group, application
  subjectApi.source.cmuDirectory.types = person
  
  # the adapter class implements the interface: edu.internet2.middleware.subject.Source
  # adapter class must extend: edu.internet2.middleware.subject.provider.BaseSourceAdapter
  # edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter2  :  if doing JDBC this should be used if possible.  All subject data in one table/view.
  # edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter   :  oldest JDBC source.  Put freeform queries in here
  # edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter   :  used for LDAP
  subjectApi.source.cmuDirectory.adapterClass = edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter
  
  # e.g. com.sun.jndi.ldap.LdapCtxFactory
  subjectApi.source.cmuDirectory.param.INITIAL_CONTEXT_FACTORY.value = com.sun.jndi.ldap.LdapCtxFactory
  
  # e.g. ldap://localhost:389
  subjectApi.source.cmuDirectory.param.PROVIDER_URL.value = ldap://ldap.andrew.cmu.edu:389
  
  # e.g. simple, none, sasl_mech
  subjectApi.source.cmuDirectory.param.SECURITY_AUTHENTICATION.value = none
  
  # ldap attribute which is the subject id.  e.g. exampleEduRegID   Each subject has one and only one subject id.  Generally it is opaque and permanent.
  subjectApi.source.cmuDirectory.param.SubjectID_AttributeType.value = guid
  
  # if the subject id should be changed to lower case after reading from datastore.  true or false
  subjectApi.source.cmuDirectory.param.SubjectID_formatToLowerCase.value = false
  
  # attribute which is the subject name
  subjectApi.source.cmuDirectory.param.Name_AttributeType.value = cn
  
  # attribute which is the subject description
  subjectApi.source.cmuDirectory.param.Description_AttributeType.value = cn
  
  # the 1st sort attribute for lists on screen that are derived from member table (e.g. search for member in group)
  # you can have up to 5 sort attributes 
  subjectApi.source.cmuDirectory.param.sortAttribute0.value = cn
  
  # the 1st search attribute for lists on screen that are derived from member table (e.g. search for member in group)
  # you can have up to 5 search attributes 
  subjectApi.source.cmuDirectory.param.searchAttribute0.value = searchAttribute0
  
  # attribute name of the email attribute
  subjectApi.source.cmuDirectory.param.emailAttributeName.value = mail
  
  #searchSubject: find a subject by ID.  ID is generally an opaque and permanent identifier, e.g. 12345678.
  #  Each subject has one and only on ID.  Returns one result when searching for one ID.
  
  # sql is the sql to search for the subject by id.  %TERM% will be subsituted by the id searched for
  subjectApi.source.cmuDirectory.search.searchSubject.param.filter.value = (& (guid=%TERM%) (objectclass=cmuPerson))
  
  # Scope Values can be: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
  subjectApi.source.cmuDirectory.search.searchSubject.param.scope.value = SUBTREE_SCOPE
  
  # base dn to search in
  subjectApi.source.cmuDirectory.search.searchSubject.param.base.value = ou=person,dc=cmu,dc=edu
  
  #searchSubjectByIdentifier: find a subject by identifier.  Identifier is anything that uniquely
  #  identifies the user, e.g. jsmith or jsmith@example.com.
  #  Subjects can have multiple identifiers.  Note: it is nice to have if identifiers are unique
  #  even across sources.  Returns one result when searching for one identifier.
  
  # sql is the sql to search for the subject by identifier.  %TERM% will be subsituted by the identifier searched for
  subjectApi.source.cmuDirectory.search.searchSubjectByIdentifier.param.filter.value = (& (cmuAndrewCommonNamespaceId=%TERM%) (objectclass=cmuPerson))
  
  # Scope Values can be: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
  subjectApi.source.cmuDirectory.search.searchSubjectByIdentifier.param.scope.value = SUBTREE_SCOPE
  
  # base dn to search in
  subjectApi.source.cmuDirectory.search.searchSubjectByIdentifier.param.base.value = ou=person,dc=cmu,dc=edu
  
  #   search: find subjects by free form search.  Returns multiple results.
  
  # sql is the sql to search for the subject by free form search.  %TERM% will be subsituted by the text searched for
  subjectApi.source.cmuDirectory.search.search.param.filter.value = (& (|(guid=%TERM%)(|(cn=*%TERM%*)(cmuAndrewCommonNamespaceId=*%TERM%*)))(objectclass=cmuPerson))
  
  # Scope Values can be: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
  subjectApi.source.cmuDirectory.search.search.param.scope.value = SUBTREE_SCOPE
  
  # base dn to search in
  subjectApi.source.cmuDirectory.search.search.param.base.value = ou=person,dc=cmu,dc=edu
  
  # attributes from ldap object to become subject attributes.  comma separated
  subjectApi.source.cmuDirectory.attributes = cn, guid, cmuAndrewCommonNamespaceId, mail
  
  # internal attributes are used by grouper only not exposed to code that uses subjects.  comma separated
  subjectApi.source.cmuDirectory.internalAttributes = searchAttribute0
  
  
  
  ```
- sources.xml config

```
  <source adapterClass="edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter">
    <id>cmuDirectory</id>
    <name>CMU Directory</name>
    <type>person</type>
    <init-param>
      <param-name>INITIAL_CONTEXT_FACTORY</param-name>
      <param-value>com.sun.jndi.ldap.LdapCtxFactory</param-value>
    </init-param>
    <init-param>
      <param-name>PROVIDER_URL</param-name>
      <param-value>ldap://ldap.andrew.cmu.edu:389</param-value>
    </init-param>
    <init-param>
      <param-name>SECURITY_AUTHENTICATION</param-name>
      <param-value>none</param-value>
    </init-param>
     <init-param>
      <param-name>SubjectID_AttributeType</param-name>
      <param-value>guid</param-value>
    </init-param>
     <init-param>
      <param-name>SubjectID_formatToLowerCase</param-name>
      <param-value>false</param-value>
    </init-param>
    <init-param>
      <param-name>Name_AttributeType</param-name>
      <param-value>cn</param-value>
    </init-param>
    <init-param>
      <param-name>Description_AttributeType</param-name>
      <param-value>cn</param-value>
    </init-param>
    <!--     
    /// Scope Values can be: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE 
    /// For filter use 
     -->
    <search>
        <searchType>searchSubject</searchType>
        <param>
            <param-name>filter</param-name>
            <param-value>
                (&amp; (guid=%TERM%) (objectclass=cmuPerson))
            </param-value>
        </param>
        <param>
            <param-name>scope</param-name>
            <param-value>
                SUBTREE_SCOPE            
            </param-value>
        </param>
        <param>
            <param-name>base</param-name>
            <param-value>
                ou=person,dc=cmu,dc=edu
            </param-value>
        </param>
         
    </search>
    <search>
        <searchType>searchSubjectByIdentifier</searchType>
        <param>
            <param-name>filter</param-name>
            <param-value>
                (&amp; (cmuAndrewCommonNamespaceId=%TERM%) (objectclass=cmuPerson))
            </param-value>
        </param>
        <param>
            <param-name>scope</param-name>
            <param-value>
                SUBTREE_SCOPE            
            </param-value>
        </param>
        <param>
            <param-name>base</param-name>
            <param-value>
                ou=person,dc=cmu,dc=edu
            </param-value>
        </param>
    </search>
    
    <search>
       <searchType>search</searchType>
         <param>
            <param-name>filter</param-name>
            <param-value>
                (&amp; (|(guid=%TERM%)(|(cn=*%TERM%*)(cmuAndrewCommonNamespaceId=*%TERM%*)))(objectclass=cmuPerson))
            </param-value>
        </param>
        <param>
            <param-name>scope</param-name>
            <param-value>
                SUBTREE_SCOPE            
            </param-value>
        </param>
         <param>
            <param-name>base</param-name>
            <param-value>
                ou=person,dc=cmu,dc=edu
            </param-value>
        </param>
    </search>
    <init-param>
      <param-name>sortAttribute0</param-name>
      <param-value>cn</param-value>
    </init-param>
    <init-param>
      <param-name>searchAttribute0</param-name>
      <param-value>searchAttribute0</param-value>
    </init-param>

    <internal-attribute>searchAttribute0</internal-attribute>

    <!-- ///Attributes you would like to display when doing a search  -->
    <attribute>cn</attribute>
    <attribute>guid</attribute>
    <attribute>cmuAndrewCommonNamespaceId</attribute>
   
  </source>

```

- Set this in the grouper-loader.properties

```
#################################
## LDAP connections
#################################
# specify the ldap connection with user, pass, url
# the string after "ldap." is the ID of the connection, and it should not have
# spaces or other special chars in it.  In this case is it "personLdap"

ldap.personLdap.url = ldap://ldap.andrew.cmu.edu/dc=cmu,dc=edu
ldap.personLdap.user = 
ldap.personLdap.pass = 

```

### LDAP Simple group

- Create folder/group test:testGroup
- Use new attribute framework to assign ldap loader

- Text version:

```
test:testGroup:         grouperLoaderLdapDef
subject attribute name: guid
search base DN:         ou=person
quartz cron:            0 0 8 * * ?
filter:                 (& (cmuAndrewCommonNamespaceId=*dest*) (objectClass=cmuPerson))
server ID:              personLdap
type:                   LDAP_SIMPLE

```

- If you want you can turn debug on in the log4j.properties

```
log4j.logger.edu.internet2.middleware.grouper.app.loader = DEBUG

```

##### Run the loader job from GSH

```
gsh 0% grouperSession = GrouperSession.startRootSession(); 
gsh 1% loaderGroup = GroupFinder.findByName(grouperSession, "test:testGroup");
gsh 2% loaderRunOneJob(loaderGroup);
2013-06-02 16:47:28,725: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(1926) -  - test:testGroup start syncing membership
2013-06-02 16:47:28,726: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(1942) -  - test:testGroup syncing 13 rows
2013-06-02 16:47:28,733: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2055) -  - Done assigning privilege to related groups: test:testGroup
2013-06-02 16:47:28,881: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/4A10366C-D7F4-11D5-8000-080020CC75D3, 1 of 13 subjects
2013-06-02 16:47:29,175: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/00000000-0000-1000-3F70-0800207F02E6, 2 of 13 subjects
2013-06-02 16:47:29,301: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/00000000-0000-1000-79FA-0800207F02E6, 3 of 13 subjects
2013-06-02 16:47:29,405: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/2BF70E82-BD36-11D9-8000-0003BA2FA263, 4 of 13 subjects
2013-06-02 16:47:29,717: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/07696C48-CBA2-11D9-8001-0003BA2FA263, 5 of 13 subjects
2013-06-02 16:47:29,854: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/AE334D56-7E40-11DD-8001-0003BA2FA263, 6 of 13 subjects
2013-06-02 16:47:29,970: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/1A97ED54-3C6D-11DE-8001-0003BA2FA263, 7 of 13 subjects
2013-06-02 16:47:30,267: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/7404BCDC-9794-11DE-8001-0003BA2FA263, 8 of 13 subjects
2013-06-02 16:47:30,426: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/293ABE32-109C-11DF-8000-0003BA2FA263, 9 of 13 subjects
2013-06-02 16:47:30,554: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/8F2D682C-6261-11DF-8000-0003BA2FA263, 10 of 13 subjects
2013-06-02 16:47:30,841: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/D6914E6E-E0D7-11DF-8001-00144F799A7A, 11 of 13 subjects
2013-06-02 16:47:30,991: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/55F202B2-72F9-11E0-8001-00144F799A7A, 12 of 13 subjects
2013-06-02 16:47:31,099: [main] DEBUG GrouperLoaderType.syncOneGroupMembership(2149) -  - test:testGroup will add subject to group: CMU Directory/8B075248-925C-11E2-8000-00144F799A7A, 13 of 13 subjects
2013-06-02 16:47:32,211: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: 55F202B2-72F9-11E0-8001-00144F799A7A, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,254: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: 00000000-0000-1000-3F70-0800207F02E6, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,302: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: AE334D56-7E40-11DD-8001-0003BA2FA263, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,346: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: 07696C48-CBA2-11D9-8001-0003BA2FA263, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,402: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: 1A97ED54-3C6D-11DE-8001-0003BA2FA263, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,447: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: 4A10366C-D7F4-11D5-8000-080020CC75D3, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,486: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: 2BF70E82-BD36-11D9-8000-0003BA2FA263, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,529: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: 8F2D682C-6261-11DF-8000-0003BA2FA263, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,577: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: 00000000-0000-1000-79FA-0800207F02E6, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,621: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: D6914E6E-E0D7-11DF-8001-00144F799A7A, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,678: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: 293ABE32-109C-11DF-8000-0003BA2FA263, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,721: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: 7404BCDC-9794-11DE-8001-0003BA2FA263, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,769: [main] DEBUG GrouperLoaderType$10.callback(2256) -  - Group: test:testGroup add Subject id: 8B075248-925C-11E2-8000-00144F799A7A, sourceId: pennDirectory, alreadyAdded: false
2013-06-02 16:47:32,769: [main] INFO  GrouperLoaderType.syncOneGroupMembership(2301) -  - test:testGroup done syncing membership, processed 13 records.  Total members: 13, inserts: 13, deletes: 0
loader ran successfully, inserted 13 memberships, deleted 0 memberships, total membership count: 13

```

### LDAP groups from attributes

- Create folders / groups: test:loader:testLdapSimple

- text config

| Attribute name | Value |
| --- | --- |
| Grouper loader LDAP group name expression | groupsFromAttributes:${groupAttribute} |
| Grouper loader LDAP subject ID type | subjectIdentifier |
| Grouper loader LDAP subject attribute name | cmuAndrewCommonNamespaceId |
| Grouper loader LDAP search base DN | ou=person |
| Grouper loader LDAP type | LDAP_GROUPS_FROM_ATTRIBUTES |
| Grouper loader LDAP group attribute name | cmuDepartment |
| Grouper loader LDAP source ID | cmuDirectory |
| Grouper loader LDAP quartz cron | 0 0 8 * * ? |
| Grouper loader LDAP server ID | personLdap |
| Grouper loader LDAP filter | (&(objectClass=cmuPerson)(cmuAndrewId=al*)(\|(cmuDepartment=Mechanical Engineering)(cmuDepartment=Biological Sciences)(cmuDepartment=English))) |

##### Run the job

##### These groups / memberships were created

### LDAP Group List

- Setup group / folders: test:loader:groupList
- Setup attributes:

Text values:

| Attribute name | Value |
| --- | --- |
| Grouper loader LDAP search base DN | ou=group |
| Grouper loader LDAP group name expression | groupList:${loaderLdapElUtils.convertDnToSpecificValue(groupAttributes['dn'])} |
| Grouper loader LDAP source ID | cmuDirectory |
| Grouper loader LDAP subject expression | ${loaderLdapElUtils.convertDnToSpecificValue(subjectId)} |
| Grouper loader LDAP quartz cron | 0 0 8 * * ? |
| Grouper loader LDAP type | LDAP_GROUP_LIST |
| Grouper loader LDAP subject ID type | subjectId |
| Grouper loader LDAP server ID | personLdap |
| Grouper loader LDAP subject attribute name | member |
| Grouper loader LDAP filter | (&(objectClass=cmuGroup)(cn=softdist2:system*)) |

Run the job

See the results
