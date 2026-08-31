---
title: "Grouper configuration in the database migrate to on demo server"
space: Grouper
pageId: 28560251
version: 10
lastUpdated: 2026-07-01T05:35:58.267Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560251/Grouper+configuration+in+the+database+migrate+to+on+demo+server
---

This wiki documents the steps taken for the Grouper demo server to use database configuration.

Note, if you have more than one configuration file in your hierarchy, you need to adjust this procedure.

## Patch

Bring the various components (UI/WS/daemon), up to grouper_v2_4_0_ui_patch_49. Or get the container that has that patch.

## Convert the easy config files

Get the grouper-ui.properties from the UI WEB-INF/classes

Get the grouper-ws.properties from the WS WEB-INF/classes

Get the grouper-loader.properties from the daemon /classes

Get the grouper.cache.properties (generally this is empty)

Note: after importing a config file, look in the bottom section "Remaining config", maybe remove those if you discover they arent used.

Note, if you want to, you can diff each config in various envs to make sure everything is there... e.g.

```
[appadmin@i2midev6 classes]$ diff /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-loader.properties /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-loader.properties
26c26
< loader.retain.db.audit_entry_no_logged_in_user.days=31
---
> loader.retain.db.audit_entry_no_logged_in_user.days=1825
35c35
< loader.retain.db.audit_entry.days=365
---
> loader.retain.db.audit_entry.days=3650
45c45
< loader.retain.db.point_in_time_deleted_objects.days=31
---
> loader.retain.db.point_in_time_deleted_objects.days=365
59c59
< loader.retain.db.folder.courses.parentFolderName=users:penn
---
> loader.retain.db.folder.courses.parentFolderName=my:folder:for:courses
61,70d60
< 
< ## TIER Instrumentation daemon - send stats to TIER.
< # otherJob.tierInstrumentationDaemon.class = edu.internet2.middleware.grouper.instrumentation.TierInstrumentationDaemon
< # otherJob.tierInstrumentationDaemon.quartzCron = 0 0 2 * * ?
< otherJob.tierInstrumentationDaemon.discoveryUrl = https://id.internet2.edu/ti/jrd/collector
< otherJob.tierInstrumentationDaemon.exclude.transactionCounts = false
< otherJob.tierInstrumentationDaemon.exclude.registryCounts = false
< otherJob.tierInstrumentationDaemon.exclude.patchesInstalled = false
< otherJob.tierInstrumentationDaemon.exclude.version = false
< otherJob.tierInstrumentationDaemon.exclude.instanceData = false

[appadmin@i2midev6 classes]$ diff /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-loader.properties /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper-loader.properties
1,5c1,2
< # Grouper loader uses Grouper Configuration Overlays (documented on wiki)
< # By default the configuration is read from grouper-loader.base.properties
< # (which should not be edited), and the grouper-loader.properties overlays
< # the base settings.  See the grouper-loader.base.properties for the possible
< # settings that can be applied to the grouper.properties
---
> # auto-add grouper loader types and attributes when grouper starts up if they are not there
> loader.autoadd.typesAttributes = false
6a4,10
> ##################################
> ## Daily report
> ##################################
> 
> #days on which to sync flat tables with daily report (comma separated)
> #blank means run never.   e.g. to run on all days: monday, tuesday, wednesday, thursday, friday, saturday, sunday
> daily.report.syncFlatTables.daysToRun = monday, tuesday, wednesday, thursday, friday, saturday, sunday
7a12,16
> #Whether or not notifications should be sent out for changes made to the flat tables.
> daily.report.syncFlatTables.sendNotifications = true
> 
> #Whether issues in the flat tables should be fixed or just reported.
> daily.report.syncFlatTables.saveUpdates = true
16c25,29
< ldap.personLdap.url = ldap://ldap.andrew.cmu.edu/dc=cmu,dc=edu
---
> #note the URL should start with ldap: or ldaps: if it is SSL.  
> #It should contain the server and port (optional if not default), and baseDn, 
> #e.g. ldaps://ldapserver.school.edu:636/dc=school,dc=edu
> ldap.cmuLdap.url = ldap://ldap.andrew.cmu.edu:389
> 
18,70d30
< ############################################
< ## audit entries with no logged in user aren't really all that useful.  There is point in time data still.
< ## So removing these shouldn't be a big deal
< ## default is remove these that are 5 years old.
< ############################################
< 
< # number of days to retain db rows in grouper_audit_entry with no logged in user (loader, gsh, etc).  -1 is forever.
< # suggested is 365 or five years: 1825.  Default is -1
< loader.retain.db.audit_entry_no_logged_in_user.days=31
< 
< ############################################
< ## Some think its ok to remove all audit entries over 10 (or X) years, but will default this
< ## to never since even at large institutions there aren't that many records.
< ## These are audits for things people do on the UI or WS generally (as a different to records with no logged in user)
< ############################################
< 
< # number of days to retain db rows in grouper_audit_entry.  -1 is forever.  suggested is -1 or ten years: 3650
< loader.retain.db.audit_entry.days=365
< 
< ############################################
< ## After you delete an object in grouper, it is still in point in time.  So if you want to know who
< ## was in a group a year ago, you need this info
< ## However, after some time it might be ok to let it go.  So the default is 5 years
< ############################################
< 
< # number of days to retain db rows for point in time deleted objects.  -1 is forever.
< # suggested is 365 or five years: 1825.  Default is -1
< loader.retain.db.point_in_time_deleted_objects.days=31
< 
< ############################################
< ## This is optional.  You can automatically obliterate folders *directly in a parent folder* that are a
< ## certain age old  e.g. courses.
< ## so you could delete a term of courses 4 years old if you like.  Note, make sure the loader isn't
< ## going to recreate or you will get churn
< ## Note this can also delete the point in time data as well.
< ############################################
< 
< # number of days after a subfolder (directly in a parent folder) is created that it will be obliterated (deleted)
< # and point in time will be deleted too.
< # "courses" or "anotherLabel" are variables you make up in these examples
< loader.retain.db.folder.courses.days=1825
< loader.retain.db.folder.courses.parentFolderName=users:penn
< loader.retain.db.folder.courses.deletePointInTime=true
< 
< ## TIER Instrumentation daemon - send stats to TIER.
< # otherJob.tierInstrumentationDaemon.class = edu.internet2.middleware.grouper.instrumentation.TierInstrumentationDaemon
< # otherJob.tierInstrumentationDaemon.quartzCron = 0 0 2 * * ?
< otherJob.tierInstrumentationDaemon.discoveryUrl = https://id.internet2.edu/ti/jrd/collector
< otherJob.tierInstrumentationDaemon.exclude.transactionCounts = false
< otherJob.tierInstrumentationDaemon.exclude.registryCounts = false
< otherJob.tierInstrumentationDaemon.exclude.patchesInstalled = false
< otherJob.tierInstrumentationDaemon.exclude.version = false
< otherJob.tierInstrumentationDaemon.exclude.instanceData = false
[appadmin@i2midev6 classes]$ 

[appadmin@i2midev6 classes]$ diff /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ui.properties /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-ui.properties

[appadmin@i2midev6 classes]$ diff /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ui.properties /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper-ui.properties
diff: /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper-ui.properties: No such file or directory

[appadmin@i2midev6 classes]$ diff /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.properties /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.properties
diff: /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.properties: No such file or directory

[appadmin@i2midev6 classes]$ diff /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.properties /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper-ws.properties
diff: /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.properties: No such file or directory

[appadmin@i2midev6 patchesAutoLoader]$ diff /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.cache.properties /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.cache.properties
[appadmin@i2midev6 patchesAutoLoader]$ diff /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.cache.properties /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper.cache.properties
[appadmin@i2midev6 patchesAutoLoader]$ 

```

That looks ok, SyncFlatTables isnt a thing anymore I think.

Take each config file and import into the UI

Miscellaneous → Configuration → Configuration files → More actions → Import config file

Note, notice grouper-ws.properties had diagnostics configs, and those were migrated to grouper.properties, so removed those from grouper-ws.properties before importing

## Convert the more difficult config files (since might be different in each JVM)

grouper.properties, grouper.client.properties, subject.propertie

```
[appadmin@i2midev6 patchesAutoLoader]$ diff /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.properties /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.properties
42d41
< 
44d42
< 
46d43
< 
48d44
< 
50d45
< 
52d46
< 
54a49,59
> 
> #mail.smtp.server = smtp.sparkpostmail.com
> #mail.smtp.port = 587
> #mail.smtp.starttls.enable = true
> #mail.smtp.user = SMTP_Injection
> #mail.smtp.pass = 7ef6a8eeb15b93ea9ba8de07a22a1668f284c801
> #mail.smtp.ssl.protocols = TLSv1.2
> #mail.from.address = mchyzer@mchyzer.co
> #mail.debug = true
> 
> 
112a118,141
> 
> 
> ##################################
> ## Lockout groups.  Could be used for other things, but used for policy group templates at least
> ## if there is no allowed group, then anyone could use it
> ##################################
> 
> # group name of a lockout group
> grouper.lockoutGroup.name.0 = ref:lockout
> 
> ##################################
> ## Require groups.  Could be used for other things, but used for policy group templates at least
> ## if there is no allowed group, then anyone could use it
> ##################################
> 
> # group name of a require group
> grouper.requireGroup.name.0 = ref:active
> 
> # group name of a require group
> grouper.requireGroup.name.1 = ref:employee
> 
> 
> 
> 
[appadmin@i2midev6 patchesAutoLoader]$ 

NOTE: use the UI grouper.properties, compare with WS grouper.properties

[appadmin@i2midev6 patchesAutoLoader]$ diff /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.properties /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper.properties
1c1,4
< grouper.ui.url = https://grouperdemo.internet2.edu/grouper_v2_4/
---
> #
> # Grouper Configuration
> # $Id: grouper.example.properties,v 1.48 2009-12-16 06:02:30 mchyzer Exp $
> #
3a7,26
> 
> ########################################
> ## General settings
> ########################################
> 
> # in cases where grouper is logging or emailing, it will use this to differentiate test vs dev vs prod
> # grouper.env.name = GROUPERDEMO_2_2_2
> grouper.env.name.elConfig = ${java.lang.System.getenv().get('GROUPER_ENV')}
> 
> #######################################
> ## inititalization and configuration settings
> #######################################
> 
> #auto-create groups (increment the integer index), and auto-populate with users
> #(comma separated subject ids) to bootstrap the registry on startup
> #(note: check config needs to be on)
> #configuration.autocreate.group.name.0 = etc:uiUsers
> #configuration.autocreate.group.description.0 = users allowed to log in to the UI
> #configuration.autocreate.group.subjects.0 = johnsmith
> 
10c33
< configuration.autocreate.group.subjects.1 = mchyzer,mchyzer@upenn.edu
---
> configuration.autocreate.group.subjects.1 = mchyzer
14,88c37
< configuration.autocreate.group.subjects.2 = mchyzer,mchyzer@upenn.edu
< 
< configuration.autocreate.group.name.3 = aStem:library
< configuration.autocreate.group.description.3 = access to the library application
< 
< configuration.autocreate.group.name.4 = etc:externalSubjectInviters
< configuration.autocreate.group.description.4 = allowed to invite people to this application
< 
< 
< groups.wheel.use                      = true
< 
< # A viewonly wheel group allows you to enable non-GrouperSystem subjects to act
< # like a root user when viewing the registry.
< groups.wheel.viewonly.use                      = true
< 
< # A readonly wheel group allows you to enable non-GrouperSystem subjects to act
< # like a root user when reading the registry.
< groups.wheel.readonly.use                      = true
< 
< 
< 
< grouperIncludeExclude.use = true
< grouperIncludeExclude.requireGroups.use = true
< 
< rules.act.as.group = etc:rulesActAsGroup
< 
< 
< mail.smtp.server = smtp.gmail.com
< mail.smtp.user = groupersystem@gmail.com
< mail.smtp.pass = /opt/grouper/2.3/pass/smtp_2.3.pass
< mail.smtp.ssl = true
< mail.from.address = groupersystem@gmail.com
< mail.subject.prefix = GROUPERDEMO_2_4:
< mail.test.address = mchyzer@yahoo.com
< 
< 
< #mail.smtp.server = smtp.sparkpostmail.com
< #mail.smtp.port = 587
< #mail.smtp.starttls.enable = true
< #mail.smtp.user = SMTP_Injection
< #mail.smtp.pass = 7ef6a8eeb15b93ea9ba8de07a22a1668f284c801
< #mail.smtp.ssl.protocols = TLSv1.2
< #mail.from.address = mchyzer@mchyzer.co
< #mail.debug = true
< 
< 
< externalSubjects.desc.el = [unverifiedInfo] ${grouperUtil.appendIfNotBlankString(externalSubject.name, ' - ', externalSubject.institution)} [externalUserID] ${externalSubject.identifier}
< 
< externalSubjects.institution.required = true
< 
< externalSubjects.attributes.jabber.systemName = jabber
< externalSubjects.attributes.jabber.required = false
< 
< externalSubjects.attributes.jabber.comment = The jabber ID of the user
< 
< externalSubjects.autoaddGroups=etc:uiGroup,etc:externalSubjectInviters
< 
< externalSubjects.autoadd.testingLibrary.externalSubjectInviteName=library
< 
< externalSubjects.autoadd.testingLibrary.groups=aStem:library
< 
< externalSubjects.autoadd.testingLibrary.actions=insert,update
< 
< externalSubjects.registerRequiresInvite=false
< 
< 
< 
< 
< hooks.group.class = edu.internet2.middleware.grouper.hooks.examples.UniqueObjectGroupHook
< hooks.stem.class = edu.internet2.middleware.grouper.hooks.examples.UniqueObjectStemHook
< hooks.attributeDef.class = edu.internet2.middleware.grouper.hooks.examples.UniqueObjectAttributeDefHook
< hooks.attributeDefName.class = edu.internet2.middleware.grouper.hooks.examples.UniqueObjectAttributeDefNameHook
< hooks.membership.class = edu.internet2.middleware.grouper.hooks.examples.MembershipOneInFolderMaxHook
< # put in a group name to exclude non admins who have a lot of privileges who have bad performance
< security.show.all.folders.if.in.group =test:testGroup
---
> configuration.autocreate.group.subjects.2 = mchyzer
91c40
< ## Deprovisioning
---
> ## security settings
94,131c43,49
< # comma separated affiliations for deprovisioning e.g. employee, student, etc
< # these need to be alphanumeric suitable for properties keys for further config or for group extensions
< deprovisioning.affiliations = employee, student, alumni
< 
< #########################################
< ## GSH
< #########################################
< gsh.useLegacy = true
< 
< #########################################
< ## Provisioning in UI
< #########################################
< 
< # if provisioning in ui should be enabled
< # {valueType: "boolean", required: true}
< provisioningInUi.enable = true
< 
< ######################################
< ## Grouper Reporting
< ######################################
<  
< # grouper reporting file system path where reports will be stored, e.g. /opt/grouper/reports
< # {valueType: "string", required: false}
< reporting.file.system.path = /opt/tomcats/tomcat_b/grouperReports
< 
< 
< ##################################
< ## Lockout groups.  Could be used for other things, but used for policy group templates at least
< ## if there is no allowed group, then anyone could use it
< ##################################
< 
< # group name of a lockout group
< grouper.lockoutGroup.name.0 = ref:lockout
< 
< ##################################
< ## Require groups.  Could be used for other things, but used for policy group templates at least
< ## if there is no allowed group, then anyone could use it
< ##################################
---
> # If set to _true_, the ALL subject will be granted that privilege on
> # each new group that is created.  Note, you can override the default
> # checkboxes on screen of UI in media.properties.
> groups.create.grant.all.admin         = false
> groups.create.grant.all.read          = true
> groups.create.grant.all.update        = false
> groups.create.grant.all.view          = true
133,134d50
< # group name of a require group
< grouper.requireGroup.name.0 = ref:active
136,137d51
< # group name of a require group
< grouper.requireGroup.name.1 = ref:employee
138a53,55
> # A wheel group allows you to enable non-GrouperSystem subjects to act
> # like a root user when interacting with the registry.
> groups.wheel.use                      = true
139a57,63
> ###################################
> ## allow and deny for db/ldap data or object deletes, without prompting the user to confirm
> ## if a listing is in the allow, it will be allowed to delete db/ldap
> ## if a listing is in the deny, it will be denied from deleting db/ldap
> ## multiple inputs can be entered with .0, .1, .2, etc.  These numbers must be sequential, starting with 0
> ###################################
> db.change.allow.url.0=jdbc:hsqldb:hsql://localhost/grouper
140a65,90
> #####################################
> ## Settings to track last membership changes for groups and stems.
> #####################################
> 
> # If true, when a membership is added to a group (either a privilege or a list member),
> # then an update will be made to the lastMembershipChange property for the group.
> groups.updateLastMembershipTime = true
> 
> # If true, when a membership is added to a stem (this would be a naming privilege),
> # then an update will be made to the lastMembershipChange property for the stem.
> stems.updateLastMembershipTime = true
> 
> #####################################
> ## misc settings which probably dont need to be changed
> #####################################
> 
> # Use this interface implementation for access privileges
> privileges.access.interface           = edu.internet2.middleware.grouper.GrouperAccessAdapter
> # Use this interface implementation for naming privileges
> privileges.naming.interface           = edu.internet2.middleware.grouper.GrouperNamingAdapter
> # Use this interface implementation for attributeDef privileges
> privileges.attributeDef.interface     = edu.internet2.middleware.grouper.privs.GrouperAttributeDefAdapter
> 
> #####################################
> ## attribute framework
> #####################################
141a92,93
> # if the attribute loader attributes should be autoconfigured (created, etc)
> grouper.attribute.loader.autoconfigure = false
[appadmin@i2midev6 patchesAutoLoader]$
```

Load in the WS grouper.properties, then overwrite with grouper UI grouper.properties (since most recent is UI, but also want WS)

```
[appadmin@i2midev6 patchesAuto]$ diff /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.client.properties /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper.client.properties
1c1,7
< encrypt.key = /opt/grouper/2.2/pass/encrypt.key
---
> 
> # The grouper.client.properties file uses Grouper Configuration Overlays (documented on wiki)
> # By default the configuration is read from grouper.client.base.properties
> # (which should not be edited), and the grouper.client.properties overlays
> # the base settings.  See the grouper.client.base.properties for the possible
> # settings that can be applied to the grouper.client.properties
> 
```

```
[appadmin@i2midev6 patchesAuto]$ diff /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/subject.properties /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/subject.properties
117a118,212
> 
> #########################################
> ## Configuration for source id: cmuDirectory
> ## Source configName: cmuDirectory
> #########################################
> subjectApi.source.cmuDirectory.id = cmuDirectory
> 
> # this is a friendly name for the source
> subjectApi.source.cmuDirectory.name = CMU Directory
> 
> # type is not used all that much.  Can have multiple types, comma separate.  Can be person, group, application
> subjectApi.source.cmuDirectory.types = person
> 
> # the adapter class implements the interface: edu.internet2.middleware.subject.Source
> # adapter class must extend: edu.internet2.middleware.subject.provider.BaseSourceAdapter
> # edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter2  :  if doing JDBC this should be used if possible.  All subject data in one table/view.
> # edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter   :  oldest JDBC source.  Put freeform queries in here
> # edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter   :  used for LDAP
> subjectApi.source.cmuDirectory.adapterClass = edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter
> 
> # link back to grouper-loader.properties
> subjectApi.source.cmuDirectory.param.ldapServerId.value = cmuLdap
> 
> # e.g. com.sun.jndi.ldap.LdapCtxFactory
> #subjectApi.source.cmuDirectory.param.INITIAL_CONTEXT_FACTORY.value = com.sun.jndi.ldap.LdapCtxFactory
> 
> # e.g. ldap://localhost:389
> #subjectApi.source.cmuDirectory.param.PROVIDER_URL.value = ldap://ldap.andrew.cmu.edu:389
> 
> # e.g. simple, none, sasl_mech
> #subjectApi.source.cmuDirectory.param.SECURITY_AUTHENTICATION.value = none
> 
> # ldap attribute which is the subject id.  e.g. exampleEduRegID   Each subject has one and only one subject id.  Generally it is opaque and permanent.
> subjectApi.source.cmuDirectory.param.SubjectID_AttributeType.value = guid
> 
> # if the subject id should be changed to lower case after reading from datastore.  true or false
> subjectApi.source.cmuDirectory.param.SubjectID_formatToLowerCase.value = false
> 
> # attribute which is the subject name
> subjectApi.source.cmuDirectory.param.Name_AttributeType.value = cn
> 
> # attribute which is the subject description
> subjectApi.source.cmuDirectory.param.Description_AttributeType.value = cn
> 
> # the 1st sort attribute for lists on screen that are derived from member table (e.g. search for member in group)
> # you can have up to 5 sort attributes 
> subjectApi.source.cmuDirectory.param.sortAttribute0.value = cn
> 
> # the 1st search attribute for lists on screen that are derived from member table (e.g. search for member in group)
> # you can have up to 5 search attributes 
> subjectApi.source.cmuDirectory.param.searchAttribute0.value = searchAttribute0
> 
> #searchSubject: find a subject by ID.  ID is generally an opaque and permanent identifier, e.g. 12345678.
> #  Each subject has one and only on ID.  Returns one result when searching for one ID.
> 
> # sql is the sql to search for the subject by id.  %TERM% will be subsituted by the id searched for
> subjectApi.source.cmuDirectory.search.searchSubject.param.filter.value = (& (guid=%TERM%) (objectclass=cmuPerson))
> 
> # Scope Values can be: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
> subjectApi.source.cmuDirectory.search.searchSubject.param.scope.value = SUBTREE_SCOPE
> 
> # base dn to search in
> subjectApi.source.cmuDirectory.search.searchSubject.param.base.value = ou=person,dc=cmu,dc=edu
> 
> #searchSubjectByIdentifier: find a subject by identifier.  Identifier is anything that uniquely
> #  identifies the user, e.g. jsmith or jsmith@institution.edu.
> #  Subjects can have multiple identifiers.  Note: it is nice to have if identifiers are unique
> #  even across sources.  Returns one result when searching for one identifier.
> 
> # sql is the sql to search for the subject by identifier.  %TERM% will be subsituted by the identifier searched for
> subjectApi.source.cmuDirectory.search.searchSubjectByIdentifier.param.filter.value = (& (cmuAndrewCommonNamespaceId=%TERM%) (objectclass=cmuPerson))
> 
> # Scope Values can be: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
> subjectApi.source.cmuDirectory.search.searchSubjectByIdentifier.param.scope.value = SUBTREE_SCOPE
> 
> # base dn to search in
> subjectApi.source.cmuDirectory.search.searchSubjectByIdentifier.param.base.value = ou=person,dc=cmu,dc=edu
> 
> #   search: find subjects by free form search.  Returns multiple results.
> 
> # sql is the sql to search for the subject by free form search.  %TERM% will be subsituted by the text searched for
> subjectApi.source.cmuDirectory.search.search.param.filter.value = (& (|(guid=%TERM%)(|(cn=*%TERM%*)(cmuAndrewCommonNamespaceId=*%TERM%*)))(objectclass=cmuPerson))
> 
> # Scope Values can be: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
> subjectApi.source.cmuDirectory.search.search.param.scope.value = SUBTREE_SCOPE
> 
> # base dn to search in
> subjectApi.source.cmuDirectory.search.search.param.base.value = ou=person,dc=cmu,dc=edu
> 
> # attributes from ldap object to become subject attributes.  comma separated
> subjectApi.source.cmuDirectory.attributes = cn, guid, cmuAndrewCommonNamespaceId
> 
> # internal attributes are used by grouper only not exposed to code that uses subjects.  comma separated
> subjectApi.source.cmuDirectory.internalAttributes = searchAttribute0
> 
```

Lets leave the CMU source out of there for performance reasons

## Backup and blank out the config files

```
[appadmin@i2midev6 configBackup]$ mkdir ui
[appadmin@i2midev6 configBackup]$ mkdir ws
[appadmin@i2midev6 configBackup]$ mkdir daemon
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/subject.properties ws
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper.properties ws
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper.client.properties ws
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper-ws.properties ws
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper-ui.properties ws
cp: cannot stat â€˜/opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper-ui.propertiesâ€™: No such file or directory
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper.cache.properties ws
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_e/webapps/grouper-ws_v2_4/WEB-INF/classes/grouper-loader.properties ws
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/subject.properties daemon
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.properties daemon
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.client.properties daemon
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.properties daemon
cp: cannot stat â€˜/opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.propertiesâ€™: No such file or directory
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ui.properties daemon
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.cache.properties daemon
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-loader.properties daemon

[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/subject.properties ui
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.properties ui
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.client.properties ui
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-loader.properties ui
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-ui.properties ui
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.properties ui
[appadmin@i2midev6 configBackup]$ cp /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.cache.properties ui

[appadmin@i2midev6 configBackup]$ rm /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/subject.properties
[appadmin@i2midev6 configBackup]$ touch /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/subject.properties
[appadmin@i2midev6 configBackup]$ rm /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.properties
[appadmin@i2midev6 configBackup]$ touch  /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.properties
[appadmin@i2midev6 configBackup]$ rm /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.cache.properties
[appadmin@i2midev6 configBackup]$ touch /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.cache.properties
[appadmin@i2midev6 configBackup]$ rm /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-ui.properties
[appadmin@i2midev6 configBackup]$ touch /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-ui.properties
[appadmin@i2midev6 configBackup]$ rm /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.properties
[appadmin@i2midev6 configBackup]$ touch  /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.properties
[appadmin@i2midev6 configBackup]$ rm /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-loader.properties
[appadmin@i2midev6 configBackup]$ touch  /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper-loader.properties
[appadmin@i2midev6 configBackup]$ rm /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.client.properties
[appadmin@i2midev6 configBackup]$ touch /opt/tomcats/tomcat_b/webapps/grouper_v2_4/WEB-INF/classes/grouper.client.properties

rm /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/subject.properties
touch /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/subject.properties
rm /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.properties
touch  /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.properties
rm /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.cache.properties
touch /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.cache.properties
rm /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ui.properties
touch /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ui.properties
rm /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.properties
touch  /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-ws.properties
rm /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-loader.properties
touch  /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper-loader.properties
rm /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.client.properties
touch /opt/tomcats/tomcat_b_gsh/webapps/grouper_v2_4/WEB-INF/classes/grouper.client.properties

```

Bounce all JVMs. You are done!
