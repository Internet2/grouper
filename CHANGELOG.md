# Grouper Change Log


## [Unreleased]

See road map: [https://spaces.at.internet2.edu/display/Grouper/Grouper+Product+Roadmap]()


## [2.4.0] - 2018-08-31

[https://spaces.at.internet2.edu/display/Grouper/Grouper+2.4+Release+Announcement]()

[https://spaces.at.internet2.edu/display/Grouper/v2.4+Release+Notes]()


- Migrate to new UI: Migrate all screens in Admin and Lite UI to the "New UI" and remove the admin and lite UI. Note, you can add the legacy UI if needed
- Deprovisioning: Deprovision access from someone to loses an affiliation or changes jobs
- Attestation: Groups and folders can be marked to require periodic membership review. Reminders will be emailed to group owners
- Grouper deployment guide	Version 1 of the Grouper deployment guide is an introduction to Grouper and best practices for using it
- New messaging strategies: Add new messaging strategies for ActiveMQ, AMQP (e.g. RabbitMQ), AWS
- Grouper loader in UI: User interface to show loader configuration, diagnostics, logs, wizard editor
- Subject API diagnostics: User interface to analyze, diagnose, and recommend improvements for subject source configuration
- Real time SQL loader: Allow a change log table (SQL triggers) or messages to trigger loader updates for a partial population or single user
- Instrumentation: Improve and standardize Grouper logging to provide centralized metrics at an institution and the ability to upload stats to a central Internet2 server
- GSH next generation: Improve gsh by adding readline like capabilities (line editing, tab completions, history, etc)
- Inbound messages: Allow Grouper to read a message queue and act on messages (e.g. membership changes etc)
- vt-ldap to Ldaptive: Upgrade from vt-ldap to Ldaptive
- properties config: Convert sources.xml and ehcache.xml to be cascaded properties files
- Update 3rd party libraries: Update 3rd party libraries to the latest version that is feasible


## [2.3.0] - 2016-04-21

[https://spaces.at.internet2.edu/display/Grouper/Grouper+2.3+Release+Announcement]()

[https://spaces.at.internet2.edu/display/Grouper/v2.3+Release+Notes]()


- PSPNG: Provisioning Service Provider Next Generation (PSPNG) implementation addresses performance problems and configuration complexity of the Provisioning Service Provider (PSP)
- Loader improvements: Grouper loader improvements: scheduling configuration stored in database facilitates high-availability changes to loader config files do not require restarting the loader, handling unresolvable subjects
- Web Service operations: New Web Service operations for attribute definitions, actions, and messaging
- Grouper messaging: Grouper messaging system and WS with integration to the change log and ESB
- UI screens: UI screens for attribute definitions and inherited privileges
- Export to GSH: Export Grouper objects to a Grouper Shell (GSH) script
- Folder privileges: Refactor folder privileges to be "admin" and "create" instead of "stem" and "create".  See the glossary for updated privileges and definitions.


### Added

  - GRP-1021: add attribute definition privileges screen
  - GRP-1029: WS subject source adapter
  - GRP-1031: add attributeDef edit screen and delete screen
  - GRP-1042: Provide find methods to return instances of Attribute usage
  - GRP-1061: add ESB feature to not send data for message, just that an event occurred
  - GRP-1062: add symmetric encryption method to grouper client library
  - GRP-1063: add encryption to esb messages
  - GRP-1124: put composite info on membership list
  - GRP-1135: allow move and copy for groups and folders from WS
  - GRP-1147: add hook to ensure unique case insensitive names for groups or stems
  - GRP-1167: Document options that subjectCheckConfig uses in the sources.xml
  - GRP-1197: incorporate John Gaspers hook to assign self opt out privilege based on attribute
  - GRP-1237: export to GSH commands
  - GRP-1242: fine-grained diagnostic per loader job
  - GRP-1269: Grouper Provisioning, The Next Generation (PSPNG)
  - GRP-1354: example of ldap loader resolving people or groups
  - GRP-1374: grouper box integration
  - GRP-1587: add rabbit mq to the installer


### Improved

  - GRP-838: make grouper ui/ws work with tomcat6 and tomcat7 and tomcat8
  - GRP-1017: add attribute definition screen to UI
  - GRP-1023: Make default widgets configurable
  - GRP-1037: merge voot forward from 2.0
  - GRP-1041: entity subject lookup is slow
  - GRP-1044: installer should not depend on SVN anymore for sample data
  - GRP-1054: grouper client expirable cache should allow time to live of 0 (or less)
  - GRP-1064: make grouper atlassian connector work on recent version of jira / confluence
  - GRP-1066: improve grouper voot connector
  - GRP-1073: Add a subject identifier to the members table
  - GRP-1076: externalize more character encodings
  - GRP-1089: add grouper admin groups for readonly and viewonly
  - GRP-1130: grouper loader should have configuration to not make changes (but log error) if too many removes
  - GRP-1131: add button to UI for loader group admins to refresh the group from the system of record
  - GRP-1161: Ensure Grouper builds have the right Java ver installed
  - GRP-1165: Provide "progress bars" as part of artifact downloads and install
  - GRP-1221: Add functionality to LoaderLdapElUtils class
  - GRP-1226: Add the ability for the loader to run on multiple nodes to it has better availability by adding tables for quartz
  - GRP-1236: Upgrade hibernate to version 5.
  - GRP-1252: Loader - fail safe with group list
  - GRP-1253: Loader - unresolvables
  - GRP-1262: Allow new UI to schedule new loader jobs without having to restart the daemon
  - GRP-1421: add grouperInstaller java check at startup
  - GRP-1422: add tasks in installer to manage tomcat, grouperDaemon, database
  - GRP-1821: cron info in Miscellaneous - Loader jobs


### Fixed

  - GRP-729: find java if in jre subdir: C:\Program Files\Java\jre6\bin\javac -version
  - GRP-1010: Changes to Browse Folders pane not automatically refreshing
  - GRP-1015: New UI has poor timeout handling
  - GRP-1016: New UI has inconsistent language
  - GRP-1018: 2.2 Installer fails on Ubuntu 14.04 trying to execute sh -version
  - GRP-1019: find members from group should take into account member search sort fields
  - GRP-1020: No custom GrouperEngine in 2.2.0?
  - GRP-1022: New UI not loading custom CSS configured in grouper-ui.properties with property css.additional
  - GRP-1024: sql server and oracle problems with paging page number when not initted
  - GRP-1025: allow any grouper external public images (e.g. institution logos) through csrf
  - GRP-1026: if folder already exists, and is created in the new UI, the existing folder is just shown (no error)
  - GRP-1027: if group name already exists, instead of a generic message, show a validation message on the ID or name
  - GRP-1030: subject not found in object creator or modifier causes ui problems
  - GRP-1032: Problem with Accented characters and ComboBox in New UI
  - GRP-1038: services links dont go anywhere
  - GRP-1039: problem with ruleThenEnum : assignMembershipDisabledDaysForOwnerGroupId
  - GRP-1045: grouperSession should be easier to use if not sure if open
  - GRP-1046: if home page is slow, change widgets to not manage groups
  - GRP-1048: problem with "Browse Folders" in New UI
  - GRP-1051: grouper loader performance bulk subject queries
  - GRP-1055: group paging misses a group
  - GRP-1060: add aws sns / sqs support to the esb change log consumer
  - GRP-1065: some grouper ui text not externalized
  - GRP-1067: add threads to loader jobs
  - GRP-1068: psp needs group display name change log event
  - GRP-1069: psp has some redundant jars with api (vt-ldap and hibernate at least)
  - GRP-1075: Webservice getMemberships not working with membershipIds
  - GRP-1079: prompt user to change existing gsh to executable and dos2unix
  - GRP-1081: upgrader does not resolve group subjects correctly
  - GRP-1082: grouper paging tag2 has one word not externalized
  - GRP-1083: cannot set enabled/disabled dates in ui
  - GRP-1086: extra system out print in ui
  - GRP-1087: edit membership page shows large H as icon in title
  - GRP-1088: attribute def left menu link throws error
  - GRP-1091: ldap loader display name doesnt change
  - GRP-1095: hibernate exception handling masked original exception if roll back fails
  - GRP-1097: grouper logout management
  - GRP-1102: Dangling html comment tag
  - GRP-1107: default stem for new ui
  - GRP-1108: option to not show links to admin ui and lite ui
  - GRP-1109: problems with inherited privileges rule
  - GRP-1111: if you leave a group via UI and leaving revokes view privs (or others), dont throw error
  - GRP-1112: problems with 'edit memberships and privileges' button
  - GRP-1114: cant assign privs to composite group
  - GRP-1117: grouper new ui tooltips wrap
  - GRP-1118: if you remove your own privs from a group via new UI, dont throw error
  - GRP-1119: Creating a new group does not refresh group list
  - GRP-1120: Viewing folder privs: Javascript dangling on page
  - GRP-1126: grouper import xml fails on attribute owner stem id
  - GRP-1127: fix test case for international characters
  - GRP-1128: Non-english chars dont seem to render correctly
  - GRP-1132: option to auto delete empty loader groups used in other groups
  - GRP-1133: this groups memberships in new ui fails if effective only memberships and wont remove memberships
  - GRP-1134: add hook to make sure names of different types of objects are unique (group, stem, attribute, attribute definition)
  - GRP-1139: PSP doesn't support configs with multiple classes in a Grouper Hook definition
  - GRP-1151: subject api needs ability to use ldap.properties for vt-ldap
  - GRP-1153: Audit Log should show Engine/Type as Loader
  - GRP-1160: Grouper-WS module builds with java installed on machine
  - GRP-1169: Improve folder privileges
  - GRP-1183: status servlet gives error if loader job is not configured correctly
  - GRP-1188: print an error if Grouper is used with invalid version of Java
  - GRP-1192: gsh should use the most recently opened grouper session
  - GRP-1193: cant delete composite group
  - GRP-1195: allow hooks to know if an object delete is occurring
  - GRP-1201: auto create user folder on ui login
  - GRP-1202: add jvm flag for utf8 file encoding to gsh.bat and gsh.sh
  - GRP-1205: grouperInstaller uses the wrong patch index file
  - GRP-1206: grouperInstaller should fix the patch index file
  - GRP-1208: on patch revert, if the patch file in the app doesnt exist.. then continue
  - GRP-1212: if a loader job has subject problems then it should be reflected in GSH and UI (give error)
  - GRP-1213: null pointer in ldap subject
  - GRP-1220: grouper_aval_asn_efmship_v view is incorrect
  - GRP-1232: member change subject tries to update effective membership
  - GRP-1233: Config check for useInClauseForIdAndIdentifier
  - GRP-1244: Opt-Out group not displayed in "My memberships"
  - GRP-1246: grouper rules privilege inheritance from stem error
  - GRP-1247: subject jarfile mismatch in 2.2.2
  - GRP-1248: if the file to replace equals the new patch file, then dont throw error that doesnt match the old file
  - GRP-1259: Remove the property loader.thread.pool.size
  - GRP-1260: MembershipFinder stem problem
  - GRP-1264: improve changelog implementations to make high level base class
  - GRP-1266: Synchronize creation of GrouperCache's
  - GRP-1270: grouper export to gsh
  - GRP-1271: add grouper web service for configuring attribute actions
  - GRP-1273: add grouper web service for sending, receiving, acknowledging messages
  - GRP-1274: add messaging system to grouper
  - GRP-1275: add tier api ws for hasMember
  - GRP-1279: grouper installer should set sql logging to on so we can see progress of DDL on install or upgrade
  - GRP-1283: grouper ws doesnt build due to classpath
  - GRP-1284: grouper installer should ignore if no dos2unix (try in java?)
  - GRP-1302: improve offline mode of grouper installer
  - GRP-1406: SSL on download URL
  - GRP-1414: grouper installer doesnt use tarball dir when installing (but it works for upgrading, patching, etc)
  - GRP-1425: admin task in grouperInstaller to help with log management
  - GRP-1426: grouperInstaller should be able to delete a file using a patch (not just add or replace)
  - GRP-1429: grouperInstaller tarballs dir fails on upgrade
  - GRP-1435: grouper installer should start hsql and loader in a new nohup process on linux
  - GRP-1511: upgrade Grouper installer to tomcat 8
  - GRP-1523: installer with mysql does not work
  - GRP-1598: allow not having every entity assigned to attribute privileges automatically
  - GRP-1628: installer mangles rabbitmq jars
  - GRP-1811: grouper-duo, edu.internet2.middleware.grouperDuo.GrouperDuoFullRefresh.java needs to extend OtherJobBase


## [2.2.2] - 2015-09-21

[https://spaces.at.internet2.edu/display/Grouper/Grouper+2.2+Release+Announcement]()

[https://spaces.at.internet2.edu/display/Grouper/v2.2+Release+Notes#v2.2ReleaseNotes-NewFeaturesinGrouper2.2.2]()

[https://spaces.at.internet2.edu/display/Grouper/Grouper+changes+v2.2]()

  - Admin groups: Readonly and viewonly admin groups
  - Loader: Grouper loader failsafe threshold


### Added

  - GRP-1124: put composite info on membership list
  - GRP-1135: allow move and copy for groups and folders from WS
  - GRP-1147: add hook to ensure unique case insensitive names for groups or stems
  - GRP-1167: Document options that subjectCheckConfig uses in the sources.xml
  - GRP-1197: incorporate John Gaspers hook to assign self opt out privilege based on attribute


### Improved

  - GRP-1089: add grouper admin groups for readonly and viewonly
  - GRP-1096: Use threads for 2.2 upgrade to decrease time of upgrade
  - GRP-1130: grouper loader should have configuration to not make changes (but log error) if too many removes
  - GRP-1131: add button to UI for loader group admins to refresh the group from the system of record
  - GRP-1161: Ensure Grouper builds have the right Java ver installed
  - GRP-1165: Provide "progress bars" as part of artifact downloads and install


### Fixed

  - GRP-1056: add ability for UI to get username from HTTP header
  - GRP-1079: prompt user to change existing gsh to executable and dos2unix
  - GRP-1080: browse folders refresh button only works in chrome (not firefox or ie)
  - GRP-1081: upgrader does not resolve group subjects correctly
  - GRP-1082: grouper paging tag2 has one word not externalized
  - GRP-1083: cannot set enabled/disabled dates in ui
  - GRP-1086: extra system out print in ui
  - GRP-1087: edit membership page shows large H as icon in title
  - GRP-1088: attribute def left menu link throws error
  - GRP-1091: ldap loader display name doesnt change
  - GRP-1093: Legacy attribute migration error
  - GRP-1094: Upgrade from v2.2.0 to v2.2.1 recreates views/constraints
  - GRP-1095: hibernate exception handling masked original exception if roll back fails
  - GRP-1097: grouper logout management
  - GRP-1100: grouper new ui not showing unresolvable subjects correctly
  - GRP-1102: Dangling html comment tag
  - GRP-1106: extra jars cause conflicts and warnings, mail, and servlet
  - GRP-1107: default stem for new ui
  - GRP-1108: option to not show links to admin ui and lite ui
  - GRP-1109: problems with inherited privileges rule
  - GRP-1111: if you leave a group via UI and leaving revokes view privs (or others), dont throw error
  - GRP-1112: problems with 'edit memberships and privileges' button
  - GRP-1114: cant assign privs to composite group
  - GRP-1117: grouper new ui tooltips wrap
  - GRP-1118: if you remove your own privs from a group via new UI, dont throw error
  - GRP-1126: grouper import xml fails on attribute owner stem id
  - GRP-1128: Non-english chars dont seem to render correctly
  - GRP-1132: option to auto delete empty loader groups used in other groups
  - GRP-1133: this groups memberships in new ui fails if effective only memberships and wont remove memberships
  - GRP-1134: add hook to make sure names of different types of objects are unique (group, stem, attribute, attribute definition)
  - GRP-1137: Group copy with new group extension
  - GRP-1138: add import / export auditing
  - GRP-1139: PSP doesn't support configs with multiple classes in a Grouper Hook definition
  - GRP-1141: PSP distribution files are not Java 8 compatible.
  - GRP-1151: subject api needs ability to use ldap.properties for vt-ldap
  - GRP-1153: Audit Log should show Engine/Type as Loader
  - GRP-1160: Grouper-WS module builds with java installed on machine
  - GRP-1173: Exceptions when sync'ing unresolveable subjects
  - GRP-1174: Exception when archiving group
  - GRP-1175: Group deletes throw change log exceptions
  - GRP-1176: Properties read incorrectly by not taking into account the consumer name
  - GRP-1177: Privileges not handled properly by Google Apps connector
  - GRP-1183: status servlet gives error if loader job is not configured correctly
  - GRP-1185: installer on new versions of oracle might need an updated driver or driver class
  - GRP-1186: Installer 2.2.1 problem with Oracle
  - GRP-1187: NullPointerException when trying to remove extra members
  - GRP-1188: print an error if Grouper is used with invalid version of Java
  - GRP-1192: gsh should use the most recently opened grouper session
  - GRP-1193: cant delete composite group
  - GRP-1195: allow hooks to know if an object delete is occurring
  - GRP-1200: Running "-registry -drop -runscript" fails
  - GRP-1201: auto create user folder on ui login
  - GRP-1202: add jvm flag for utf8 file encoding to gsh.bat and gsh.sh
  - GRP-1205: grouperInstaller uses the wrong patch index file
  - GRP-1206: grouperInstaller should fix the patch index file


## [2.2.1] - 2015-07-20

[https://spaces.at.internet2.edu/display/Grouper/Grouper+2.2+Release+Announcement]()

[https://spaces.at.internet2.edu/display/Grouper/v2.2+Release+Notes#v2.2ReleaseNotes-NewFeaturesinv2.2.1]()

[https://spaces.at.internet2.edu/display/Grouper/Grouper+changes+v2.2]()

  - Grouper upgrader: Automatically upgrade Grouper to the latest version.
  - Loader performance improvements: New indexes and loader jobs run in threads.
  - AWS SNS/SQS and encryption for ESB: ESB improvements with compatibility for AWS messaging


### Added

  - GRP-1007: add a way to refresh the left menu of the new UI, refresh on object creation
  - GRP-1042: Provide find methods to return instances of Attribute usage
  - GRP-1061: add ESB feature to not send data for message, just that an event occurred
  - GRP-1062: add symmetric encryption method to grouper client library
  - GRP-1063: add encryption to esb messages
  - GRP-1149: add patch creator to grouper installer
  - GRP-1150: add non-interactive mode to grouper installer


### Improved

  - GRP-1023: Make default widgets configurable
  - GRP-1034: Cache JEXL expressions in Grouper
  - GRP-1037: merge voot forward from 2.0
  - GRP-1040: Main screen performance - user audit
  - GRP-1041: entity subject lookup is slow
  - GRP-1044: installer should not depend on SVN anymore for sample data
  - GRP-1047: Prevent GrouperAll from getting admin/update/groupAttrUpdate/member privileges on groups
  - GRP-1049: Indexes for grouper_change_log_entry_temp
  - GRP-1054: grouper client expirable cache should allow time to live of 0 (or less)
  - GRP-1059: If flattened privileges are disabled, don't perform flattened queries on privilege add/delete
  - GRP-1064: make grouper atlassian connector work on recent version of jira / confluence
  - GRP-1066: improve grouper voot connector
  - GRP-1076: externalize more character encodings
  - GRP-1077: remove grouperClient from misc projects
  - GRP-1165: Provide "progress bars" as part of artifact downloads and install


### Fixed

  - GRP-1006: services show up in the main screen widget panel, but not in the "my services" quick links
  - GRP-1010: Changes to Browse Folders pane not automatically refreshing
  - GRP-1012: Transactions committed with failures (Admin UI)
  - GRP-1015: New UI has poor timeout handling
  - GRP-1016: New UI has inconsistent language
  - GRP-1018: 2.2 Installer fails on Ubuntu 14.04 trying to execute sh -version
  - GRP-1019: find members from group should take into account member search sort fields
  - GRP-1020: No custom GrouperEngine in 2.2.0?
  - GRP-1022: New UI not loading custom CSS configured in grouper-ui.properties with property css.additional
  - GRP-1024: sql server and oracle problems with paging page number when not initted
  - GRP-1025: allow any grouper external public images (e.g. institution logos) through csrf
  - GRP-1026: if folder already exists, and is created in the new UI, the existing folder is just shown (no error)
  - GRP-1027: if group name already exists, instead of a generic message, show a validation message on the ID or name
  - GRP-1030: subject not found in object creator or modifier causes ui problems
  - GRP-1032: Problem with Accented characters and ComboBox in New UI
  - GRP-1033: PSP can't see IntegerID
  - GRP-1038: services links dont go anywhere
  - GRP-1039: problem with ruleThenEnum : assignMembershipDisabledDaysForOwnerGroupId
  - GRP-1045: grouperSession should be easier to use if not sure if open
  - GRP-1046: if home page is slow, change widgets to not manage groups
  - GRP-1048: problem with "Browse Folders" in New UI
  - GRP-1051: grouper loader performance bulk subject queries
  - GRP-1052: grouper installer to upgrade Grouper
  - GRP-1055: group paging misses a group
  - GRP-1056: add ability for UI to get username from HTTP header
  - GRP-1058: Member.hasAttrAdmin() returns all attribute defs
  - GRP-1060: add aws sns / sqs support to the esb change log consumer
  - GRP-1065: some grouper ui text not externalized
  - GRP-1067: add threads to loader jobs
  - GRP-1068: psp needs group display name change log event
  - GRP-1069: psp has some redundant jars with api (vt-ldap and hibernate at least)
  - GRP-1072: junit loader test fails group not found, but runs by itself
  - GRP-1075: Webservice getMemberships not working with membershipIds
  - GRP-1079: prompt user to change existing gsh to executable and dos2unix
  - GRP-1080: browse folders refresh button only works in chrome (not firefox or ie)
  - GRP-1081: upgrader does not resolve group subjects correctly
  - GRP-1122: on an upgrade, the patcher would not download from right URL, thought no patches available
  - GRP-1145: patches should have test mode
  - GRP-1168: grouper installer patch creator doesnt find old files when they are jsps (for example)
  - GRP-1179: installer prints progress dots while waiting on user input
  - GRP-1185: installer on new versions of oracle might need an updated driver or driver class
  - GRP-1186: Installer 2.2.1 problem with Oracle
  - GRP-1205: grouperInstaller uses the wrong patch index file
  - GRP-1206: grouperInstaller should fix the patch index file
  - GRP-959: add some error handling for new ui logins
  - GRP-992: rename a stem by id (and a group?) and you get an error


## [2.2.0] - 2014-07-10

[https://spaces.at.internet2.edu/display/Grouper/Grouper+2.2+Release+Announcement]()

[https://spaces.at.internet2.edu/display/Grouper/v2.2+Release+Notes#v2.2ReleaseNotes-NewFeaturesinv2.2.0]()

[https://spaces.at.internet2.edu/display/Grouper/Grouper+changes+v2.2]()


- New UI: There is a new more usable UI which makes Grouper easy to use by end users and administrators. It co-exists with the legacy Admin UI and the Lite UIs.
- Legacy attribute migration: Migrate from legacy attributes to the new attribute framework in a transparent way.  The API, WS, and UI work similar as they did before.
- SCIM events: SCIM events can be sent from Grouper to keep SCIM compatible systems in sync with Grouper.
- Improved configuration: Grouper config files can have hierarchical overlays to make Grouper easier to deploy and upgrade across multiple environments.
- Unix GID management: Grouper objects now have a unique integer ID which can be used in GID management
- Services in Grouper: Ability to tag folders in Grouper (via the attribute framework) so that folders, groups, permissions can be grouped into a "service".  The API/UI/WS can filter search results based on the service to make it easier for users to perform tasks in Grouper.
- Many other fixes and improvements were also made to all components of the Grouper Toolkit: Grouper API, Administrative & Lite UIs, Grouper Web Services, Grouper Client, Grouper Shell, Grouper Loader, PSP, and the Subject API.


### Added

  - GRP-1013: Publish Grouper 2.2 Artifacts to the SonaType OSS Repo
  - GRP-525: stem set table
  - GRP-529: config file overlays
  - GRP-845: getMembers WS needs paging/sorting
  - GRP-919: add api for storing recent and favorite objects
  - GRP-920: subject api security by calling user "realm"
  - GRP-957: add status servlet to UI (API)


### Improved

  - GRP-827: grouper kim connector causes routing problems when user has no email address
  - GRP-836: update grouper database drivers
  - GRP-848: assert that grouper privileges are group lists
  - GRP-885: Investigate SCIM Integration
  - GRP-896: move jdbc2 source from grouper to subject api
  - GRP-917: default group read/view privileges to not be globally public
  - GRP-927: Adding attribute read and update privileges
  - GRP-933: grouper kerberos authn should use krb5.conf so multiple kdcs can be used
  - GRP-962: GroupID/FolderID - default to foldername
  - GRP-972: Cache JEXL expressions
  - GRP-973: add rule (or give example) of remove from org and also in another group sends email
  - GRP-988: Notify if Javascript is disabled on the UI
  - GRP-989: Provide sample import file layouts
  - GRP-991: Migrate group types and legacy attributes to attribute framework
  - GRP-993: add id index to new ui in Group and Stem


### Fixed

  - GRP-1000: Grouper UI character encoding problem
  - GRP-1001: Registry needs to indexes to help with SQL export
  - GRP-1002: If a non-wheel user has STEM but not CREATE privilege, they cannot create a group
  - GRP-1003: grouperUserData sort is wrong
  - GRP-1004: grouper user data should not be stored in PIT, audits, or change log
  - GRP-1008: Java7 and tomcat7 have jar problems on sql server driver  sqljdbc4.jar,  java.lang.SecurityException: Invalid signature file digest for Manifest main attributes
  - GRP-1009: grouper shows partial screen in new ui (2.2) when browser language is not en/us
  - GRP-821: [psp] Object Class Violiation silently ignored
  - GRP-843: add paging for ldap
  - GRP-844: subject api multiple identifiers in jdbc1 sources do not work for non-first identifier
  - GRP-893: for a subjectfinder findall allow multiple searches comma separated
  - GRP-914: PSP improperly handles membership delete from changelog when a group is deleted
  - GRP-949: RangeSearchResultHandler from ldappc-ng not included in recent distributions
  - GRP-956: esb request header not compatible with some versions of activemq
  - GRP-958: trace folder privileges mentions a direct priv of STEM, but not the indirect of CREATE, ATTR, etc...
  - GRP-960: grouper_ext_subject_invite_v should not have null rows
  - GRP-961: if you assign privs in stem, and dont pick privs, doesnt give the right validation error
  - GRP-965: lite ui import doesnt work
  - GRP-966: make dynamic csrf error page with externalized text
  - GRP-967: move source files for dojo out of grouper-ui, e.g. to a misc project of its own
  - GRP-968: merge config overlay changes into grouper (so it doesnt think files change when they dont)
  - GRP-971: Hibernate makes repeated calls to load classes
  - GRP-975: attribute def and attribute def name should go to lite ui
  - GRP-976: my folders does show folders the user manages
  - GRP-977: in new ui if creating a group which already exists, show error
  - GRP-978: new ui edit membership throws error if the member has no privileges
  - GRP-983: group export causes csrf issue
  - GRP-984: unable to set start/end dates on memberships if not admin on group
  - GRP-986: CSRF error when adding group/stem/entity to workspace on admin UI
  - GRP-990: Crash when creating new groups
  - GRP-994: remove member when caller cant READ group (only UPDATE) will not remove immediate member
  - GRP-996: More CSRF errors
  - GRP-997: Recently used widget doesn't contain anything
  - GRP-998: csrf fails on admin ui delete group/stem
  - GRP-999: lite ui forms dont work with radios and brackets e.g. create group


## [2.1.5] - 2013-08-22

[https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes]()


### Improved

  - GRP-925: LDAP_GROUPS_FROM_ATTRIBUTES where membership carried in two attributes
  - GRP-926: allow getMemberships WS to return privileges, especially for stems


### Fixed

  - GRP-909: LDAP loader may miss memberships if using grouperLoaderLdapGroupNameExpression
  - GRP-911: Lite UI doesn't check privileges when deleting attribute assignments
  - GRP-912: mail body is badly quoted-printable encoded => accents issues
  - GRP-913: grouper loader ldap loader.ldap.requireTopStemAsStemFromConfigGroup put an extra colon at front of name and hence doesnt work
  - GRP-923: WS getGrouperPrivilegesLite can return more data than the user should be able to see
  - GRP-924: Grouper WS allows unauthorized users to delete attribute assignments
  - GRP-928: Grouper UI allows unauthorized users to view the privileges of other subjects
  - GRP-929: reduce memory footprint for loader
  - GRP-930: optin privilege should not imply read



## [2.1.4] - 2013-05-20

[https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes]()


### Added

  - GRP-881: add rule to change privileges on created objects to be assigned to inherited folder CREATE groups (if applicable)
  - GRP-904: add rule to implement lifelong membership in a group (members are members forever)
  - GRP-905: grouper ws ldap authentication
  - GRP-906: grouper ldap loader should be able to control which attributes are used in GROUPS_FROM_ATTRIBUTES


### Improved

  - GRP-878: add support for java 7
  - GRP-892: add debug info to the UI for REMOTE_USER etc for authentication
  - GRP-897: add support for AD ldap loader large resultsets
  - GRP-899: wheel group members should be able to edit any rules


### Fixed

  - GRP-797: psp in Maven Central?
  - GRP-873: Wrong group name shown in creation and modification success message
  - GRP-876: Membership remove options don't appear for groups that are a factor in a composite
  - GRP-879: lite ui attribute assignment on attribute assignments should require ATTR_UPDATE on the assignee attribute definition not ATTR_ADMIN
  - GRP-880: Deleting an attributeDef can cause incorrect membership deletes
  - GRP-891: grouper session from static threadlocal can be an invalid session
  - GRP-894: sources.example.xml had ldap invalid filter
  - GRP-898: rescheduling ldap loader job causes error
  - GRP-900: subject API free form search should be able to filter by status
  - GRP-901: UI GroupComparatorHelper in admin ui gives these errors occasionally (not sure how to reproduce)
  - GRP-903: if the same member is created on two jvms, it throws a constraint exception
  - GRP-907: Add method in point in time sync process to fix effective group sets
  - GRP-921: ldap session list throws an error if the attribute does not exist on the server
  - GRP-922: deleting a group should unassign privileges from the change log


## [2.1.3] - 2013-01-02

[https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes]()


### Added

  - GRP-845: getMembers WS needs paging/sorting


### Improved

  - GRP-856: allow local entities to login to WS without the folder prefix


### Fixed

  - GRP-840: media.properties require user in group not working
  - GRP-843: add paging for ldap
  - GRP-844: subject api multiple identifiers in jdbc1 sources do not work for non-first identifier
  - GRP-846: LdapComparator compares based on subject description
  - GRP-850: getAttributeAssignment on multiple owners fails
  - GRP-851: assign membership attributes batch gives error
  - GRP-854: my memberships on UI shows folders where there are no groups inside
  - GRP-855: Using the stem search in <<My responsibilities -- Create Groups>> does nothing
  - GRP-857: local entity find by identifier does not work for attribute value entitySubjectIdentifier
  - GRP-859: non-ascii chars need more space in the DB
  - GRP-864: external subjects in UI shows "null" for institution
  - GRP-865: insert a new group with lite ui that exists will overwrite instead of give error
  - GRP-866: Fix error message when renaming a group using a name that already exists
  - GRP-867: find subject by identifier is not efficient for entities (causes problems)
  - GRP-868: exception while importing members from lite ui
  - GRP-870: Duplicate point in time records
  - GRP-874: permissions lite ui will not filter by all or disabled permissions
  - GRP-875: Stop preventing the basic subject attributes from being added to the subject's attribute map


## [2.1.2] - 2012-09-05

[https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes]()


### Added

  - GRP-839: new WS operation batch attribute assign


### Improved

 - GRP-826: genericize groups as ldap loader members
 - GRP-834: grouper tests should load the expected grouper.properties in the memory overrides


### Fixed

  - GRP-814: The GrouperPagingTag taglib is not l10n friendly
  - GRP-815: duplicate build.properties logout.link.show
  - GRP-818: [psp] incorrectly deletes groups when provisioning Active Directory
  - GRP-819: see if grouper ws axis2 is listening on 8443 (per PSU)
  - GRP-825: two group move/rename failures in postgres
  - GRP-830: subject filtering doesnt work in UI member lists
  - GRP-832: sql server driver mismatch
  - GRP-835: lite ui ajax shows uninformative message when session is logged out with HTTP 302 response
  - GRP-863: external subjects has not found error


## [2.1.1] - 2012-07-02

[https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes]()


### Improved

  - GRP-766: make it easier to start a non root session
  - GRP-775: Avoid generating an upgrade script if temp change log is not empty
  - GRP-781: improve logging of grouper ws
  - GRP-784: grouper ws login cache is too long
  - GRP-785: validation of assigning attributes to various owner types is in UI and not API
  - GRP-787: [psp] Improve performance when searching for members by specifying source.
  - GRP-789: [grouper-shib] improve member data connector cache performance
  - GRP-792: make grouper client easier to do scheduled jobs and incremental jobs
  - GRP-798: improve WS search attributes including allow search by value
  - GRP-799: psp - real-time provisioning - option to block on errors
  - GRP-801: should make gsh symlink in unix to gsh.sh so the scripts have same documentation
  - GRP-803: grouper loader ldap should give option for full access of registry
  - GRP-807: change the grouper ws default restlike format to json from xhtml
  - GRP-812: [psp] memory issue with bulk sync


### Fixed

  - GRP-767: grouper should not change case of subject ids in jndi source adapter (to lower in 2.1.0)
  - GRP-768: grouper ws soap wsdl not available over https
  - GRP-770: grouper loader LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES will not schedule from cron
  - GRP-771: [psp] multiple slf4j bindings
  - GRP-777: sending wrong owner type for get attribute assignments WS does not return error and queries for all attributes
  - GRP-780: gruoper startup prints null for some ldap subject sources
  - GRP-782: grouper-ws build script does not copy grouper/lib/custom
  - GRP-786: can not remove group type from a group with an attribute that is required
  - GRP-788: enable some ehcache capture statistics
  - GRP-796: xml files shouldnt depend on external web servers, are they?
  - GRP-800: grouper loader has log message error when not an error, though doesnt fail
  - GRP-802: logging DEBUG on GrouperUtil and doing an EL substitution can cause an error
  - GRP-804: grouper loader ldap group or groups should create empty groups
  - GRP-805: [psp] provisioning all group members via bulk sync takes a long time
  - GRP-808: ws will return all memberships when searching for a subject which is not yet a member


## [2.1.0] - 2012-03-22

[https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes]()


- PSP: Changes to user access can now happen in real time courtesy of real time and incremental provisioning based on Grouper's changelog. Group and folder moves and renames can be provisioned as well. This is accomplished with the new Provisioning Service Provider (PSP), which replaces LDAPPCNG.
- Provision from LDAP: Grouper can now be updated from LDAP via PSP or Grouper Loader.
- More Web Services: Improved web services support for applications that outsource their
- internal access management to Grouper: Operations
    - Assign Attribute Definition Name Inheritance
    - Attribute Definition Name Save
    - Attribute Definition Name Delete
    - Find Attribute Definition Names
- Local Entities: Improved management of access by service principals to info stored in Grouper using a new "local entity" object type.
- Grouper Installer: It's quick and easy to get started using Grouper with the new installer. Really!
- Subject Filter and Attribute Decorator: Manage who can see which subject attributes with new subject attribute security support. You can also decorate subjects after retrieval from their source.
- Grouper Failover Client: Higher availability of web services using a new failover client library and discovery service library.


### Added

  - GRP-545: subject attribute security
  - GRP-587: [ldappcng] should support object renaming
  - GRP-592: real-time incremental provisioning
  - GRP-607: find groups in stem should support paging
  - GRP-611: Permission Notifications
  - GRP-642: add an entity type group, and allow assignments of permissions from permissions screen
  - GRP-659: add status servlet to the UI for nagios
  - GRP-699: add ability to create entities in namespace
  - GRP-700: always available grouper web services and client
  - GRP-701: add WS operations for attributeName creation/editing/deleting (including permissionNames), and permissionName inheritance
  - GRP-708: Alternate names for stems
  - GRP-739: [psp] Include example configuration files for Active Directory from U Montreal.
  - GRP-740: [psp] Include example provisioning of an attribute value assignment to a stem
  - GRP-742: add WS operation for find attirbute def names
  - GRP-744: add WS operation for saving attribute def names
  - GRP-745: add WS operation for deleting attribute def names
  - GRP-746: allow customization of lite ui subjects

### Improved

  - GRP-410: license and copyright procedures
  - GRP-437: Add JSONP support to Grouper-WS
  - GRP-442: allow ldap loader jobs
  - GRP-470: [grouper-shib] code cleanup
  - GRP-505: provide OpenLDAP schema in ldappc[ng] distributions
  - GRP-603: [grouper-shib] genericize grouper data connector match filters
  - GRP-605: refactor "-" in attribute resolver configurations
  - GRP-646: [ldappcng] add example config for ldap subject source
  - GRP-649: group source adaptor searching was getting all results and filtering on client
  - GRP-655: grouper diagnostics default failure interval does not take into account daily jobs that fail once and cant catch up
  - GRP-657: [ldappcng] remove getAllIdentifiers() from DataConnector extension
  - GRP-678: upgrade hibernate to 3.6.7
  - GRP-730: Encryption of the password for the service account used in LDAPPC-NG
  - GRP-741: [psp] Replace script attribute definitions with a custom attribute definition for filtering change log names.
  - GRP-743: Bad membership finder updates
  - GRP-747: remove flattened permission rule check types
  - GRP-751: [psp] synchronize groups from LDAP to Grouper

### Fixed

  - GRP-586: ldappcng should not delete subjects when a source is unavailable
  - GRP-596: ldappcng does not remove memberships for subjects removed from a group when --interval is used
  - GRP-597: ldappcng de-provisioning of stems
  - GRP-658: [ldappcg] support case-insensitive attribute values
  - GRP-688: Ordering of change log events when renaming a stem
  - GRP-690: remove immediate permission rule builtin types, and give an upgrade path (ability to search to see if they are being used)
  - GRP-731: Add additional columns to point in time tables so that ids in the point in time tables are not dependent on the ids of the other tables
  - GRP-732: Add unique index on owner column of grouper_composites
  - GRP-733: Race condition when adding composite and immediate membership
  - GRP-736: Identifier comparison is case sensitive in the provisionning
  - GRP-737: [psp] fix "IllegalStateException: null session member" in a threaded environment
  - GRP-738: [psp] Filter attribute assign value change log entries by assign type (group, stem, etc).
  - GRP-748: new ehcache version creates many threads when creating ehcache controllers per grouper session
  - GRP-752: The baseStem should be omitted (optionally) from ldap DNs
  - GRP-758: lite ui attribute definition new, folder search returns as error
  - GRP-760: assign a permission, then filter, the checkbox should be checked for immediate assignment
  - GRP-763: [psp] Exception when attribute name contains more than 2 colons


## [2.0.3] - 2012-01-08

[https://spaces.at.internet2.edu/display/Grouper/v2.0+Release+Notes]()


  - fixes a serious SQL problem with 2.0.2


### Fixed

  - GRP-723: missing parens in SQL statement in large deployment cause severe performance problem


## [2.0.2] - 2012-01-04

[https://spaces.at.internet2.edu/display/Grouper/v2.0+Release+Notes]()


-- fixes a couple dozen issues including making subject searches more efficient and some UI fixes


### Added

  - GRP-684: create obliterate stem gsh method to remove stem and all subobjects

### Improved

  - GRP-480: autodetect the driver and dialect in grouper.hibernate.properties
  - GRP-685: Change the default sort string for groups
  - GRP-689: fix grouper subject sort order
  - GRP-691: option to require grouper session when doing subject lookups
  - GRP-695: add ability to search for groups in permission filter and assign screen
  - GRP-697: Change defaults for last membership timestamp updates on groups and stems
  - GRP-703: group searches on UI should be case-insensitive
  - GRP-712: add batching to subject find by id, identifier, idOrIdentifier
  - GRP-713: implement subject batching to UI and WS parts

### Fixed

  - GRP-645: grouper admin ui can throw an error when assigning folder permissions
  - GRP-648: admin ui privilege list wraps to next line due to div
  - GRP-662: media.properties setting stem.sort.sibjectSumamry is misspelled twice
  - GRP-683: ClassNotFoundException org/hsqldb/Session
  - GRP-687: grouper subject performance problems
  - GRP-692: update member attributes can throw stale state exceptions
  - GRP-710: lite ui export should support multivalued subject attributes
  - GRP-716: need to remove paging from subject search on admin ui
  - GRP-717: paging in dropdown doesnt stick
  - GRP-718: some groups are not formatted correctly as subjects
  - GRP-720: ws will not allow remove member of unresolvable subject


## [2.0.1] - 2011-10-26

[https://spaces.at.internet2.edu/display/Grouper/v2.0+Release+Notes]()


  - fixes several issues


### Fixed

  - GRP-650: grouper rampart services.xml has the wrong ServiceClass, needs to be 2.0
  - GRP-667: Change log processor doesn't handle unassignment of the requireInGroups group type
  - GRP-668: Change log processor should not process temp change log entries with missing data
  - GRP-669: Point in time sync script fails due to a caching issue
  - GRP-672: change default of grouper-loader.properties loader.autoadd.typesAttributes to true
  - GRP-674: grouper external user ui does not work in IE


## [2.0.0] - 2011-09-06

[https://spaces.at.internet2.edu/display/Grouper/v2.0+Release+Notes]()


### New Features

  - Rules: Similar to Grouper Hooks, but instead of Java logic, built in actions or expression language scripts can be executed
  - External subjects: If your Identity Management System does not support external users (e.g. via EPPN), then Grouper can manage that with self registration and or invitations which will can provision memberships
  - Syncing groupers: A group in one Grouper can be sync'ed with a group in another Grouper.  For instance if two institutions want to share a group of subjects but store them in their own Grouper
  - Attribute and Permissions UI: User interface to define, view, and assign attributes and permissions in Grouper.  The attributes can be assigned to many types of Grouper objects including Groups, Folders, Members, Memberships, etc.  The permissions are used as a central permissions management system for other applications at your institution
  - Grouper-Atlassian connector: If you cannot connect Atlassian applications (e.g. Jira, Confluence) to your Grouper managed LDAP, then you can use this connector which used Grouper Web Services to manage your Atlassian groups and person information
  - Permissions Allow/disallow: A permission assignment can be an allow or disallow (to filter out allows inherited from another assignment)
  - Permission limits: A run-time decision can be applied to immediate permission allows so that context environment variables can change an allow to a disallow.  e.g. permissions are only allowed at a certain time of day or from a certain IP address.  Grouper can calculate this on the server or the client can get the limits and calculate them.
  - Web service versioning: Grouper 2.0 web servers will accept clients coded against Grouper 1.6 or previous WS API's
  - Point in Time Audit: This allows you to query the state of Grouper at a point in time in the past or a date range in the past.  You can query for memberships, privileges and permissions.

### Improvements and Fixes

  - Member Search and Sort: Additional data is now stored about subjects in Grouper.  This allows you to sort a list of members and search a list of members without having to go to the subject source to query attributes for each subject in the list that you would then use for the sort or search operation.
  - ldappcng caching (performance): The SPMLDataConnector supports caching similar to other Shibboleth DataConnectors
  - Notification improvements: Additional notifications are available now for permissions and the attribute framework.
  - Many other fixes and improvements were also made to all components of the Grouper Toolkit: Grouper API, Administrative & Lite UIs, Grouper Web Services, Grouper Client, Grouper Shell, Grouper Loader, Ldappc, Ldappc-ng, and the Subject API.


### Added

  - GRP-298: email notifications
  - GRP-366: add point in time auditing on memberships, permissions, and attributes
  - GRP-485: add hooks to new attribute framework
  - GRP-495: add rule for inherited privs based on group name
  - GRP-526: Subject improvements in Grouper
  - GRP-540: point in time web services for getMembers, getGroups, hasMember, and getPermissionAssignments
  - GRP-559: add rule to restrict use of subjects in folder
  - GRP-560: add db view to show rules assigned
  - GRP-571: last_immediate_membership_change for groups
  - GRP-578: Deleting point in time records based on groups and stems
  - GRP-583: sync point in time tables
  - GRP-612: Namespace transition & the new attribute framework


### Improved

  - GRP-456: add notifications to permissions and attribute framework
  - GRP-489: Admin UI 'Act As' permanent preference
  - GRP-503: ldappcng performance improvement - reduce the number of ldap queries
  - GRP-514: allow WS lookup by subjectId or identifier
  - GRP-515: addMember WS param for add external subject if not exist
  - GRP-517: add throbber to comboboxes in lite UI
  - GRP-527: add rule to allow/disallow sources
  - GRP-542: grouper client encrypts passwords differently than grouper API
  - GRP-551: add an icon for external people in lite picker
  - GRP-552: add option to not allow external subject self deletes
  - GRP-553: grouper api subjectfinder and subjectresolver adjust "type"
  - GRP-554: external subject invite and response is slow
  - GRP-561: restrict search on UI to sources that allowed to be searched by rules
  - GRP-567: can we alphabetize group names in the admin UI when listing a subject's groups?
  - GRP-614: Effect of deny permissions on point in time

### Fixed

  - GRP-403: cant unassign stem privileges from unresolvable subject
  - GRP-486: make sure lastUpdated, createdOn, and createdBy fields are accurate for new attribute and role tables
  - GRP-501: Permission views include non-permission attributes
  - GRP-518: grouper ui shows error on null groupAsMap
  - GRP-563: change log consumers do not have a SUCCESS end state in the grouper loader log
  - GRP-564: grouperEngine is blank for loader audit logs
  - GRP-565: delete old change log entry records
  - GRP-568: i click on a stem name as admin, i get an error screen
  - GRP-570: source not available should not throw subject not found
  - GRP-585: dont show logout link on lite ui if logout.link.show is false in media.properties
  - GRP-601: jndi source should allow anonymous binds without specifying credentials
  - GRP-622: Grouper loader type SQL_GROUP_LIST only syncs groups with members
  - GRP-624: admin ui doesnt show members on loader group
  - GRP-625: lite ui doesnt find folders if non admin
  - GRP-627: grouper limits should check validations on api (and hence ws) not just ui
  - GRP-628: lite ui comboboxes do not work with ie9
  - GRP-629: Change log entries given to change log consumers are not ordered
  - GRP-631: lite ui edit group should not show results which arent admin
  - GRP-632: ie9 and inheritance graph applet
  - GRP-637: internationalization with grouper lite UI


## [1.6.3] - 2011-01-04

[https://spaces.at.internet2.edu/display/Grouper/v1.6+Release+Notes]()

[https://spaces.at.internet2.edu/display/Grouper/Archives]()


### Improved

  - GRP-513: allow a filter on membership lite subject finder on add member and import for subject source

### Fixed

  - GRP-504: ldappcng subtree searches for subjects
  - GRP-512: grouper lite ui shows error page sometimes which is not descriptive
  - GRP-519: Security issue with user audit logs in UI


## [1.6.2] - 2010-10-15

[https://spaces.at.internet2.edu/display/Grouper/v1.6+Release+Notes]()


### Added

  - GRP-496: allow a filter on membership lite subject finder on add member and import for group name

### Improved

  - GRP-441: allow GSH to support newlines
  - GRP-493: Schema files should reference classpath
  - GRP-500: Improve wording of ldappcng's -interval option to indicate delay between recurring executions

### Fixed

  - GRP-499: ldappc-ng's optional -lastModifyTime option incorrectly deletes groups
  - GRP-502: attribute assignments might not work if assignable to more than one type of owner object


## [1.6.1] - 2010-08-14

[https://spaces.at.internet2.edu/display/Grouper/v1.6+Release+Notes]()


### Added

  - GRP-475: import/export should support stem or certain groups

### Improved

  - GRP-452: improve error handling of loader so it is easy to tell which job has the problem
  - GRP-453: unresolvable subject deletion utility should filter subjects that are used as members
  - GRP-454: subject api search on every entity or grouper sys admin should search by substring like other sources
  - GRP-457: grouper ui should default to "explore" view
  - GRP-458: Manage Groups in UI should perform well when user is admin of many groups
  - GRP-461: find groups should not exception if parent stem not found, it should just not find it, right?
  - GRP-462: When adding a group as a member of another, avoid doing an unnecessary composite related query if the group is not a factor or a member of a factor.
  - GRP-466: can we put the username using UI/WS in log message from threadlocal?
  - GRP-469: allow WS status servlet to ensure certain groups have a minimum number of members
  - GRP-471: log the nextException in the DB layer (e.g. for postgres)
  - GRP-473: grouper client xmpp client should support managing incremental groups where the group list is not known at startup
  - GRP-474: allow configurable fields to show on simple membership update screen by default, and which in the details view
  - GRP-477: Batch flat membership updates
  - GRP-479: composite groups have a uuid of the owner group

### Fixed

  - GRP-429: grouper report should summarize so it is not too large...
  - GRP-459: postgres might not drop all views, we should check the order here or something...  (cascade will drop dependent views)
  - GRP-460: grouper client api gcstemdelete has stem lookup named addGroupLookup
  - GRP-464: grouper ws get groups lite in certain circumstances returns no data
  - GRP-465: loader security groups sometimes cant be found
  - GRP-468: fix grouper_rpt_stems_v, roles_v, groups_v,  to take advantage of new grouper_groups table
  - GRP-472: If sending multiple attributes to XMPP in ESB connector you get ArrayIndexOutOfBoundsException
  - GRP-476: Don't flush on every row received in query
  - GRP-478: hibernate layer should not log errors, should inject useful information in the exception and rethrow
  - GRP-481: subject picker didnt have grouper session which causes intermittent failures
  - GRP-483: UI gives error when displaying memberships of a Composite


## [1.6.0] - 2010-07-24

[https://spaces.at.internet2.edu/display/Grouper/v1.6+Release+Notes]()


### New Features

  - Web service enhancements: Web services now support the new attribute and permission frameworks, subject service, new options for existing operations.
  - Kuali Rice integration: Connector for Kuali Rice to communicate with Grouper, can delegate Group decisions to Grouper and facilitate workflow in Grouper actions.
  - New Ldappc: A completely new Ldappc, Ldappc-ng, is included in the Grouper Toolkit in addition to the original Ldappc. Ldappc-ng uses SPML for provisioning and builds on Shibboleth's attribute resolver technology. The new attribute framework and permissions can be provisioned to LDAP with Ldappc-ng.
  - ESB integration: Grouper can provision group and folder information to and read in changes from an ESB using HTTPS or XMPP.
  - Subject picker: External applications can use the Grouper subject picker UI component to pick people.
  - Lite UI skinning: Lite UI can look like application that launches it, and can have customized text and instructions.
  - Flattened memberships: A new, additional flattened memberships table improves notification performance and lays the foundation for further read performance enhancement through the Grouper Toolkit.


### Improvements & Fixes

  - UI sorting and paging of memberships: The effective membership list was improved to sort and page correctly
  - Composite groups and memberships: Ldappc had trouble provisioning include/exclude groups
  - Grouper UI access control lists: Grouper UI ACL's were straightened out
  - Ldappc-ng is its own project: Separates lots of jars from main Grouper API
  - XML Import/Export improvements: XML import/export uses less memory, is faster, and 100% of the Groups Registry is exported and imported.

Many other fixes and improvements were also made to all components of the Grouper Toolkit: Grouper API, Administrative & Lite UIs,
Grouper Web Services, Grouper Client, Grouper Shell, Grouper Loader, Ldappc, Ldappc-ng, and the Subject API.


### Added

  - GRP-72: Allow sites to disable 'editing' of groups (+- attributes, members and privs) in the UI
  - GRP-242: add multiple privilege management web service
  - GRP-315: filter web service results by source
  - GRP-340: Would like to be able to provision the group read and view privs of a group to the directory
  - GRP-344: Provision list attributes using Ldappc
  - GRP-356: add non immediate membership query type
  - GRP-357: query members by source(s)
  - GRP-358: add ability to call client with group id's (instead of name)
  - GRP-359: allow ability to get groups for a member based on stem
  - GRP-362: allow multi-free form values on attributes
  - GRP-363: add attribute type schemas where one attribute allows others to be assigned
  - GRP-364: allow the new attribute framework to be reflected in ldap
  - GRP-367: allow findGroups in WS and client to accept a list of group names or uuids
  - GRP-369: add grouper getMemberships service (batched and lite) in api/ws/client
  - GRP-372: get subject service
  - GRP-376: add subject picker ui component
  - GRP-381: add skinning to Grouper membership UI lite
  - GRP-385: add grouper change log view
  - GRP-390: allow virtual attributes for subjects
  - GRP-411: add status check for grouper ws
  - GRP-413: Flattened memberships
  - GRP-415: add assignAttributes web service and client call
  - GRP-416: get permission assignments web service
  - GRP-417: assign permissions web service
  - GRP-420: add getAttributes ws and client call
  - GRP-423: add lightweight membership view on flattened table
  - GRP-424: add attribute framework scoping
  - GRP-428: add loader type for attribute definitions
  - GRP-431: Merge ESB Connector code into trunk
  - GRP-433: add resource picker
  - GRP-439: add enabled/disabled dates to addMember WS and client
  - GRP-448: kuali rice integration
  - GRP-449: xmpp integration with esb connector in grouper client

### Improved

  - GRP-55: Enhance API so that Groups can return a Set of Members / Memberships with a 'count' for how many routes each Subject is a member
  - GRP-190: migrate grouper to svn
  - GRP-293: Support provisioning eduCourseMember
  - GRP-351: export/import should handle all new fields for 1.5 and uuids
  - GRP-352: attribute-mapping for stems?
  - GRP-360: add ability to remove all members of a group via grouper client
  - GRP-370: saveGroup and saveStem web service and client call should allow createParentStemsIfNotExist
  - GRP-374: allow configurable subject checking on check config
  - GRP-377: grouper ui access control lists
  - GRP-378: allow a setting for the default "delete multiple" on membership lite ui
  - GRP-412: have a switch on has_member web service so that subject_not_found returns success and not a member
  - GRP-422: make ldappcng its own project
  - GRP-438: we need a "replace" operation on attributes and permissions
  - GRP-440: add "replace" as operation in attribute framework and permissions WS and client
  - GRP-444: indexes on all 8 string cols in audit table
  - GRP-455: write Grouper XML export/import


### Fixed

  - GRP-386: change log index should be in separate table
  - GRP-402: versions in grouper ws should be more flexible
  - GRP-409: [ldappc] includeExclude _includes and _systemOfRecord provisioning
  - GRP-414: Changes in change log entries for memberships and privileges
  - GRP-418: Member.getGroups(field) assumes default members list
  - GRP-421: attributeassign column for enabled time is wrong type
  - GRP-425: Rolesets not added when groups turned into roles
  - GRP-426: paging does not work correct with groups and stems
  - GRP-430: id and membershipType unavailable on MEMERSHIP_ADD and MEMBERSHIP_DELETE changeLog events
  - GRP-434: UI paging when browsing hierarchy does not work properly
  - GRP-435: Paging not working for effective membership lists where one or more members are members by more than one path
  - GRP-436: large person descriptions make the admin link not usable
  - GRP-445: lite ui doesnt display internal subjects correctly
  - GRP-446: postgres transaction rollback fails unless connection.rollback
  - GRP-447: Group membership paging  and exporting incorrect
  - GRP-451: grouper type and attribute security via grouper.properties only works with grouperIncludeExclude.requireGroups.use=true


## [1.5.3] - 2010-04-02

[https://spaces.at.internet2.edu/display/Grouper/v1.5+Release+Notes]()

[https://spaces.at.internet2.edu/display/Grouper/Archives]()


### Added

  - GRP-406: add a readonly mode for grouper

### Improved

 - GRP-408: put the apache license file in each grouper jar

### Fixed

  - GRP-401: ui lite cant search for groups when non wheel
  - GRP-404: [ldappc] stem OUs deeper than one level are not created correctly
  - GRP-405: grouper export (original version) fails on non ascii chars
  - GRP-407: Grouper Loader consumer name may be mangled by GrouperUtil.stripStart, leading to ClassNotFoundException


## [1.5.2] - 2010-03-12

[https://spaces.at.internet2.edu/display/Grouper/v1.5+Release+Notes]()

[https://spaces.at.internet2.edu/display/Grouper/Archives]()


### Improved

  - GRP-379: add membership lite textarea option
  - GRP-382: import of simple ui should not require header if one col
  - GRP-384: support sql server
  - GRP-387: add progress and batch improvements to group set function
  - GRP-388: import export should not be on by default in admin ui

### Fixed

  - GRP-380: the simple UI will get confused if two windows or tabs of the same browser are editing different groups
  - GRP-389: null pointer on subject attribute


## [1.5.1] - 2010-01-22

[https://spaces.at.internet2.edu/display/Grouper/v1.5+Release+Notes]()


### Fixed

  - GRP-368: Group and stem descriptions were not properly handled during an xml import.
  - GRP-373: find by subject id or identifier has bad logic
  - GRP-371: duration micros database size does not hold more than 30 minutes


## [1.5.0] - 2009-12-16

[https://spaces.at.internet2.edu/display/Grouper/v1.5.0+Software]()

[https://spaces.at.internet2.edu/display/Grouper/v1.5+Release+Notes]()


### New Features

  - Lite UI: An AJAX-based widget simplifies some end user interactions. Available in two ways: free-standing, or integrated within the Administrative UI.
  - Audit: Who took which management actions when is recorded and made available for viewing and reporting.
  - Move and Copy: Move or copy groups and folders to other folders, with the option to preserve old group names so that applications may continue to refer to the old name.
  - Notification: Real-time notification of group, folder, membership, and privilege changes are available through the Grouper API.
  - Attribute framework: Assign custom attributes to groups, memberships, folders, and other attributes.
  - Roles and Permissions: Support for Roles and Role hierarchies. Permissions can be attached to Roles or to Memberships in Roles.
  - Shibboleth integration: The integration of Shibboleth's Attribute Resolver within LDAPPC provides substantial attribute calculation capabilities. Also, memberships and group attributes can be accessed directly by a Shibboleth IdP.

### Improvements and Fixes

  - Performance  - A fundamental change to Grouper's underlying relational schema makes it far faster at write operations. Write time now is nearly independent of the number of indirect memberships involved.
  - Bad membership fix  - Schema change that makes it impossible for spurious "bad memberships" to occur in direct or indirect memberships.
  - Membership enable and disable dates  - Membership assignments can have enabled/disabled dates where the membership might be enabled in the future, or disabled after a certain period of time.
  - LDAPPC  - Improved performance, configurability, and integration with Active Directory, as well as several other enhancements and fixes.
  - Administrative UI  - Updated to enable AJAX support, enable clustering, and several other fixes and enhancements.


### Added

  - GRP-213: Namespace Transition
  - GRP-231: make a lightweight membership view
  - GRP-235: should we consider adding gsh to ws and ui?  maybe optionally?
  - GRP-238: Add configuration option to restrict users that can rename stems
  - GRP-255: add in hibernate versioning
  - GRP-267: Add 'Folder workspace' to complement Entity and Group workspaces
  - GRP-284: add enabled/disabled dates to memberships
  - GRP-288: grouper now requires DB views
  - GRP-307: Make the audit log queryable in the UI
  - GRP-325: calculate provisioning command line option
  - GRP-326: calculate provisioning changes (dry-run) command line option
  - GRP-327: log provisioning changes command line option
  - GRP-328: configuration file macros, properties file command line option
  - GRP-329: initial integration with shibboleth attribute resolver
  - GRP-330: provision multiple objectclasses
  - GRP-331: allow multiple target objects to be provisioned per subject
  - GRP-332: configure behavior when a subject can not be found
  - GRP-333: "g:gsa" source-subject-identifier not necessary nor allowed
  - GRP-334: bundle attribute modifications option
  - GRP-335: integration with vt-ldap 3.2
  - GRP-346: ldappc supports more advanced filters when selecting groups to be provisioned
  - GRP-406: add a readonly mode for grouper

### Improved

  - GRP-15: Alter query filter workflow to restrict by scope before searching
  - GRP-201: move built in group attributes from the attributes table to the groups table
  - GRP-212: expand two grouper_memberships cols so we can add foreign keys
  - GRP-217: make gsh easier to use by having subjectId calls (like grantPriv), use SubjectFinder.findByIdOrIdentifier
  - GRP-248: improve exception handling and finders
  - GRP-251: improve organization of junit tests
  - GRP-275: ldappc must be run twice to correctly provision groups whose members include other groups
  - GRP-292: Remove effective memberships out of grouper_memberships
  - GRP-309: Update UI to use the latest servlet API and JSTL
  - GRP-310: Make UI clusterable by not putting anything in the session which is not serializable
  - GRP-319: update build process with ldappc shibboleth and spring integration
  - GRP-337: see if web service subject name can go to a name field in the wsSubject instead of attribute
  - GRP-339: LDAPpc should not search LDAP for groups that are not in LDAP
  - GRP-341: Imporove LDAPpc's log output usefulness
  - GRP-353: make grouper ws build process consistent with grouper and grouperUi

## Fixed

  - GRP-214: group disappears temporarily from list
  - GRP-224: FindByOwnerAndMemberAndFieldAndType should not throw an error if there are duplicates
  - GRP-243: UI incorrectly exposes privilege management options
  - GRP-244: Unresolvable subjects cause errors in the UI
  - GRP-252: fix p6spy so it doesnt depend on oracle
  - GRP-254: Group deletes do not add potential new memberships
  - GRP-295: group gets lost in ui on membership delete
  - GRP-300: LDAPPC configuration schema incorrectly requires grouper-attribute
  - GRP-301: When searching for new members to add to a group, the links to page results do not maintain groupId
  - GRP-303: null pointer exception in FieldFinder line 177
  - GRP-304:  LDAPpc scanning LDAP for non-provisioned groups
  - GRP-306: Users performing a subject search can see groups for which they do not have vioew access
  - GRP-311: grouper client cant find config file if dir has special chars (e.g. space)
  - GRP-312: Group deletions do not delete naming privileges where the group is the member
  - GRP-318: grouper WS and client does not do international characters well
  - GRP-320: subject API returns too many results and causes errors
  - GRP-321: change uuid length in db from 128 to 40
  - GRP-324: ui-lite doesnt work with IE8
  - GRP-336: [ldappc] source-subject-name-map failed to lookup subject-attribute="id|name"
  - GRP-342: Error when synchronizing a group name containing a forward slash '/'.
  - GRP-345: add member WS and delete member right after might not work for unit tests (caching)
  - GRP-347: compile warning about base64
  - GRP-350: handle pound sign and question mark in url for lite ui
  - GRP-405: grouper export (original version) fails on non ascii chars
  - GRP-47: Do not permit creation of multiple identical membership paths
  - GRP-79: HibernateMembershipDAO  GrouperDAOException: query did not return a unique result: 2


## [1.4.2] - 2009-06-05

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

[https://spaces.at.internet2.edu/display/Grouper/v1.4+Release+Notes]()


  - Added: Ability to manage hierarchical structures with grouper loader (e.g. org lists)
  - Added: New JDBC source adapter with better searching
  - Fixed: Prevent duplicate group names
  - Fixed: Various ldappc, web service, and API bugs
  - Improved: Better UI performance for large resultsets
  - Improved: Better caching in the API
  - Improved: Better privilege web service operations

### Added

  - GRP-221: add a field view
  - GRP-259: add more options for getGrouperPrivileges
  - GRP-273: org hierarchies in grouper
  - GRP-274: take dashes out of uuids
  - GRP-276: add ability to manage privileges of loader managed group_list groups
  - GRP-279: loader improvements for orgs
  - GRP-280: add new jdbc subject source adapter which allows better seaching

### Improved

  - GRP-206: cache web service login credentials based on hash
  - GRP-228: modifyTime and modifierUuid for groups and stems when memberships get updated
  - GRP-230: improve how saveGroup web service works
  - GRP-261: web service kerberos authentication optimization
  - GRP-264: group delete was not in transaction, can cause loss of privileges without deleting group
  - GRP-268: performance improvements
  - GRP-269: put grouper ui source and classes into grouper-ui.jar
  - GRP-271: Extend repository browser configuration to allow flat mode to be the default
  - GRP-277: subject search should sort subjectId or attribute matches to the top in unsorted list
  - GRP-281: improve groupList loader time by loading registry memberships in one query
  - GRP-283: enable caching for things like stem and group searches by uuid

### Fixed

  - GRP-219: addType doesnt work in postInsert group hook
  - GRP-220: group.addType in a postInsertCommit hook needs a new transaction and explicit commit
  - GRP-222: seeing if a type is available causes queries
  - GRP-225: dbVersion does not work with lazy loaded attributes
  - GRP-226: subject attributes not handled consistently
  - GRP-227: ldappc incorrectly replaces attribute values after incomplete provisioning
  - GRP-229: Ldappc will not remove membership for a deleted group correctly if the member has other provisioned memberships.
  - GRP-232: add getGroups by field to WS and API
  - GRP-233: save group in UI erases hook actions when hook adds type
  - GRP-245: grouper query AND and OR are backwards
  - GRP-246: cant see my memberships even though a member and can view the group
  - GRP-249: endless loop on privilege checking
  - GRP-253: null pointer on priv operation with no results
  - GRP-260: field caching should cache better
  - GRP-262: Lose ability to select privileges when assigning privileges after browsing
  - GRP-263: allow slashes in grouper client and web services
  - GRP-265: UI does not enforce role in auth-constraint
  - GRP-266: null subject attribute values are not returned
  - GRP-270: Session already invalidated exception on logging out
  - GRP-278: should not be able to have two groups with the same name
  - GRP-294: null pointer in grouper query
  - GRP-300: LDAPPC configuration schema incorrectly requires grouper-attribute


## [1.4.1] - 2009-02-02

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

[https://spaces.at.internet2.edu/display/Grouper/v1.4+Release+Notes]()


  - Improved: Ldappc is included in the Grouper API and is launched via Grouper Shell
  - Improved: WS: replacing existing members and actAs
  - Improved: Prevent duplicate stem names
  - Fixed: Replace hsql file mode with server mode in default grouper configs


### Added

  - GRP-205: allow more flexibility in SSL in grouper client
  - GRP-211: allow custom grouper client operations

### Improved

  - GRP-209: make actAs in WS more usable
  - GRP-210: make stem name index unique
  - GRP-215: make startup easier

### Fixed

  - GRP-203: add fail xml import on subject not unique exception option
  - GRP-204: can create multiple stems of the same name with web services
  - GRP-207: grouper-ws addMember replaceAllExisting will remove existing members
  - GRP-208: error saving group with pre 1.4 client
  - GRP-216: grouper doesnt work on mysql unix with case sensitive table names
  - GRP-218: hsql in file mode doesnt work well with unit tests


## [1.4.0] - 2009-01-04

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

[https://spaces.at.internet2.edu/display/Grouper/v1.4+Release+Notes]()


  - Added: A binary form of the Grouper Toolkit.
  - Fixed: Circumstances in which composites and circular membership loops could produce incorrect membership information.
  - Improved: API internal design and packaging of classes.
  - Improved: Faster, more efficient Grouper relational database schema.
  - Improved: Grouper Shell, a comprehensive command line interface to Grouper and its allied utilities, is now a core part of the Grouper distribution. And there are correspondingly fewer ant targets in the source distribution.
  - Improved: Support for group details and privileges via Grouper Web Services.
  - Improved: Transaction support to ensure relational consistency of logical group operations.
  - Improved: UI tooltips for group types and custom attributes.
  - Improved: XML import and export capabilities are incorporated into Grouper Shell.
  - New: Ability to merge two Members formerly thought to be distinct.
  - New: Automatically maintain groups and memberships from external sources with Grouper Loader.
  - New: Configuration checking, daily health report, views, and other diagnostic aids.
  - New: Configuration directives that provide more granular system administration and security.
  - New: Group type to automatically create include & exclude groups.
  - New: Programmatic hooks for local extensions to Grouper.
  - New: Regex-style validation of group attributes.


### Added

  - GRP-101: Add views and comments on db columns
  - GRP-102: validate grouper setup on startup and give detailed error messages if problems
  - GRP-116: gsh transaction support
  - GRP-117: gsh scope
  - GRP-122: keep passwords encrypted in external files to the config files
  - GRP-137: allow state of DB of object to be stored and compared
  - GRP-142: allow group attribute validation via regex, configured in grouper.properties
  - GRP-143: grouper hooks
  - GRP-149: load / manage groups via sql
  - GRP-151: add ability to merge to members
  - GRP-172: add binary build target in grouper api
  - GRP-178: add a grouperIncludeExclude and requireGroups type and hook to auto-create groups
  - GRP-179: add privileges read/add/delete web services on groups / stems
  - GRP-180: add group detail web service
  - GRP-183: add web service support for group detail (types, attributes, composites)
  - GRP-184: grouper daily report
  - GRP-186: add built-in hooks to secure who can edit types and attributes (based on type of attribute)
  - GRP-193: add grouper client
  - GRP-195: add group metadata query to grouper loader when using group list type

### Improved

  - GRP-98: remove the unique id cols in many tables, in favor of keeping the uuid col
  - GRP-105: dont delegate error or debug logging anymore so it doesnt hide caller class/method/line#
  - GRP-119: allow tooltips on group types and attributes
  - GRP-132: improve the way ddl and database upgrades work
  - GRP-133: get rid of home made cache, and use ehcache.  improve with GrouperCache
  - GRP-134: remove grouper session reference from business objects
  - GRP-135: remove DTO's from the hibernate data layer
  - GRP-140: setters on business objects should not have database side effects
  - GRP-141: remove grouper ant target dist.lib which makes the aggregate grouper-lib.jar
  - GRP-144: repackage grouper so that there arent so many classes in one package
  - GRP-145: move gsh, loader, usdu into the grouper source tree
  - GRP-147: future and function of grouper_sessions table
  - GRP-153: if "id" is the subject field to show on the subject results, then dont load all subject data
  - GRP-155: normalize the grouper_fields table
  - GRP-159: init db on each grouper startup, remove ant task db.init
  - GRP-163: Membership changes should not delete and re-add other immediate memberships
  - GRP-164: Update equality checks to use the business key rather than the hibernate id
  - GRP-169: remeove columns group.modify_source, create_source, and stem.modify_source and create_source
  - GRP-170: add end to end help text in UI
  - GRP-175: move xmlImport and xmlExport to GSH from ant
  - GRP-177: upgrade from apache axis2 1.3 to axis2 1.4.1

### Fixed

  - GRP-120: exception when removing GrouperAll user
  - GRP-125: wrong mime type of page is sent for user error?
  - GRP-156: Issues with membership deletes
  - GRP-165: add to group workspace button doesnt work after creating a group
  - GRP-166: Import of an export from Grouper gave error due to character encoding issues
  - GRP-168: Circular Memberships Cause Incorrect Effective Memberships
  - GRP-171: Incorrect setting for default.browse.stem gives undescriptive error
  - GRP-173: Composites don't work correctly when type is intersection or complement and factors have memberships with a depth greater than 0
  - GRP-174: Re-importing repository fragments to a new location does not behave as expected
  - GRP-176: Exception when querying a Field
  - GRP-185: if a type is removed from a group, remove the related attributes
  - GRP-188: grouper UI search for entity allows to see groups without view privilege
  - GRP-191: /populateFindNewMembers.do press 'assign privileges' bombs on nothing selected
  - GRP-194: ui caches session stuff, but it should refresh on cache miss
  - GRP-197: Grouper db initialization fails due to MySQL max key length issue
  - GRP-198: Go to advanced search for ANY group privileges from the subject summary page gives error after doing a search
  - GRP-199: can add a VIEW only group to another as member, thus seeing its members
  - GRP-200: Xml import does not  process import.metadata.group-type-attributes property correctly


## [1.3.1] - 2008-09-22

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

[https://spaces.at.internet2.edu/display/Grouper/v1.3-Release+Notes]()


  - Some circumstances in which memberships were "orphaned"
  - Several UI bugs
  - Memberships of unresolvable Subjects could not be deleted


### Added

  - GRP-154: Allow multiple messages, including warning messages

### Improved

  - GRP-127: ui should give descriptive error when putting in blank value for required group attribute

### Fixed

  - GRP-114: The grouper_memberships.parent_membership column does not always get populated correctly
  - GRP-126: error has no nav.properties value: error.group-members.missing-grouporstem-id
  - GRP-129: Clicking 'list my groups' when browsing 'My memberships' causes the breadcrumb location to disappear when you then click 'Explore
  - GRP-130: Inappropriate caching of Members in Hib3MemberDAO requires JVM restart to pick up changes to database from other JVM
  - GRP-131: JSP Tile definition exception if  no insertFragment in pageContext
  - GRP-138: property file entries should have whitespace trimmed off the end
  - GRP-139: XmlExporter fails if a subject cannot be resolved and gives truncated output
  - GRP-157: UI incorrectly shows 'Create group' link for root stem
  - GRP-158: allow delete membership of unresolvable subject
  - GRP-160: Indirect privileges for GrouperAll (Every Entity) are not displayed and link is 'broken'
  - GRP-161: GrouperAll appears in text rather than EveryEntity + cannot search for EveryEntity
  - GRP-162: ADMIN privilege omitted when displaying indirect privileges


## [1.3.0] - 2008-05-22

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

[https://spaces.at.internet2.edu/display/Grouper/v1.3-Release+Notes]()


  - Added: ability to disable editing of group attributes / member lists for site configured groups i.e. groups which should be loader maintained.
  - Added: additional error checking in the UI - whether key properties are set and added option to build the generated client when building the web service.
  - Added: compiled Java in all components includes debug information, which means that stack traces in log files will have line numbers which makes it easier to debug problems.
  - Added: example kerberos authentication configuration for the UI.
  - Added: exception handling and logging to the UI.
  - Added: foreign keys to database.
  - Added: gsh source to the Internet2 CVS repository and added a page to the Wiki.
  - Added: jsr107cache-1.0.jar - required by default on Solaris.
  - Fixed: Advanced Search link for groups in the UI.
  - Fixed: gsh.bat - was not correctly building the classpath.
  - Fixed: some CSS issues with IE6.
  - Improved: Hibernate support. Upgraded to Hibernate 3.2.6 and added transaction support Hibernate 2.x support is no longer included.
  - Improved: changed the XHTML declaration to use transitional DTD rather than strict - reduces number of errors until we can try to tidy up issues.
  - Improved: gsh error reporting. Exceptions from Grouper should now be summarised in gsh output and full stack traces writen to grouper_error.log.
  - Improved: new settings in grouper.properties make it possible to define user / db connection urls which can / cannot have their schemas rebuilt. If not configured the test script will prompt the user to confirm that it is OK to drop a schema. This makes it more difficult to accidentally lose data.
  - Improved: performance.
  - Improved: transaction handling code will attempt to inject additional information into a caught Exception. If successful the Exception is not logged - assumes that your code will handle logging.
  - Improved: user interface.
  - New: applied transactions to the UI so that a whole action succeeds and is committed, or fails and is rolled back.
  - New: experimental web services interface.
  - New: extension for removing memberships for unresolvable subjects.


### Added

  - GRP-88: add saveGroup method to the Group class, remove warnings, improve exceptions in Stem class
  - GRP-99: add transactions and inverse of control to grouper
  - GRP-100: add web services to grouper

### Improved

  - GRP-103: grouper ui improvements
  - GRP-104: Apply new database transactions to UI so that UI operations become atomic
  - GRP-108: Enable logout link by default
  - GRP-115: prevent deleting important data
  - GRP-13: Poor integration with Ldappc: attribute-matching-queries generates full table scan
  - GRP-70: Sub-optimal index for grouper_composites
  - GRP-73: Improve error checking and logging
  - GRP-77: Improve membership listing speed by caching groups associated with Memberships
  - GRP-78: Improve apparent performance when listing privilegees for stems/groups
  - GRP-89: Add Foreign Keys
  - GRP-9: Group attribute queries generate full table scans
  - GRP-91: tune the grouper subject tables
  - GRP-92: Allow alternative implementation classes to be configured for ObjectAsMaps
  - GRP-93: For performance reasons allow case-sensitive sorting
  - GRP-97: upgrade to hibernate3, remove hibernate2

### Fixed

  - GRP-107: populateSubjectSummary generates an error if the subjectId provided is not unique across sources
  - GRP-109: Clicking cancel button after following a link from Group Members page after the scope change form has been submitted gives error
  - GRP-110: Incorrect detail shown when displaying how a Subject is a memebr of a composite group
  - GRP-111: XmlImporter does not keep creatorUuid for groups and stems
  - GRP-128: memory leak in grouper
  - GRP-14: Poor integration with Ldappc: Null DTO error message in Ldappc run causes run to fail
  - GRP-46: Support deleting group membership for subjects who are no longer in the directory
  - GRP-81: XmlImporter fails if a null attribute value is set
  - GRP-85: remove typos, and improve exception handling
  - GRP-87: error message is not shown correctly for creating groups/stems
  - GRP-90: UI does not handle situation when a user tries to create a group in a stem where the extension already exists
  - GRP-94: Class cast exception when hiding stem hierarchy and showing groups
  - GRP-96: grouper db-init doesnt work with mysql


## [1.2.1] - 2007-12-06

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

[https://spaces.at.internet2.edu/display/Grouper/v1.2.1+Release+Notes]()

Note: Start use of JIRA for issue tracking

  - Improved: API caching strategy
  - Improved: performance
  - New: API and UI use new strategies to check privileges


### Added

  - GRP-44: List all ACLs on a group, either direct or inherited
  - GRP-59: Allow default group search to search any group attribute, not just name as at present
  - GRP-60: Allow greater control when searching for subjects i.e. specify subject attributes to search

### Improved

  - GRP-10: Modify Membership Indices
  - GRP-19: Refactor privilege resolution
  - GRP-21: Add Group attribute caching
  - GRP-22: Excessive Group member queries
  - GRP-25: Eliminate references to @HEAD@ before release
  - GRP-30: Optimize query logic in "PrivilegeHelper#can*()" methods
  - GRP-31: Kill "internal.cache.SimpleCache" and "internal.util.SimpleBooleanCache"
  - GRP-32: Add Hibernate3 support
  - GRP-37: Performance of PopulateGroupMembersAction
  - GRP-39: Provide abiity to control sort order of subject attributes listed on Subject Summary page
  - GRP-42: Allow users and groups to be sorted separately in List Members page
  - GRP-43: Find New Members page should default to 'member' privilege
  - GRP-48: Performance of GroupAttributeFilter.getResults()
  - GRP-50: Update Grouper 1.2.1 schema documentatin
  - GRP-52: Interface to conditionally veto UI menu items
  - GRP-54: Filtering by Member Searches
  - GRP-57: Repetitive calls to Member.hasXXX when skipping stems causes slow response times
  - GRP-62: Reduce cost of instantiating ObjectAsMap wrappers
  - GRP-63: In addition to allowing import from a text file, allow user to type / paste data into a textarea
  - GRP-64: Ensure UI always ask sfor confirmation before deleting objects

### Fixed

  - GRP-11: StemFinder#internal_isChild generates spurious error logging
  - GRP-16: Can't update member privilege on self
  - GRP-24: Intermittent test failure: Test_uc_WheelGroup.test_fromNotAMemberOfTheWheelGroupToAMemberOfTheWheelGroup
  - GRP-34: Error initializing Oracle database with Grouper 1.2.1
  - GRP-36: Infinite Loop on GroupFinder.findByName() while using wheel group
  - GRP-38: Do not list nameless groups for which no View privilege is granted
  - GRP-40: Subject Summary Page does not display modify or create time or subject values
  - GRP-41: Deleting the last member from a group using Remove selected members button creates a java excption
  - GRP-58: Incorrect default search logic would cause two API searches when only one was configured
  - GRP-65: With 1.2.1 rc1 the UI is no longer correctly indicating direct vs indirect privileges
  - GRP-67: Missing 'active' Wheel group breaks Grouper
  - GRP-68: Finding groups and displaying privileges for a wheel group Subject gives exception when clicking to view how privileges are derived
  - GRP-69: Some JUnit tests fail



## [1.2.0] - 2007-07-18

[https://spaces.at.internet2.edu/display/Grouper/Archives]()


  - Added: new, experimental extension frameworkto ease integration of code external to the core API by defining a common build process and means of referring to the Grouper installation.
  - Added: search for groups by their Type and other more advanced group and stem searching capabilities.
  - Added: sorting of groups, stems, and subjects when browsing or searching in the UI.
  - Added: stems can be renamed.
  - Fixed: several critical membership and XML export/import bugs.
  - Improved: control over attributes used to display groups, stems, and subjects in the UI.
  - Improved: updated the Subject API and GrouperShell.
  - Includes updated versions of the Subject API and gsh.


## [1.1] - 2006-11-30

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

Release v1.1 is mostly a maintenance release; however, there are a few functional and operational enhancements:

  - Added: Additional GrouperQuery filters, especially to support provisioning applications.
  - Added: Support for XML Import/Export & custom group types & fields to the GrouperShell command line shell.
  - Improved: Configurable Privilege and Subject caching regimens.
  - Improved: Query performance through caching.
  - Improved: UI support for removing many members from a group.


## [1.0] - 2006-07-20

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

The Grouper v1.0 UI embodies our first attempt to enable two tasks that are complicated in a UI context: managing composite groups, and managing custom group types and attributes. We've prepared a brief tutorial to help smooth your exploration of these new UI capabilities.

  - Added: Basic XML export-and-importof the Groups Registry.
  - Added: Custom Group Types, attributes and lists.
  - Added: Group Math.
  - Added: GrouperShell command line shell - gsh.
  - Improved: Query performance through caching.


## [0.9] - 2005-12-19

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

The v0.9 toolkit includes a full UI designed to be deployed to a java application server, java source implementing the Grouper 0.9 API, documentation for developers and implementers, and sample utilities.


  - Added: "Wheel" group whose members have root-like privileges within the API.
  - Added: New "all" subject that can be assigned memberships and granted privileges that map to all subjects identifiable through the Subject API.
  - Added: New query API that enables sites to add their own custom queries.
  - Improved: Enhanced UI support for management of effective memberships and privileges.
  - Improved: Enhanced UI support for searching.
  - Improved: Enhancements to the Access and Naming interfaces.
  - Improved: Logging capabilities.


## [0.6] - 2005-09-16

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

  - Added: Ability to delete groups containing members.
  - Added: Full implementations of the privilege interfaces.
  - Added: GrouperAccess.has().
  - Added: More verbose and precise error reporting via exceptions.
  - Added: Search by the name, extension, or displayName attributes of groups scoped to groups within a namespace subordinate to a given stem. [S&B#7]
  - Added: Search by the name, extension, or displayName attributes of groups. [S&B#1]
  - Added: Search by the name, extension, or displayName attributes of namespaces scoped to namespaces subordinate to a given stem. [S&B#8]
  - Added: Search by the name, extension, or displayName attributes of namespaces. [S&B#2]
  - Fixed: Proper schema validation. (#267)
  - Improved: Subject interface.
  - Improved: search and browse capabilities.



## [0.5.6] - 2005-04-29

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

  - Fixed: Effective memberships for non-"members" lists (e.g. access and naming privileges) were being calculated incorrectly. (#350)
  - Fixed: Non-root subjects could not create stems or groups. (#353)
  - Fixed: STEM, not ADMIN, needed to modify namespace attributes. (#352)
  - Fixed: Added NullGrouperAttribute class (which extends GrouperAttribute) to handle group attributes that either do not have values or have had their values deleted. (#356)
  - Fixed: GrouperMember.load(session, subject) replaces GrouperMember.load (subject). (#348)
  - Fixed: GrouperGroup.loadByID() now returns properly casted GrouperGroup objects. (#349)
  - Fixed: GrouperStem.loadByID() now returns properly casted GrouperStem objects. (#349)

## [0.5.5] - 2005-04-25

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

a.k.a 0.5.1?

  - Added: GrouperStem class for managing and representing namespaces.
  - Added: public methods to list all groups and stems within a given stem.
  - Improved: compatibility with Oracle.
  - Revised: Hibernate session and transaction handling code.
  - Revised: effective membership algorithm.


## [0.5] - 2004-12-09

[https://spaces.at.internet2.edu/display/Grouper/Archives]()

The API release v0.5 is the first release of Grouper:

  - Added: Creation, update, and removal of groups from the Groups Registry.
  - Added: Subgroups.
  - Added: Export capabilities.
  - Added: Limited querying.
  - Added: Graphical user interface for manual groups management.
  - Added: Logging.



<!-- These are broken because the tags aren't always on the same branch
[Unreleased]: https://github.com/Internet2/grouper/compare/GROUPER_2_4_0...HEAD
[2.4.0]: https://github.com/Internet2/grouper/compare/GROUPER_2_4_0...GROUPER_2_3_0
[2.3.0]: https://github.com/Internet2/grouper/compare/GROUPER_2_3_0...GROUPER_2_2_2
[2.2.2]: https://github.com/Internet2/grouper/compare/GROUPER_2_2_2...GROUPER_2_2_1
[2.2.1]: https://github.com/Internet2/grouper/compare/GROUPER_2_2_1...GROUPER_2_2_0
[2.2.0]: https://github.com/Internet2/grouper/compare/GROUPER_2_2_0...GROUPER_2_1_5
[2.1.5]: https://github.com/Internet2/grouper/compare/GROUPER_2_1_5...GROUPER_2_1_4
[2.1.4]: https://github.com/Internet2/grouper/compare/GROUPER_2_1_4...GROUPER_2_1_3
[2.1.3]: https://github.com/Internet2/grouper/compare/GROUPER_2_1_3...GROUPER_2_1_2
[2.1.2]: https://github.com/Internet2/grouper/compare/GROUPER_2_1_2...GROUPER_2_1_1
[2.1.1]: https://github.com/Internet2/grouper/compare/GROUPER_2_1_1...GROUPER_2_1_0
[2.1.0]: https://github.com/Internet2/grouper/compare/GROUPER_2_1_0...GROUPER_2_0_3
[2.0.3]: https://github.com/Internet2/grouper/compare/GROUPER_2_0_3...GROUPER_2_0_2
[2.0.2]: https://github.com/Internet2/grouper/compare/GROUPER_2_0_2...GROUPER_2_0_1
[2.0.1]: https://github.com/Internet2/grouper/compare/GROUPER_2_0_1...GROUPER_2_0_0
[2.0.0]: https://github.com/Internet2/grouper/compare/GROUPER_2_0_0...GROUPER_1_6_3
[1.6.3]: https://github.com/Internet2/grouper/compare/GROUPER_1_6_3...GROUPER_1_6_2
[1.6.2]: https://github.com/Internet2/grouper/compare/GROUPER_1_6_2...GROUPER_1_6_1
[1.6.1]: https://github.com/Internet2/grouper/compare/GROUPER_1_6_1...GROUPER_1_6_0
[1.6.0]: https://github.com/Internet2/grouper/compare/GROUPER_1_6_0...GROUPER_1_5_3
[1.5.3]: https://github.com/Internet2/grouper/compare/GROUPER_1_5_3...GROUPER_1_5_2
[1.5.2]: https://github.com/Internet2/grouper/compare/GROUPER_1_5_2...GROUPER_1_5_1
[1.5.1]: https://github.com/Internet2/grouper/compare/GROUPER_1_5_1...GROUPER_1_5_0
[1.5.0]: https://github.com/Internet2/grouper/compare/GROUPER_1_5_0...GROUPER_1_4_2
[1.4.2]: https://github.com/Internet2/grouper/compare/GROUPER_1_4_2...GROUPER_1_4_1
[1.4.1]: https://github.com/Internet2/grouper/compare/GROUPER_1_4_1...GROUPER_1_4_0
[1.4.0]: https://github.com/Internet2/grouper/compare/GROUPER_1_4_0...GROUPER_1_3_1
[1.3.1]: https://github.com/Internet2/grouper/compare/GROUPER_1_3_1...GROUPER_1_3_0
[1.3.0]: https://github.com/Internet2/grouper/compare/GROUPER_1_3_0...GROUPER_1_2_1
[1.2.1]: https://github.com/Internet2/grouper/compare/GROUPER_1_2_1...GROUPER_1_2_0
[1.2.0]: https://github.com/Internet2/grouper/compare/GROUPER_1_2_0...GROUPER_1_1
[1.1]: https://github.com/Internet2/grouper/compare/GROUPER_1_1...GROUPER_1_0
[1.0]: https://github.com/Internet2/grouper/compare/GROUPER_1_0...GROUPER_0_9
[0.9]: https://github.com/Internet2/grouper/compare/GROUPER_0_9...GROUPER_0_6
[0.6]: https://github.com/Internet2/grouper/compare/GROUPER_0_6...GROUPER_0_5_6
[0.5.6]: https://github.com/Internet2/grouper/compare/GROUPER_0_5_6...GROUPER_0_5_5
-->

<!-- ok to use, but JIRA-specific
[2.4.0]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.4.0+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.3.0]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.3.0+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.2.2]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.2.2+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.2.1]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.2.1+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.2.0]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.2.0+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.1.5]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.1.5+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.1.4]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.1.4+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.1.3]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.1.3+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.1.2]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.1.2+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.1.1]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.1.1+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.1.0]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.1.0+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.0.3]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.0.3+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.0.2]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.0.2+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.0.1]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.0.1+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[2.0.0]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+2.0.0+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.6.3]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.6.3+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.6.2]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.6.2+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.6.1]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.6.1+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.6.0]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.6.0+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.5.3]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.5.3+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.5.2]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.5.2+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.5.1]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.5.1+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.5.0]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.5.0+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.4.2]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.4.2+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.4.1]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.4.1+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.4.0]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.4.0+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.3.1]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.3.1+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.3.0]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.3.0+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.2.1]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.2.1+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.2.0]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.2.0+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.1]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.1+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[1.0]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+1.0+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[0.9]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+0.9+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[0.6]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+0.6+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
[0.5.6]: https://bugs.internet2.edu/jira/issues/?jql=project+=+GRP+AND+fixVersion+=+0.5.6+AND+status+=+Resolved+OR+status+=+Closed+AND+Resolution+in+Completed,+Done,+Fixed+ORDER+BY+issuetype+DESC,+priority+ASC
-->

[Unreleased]: https://spaces.at.internet2.edu/display/Grouper/Grouper+Product+Roadmap
[2.4.0]: https://spaces.at.internet2.edu/display/Grouper/v2.4+Release+Notes
[2.3.0]: https://spaces.at.internet2.edu/display/Grouper/v2.3+Release+Notes
[2.2.2]: https://spaces.at.internet2.edu/display/Grouper/v2.2+Release+Notes
[2.2.1]: https://spaces.at.internet2.edu/display/Grouper/v2.2+Release+Notes
[2.2.0]: https://spaces.at.internet2.edu/display/Grouper/v2.2+Release+Notes
[2.1.5]: https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes
[2.1.4]: https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes
[2.1.3]: https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes
[2.1.2]: https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes
[2.1.1]: https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes
[2.1.0]: https://spaces.at.internet2.edu/display/Grouper/v2.1+Release+Notes
[2.0.3]: https://spaces.at.internet2.edu/display/Grouper/v2.0+Release+Notes
[2.0.2]: https://spaces.at.internet2.edu/display/Grouper/v2.0+Release+Notes
[2.0.1]: https://spaces.at.internet2.edu/display/Grouper/v2.0+Release+Notes
[2.0.0]: https://spaces.at.internet2.edu/display/Grouper/v2.0+Release+Notes
[1.6.3]: https://spaces.at.internet2.edu/display/Grouper/v1.6+Release+Notes
[1.6.2]: https://spaces.at.internet2.edu/display/Grouper/v1.6+Release+Notes
[1.6.1]: https://spaces.at.internet2.edu/display/Grouper/v1.6+Release+Notes
[1.6.0]: https://spaces.at.internet2.edu/display/Grouper/v1.6+Release+Notes
[1.5.3]: https://spaces.at.internet2.edu/display/Grouper/v1.5+Release+Notes
[1.5.2]: https://spaces.at.internet2.edu/display/Grouper/v1.5+Release+Notes
[1.5.1]: https://spaces.at.internet2.edu/display/Grouper/v1.5+Release+Notes
[1.5.0]: https://spaces.at.internet2.edu/display/Grouper/v1.5+Release+Notes
[1.4.2]: https://spaces.at.internet2.edu/display/Grouper/v1.4+Release+Notes#v1.4ReleaseNotes-ReleaseNotesforGrouperv1.4.2
[1.4.1]: https://spaces.at.internet2.edu/display/Grouper/v1.4+Release+Notes#v1.4ReleaseNotes-ReleaseNotesforGrouperv1.4.1
[1.4.0]: https://spaces.at.internet2.edu/display/Grouper/v1.4+Release+Notes#v1.4ReleaseNotes-ReleaseNotesforGrouperv1.4.0
[1.3.1]: https://spaces.at.internet2.edu/display/Grouper/v1.3-Release+Notes
[1.3.0]: https://spaces.at.internet2.edu/display/Grouper/v1.3-Release+Notes
[1.2.1]: https://spaces.at.internet2.edu/display/Grouper/v1.2.1+Release+Notes
[1.2.0]: https://spaces.at.internet2.edu/display/Grouper/Archives
[1.1]: https://spaces.at.internet2.edu/display/Grouper/Archives
[1.0]: https://spaces.at.internet2.edu/display/Grouper/Archives
[0.9]: https://spaces.at.internet2.edu/display/Grouper/Archives
[0.6]: https://spaces.at.internet2.edu/display/Grouper/Archives
[0.5.6]: https://spaces.at.internet2.edu/display/Grouper/Archives
[0.5.5]: https://spaces.at.internet2.edu/display/Grouper/Archives
[0.5]: https://spaces.at.internet2.edu/display/Grouper/Archives
