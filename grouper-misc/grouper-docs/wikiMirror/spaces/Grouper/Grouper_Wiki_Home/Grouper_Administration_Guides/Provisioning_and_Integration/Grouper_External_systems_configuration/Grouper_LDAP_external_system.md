---
title: "Grouper LDAP external system"
space: Grouper
pageId: 28549394
version: 7
lastUpdated: 2026-07-01T05:42:06.477Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549394/Grouper+LDAP+external+system
---

Grouper LDAP connections are managed from the external system UI page (or from the underlying configs)

As a Grouper admin navigate to Miscellaneous → External systems

Here is an example

## Pool debug

Set this in log4j2.xml to see trace information on ldaptive pools

```
        <Logger name="org.ldaptive.pool" level="all" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>

```

This is a sample log message about the pool with default settings in Grouper v4

```
2024-05-03T19:01:30,376: [Thread-23] DEBUG AbstractConnectionPool.initialize(213) - [] - beginning pool initialization for [org.ldaptive.pool.BlockingConnectionPool@773912331::name=null, poolConfig=[org.ldaptive.pool.PoolConfig@586783321::minPoolSize=3, maxPoolSize=10, validateOnCheckIn=false, validateOnCheckOut=false, validatePeriodically=true, validatePeriod=PT30M, validateTimeout=PT5S], activator=null, passivator=null, validator=[org.ldaptive.pool.SearchValidator@364318632::searchRequest=[org.ldaptive.SearchRequest@-1135521983::baseDn=, searchFilter=[org.ldaptive.SearchFilter@1642584434::filter=(objectClass=*), parameters={}], returnAttributes=[1.1], searchScope=OBJECT, timeLimit=PT0S, sizeLimit=1, derefAliases=null, typesOnly=false, binaryAttributes=null, sortBehavior=UNORDERED, searchEntryHandlers=null, searchReferenceHandlers=null, controls=null, referralHandler=null, intermediateResponseHandlers=null]] pruneStrategy=[org.ldaptive.pool.IdlePruneStrategy@1180802597::prunePeriod=PT5M, idleTime=PT10M], connectOnCreate=true, connectionFactory=[org.ldaptive.DefaultConnectionFactory@383238211::provider=org.ldaptive.provider.jndi.JndiProvider@2238924d, config=[org.ldaptive.ConnectionConfig@671404430::ldapUrl=ldaps://server.school.edu:636, connectTimeout=null, responseTimeout=null, sslConfig=null, useSSL=false, useStartTLS=false, connectionInitializer=[org.ldaptive.BindConnectionInitializer@1250921659::bindDn=cn=whatever,ou=accts,dc=somewhere,dc=upenn,dc=edu, bindSaslConfig=null, bindControls=null], connectionStrategy=org.ldaptive.DefaultConnectionStrategy@4641bd85]], initialized=false, availableCount=0, activeCount=0]

```

## Pool issues GSH v2 template to reproduce

Sometimes networking components close idle tcp connections, e.g. the AWS network load balancer closes connections after 350 seconds. You can test this by having a GSH template that uses threads and does an ldap call. Run it in the UI. do not bounce the UI, and run it again.

GSH v2 template config:

```
grouperGshTemplate.ldapTest.defaultRunButtonFolderUuidOrName = test
grouperGshTemplate.ldapTest.displayErrorOutput = true
grouperGshTemplate.ldapTest.folderShowOnDescendants = certainFolders
grouperGshTemplate.ldapTest.folderShowType = certainFolders
grouperGshTemplate.ldapTest.folderUuidToShow = test
grouperGshTemplate.ldapTest.gshTemplate = //
grouperGshTemplate.ldapTest.moreActionsLabel = Ldap test
grouperGshTemplate.ldapTest.runAsType = GrouperSystem
grouperGshTemplate.ldapTest.runButtonGroupOrFolder = folder
grouperGshTemplate.ldapTest.securityRunType = wheel
grouperGshTemplate.ldapTest.showInMoreActions = true
grouperGshTemplate.ldapTest.showOnFolders = true
grouperGshTemplate.ldapTest.templateDescription = Ldap test
grouperGshTemplate.ldapTest.templateName = Ldap test
grouperGshTemplate.ldapTest.templateVersion = V2
```

GSH template source

```
import java.util.List;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;

public class Test89ldapTest extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    Thread[] threads = new Thread[10];
    
    for (int i=0;i<10;i++) {
      threads[i] = new Thread(new Runnable() {

        @Override
        public void run() {

          try {
            String[] attributesToRead = new String[2];
            attributesToRead[0] = "pennid";
            attributesToRead[1] = "pennRecoveryEmail";
  
            //  List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("iamLdapProd", "dc=penncommunity,dc=upenn,dc=edu", 
            //      LdapSearchScope.SUBTREE_SCOPE, "(&(pennid=*)(pennRecoveryEmail=*))", attributesToRead, null);
            
            List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("iamLdapProd", "dc=penncommunity,dc=upenn,dc=edu", 
                LdapSearchScope.SUBTREE_SCOPE, "(pennid=10021368)", attributesToRead, null);

            
            gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("Thread: " + Thread.currentThread().getName() + ", found: " + ldapEntries.size() + " results");
          } catch (Exception e) {
            gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("Thread: " + Thread.currentThread().getName() + ", " + e.getMessage() + 
                (e.getCause() != null ? (", " + e.getCause().getMessage()) : ""));
          }
        }
        
      });
      
      threads[i].start();
    }
    
    for (int i=0;i<10;i++) {
      try {
        threads[i].join();
      } catch (Exception e) {
        
      }
    }

    gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("Complete");
  }
}
```

Run it the first time (assuming LDAP hasnt been used recently)

Wait the amount of time that connections are closed, and run again

Now adjust the config (might need to bounce UI), and try the two calls again.

In this case solution 1 is adjusting the idle connection check and minimum pool size. Note the expiration time should be greater than the prune timer period, and the sum of those should be less than the idle timeout of the load balancer or firewall or whatever.

```
ldap.iamLdapProd.pruneTimerPeriod = 90000
ldap.iamLdapProd.expirationTime = 250000
ldap.iamLdapProd.minPoolSize = 0
ldap.iamLdapProd.customizePooling = true
```

See the connections get killed

```
2024-05-03T19:52:41,363: [Thread-24] DEBUG AbstractConnectionPool.prune(704) - [] - pruning available pool of size 10 for [org.ldaptive.pool.BlockingConnectionPool@152899303::name=null, poolConfig=[org.ldaptive.pool.PoolConfig@305986280::minPoolSize=0, maxPoolSize=10, validateOnCheckIn=false, validateOnCheckOut=false, validatePeriodically=true, validatePeriod=PT30M, validateTimeout=PT5S], activator=null, passivator=null, validator=[org.ldaptive.pool.SearchValidator@917530468::searchRequest=[org.ldaptive.SearchRequest@1434260343::baseDn=, searchFilter=[org.ldaptive.SearchFilter@1642584434::filter=(objectClass=*), parameters={}], returnAttributes=[1.1], searchScope=OBJECT, timeLimit=PT0S, sizeLimit=1, derefAliases=null, typesOnly=false, binaryAttributes=null, sortBehavior=UNORDERED, searchEntryHandlers=null, searchReferenceHandlers=null, controls=null, referralHandler=null, intermediateResponseHandlers=null]] pruneStrategy=[org.ldaptive.pool.IdlePruneStrategy@698402967::prunePeriod=PT1M30S, idleTime=PT4M10S], connectOnCreate=true, connectionFactory=[org.ldaptive.DefaultConnectionFactory@466059917::provider=org.ldaptive.provider.jndi.JndiProvider@75437c84, config=[org.ldaptive.ConnectionConfig@863272974::ldapUrl=ldaps://server.school.edu:636, connectTimeout=null, responseTimeout=null, sslConfig=null, useSSL=false, useStartTLS=false, connectionInitializer=[org.ldaptive.BindConnectionInitializer@1181402930::bindDn=cn=user,ou=serviceAccts,dc=server,dc=server,dc=edu, bindSaslConfig=null, bindControls=null], connectionStrategy=org.ldaptive.DefaultConnectionStrategy@619ef698]], initialized=true, availableCount=10, activeCount=0]
2024-05-03T19:52:41,363: [Thread-24] TRACE IdlePruneStrategy.prune(61) - [] - evaluating timestamp 2024-05-03T23:48:17.205438Z for connection org.ldaptive.pool.AbstractConnectionPool$DefaultPooledConnectionProxy@1810162d
2024-05-03T19:52:41,367: [Thread-24] TRACE AbstractConnectionPool.prune(713) - [] - destroyed connection: org.ldaptive.pool.AbstractConnectionPool$DefaultPooledConnectionProxy@1810162d

```

Solution 2 is to validate the connections on an interval that is less than the idle timeout of the networking component (e.g. load balancer).

```
ldap.iamLdapProd.validateTimerPeriod = 300000
ldap.iamLdapProd.validatePeriodically = true
ldap.iamLdapProd.customizePooling = true
```

By default the validation strategy will perform an object level rootDSE search for (objectClass=*) which is a lightweight operation. Log entries

```
2024-05-03T23:42:18,142: [Thread-27] TRACE AbstractConnectionPool.validate(764) - [] - validating org.ldaptive.pool.AbstractConnectionPool$DefaultPooledConnectionProxy@29f3b392
2024-05-03T23:42:18,157: [pool-7-thread-1] TRACE AbstractPool.validate(223) - [] - validation for [org.ldaptive.DefaultConnectionFactory$DefaultConnection@1189934238::config=[org.ldaptive.ConnectionConfig@1106216755::ldapUrl=ldaps://server.school.edu:636, connectTimeout=null, responseTimeout=null, sslConfig=null, useSSL=false, useStartTLS=false, connectionInitializer=[org.ldaptive.BindConnectionInitializer@1388592237::bindDn=cn=user,ou=serviceAccts,dc=server,dc=school,dc=edu, bindSaslConfig=null, bindControls=null], connectionStrategy=org.ldaptive.DefaultConnectionStrategy@5a2f542e], providerConnectionFactory=[org.ldaptive.provider.jndi.JndiConnectionFactory@208085750::metadata=[ldapUrl=ldaps://a.b.c.d:636, count=1], environment={java.naming.ldap.factory.socket=org.ldaptive.ssl.ThreadLocalTLSSocketFactory, java.naming.ldap.version=3, java.naming.factory.initial=com.sun.jndi.ldap.LdapCtxFactory}, classLoader=null, providerConfig=[org.ldaptive.provider.jndi.JndiProviderConfig@1354546403::operationExceptionResultCodes=[PROTOCOL_ERROR, SERVER_DOWN], properties={}, controlProcessor=org.ldaptive.provider.ControlProcessor@5483322c, environment=null, tracePackets=null, removeDnUrls=true, searchIgnoreResultCodes=[SIZE_LIMIT_EXCEEDED], classLoader=null, sslSocketFactory=null, hostnameVerifier=null]], providerConnection=org.ldaptive.provider.jndi.JndiConnection@2a0cd0a] = true
```

## Testing LDAP connections

Search in a DN, one level, or an object, and test that the attribute value retrieved is expected, see the example above

## GSH LDAP command

[GSH LDAP command](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547785/GrouperShell+gsh+ldap+session+LdapSessionUtils)
