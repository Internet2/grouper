---
title: "v6 Upgrade Instructions from v4 / v5"
space: Grouper
pageId: 28547828
version: 40
lastUpdated: 2026-07-21T20:41:46.856Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547828/v6+Upgrade+Instructions+from+v4+v5
---

## Summary

Many things have changed in v6. You should prepare v4 and make changes in v4 before going to v6. You do not need to upgrade to v5 while going from v4 to v6. Configure Grouper in v4 to be able to be upgrade with no major configuration or functionality changes when you switch to v6. v4 can run with only tomcat (no apache or shib sp), all the v6 provisioners, no soap. In v4, to have container logs work, you need to run supervisor and the log pipes. In v6, supervisor and logpipes go away.

**Upgrading from v5:** Since most of the major changes in v6 are already incorporated into v5, there are only a few areas of concern. When upgrading from v5 to v6, you can skip the v4-specific preparation above (membership cache, single-process container, and UI authentication) and focus on the items that also apply from v5: the removed unsupported legacy features, the removed installer jar, the Java library changes, and the removed Lite UI.

1. You must upgrade to v6 from v4. The membership cache must have an initial run (does not need to be exact, can run before the upgrade e.g. a week before).
  
  1. If the membership cache is not populated in v6, there can be negative consequences, so by default v6 will not start if there are groups in Grouper and the membership cache is not there. See this jira.
  2. **You must upgrade from v4 (latest) to v6+**. You will encounter DDL related issues if you upgrade from before v4.19 to v6.
  3. **When on v4 (latest), run the OTHER_JOB_****sqlCacheInitialPopulator daemon before upgrading to v6.** This will populate the membership cache. Depending on the size of your Grouper deployment (number of groups, memberships, etc) and your database performance/latency/etc, this job may be relatively quick or may take up to a day or two. This must be run within a week of upgrading. If you run it, and a week goes by, and you do not upgrade, run it again within a week of upgrading. Note, Grouper is in read/write mode while this job is running.  
      
    Check progress  
      
    
    
    # Estimating progress of `SqlCacheInitialPopulator`
    
    The `OTHER_JOB_sqlCacheInitialPopulator` daemon writes logs only at coarse boundaries (per‑field, every 100,000 non‑skip iterations in STEP 6, and on batch commit), so on long‑running invocations you need to poll the database directly. The queries below are listed DB‑agnostic first; the live‑session query at the end has a variant per database.
    
    
    
    ## 1. Daemon's own log line (start here)
    
    `SELECT started_time, status, total_count, insert_count, update_count, delete_count, job_message FROM grouper_loader_log WHERE job_name='OTHER_JOB_sqlCacheInitialPopulator' ORDER BY started_time DESC FETCH FIRST 5 ROWS ONLY;`The currently‑running row's `job_message` is updated only every 100,000 non‑skip iterations and on store(); on a struggling system it can be hours apart. It still tells you what step the daemon last reported.
    
    
    
    ## 2. Progress of the early PIT backfill `UPDATE`s
    
    (`SqlCacheInitialPopulatorDaemon.java` lines 80–84)
    
    
    
    
    ```
    SELECT 'pit_stems' AS tbl, COUNT(*) AS remaining FROM grouper_pit_stems WHERE source_id_index IS NULL AND active='T'
    UNION ALL SELECT 'pit_attribute_def', COUNT(*) FROM grouper_pit_attribute_def WHERE source_id_index IS NULL AND active='T'
    UNION ALL SELECT 'pit_groups', COUNT(*) FROM grouper_pit_groups WHERE source_internal_id IS NULL AND active='T'
    UNION ALL SELECT 'pit_fields', COUNT(*) FROM grouper_pit_fields WHERE source_internal_id IS NULL AND active='T'
    UNION ALL SELECT 'pit_members', COUNT(*) FROM grouper_pit_members WHERE source_internal_id IS NULL AND active='T';
    ```
    
    
    
    Run twice, a minute apart. If a number is dropping → still in that `UPDATE`. All zeros → past line 84.
    
    
    
    ## 3. Progress of STEP 3 (inserts into `grouper_sql_cache_group`)
    
    
    
    
    ```
    SELECT COUNT(*) AS total,
           SUM(CASE WHEN disabled_on IS NULL          THEN 1 ELSE 0 END) AS enabled,
           SUM(CASE WHEN last_membership_sync IS NULL THEN 1 ELSE 0 END) AS never_synced,
           SUM(CASE WHEN membership_size = -1         THEN 1 ELSE 0 END) AS not_yet_counted
    FROM grouper_sql_cache_group;
    ```
    
    
    
    
    
    - `total` climbing → STEP 3 (inserts) is running.
    - Once `total` is steady and `never_synced` / `not_yet_counted` start dropping → STEP 6 is running, which is where most wall time lives.
    
    
    
    ## 4. Progress of STEP 6 (per‑batch membership sync — the long one)
    
    
    
    
    ```
    SELECT
      SUM(CASE WHEN last_membership_sync IS NULL THEN 1 ELSE 0 END) AS remaining_initial,
      SUM(CASE WHEN membership_size = -1         THEN 1 ELSE 0 END) AS never_counted,
      MAX(last_membership_sync)                                     AS most_recent_sync,
      COUNT(*) AS total_enabled
    FROM grouper_sql_cache_group
    WHERE disabled_on IS NULL;
    ```
    
    
    
    Run every minute. `remaining_initial` drops in flushes of ~500 as batches commit. If it doesn't move for 30+ minutes, it really is stuck on one batch's query at line 599.
    
    
    
    ## 5. Membership row throughput
    
    
    
    
    ```
    SELECT COUNT(*) FROM grouper_sql_cache_mship;
    ```
    
    
    
    Diff over a minute = insert throughput.
    
    
    
    ## 6. What is it actually doing *right now* (DB‑specific)
    
    
    
    ### PostgreSQL
    
    
    ```
    SELECT pid, now()-query_start AS elapsed, state, wait_event, substring(query,1,300) AS query
    FROM pg_stat_activity
    WHERE state != 'idle'
    ORDER BY query_start;
    ```
    
    
    
    Oracle
    
    
    ```
    SELECT s.sid, s.serial#, s.status, s.event, s.last_call_et AS elapsed_secs,
           SUBSTR(q.sql_text,1,300) AS sql_text
    FROM v$session s JOIN v$sql q ON s.sql_id = q.sql_id
    WHERE s.type='USER' AND s.status='ACTIVE'
    ORDER BY s.last_call_et DESC;
    ```
    
    
    
    MySQL
    
    
    ```
    SELECT id, time AS elapsed_secs, state, LEFT(info,300) AS query
    FROM information_schema.processlist
    WHERE command != 'Sleep'
    ORDER BY time DESC;
    ```
    
    
    
    The captured query text tells you which of the daemon's distinct SQL shapes it's stuck on:
    
    
    
    | Line in code | Query shape |
    | --- | --- |
    | 80–84 (early) | `update grouper_pit_stems/attribute_def/groups/fields/members ... where source_internal_id is null and active='T'` |
    | 295–298 (STEP 6 prep) | `select distinct ... from grouper_pit_group_set, grouper_pit_memberships where ... end_time > ?` (4 variants) |
    | 599 (`processBatch`) | `select gpgs1.owner_id ... from grouper_pit_group_set gpgs1, grouper_pit_memberships gpm1 where ... and exists (select 1 from grouper_pit_group_set gpgs2, grouper_pit_memberships gpm2 ...)` |
    
    
    
    ## Most likely culprit if jstack shows a PIT query
    
    “jstack in a PIT query, days, zero output” almost certainly = the line 599 `processBatch` query — the join of `grouper_pit_group_set` ⋈ `grouper_pit_memberships` with an `EXISTS` subquery on the *same two tables again*. Worth checking:
    
    
    
    - Config `otherJob.sqlCacheFullSync.maxObjectFieldPairMembershipSyncBatchSize` (default `1`). If someone bumped it, dropping back to 1 simplifies the query.
    - Indexes on `grouper_pit_memberships(field_id, owner_id, member_id)` and `grouper_pit_group_set(member_id, member_field_id, owner_id, field_id)` — confirm they exist and aren't bloated.
    - Once you have the live query from section 6, `EXPLAIN` / `EXPLAIN PLAN` / `EXPLAIN ANALYZE` it in a separate session.
  4. There is an upgrade task in v6 which will true up the membership cache table
2. Tomcat is a single process in the container, there is no apache, shib, supervisor, logpipes
  
  1. Adjust UI authentication in v4, and run tomcat as a single process in the container. You can either
    
    1. Use the [built-in OIDC](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548296/OIDC+authentication+to+Grouper+UI)
    2. Use the [Unicon pacj4 authentication plugin that does SAML/CAS](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549858/Pac4j+Plugin+for+Built-in+Single+Sign-on+SSO)
    3. Install apache/shib/supervisor in your derived image
    4. Run an authentication container separate from Grouper and reverse proxy
  2. If you are doing apache WS authn (LDAP or htaccess) you need to:
    
    1. Migrate to Grouper LDAP or built-in users
  3. You might want to set GROUPER_TOMCAT_REMOTE_IP_VALVE=true, read the docs, there are other related settings too
  4. Make sure you only have traffic from the load balancer or external web server and not allowed directly to tomcat (if shouldnt be allowed)
  5. Since Apache isn't used, the /status_grouper/status health check endpoint no longer works. Adjust your health checks to use /grouper/status or /grouper-ws/status
  6. Since Apache isn't used, hosting static files outside of the /grouper or /grouper-ws base no longer works
3. Migrate from legacy provisioners to the provisioning framework
  
  1. pspng
  2. googleapps
  3. grouperAtlassianConnector
  4. grouper-azure
  5. grouper-box
  6. grouper-duo
  7. grouperKimConnector
  8. grouper-shib (use SQL or LDAP)
4. Migrate from unsupported legacy features, unlikely in use. The other legacy provisioners were already gone from v5, but these are additionally removed in v6.
  
  1. grouperScim (legacy unsupported version of SCIM, not the supported one)
  2. grouperActivemq (legacy unsupported version of activeMq for a specific use case)
  3. grouper-aws-changelog (legacy unsupported version of AWS for a specific use case)
  4. grouper-tierApiAuthz-connector, tierInstrumentationCollector (unused legacy function)
  5. grouper-messaging-activemq (removed since v5.1.0)
  6. grouper-messaging-aws (removed since v5.1.0)
  7. grouper-messaging-rabbitmq (removed since v5.1.0)
5. If someone is using SOAP (there are logs to alert you if so), migrate to REST
6. The installer jar (grouper-installer-a.b.c.jar) has been removed. If you have a workflow task that was using it, migrate off of it
7. Some Java libraries have significant upgrades or have been removed. Check your gsh scripts (templates, daemon jobs, batch scripts) and custom Java code for usage of:
  
  1. commons-httpclient (classes org.apache.commons.httpclient.*)
  2. json-lib (classes net.sf.json.*) - migrate to Jackson
  3. commons-lang (classes org.apache.commons.lang.*) - migrate to commons-lang3, **this is common in many scripts**
  4. ldaptive V1 and ldaptive-unboundid (migrate to ldaptive V2, major API changes)
  5. org.json (classes org.json.*)
  6. okhttp3 and Retrofit2 (only used by the removed legacy azure provisioner)
8. The Lite UI is totally removed in V6. If you were relying on legacy functionality from it, migrate to other solutions

## Upgrade from v4 to v6

- Turn off the daemon server of the old version and other servers connecting to Grouper
- Make sure the change log temp in the old version is empty by running CHANGE_LOG_changeLogTempToChangeLog from GSH
  
  - loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog");
- Change this property in grouper.hibernate.properties, run the GSH container in the v6 version, it will upgrade your database
  
  - GROUPER_AUTO_DDL_UPTOVERSION=6.*.*
  - You can run this to see if you have everything, do not run the generated script though
    
    - (v6 version) gsh.sh -registry -check -runscript
- Do not turn on the daemon server until the following tasks are complete:
  
  - Run the OTHER_JOB_upgradeTasks daemon from a GSH terminal, and see that it runs successfully. This will make sure all the data is setup in the v6 way. Note: this should happen on a GSH startup, but you can also run this job
    
    - loaderRunOneJob("OTHER_JOB_upgradeTasks");
  - Run this job via GSH (with lots of memory): OTHER_JOB_sqlCacheFullSync job. This will make sure the membership cache tables are setup correctly. Note: this should happen on a GSH startup, but you can also run this job
    
    - edu.internet2.middleware.grouper.app.loader.GrouperLoader.scheduleJobs();
    - loaderRunOneJob("OTHER_JOB_sqlCacheFullSync");
- Make sure ports are listening as expected, e.g. if you expect AJP 8009 you need to set the env variable: GROUPER_TOMCAT_AJP_PORT=8009. If you do not want tomcat ssl to listen, set GROUPER_TOMCAT_HTTPS_PORT=false
  
  - Ideally if apache in v4 was listening on 443, you would change your load balancer to point to 8443 instead and have tomcat listen on that port.
  - If you want to have tomcat listen on 443 it needs to run as root since 443 is a privileged port. Running tomcat as root is not a good security posture.
  - If you make no changes, then the load balancer will not be pointing to the Grouper port.
  - If you are using Tomcat with https, it is a self-signed certificate. If you are using curl on localhost for health checks, you can add `--insecure` to get past the self-signed certificate error. In AWS ECS, update the health check (add `--insecure`) at the level of the UI and WS task definition
- Custom Java
  
  - You should check to see if your Java still compiles. It should, but check anyways. Tweak it if you need to or ask for advice on slack. You might want to rebuild anyways. See above for the list of changes in other libraries.
- There are no logpipes in the logging anymore, if you customized logging, make sure you use the std out/err appender and not e.g. <AppenderRef ref="logpipe_grouper_daemon"/>
  
  - remove GROUPER_LOG_TO_PIPE=true  
    
    
    - Note: You will lose logging in 4 but logpipes are no longer in 6

This gets you to v6.X.X. Now look at the[v5 upgrade steps and see which ones apply to you](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5). Then look at the v6 upgrade steps and see which ones apply to you.

## Experience at Penn

### SOAP

- No web service clients using SOAP, so that is a non-issue

### UI Authentication

- In v4, the authentication was switched from SAML to OIDC. The IdP operator registered an OIDC endpoint for PennGroups. The external system was configured, and SAML was removed from the container. It was surprisingly easy.

- Container changes for authentication change
  
  - Set run shib to false: ENV GROUPER_RUN_SHIB_SP=false
  - Keep apache running initially so it is easy to switch back and reduce the number of changes
  - Comment out the apache shib configuration
    
    
    ```
    #<Location />
    #  ShibRequestSetting REMOTE_ADDR X-Forwarded-For
    #</Location>
    
    
    ```
  - There were a lot of grouperScriptHooks.sh commands to configure shibboleth, these were commented out and then deleted
    
    
    ```
      #cp -v /etc/httpd/conf/httpd.conf /etc/httpd/conf/httpd.conf.origGrouper
      #echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp -v /etc/httpd/conf/httpd.conf /etc/httpd/conf/httpd.conf.origGrouper , result=$?"
    
      #cp -v /etc/httpd/conf/httpd.conf.penn /etc/httpd/conf/httpd.conf
      #echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp -v /etc/httpd/conf/httpd.conf.penn /etc/httpd/conf/httpd.conf , result=$?"
      
      #mv -f /etc/shibboleth/shibboleth2.xml /etc/shibboleth/shibboleth2.xml.grouperOrig
      #echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) mv -f /etc/shibboleth/shibboleth2.xml /etc/shibboleth/shibboleth2.xml.grouperOrig , result=$?"
      
      #cp /etc/shibboleth/shibboleth2.xml.penn /etc/shibboleth/shibboleth2.xml
      #echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) mv -f /etc/shibboleth/shibboleth2.xml.penn /etc/shibboleth/shibboleth2.xml , result=$?"
    
      #Replace entityID with parameter in grouper.properties
      #if [[ -z "${SHIBBVAR}" ]]; then
      #  echo "pennContainer; ERROR: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) $ SHIBBVAR is not set!"
      #else
      #  # dont blank this out if the var isnt there
      #  sed -i "s|replace_me_entitiyID|$SHIBBVAR|g" /etc/shibboleth/shibboleth2.xml
      #  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) sed -i ''s|replace_me_entitiyID|$SHIBBVAR|g'' /etc/shibboleth/shibboleth2.xml , result=$?"
      #fi
      
      # TODO is this in grouper yet??????
      #Replace web.xml session timeout with env variable
      #if [[ -z "${SESSION_TIMEOUT}" ]]; then
      #  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) $ SESSION_TIMEOUT is not set, not editing web.xml"
      #else
      #  # dont blank this out if the var isnt there
      #  sed -i "s|<session-timeout>30</session-timeout>|<session-timeout>$SESSION_TIMEOUT</session-timeout>|g" /opt/tomcat/conf/web.xml
      #  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) sed -i \"s|<session-timeout>30</session-timeout>|<session-timeout>$SESSION_TIMEOUT</session-timeout>|g\" /opt/tomcat/conf/web.xml , result=$?"
      #fi
      
      #mv -f /etc/shibboleth/attribute-map.xml /etc/shibboleth/attribute-map.xml.grouperOrig
      #echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) mv -f /etc/shibboleth/attribute-map.xml /etc/shibboleth/attribute-map.xml.grouperOrig , result=$?"
      
      #cp /etc/shibboleth/attribute-map.xml.penn /etc/shibboleth/attribute-map.xml
      #echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp /etc/shibboleth/attribute-map.xml.penn /etc/shibboleth/attribute-map.xml , result=$?"
    
      #if [ "$SERVER_TYPE" = "ui" ]; then
      #  cp -v /etc/httpd/conf.d/shib.conf.penn /etc/httpd/conf.d/shib_penn.conf 
      #  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp -v /etc/httpd/conf.d/shib.conf.penn /etc/httpd/conf.d/shib_penn.conf  , result=$?"
    
      #  cp -v /etc/httpd/conf.d/grouper-www_penn_healthCheck.conf.ui /etc/httpd/conf.d/grouper-www_penn_healthCheck.conf
      #  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp -v /etc/httpd/conf.d/grouper-www_penn_healthCheck.conf.ui /etc/httpd/conf.d/grouper-www_penn_healthCheck.conf  , result=$?"
      #else 
      #  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) not ui: $SERVER_TYPE"
      #fi
    
      #if [ "$SERVER_TYPE" = "ws" ]; then
      #  cp -v /etc/httpd/conf.d/grouper-www_penn_healthCheck.conf.ws /etc/httpd/conf.d/grouper-www_penn_healthCheck.conf
      #  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp -v /etc/httpd/conf.d/grouper-www_penn_healthCheck.conf.ws /etc/httpd/conf.d/grouper-www_penn_healthCheck.conf  , result=$?"
      #else 
      #  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) not ws: $SERVER_TYPE"
      #fi
    ```

### Single process container

In v4 Grouper can be run in a single process container, like it will be in v6. The v6 upgrade should have as few changes as possible to reduce risk. To change v4 to be single process:

- At first the ports from the load balancer were changed, so they are a privileged port: 443
- So tomcat needs to run as root like apache did
  
  
  ```
    && /opt/container_files/docker-build-bin/containerDockerfileInstallPermissions.sh root root \
    && chown -R root.root /opt/grouper/logs \
  
  ENV GROUPER_TOMCAT_UID=0
  ```
- ENV GROUPER_RUN_APACHE=false
- When apache was turned off, the health check for the grouper container needed to be adjusted to look at tomcat instead of apache
- The output of tomcat is the container log, so there are no pipes
  
  
  ```
  ENV GROUPER_LOG_TO_PIPE=false
  ```
- Run the container and see tomcat is the only process. Not supervisor, not apache, not shib sp
- Update admin docs that if the tomcat process is killed, supervisor will not start it up again, the container will end due to health check and another will start

### Provisioning

- There were a lot of legacy provisioners at Penn
- These were configured in the provisioning framework, and the legacy provisioners were turned off
- Each was done one at a time
  
  - Box
  - Remedy
  - Duo
  - PSPNG (was done last)
- PSPNG provisioning upgrade
  
  - Configure the provisioning framework
  - Disable the PSPNG jobs
  - Export old PSPNG provisionable
    
    
    ```
    select * from grouper_aval_asn_group_v gaagv where attribute_def_name_name = 'penn:etc:pspng:provision_to' ;
    select * from grouper_aval_asn_stem_v gaagv where attribute_def_name_name = 'penn:etc:pspng:provision_to' ;
    
    
    ```
  - Sync up provisionable with provisioning framework
    
    
    ```
    [tomcat@20d1e04593cb4c07a48b272226ec67f7-2673161782 bin]$ ./gsh.sh -pspngAttributesToProvisioningAttributes pspng_activedirectoryDevBushy1 kiteTest readonly deleteOrphans
    Found 2 folder(s) that have provision_to attribute assigned with target pspng_activedirectoryDevBushy1
    Found 0 folder(s) that have do_not_provision_to attribute assigned with target pspng_activedirectoryDevBushy1
    Found 0 group(s) that have provision_to attribute assigned with target pspng_activedirectoryDevBushy1
    Found 0 group(s) that have do_not_provision_to attribute assigned with target pspng_activedirectoryDevBushy1
    Found 3 folder(s) that already have direct new provisioning attribute assigned with target kiteTest
    Found 1 group(s) that already have direct new provisioning attribute assigned with target kiteTest
    Going to assign new provisioning attributes to folder penn:community:employee:org with target name kiteTest with provisionable true
    Going to assign new provisioning attributes to folder test:pspngTest1 with target name kiteTest with provisionable true
    Found 3 folders that have new provisioning attributes directly assigned but equivalent pspng attribute not found. Going to clear the direct assignment from those folders.
    Going to clear direct new provisioning attribute assignment from folder penn:community:centers
    Going to clear direct new provisioning attribute assignment from folder penn:community:employee:jobGrade:letteredJobGrades
    Going to clear direct new provisioning attribute assignment from folder test:kiteTestProvisioningFramework
    Found 1 groups that have new provisioning attributes directly assigned but equivalent pspng attribute not found. Going to clear the direct assignment from those groups.
    Going to clear direct new provisioning attribute assignment from group test:bigGroup
    
    
    Do this non readonly
    
    Found 31 folder(s) that have provision_to attribute assigned with target pspng_activedirectoryFull
    Found 0 folder(s) that have do_not_provision_to attribute assigned with target pspng_activedirectoryFull
    Found 107 group(s) that have provision_to attribute assigned with target pspng_activedirectoryFull
    Found 0 group(s) that have do_not_provision_to attribute assigned with target pspng_activedirectoryFull
    Found 0 folder(s) that already have direct new provisioning attribute assigned with target kite
    Found 0 group(s) that already have direct new provisioning attribute assigned with target kite
    Going to assign new provisioning attributes to folder penn:asc:apps:ascvpn with target name kite with provisionable true
    Successfully assigned kite to folder penn:asc:apps:ascvpn with provisionable true
    Going to assign new provisioning attributes to folder penn:community:employee:affiliationsByOrg with target name kite with provisionable true
    Successfully assigned kite to folder penn:community:employee:affiliationsByOrg with provisionable true
    Going to assign new provisioning attributes to folder penn:community:employee:org with target name kite with provisionable true
    Successfully assigned kite to folder penn:community:employee:org with provisionable true
    
    
    ```
  - Set provisioner to readonly and run a full sync. Not much should be projected to change
  - Set the incremental change log pointer to current if no already (so it doesnt have to run through everything)
  - See no changes
  - Configure read/write, enable incremental
  - Run tests and adjust if needed

### After all these steps were done in v4, do the upgrade

- Make sure change log temp is empty
- Turn off Grouper in all modules
- Confirm change log temp is still empty
- Start up a v5 and see upgrade steps complete
- Turn on each module and test: Daemon, WS, UI

## See Also

[Release Notes for Grouper 5](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes)

[Release Notes for Grouper 6](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547614/v6+Release+Notes)
