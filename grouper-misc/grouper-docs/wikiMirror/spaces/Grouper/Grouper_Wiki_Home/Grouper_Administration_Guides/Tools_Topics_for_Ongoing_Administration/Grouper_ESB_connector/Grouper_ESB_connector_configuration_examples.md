---
title: "Grouper ESB connector configuration examples"
space: Grouper
pageId: 28673883
version: 10
lastUpdated: 2026-07-01T05:35:06.031Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28673883/Grouper+ESB+connector+configuration+examples
---

All configuration for the ESB link goes into grouper-loader.properties. You can have multiple instances of consumers and listeners, but be careful to keep ports, usernames etc. unique.

 Example 1:

 
```

# sample configuration to send MEMBERSHIP_DELETE & MEMBERSHIP_ADD events to one port on an
# ESB, and MEMBERSHIP_DELETE, MEMBERSHIP_ADD, GROUP_DELETE & GROUP_ADD events to another by running 2
# changelog consumers
# this configuration is an example for directories which require the user DN to be present
# in an attribute on the group, and the group DN to be present in an attribute on the user
# Novell eDirectory and Microsoft Active Directory both need this for proper operation
changeLog.consumer.httpTest.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.httpTest.elfilter = event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD'
changeLog.consumer.httpTest.publisher.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbHttpPublisher
changeLog.consumer.httpTest.publisher.url = http://localhost:4567
changeLog.consumer.httpTestGroup.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.httpTestGroup.elfilter = event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD || event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD'
changeLog.consumer.httpTestGroup.publisher.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbHttpPublisher
changeLog.consumer.httpTestGroup.publisher.url = http://localhost:4568

```

 Example 2:

 
```

# Sample conifiguration to sync to OpenLdap where only Group objects need to be changed. Note than
# in OpenLDAP a groupOfUnique names must have at lease one member, but this is not reuquired to
# resolve to the DN of a real object
changeLog.consumer.httpOpenLdapTest.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.httpOpenLdapTest.elfilter = event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD'
changeLog.consumer.httpOpenLdapTest.publisher.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbHttpPublisher
changeLog.consumer.httpOpenLdapTest.publisher.url = http://localhost:4568
# next line consists of a list of additional subject attributes to send with each event, these
# attributes must be provided from the source
changeLog.consumer.httpOpenLdapTest.publisher.addSubjectAttributes = cardiffidmanaffiliation

```

 Example 3:

 
```

# Sample config to run a server listening for incoming events from an ESB
esb.listeners.http.enable = false esb.listeners.http.port = 8443 esb.listeners.http.bindaddress = 127.0.0.1
esb.listeners.http.authConfigFile = /home/rob/Customers/Internet2/users
# ssl config parameters - see http://docs.codehaus.org/display/JETTY/How+to+configure+SSL for details
# of how to use certs in Jetty
esb.listeners.http.ssl.keystore = /home/rob/tests/keystore
esb.listeners.http.ssl.keyPassword = changeit
esb.listeners.http.ssl.trustStore = /home/rob/tests/keystore
esb.listeners.http.ssl.trustPassword = changeit
esb.listeners.http.ssl.password = changeit

```

 Example 4:

 
```

# Sample config for sending events from Grouper to a recipient on and XMPP (Jabber) server
changeLog.consumer.xmppTest.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.xmppTest.elfilter = event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD'
changeLog.consumer.xmppTest.publisher.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbXmppPublisher
changeLog.consumer.xmppTest.publisher.server = 10.0.0.147
changeLog.consumer.xmppTest.publisher.port = 5222
changeLog.consumer.xmppTest.publisher.username = groupersend
changeLog.consumer.xmppTest.publisher.password = groupersend
changeLog.consumer.xmppTest.publisher.recipient = rob@procopius
changeLog.consumer.xmppTest.publisher.addSubjectAttributes = cardiffidmanaffiliation

```

 Example 5:

 
```

# Sample config to run a client that will monitor an XMPP server for events (messages) sent
# from a certain address and process them
esb.lisenters.xmpp.enable = true esb.listeners.xmpp.server = 10.0.0.147
esb.listeners.xmpp.port = 5222 esb.listeners.xmpp.username = grouper
esb.listeners.xmpp.password = grouper esb.listeners.xmpp.sendername = rob@procopius

```
