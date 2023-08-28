/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: GrouperUtilTest.java,v 1.16 2009-11-09 03:12:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.util.versioningV1.BeanA;
import edu.internet2.middleware.grouper.util.versioningV1.BeanB;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;


/**
 *
 */
public class GrouperUtilTest extends GrouperTest {
  
  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GrouperUtilTest("testParentStemNameFromName"));
    //TestRunner.run(TestGroup0.class);
    //runPerfProblem();
    
  }
 
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperUtilTest.class);

  public void testStringFormatNameReverseTruncate() {
    assertEquals("c.b.a", GrouperUtil.stringFormatNameReverseReplaceTruncate("a:b:c", ".", -1));
    assertEquals("c.b.", GrouperUtil.stringFormatNameReverseReplaceTruncate("a:b:c", ".", 4));
  }
  
  public void testExceptionTruncate() {
    String exception = "2021-10-09 14:43:00,114: [DefaultQuartzScheduler_Worker-9] ERROR GrouperLoaderJob.execute(347) -  - Error running up job\n";
    exception += "java.lang.RuntimeException: Error in loader job: null, check logs: type: consumer, finalLog: false, state: init, consumerName: ldapGroupsWithOverrideIncremental, totalCount: 11, currentSequenceNumber: null, publisherClass: edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer, exception: java.lang.RuntimeException: provisionerClass: LdapSync, configId: ldapGroupsWithOverride, provisioningType: incrementalProvisionChangeLog, state: retrieveIncrementalTargetData, changeLogItemsApplicableByType: 1, recalcEventsDuringFullSync: 0, checkErrors: all, syncGroupsToQuery: 2, syncGroupsFound: 2, retrieveSyncGroupsMillis: 0, syncGroupCount: 2, convertToFullSyncScore: 20, recalcEventsDuringGroupSync: 0, syncMembershipsToQueryFromGroup: 2, syncMembershipsFromGroup: 0, retrieveSyncMembershipsMillis: 1, syncMembershipCount: 0, syncMembersToQuery: 0, syncMembersFound: 0, retrieveSyncMembersMillis: 0, syncMemberCount: 0, retrieveData\n";
    exception += "MillisSince1970: 1633804980092, retrieveGrouperMshipsMillis: 2, grouperMshipCount: 0, retrieveGrouperGroupsMillis: 1, grouperGroupCount: 2, retrieveGrouperEntitiesMillis: 0, grouperEntityCount: 0, retrieveGrouperDataMillis: 3, exception: java.lang.RuntimeException: Problem with ldap conection: personLdap,\n";
    exception += "Error querying ldap server id: personLdap, searchDn: ou=GrouperGroups,dc=example,dc=edu, filter: '(|(gidNumber=10034)(gidNumber=10023))', returning attributes: cn, gidNumber, objectClass\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.callbackLdapSession(LdaptiveSessionImpl.java:465)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.list(LdaptiveSessionImpl.java:606)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap.search(LdapSyncDaoForLdap.java:16)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisioningTargetDao.retrieveGroups(LdapProvisioningTargetDao.java:650)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoAdapter.retrieveGroups(GrouperProvisionerTargetDaoAdapter.java:489)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoAdapter.retrieveIncrementalData(GrouperProvisionerTargetDaoAdapter.java:444)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogicIncremental.retrieveIncrementalTargetData(GrouperProvisioningLogicIncremental.java:2508)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogic.provisionIncremental(GrouperProvisioningLogic.java:661)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType$3.provision(GrouperProvisioningType.java:100)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogic.provision(GrouperProvisioningLogic.java:50)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.provision(GrouperProvisioner.java:587)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer.dispatchEventList(ProvisioningConsumer.java:91)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer.processChangeLogEntries(EsbConsumer.java:503)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.ChangeLogHelper.processRecords(ChangeLogHelper.java:261)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderType$6.runJob(GrouperLoaderType.java:677)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.runJob(GrouperLoaderJob.java:493)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.execute(GrouperLoaderJob.java:344)\n";
    exception += "\tat org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n";
    exception += "\tat org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:573)\n";
    exception += "Caused by: [org.ldaptive.LdapException@1755876524::resultCode=NO_SUCH_OBJECT, matchedDn=null, responseControls=null, referralURLs=null, messageId=-1, message=javax.naming.NameNotFoundException: [LDAP: error code 32 - No Such Object]; remaining name 'ou=GrouperGroups,dc=example,dc=edu', providerException=javax.naming.NameNotFoundException: [LDAP: error code 32 - No Such Object]; remaining name 'ou=GrouperGroups,dc=example,dc=edu']\n";
    exception += "\tat org.ldaptive.provider.ProviderUtils.throwOperationException(ProviderUtils.java:55)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection.processNamingException(JndiConnection.java:619)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection$JndiSearchIterator.initialize(JndiConnection.java:741)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection.search(JndiConnection.java:463)\n";
    exception += "\tat org.ldaptive.SearchOperation.executeSearch(SearchOperation.java:103)\n";
    exception += "\tat org.ldaptive.SearchOperation.invoke(SearchOperation.java:85)\n";
    exception += "\tat org.ldaptive.SearchOperation.invoke(SearchOperation.java:15)\n";
    exception += "\tat org.ldaptive.AbstractOperation.execute(AbstractOperation.java:126)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.processSearchRequest(LdaptiveSessionImpl.java:804)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.access$0(LdaptiveSessionImpl.java:749)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl$3.callback(LdaptiveSessionImpl.java:612)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.callbackLdapSession(LdaptiveSessionImpl.java:459)\n";
    exception += "\t... 18 more\n";
    exception += "Caused by: javax.naming.NameNotFoundException: [LDAP: error code 32 - No Such Object]; remaining name 'ou=GrouperGroups,dc=example,dc=edu'\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.mapErrorCode(LdapCtx.java:3179)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.processReturnCode(LdapCtx.java:3100)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.processReturnCode(LdapCtx.java:2891)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.searchAux(LdapCtx.java:1846)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.c_search(LdapCtx.java:1769)\n";
    exception += "\tat com.sun.jndi.toolkit.ctx.ComponentDirContext.p_search(ComponentDirContext.java:392)\n";
    exception += "\tat com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(PartialCompositeDirContext.java:358)\n";
    exception += "\tat com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(PartialCompositeDirContext.java:341)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection$JndiSearchIterator.search(JndiConnection.java:806)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection$JndiSearchIterator.initialize(JndiConnection.java:735)\n";
    exception += "\t... 27 more\n";
    exception += ", finalLog: true, queryCount: 13, tookMillis: 55, took: 0:00:00.055\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.provisionFinallyBlock(GrouperProvisioner.java:660)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.provision(GrouperProvisioner.java:599)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer.dispatchEventList(ProvisioningConsumer.java:91)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer.processChangeLogEntries(EsbConsumer.java:503)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.ChangeLogHelper.processRecords(ChangeLogHelper.java:261)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderType$6.runJob(GrouperLoaderType.java:677)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.runJob(GrouperLoaderJob.java:493)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.execute(GrouperLoaderJob.java:344)\n";
    exception += "\tat org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n";
    exception += "\tat org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:573)\n";
    exception += ", tookMillis: 62Error: java.lang.RuntimeException: Couldn't process any records: type: consumer, finalLog: true, state: init, consumerName: ldapGroupsWithOverrideIncremental, totalCount: 11, currentSequenceNumber: null, publisherClass: edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer, exception: java.lang.RuntimeException: provisionerClass: LdapSync, configId: ldapGroupsWithOverride, provisioningType: incrementalProvisionChangeLog, state: retrieveIncrementalTargetData, changeLogItemsApplicableByType: 1, recalcEventsDuringFullSync: 0, checkErrors: all, syncGroupsToQuery: 2, syncGroupsFound: 2, retrieveSyncGroupsMillis: 0, syncGroupCount: 2, convertToFullSyncScore: 20, recalcEventsDuringGroupSync: 0, syncMembershipsToQueryFromGroup: 2, syncMembershipsFromGroup: 0, retrieveSyncMembershipsMillis: 1, syncMembershipCount: 0, syncMembersToQuery: 0, syncMembersFound: 0, retrieveSyncMembersMillis: 0, syncMemberCount: 0, retrieveData\n";
    exception += "MillisSince1970: 1633804980092, retrieveGrouperMshipsMillis: 2, grouperMshipCount: 0, retrieveGrouperGroupsMillis: 1, grouperGroupCount: 2, retrieveGrouperEntitiesMillis: 0, grouperEntityCount: 0, retrieveGrouperDataMillis: 3, exception: java.lang.RuntimeException: Problem with ldap conection: personLdap,\n";
    exception += "Error querying ldap server id: personLdap, searchDn: ou=GrouperGroups,dc=example,dc=edu, filter: '(|(gidNumber=10034)(gidNumber=10023))', returning attributes: cn, gidNumber, objectClass\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.callbackLdapSession(LdaptiveSessionImpl.java:465)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.list(LdaptiveSessionImpl.java:606)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap.search(LdapSyncDaoForLdap.java:16)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisioningTargetDao.retrieveGroups(LdapProvisioningTargetDao.java:650)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoAdapter.retrieveGroups(GrouperProvisionerTargetDaoAdapter.java:489)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoAdapter.retrieveIncrementalData(GrouperProvisionerTargetDaoAdapter.java:444)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogicIncremental.retrieveIncrementalTargetData(GrouperProvisioningLogicIncremental.java:2508)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogic.provisionIncremental(GrouperProvisioningLogic.java:661)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType$3.provision(GrouperProvisioningType.java:100)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogic.provision(GrouperProvisioningLogic.java:50)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.provision(GrouperProvisioner.java:587)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer.dispatchEventList(ProvisioningConsumer.java:91)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer.processChangeLogEntries(EsbConsumer.java:503)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.ChangeLogHelper.processRecords(ChangeLogHelper.java:261)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderType$6.runJob(GrouperLoaderType.java:677)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.runJob(GrouperLoaderJob.java:493)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.execute(GrouperLoaderJob.java:344)\n";
    exception += "\tat org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n";
    exception += "\tat org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:573)\n";
    exception += "Caused by: [org.ldaptive.LdapException@1755876524::resultCode=NO_SUCH_OBJECT, matchedDn=null, responseControls=null, referralURLs=null, messageId=-1, message=javax.naming.NameNotFoundException: [LDAP: error code 32 - No Such Object]; remaining name 'ou=GrouperGroups,dc=example,dc=edu', providerException=javax.naming.NameNotFoundException: [LDAP: error code 32 - No Such Object]; remaining name 'ou=GrouperGroups,dc=example,dc=edu']\n";
    exception += "\tat org.ldaptive.provider.ProviderUtils.throwOperationException(ProviderUtils.java:55)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection.processNamingException(JndiConnection.java:619)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection$JndiSearchIterator.initialize(JndiConnection.java:741)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection.search(JndiConnection.java:463)\n";
    exception += "\tat org.ldaptive.SearchOperation.executeSearch(SearchOperation.java:103)\n";
    exception += "\tat org.ldaptive.SearchOperation.invoke(SearchOperation.java:85)\n";
    exception += "\tat org.ldaptive.SearchOperation.invoke(SearchOperation.java:15)\n";
    exception += "\tat org.ldaptive.AbstractOperation.execute(AbstractOperation.java:126)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.processSearchRequest(LdaptiveSessionImpl.java:804)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.access$0(LdaptiveSessionImpl.java:749)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl$3.callback(LdaptiveSessionImpl.java:612)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.callbackLdapSession(LdaptiveSessionImpl.java:459)\n";
    exception += "\t... 18 more\n";
    exception += "Caused by: javax.naming.NameNotFoundException: [LDAP: error code 32 - No Such Object]; remaining name 'ou=GrouperGroups,dc=example,dc=edu'\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.mapErrorCode(LdapCtx.java:3179)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.processReturnCode(LdapCtx.java:3100)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.processReturnCode(LdapCtx.java:2891)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.searchAux(LdapCtx.java:1846)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.c_search(LdapCtx.java:1769)\n";
    exception += "\tat com.sun.jndi.toolkit.ctx.ComponentDirContext.p_search(ComponentDirContext.java:392)\n";
    exception += "\tat com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(PartialCompositeDirContext.java:358)\n";
    exception += "\tat com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(PartialCompositeDirContext.java:341)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection$JndiSearchIterator.search(JndiConnection.java:806)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection$JndiSearchIterator.initialize(JndiConnection.java:735)\n";
    exception += "\t... 27 more\n";
    exception += ", finalLog: true, queryCount: 13, tookMillis: 55, took: 0:00:00.055\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.provisionFinallyBlock(GrouperProvisioner.java:660)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.provision(GrouperProvisioner.java:599)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer.dispatchEventList(ProvisioningConsumer.java:91)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer.processChangeLogEntries(EsbConsumer.java:503)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.ChangeLogHelper.processRecords(ChangeLogHelper.java:261)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderType$6.runJob(GrouperLoaderType.java:677)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.runJob(GrouperLoaderJob.java:493)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.execute(GrouperLoaderJob.java:344)\n";
    exception += "\tat org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n";
    exception += "\tat org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:573)\n";
    exception += ", tookMillis: 62\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer.processChangeLogEntries(EsbConsumer.java:592)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.ChangeLogHelper.processRecords(ChangeLogHelper.java:261)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderType$6.runJob(GrouperLoaderType.java:677)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.runJob(GrouperLoaderJob.java:493)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.execute(GrouperLoaderJob.java:344)\n";
    exception += "\tat org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n";
    exception += "\tat org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:573)\n";
    exception += "Error: Error processing record -1, sequenceNumber: -1, java.lang.RuntimeException: provisionerClass: LdapSync, configId: ldapGroupsWithOverride, provisioningType: incrementalProvisionChangeLog, state: retrieveIncrementalTargetData, changeLogItemsApplicableByType: 1, recalcEventsDuringFullSync: 0, checkErrors: all, syncGroupsToQuery: 2, syncGroupsFound: 2, retrieveSyncGroupsMillis: 0, syncGroupCount: 2, convertToFullSyncScore: 20, recalcEventsDuringGroupSync: 0, syncMembershipsToQueryFromGroup: 2, syncMembershipsFromGroup: 0, retrieveSyncMembershipsMillis: 1, syncMembershipCount: 0, syncMembersToQuery: 0, syncMembersFound: 0, retrieveSyncMembersMillis: 0, syncMemberCount: 0, retrieveData\n";
    exception += "MillisSince1970: 1633804980092, retrieveGrouperMshipsMillis: 2, grouperMshipCount: 0, retrieveGrouperGroupsMillis: 1, grouperGroupCount: 2, retrieveGrouperEntitiesMillis: 0, grouperEntityCount: 0, retrieveGrouperDataMillis: 3, exception: java.lang.RuntimeException: Problem with ldap conection: personLdap,\n";
    exception += "Error querying ldap server id: personLdap, searchDn: ou=GrouperGroups,dc=example,dc=edu, filter: '(|(gidNumber=10034)(gidNumber=10023))', returning attributes: cn, gidNumber, objectClass\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.callbackLdapSession(LdaptiveSessionImpl.java:465)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.list(LdaptiveSessionImpl.java:606)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap.search(LdapSyncDaoForLdap.java:16)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisioningTargetDao.retrieveGroups(LdapProvisioningTargetDao.java:650)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoAdapter.retrieveGroups(GrouperProvisionerTargetDaoAdapter.java:489)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoAdapter.retrieveIncrementalData(GrouperProvisionerTargetDaoAdapter.java:444)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogicIncremental.retrieveIncrementalTargetData(GrouperProvisioningLogicIncremental.java:2508)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogic.provisionIncremental(GrouperProvisioningLogic.java:661)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType$3.provision(GrouperProvisioningType.java:100)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogic.provision(GrouperProvisioningLogic.java:50)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.provision(GrouperProvisioner.java:587)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer.dispatchEventList(ProvisioningConsumer.java:91)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer.processChangeLogEntries(EsbConsumer.java:503)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.ChangeLogHelper.processRecords(ChangeLogHelper.java:261)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderType$6.runJob(GrouperLoaderType.java:677)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.runJob(GrouperLoaderJob.java:493)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.execute(GrouperLoaderJob.java:344)\n";
    exception += "\tat org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n";
    exception += "\tat org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:573)\n";
    exception += "Caused by: [org.ldaptive.LdapException@1755876524::resultCode=NO_SUCH_OBJECT, matchedDn=null, responseControls=null, referralURLs=null, messageId=-1, message=javax.naming.NameNotFoundException: [LDAP: error code 32 - No Such Object]; remaining name 'ou=GrouperGroups,dc=example,dc=edu', providerException=javax.naming.NameNotFoundException: [LDAP: error code 32 - No Such Object]; remaining name 'ou=GrouperGroups,dc=example,dc=edu']\n";
    exception += "\tat org.ldaptive.provider.ProviderUtils.throwOperationException(ProviderUtils.java:55)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection.processNamingException(JndiConnection.java:619)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection$JndiSearchIterator.initialize(JndiConnection.java:741)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection.search(JndiConnection.java:463)\n";
    exception += "\tat org.ldaptive.SearchOperation.executeSearch(SearchOperation.java:103)\n";
    exception += "\tat org.ldaptive.SearchOperation.invoke(SearchOperation.java:85)\n";
    exception += "\tat org.ldaptive.SearchOperation.invoke(SearchOperation.java:15)\n";
    exception += "\tat org.ldaptive.AbstractOperation.execute(AbstractOperation.java:126)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.processSearchRequest(LdaptiveSessionImpl.java:804)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.access$0(LdaptiveSessionImpl.java:749)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl$3.callback(LdaptiveSessionImpl.java:612)\n";
    exception += "\tat edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveSessionImpl.callbackLdapSession(LdaptiveSessionImpl.java:459)\n";
    exception += "\t... 18 more\n";
    exception += "Caused by: javax.naming.NameNotFoundException: [LDAP: error code 32 - No Such Object]; remaining name 'ou=GrouperGroups,dc=example,dc=edu'\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.mapErrorCode(LdapCtx.java:3179)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.processReturnCode(LdapCtx.java:3100)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.processReturnCode(LdapCtx.java:2891)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.searchAux(LdapCtx.java:1846)\n";
    exception += "\tat com.sun.jndi.ldap.LdapCtx.c_search(LdapCtx.java:1769)\n";
    exception += "\tat com.sun.jndi.toolkit.ctx.ComponentDirContext.p_search(ComponentDirContext.java:392)\n";
    exception += "\tat com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(PartialCompositeDirContext.java:358)\n";
    exception += "\tat com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(PartialCompositeDirContext.java:341)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection$JndiSearchIterator.search(JndiConnection.java:806)\n";
    exception += "\tat org.ldaptive.provider.jndi.JndiConnection$JndiSearchIterator.initialize(JndiConnection.java:735)\n";
    exception += "\t... 27 more\n";
    exception += ", finalLog: true, queryCount: 13, tookMillis: 55, took: 0:00:00.055\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.provisionFinallyBlock(GrouperProvisioner.java:660)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.provision(GrouperProvisioner.java:599)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer.dispatchEventList(ProvisioningConsumer.java:91)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer.processChangeLogEntries(EsbConsumer.java:503)\n";
    exception += "\tat edu.internet2.middleware.grouper.changeLog.ChangeLogHelper.processRecords(ChangeLogHelper.java:261)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderType$6.runJob(GrouperLoaderType.java:677)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.runJob(GrouperLoaderJob.java:493)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.execute(GrouperLoaderJob.java:344)\n";
    exception += "\tat org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n";
    exception += "\tat org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:573)\n";
    exception += "Did not get all the way through the batch! -1 != 1082,\n";
    exception += "jobName: CHANGE_LOG_consumer_ldapGroupsWithOverrideIncremental\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.runJob(GrouperLoaderJob.java:502)\n";
    exception += "\tat edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob.execute(GrouperLoaderJob.java:344)\n";
    exception += "\tat org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n";
    exception += "\tat org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:573)";
    
    String exceptionOriginal = exception;
    String exceptionTruncate0 = GrouperUtil.exceptionWithoutDupes(exception);
    String exceptionTruncate1 = GrouperUtil.exceptionWithoutPackages(exceptionTruncate0);
    String exceptionTruncate2 = GrouperUtil.exceptionWithoutClasses(exceptionTruncate0);
    String exceptionTruncate3 = GrouperUtil.exception4kZipBase64(exceptionTruncate0);
    String exceptionTruncateUnzip3 = GrouperUtil.zipUnBase64UnGzip(exceptionTruncate3);
    
    

//    System.out.println("\n\n#############  ORIGINAL ##################\n\n");
//    System.out.println(exceptionOriginal);
//    System.out.println("\n\n###############################\n\n");
//
//    System.out.println("\n\n############# EXPECTED ##################\n\n");
//    System.out.println(exception);
//    System.out.println("\n\n###############################\n\n");
//
//    System.out.println("\n\n############## DUPES #################\n\n");
//    System.out.println(exceptionTruncate0);
//    System.out.println("\n\n###############################\n\n");
//
//    System.out.println("\n\n############ PACKAGES ###################\n\n");
//    System.out.println(exceptionTruncate1);
//    System.out.println("\n\n###############################\n\n");
//    
//    System.out.println("\n\n############# CLASSES ##################\n\n");
//    System.out.println(exceptionTruncate2);
//    System.out.println("\n\n###############################\n\n");
//    
//    System.out.println("\n\n############### ZIP ################\n\n");
//    System.out.println(exceptionTruncate3);
//    System.out.println("\n\n###############################\n\n");
//    
//    System.out.println("\n\n############### UNZIP ################\n\n");
//    System.out.println(exceptionTruncateUnzip3);
//    System.out.println("\n\n###############################\n\n");
//    
//    System.out.println("Exception: " + exceptionOriginal.length());
//    System.out.println("Exception truncate0 (dupes): " + exceptionTruncate0.length());
//    System.out.println("Exception truncate1 (packages): " + exceptionTruncate1.length());
//    System.out.println("Exception truncate2 (classes): " + exceptionTruncate2.length());
//    System.out.println("Exception truncate3 (classes zip): " + exceptionTruncate3.length());
//    System.out.println("Exception truncate3 (classes unzip): " + exceptionTruncateUnzip3.length());

    
  }
  
  /**
   * 
   */
  public void testSubstituteExpressionLanguageExternal() {

    String el = "${grouperUtil.appendPrefixIfStringNotBlank('[unverifiedInfo]', ' ', grouperUtil.appendIfNotBlankString(externalSubject.name, ' - ', externalSubject.institution))} [externalUserID] ${externalSubject.identifier}";

    //###########################
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setName("My Name");
    externalSubject.setInstitution("My Institution");
    externalSubject.setIdentifier("me@inst.edu");

    Map<String, Object> substitutionMap = new HashMap<String, Object>();
    substitutionMap.put("externalSubject", externalSubject);
    
    //do silent since there are warnings on null...
    String description = GrouperUtil.substituteExpressionLanguage(el, substitutionMap, false, true, true);
    //System.out.println("has all three: " + description);
    assertEquals("[unverifiedInfo] My Name - My Institution [externalUserID] me@inst.edu", description);
    
    //###########################
    externalSubject = new ExternalSubject();
    externalSubject.setName("My Name");
    externalSubject.setInstitution(null);
    externalSubject.setIdentifier("me@inst.edu");

    substitutionMap = new HashMap<String, Object>();
    substitutionMap.put("externalSubject", externalSubject);
    
    //do silent since there are warnings on null...
    description = GrouperUtil.substituteExpressionLanguage(el, substitutionMap, false, true, true);
    //System.out.println("has no institution: " + description);
    assertEquals("[unverifiedInfo] My Name [externalUserID] me@inst.edu", description);
    
    //###########################
    externalSubject = new ExternalSubject();
    externalSubject.setName(null);
    externalSubject.setInstitution("My Institution");
    externalSubject.setIdentifier("me@inst.edu");

    substitutionMap = new HashMap<String, Object>();
    substitutionMap.put("externalSubject", externalSubject);
    
    //do silent since there are warnings on null...
    description = GrouperUtil.substituteExpressionLanguage(el, substitutionMap, false, true, true);
    //System.out.println("has no name: " + description);
    assertEquals("[unverifiedInfo] My Institution [externalUserID] me@inst.edu", description);
    
    //###########################
    externalSubject = new ExternalSubject();
    externalSubject.setName(null);
    externalSubject.setInstitution(null);
    externalSubject.setIdentifier("me@inst.edu");

    substitutionMap = new HashMap<String, Object>();
    substitutionMap.put("externalSubject", externalSubject);
    
    //do silent since there are warnings on null...
    description = GrouperUtil.substituteExpressionLanguage(el, substitutionMap, false, true, true);
    //System.out.println("has no name or institution: " + description);
    assertEquals(" [externalUserID] me@inst.edu", description);
  }
  
  /**
   * 
   */
  public void testAppendIfNotBlankString() {
    
  }

  /**
   * 
   */
  public void testTruncateAscii() {

    String testString = "H13_FRA2007, Questions d’histoire de la litérature";
    System.out.println(GrouperUtil.truncateAscii(testString, 50));
  }
  
  /**
   * 
   */
  public void testMail() {

    //this property has been commented out since 2010
    //String testEmailAddress = GrouperConfig.getProperty("mail.test.address");
    String testEmailAddress = "a@b.c";

    new GrouperEmail()
      .setBody("test body")
      .setSubject("test subject")
      .setTo(testEmailAddress)
      .setCc(testEmailAddress)
      .setBcc(testEmailAddress)
      .setReplyTo(testEmailAddress)
      .send();
  }

  /**
   * test log next exception
   */
  public void testLogNextException() {
    GrouperUtil.logErrorNextException(LOG, new Throwable("whatever", new Throwable()), 100);
    GrouperUtil.logErrorNextException(LOG, new Throwable("whatever"), 100);
    SQLException sqlException = new SQLException("whatever", new Throwable());
    SQLException nextException = new SQLException("THE NEXT EXCEPTION");
    sqlException.setNextException(nextException);
    GrouperUtil.logErrorNextException(LOG, new Throwable("whatever", sqlException), 100);
  }
  
  /**
   * yyyy/mm/dd
   * yyyy-mm-dd
   * dd-mon-yyyy
   * yyyy/mm/dd hh:mm:ss
   * dd-mon-yyyy hh:mm:ss
   * yyyy/mm/dd hh:mm:ss.SSS
   * dd-mon-yyyy hh:mm:ss.SSS
   */
  public void testStringToDate2() {
    assertEquals(GrouperUtil.dateValue("2001/02/03 00:00:00.000"), GrouperUtil.stringToDate2("2001/02/03"));
    assertEquals(GrouperUtil.dateValue("2001/02/03 00:00:00.000"), GrouperUtil.stringToDate2("2001-2-3"));
    assertEquals(GrouperUtil.dateValue("2001/02/03 00:00:00.000"), GrouperUtil.stringToDate2("03-Feb-2001"));
    assertEquals(GrouperUtil.dateValue("2001/02/03 00:00:00.000"), GrouperUtil.stringToDate2("3-FEB-2001"));

    assertEquals(GrouperUtil.dateValue("2001/02/03 04:05:06.000"), GrouperUtil.stringToDate2("2001-2-3  4:05_06"));
    assertEquals(GrouperUtil.dateValue("2001/02/03 04:05:06.000"), GrouperUtil.stringToDate2("3_feb, 2001 04 5 6"));

    assertEquals(GrouperUtil.dateValue("2001/02/03 04:05:06.007"), GrouperUtil.stringToDate2("2001-2-3  4:05_06.7"));
    assertEquals(GrouperUtil.dateValue("2001/02/03 04:05:06.007"), GrouperUtil.stringToDate2("3_feb, 2001 04 5 6.007"));
  }
  
  
  /**
   * 
   */
  public void testMatchSqlString() {
    //  * e.g. if the input is a:b:%, and the input is: a:b:test:that, then it returns true
    assertTrue(GrouperUtil.matchSqlString("a:b:%", "a:b:test:that"));
    assertFalse(GrouperUtil.matchSqlString("a:b:%", "a:c:test:that"));
    assertTrue(GrouperUtil.matchSqlString("a_b:%", "a:b:test:that"));
    assertTrue(GrouperUtil.matchSqlString("a%b:%", "aasdfasdfb:test:that"));
  }

  
  /**
   * see that properties can be retrieved from url
   * note, this is called atestUrlProperties since the URL might not be setup
   */
  public void atestUrlProperties() {
    
    //put test.properties at a url with these contents:
    //test=testProp
    //test2=hey<b> < what
    
    String url = "https://medley.isc-seo.upenn.edu/docroot/misc/grouperUnit.properties";
    //different url in cache, but same principal
    String url2 = url + "?";
    
    Properties properties = null;
    
    int propertiesFromUrlHttpCount = GrouperUtil.propertiesFromUrlHttpCount;
    int propertiesFromUrlFailsafeGetCount = GrouperUtil.propertiesFromUrlFailsafeGetCount;
    
    properties = GrouperUtil.propertiesFromUrl(url, false, false, null);
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> < what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+1, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //go again, no cache:
    properties = GrouperUtil.propertiesFromUrl(url, false, false, null);
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> < what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+2, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //lets cache
    properties = GrouperUtil.propertiesFromUrl(url, true, false, null);
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> < what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+3, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //should get from cache
    properties = GrouperUtil.propertiesFromUrl(url, true, false, null);
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> < what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+3, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //transform
    properties = GrouperUtil.propertiesFromUrl(url, false, false, new GrouperHtmlFilter());
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> &lt; what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+4, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //failsafe
    properties = GrouperUtil.propertiesFromUrl(url2, true, true, new GrouperHtmlFilter());
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> &lt; what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+5, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    GrouperUtil.propertiesFromUrlFailForTest = true;
    
    //try a failure when not caching
    try {
      properties = GrouperUtil.propertiesFromUrl(url, false, false, new GrouperHtmlFilter());
      fail();
    } catch (Exception e) {
      //good
    }
    assertEquals(propertiesFromUrlHttpCount+5, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);
    
    //try a failure when caching (second level)
    GrouperUtil.propertiesFromUrlFailForTest = true;
    GrouperUtil.propertiesFromUrlCache.clear();
    properties = GrouperUtil.propertiesFromUrl(url2, true, true, new GrouperHtmlFilter());

    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> &lt; what", properties.get("test2"));

    assertEquals(propertiesFromUrlHttpCount+5, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount+1, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //should be in cache
    properties = GrouperUtil.propertiesFromUrl(url2, true, true, new GrouperHtmlFilter());

    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> &lt; what", properties.get("test2"));

    assertEquals(propertiesFromUrlHttpCount+5, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount+1, GrouperUtil.propertiesFromUrlFailsafeGetCount);
    
    
    
  }
  
  /**
   * 
   */
  public void testFormatNumberWithCommas() {
    assertEquals("123", GrouperUtil.formatNumberWithCommas(123L));
    assertEquals("1,234", GrouperUtil.formatNumberWithCommas(1234L));
    assertEquals("12,345", GrouperUtil.formatNumberWithCommas(12345L));
    assertEquals("123,456", GrouperUtil.formatNumberWithCommas(123456L));
    assertEquals("1,234,567", GrouperUtil.formatNumberWithCommas(1234567L));
    assertEquals("123,456,789,123,456,789", GrouperUtil.formatNumberWithCommas(123456789123456789L));

  }
  
  /**
   * test encrypt sha
   */
  public void testEncryptSha() {
    assertEquals("9yAXSF+/ZCNJm6+bJA2qFPXwlaE=", GrouperUtil.encryptSha("This is a string"));
  }
  
  /**
   * 
   */
  public void testParentStemNames() {
    Set<String> stemNames = GrouperUtil.findParentStemNames("a:b:c:d");
    assertEquals(4, stemNames.size());
    List<String> stemNamesList = new ArrayList<String>(stemNames);

    assertEquals(":", stemNamesList.get(0));
    assertEquals("a", stemNamesList.get(1));
    assertEquals("a:b", stemNamesList.get(2));
    assertEquals("a:b:c", stemNamesList.get(3));
    
  }

  /**
   * 
   */
  public void testParentStemNameFromName() {
    assertNull(GrouperUtil.parentStemNameFromName("a", true));
    assertEquals(":", GrouperUtil.parentStemNameFromName("a", false));
    assertEquals("a", GrouperUtil.parentStemNameFromName("a:b", false));
    assertNull(GrouperUtil.parentStemNameFromName(":", true));
    assertNull(GrouperUtil.parentStemNameFromName("", true));
    assertNull(GrouperUtil.parentStemNameFromName(null, true));
    assertNull(GrouperUtil.parentStemNameFromName(":", false));
    assertNull(GrouperUtil.parentStemNameFromName("", false));
    assertNull(GrouperUtil.parentStemNameFromName(null, false));
    
  }

  /**
   * 
   */
  public void testParentStemNames2() {
    
    Group group1 = new Group();
    group1.setNameDb("a:b:c:d");
    Group group2 = new Group();
    group2.setNameDb("a:d:r");
    
    Set<String> stemNames = GrouperUtil.findParentStemNames(GrouperUtil.toSet(group1, group2));
    assertEquals(5, stemNames.size());
    assertTrue(stemNames.contains(":"));
    assertTrue(stemNames.contains("a"));
    assertTrue(stemNames.contains("a:b"));
    assertTrue(stemNames.contains("a:b:c"));
    assertTrue(stemNames.contains("a:d"));
    
  }
  
  /**
   * 
   */
  public void splitTrim() {
    String string = "a:b :::: b:c";
    String[] stringArray = GrouperUtil.splitTrim(string, "::::");
    assertEquals(2, stringArray.length);
    assertEquals("a:b", stringArray[0]);
    assertEquals("b:c", stringArray[1]);
  }
  
  /**
   * test strip suffix
   */
  public void testStringSuffix() {
    assertEquals("whatever", GrouperUtil.stripSuffix("whatever", "what"));
    assertEquals("whatever", GrouperUtil.stripSuffix("whatever", "eve"));
    assertEquals(null, GrouperUtil.stripSuffix(null, null));
    assertEquals("a", GrouperUtil.stripSuffix("a", null));
    assertEquals(null, GrouperUtil.stripSuffix(null, "a"));
    assertEquals("what", GrouperUtil.stripSuffix("whatever", "ever"));
  }
  
  /**
   * 
   */
  public void testConvertMillisToFriendlyString() {
    assertEquals("30ms", GrouperUtil.convertMillisToFriendlyString(30L));
    assertEquals("4s, 30ms", GrouperUtil.convertMillisToFriendlyString(4030L));
    assertEquals("5m, 4s, 30ms", GrouperUtil.convertMillisToFriendlyString(304030L));
    assertEquals("6h, 5m, 4s, 30ms", GrouperUtil.convertMillisToFriendlyString(21904030L));
    assertEquals("7d, 6h, 5m, 4s, 30ms", GrouperUtil.convertMillisToFriendlyString(626704030L));
  }
  
  /**
   * 
   */
  public void testFixHibernateConnectionUrl() {
    Properties properties = new Properties();
    String hibKey = "hibernate.connection.url";
    properties.put(hibKey, "jdbc:hsqldb:file:whatever");
    String oldGrouperHome = GrouperUtil.grouperHome;
    try {
      GrouperUtil.grouperHome = "/something";
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:/something" + File.separator + "whatever", properties.get(hibKey));
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:/something" + File.separator + "whatever", properties.get(hibKey));
  
      properties.put(hibKey, "jdbc:hsqldb:file:whatever");
      GrouperUtil.grouperHome = "/something/";
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:/something/whatever", properties.get(hibKey));
  
      properties.put(hibKey, "jdbc:hsqldb:file:/whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:/whatever", properties.get(hibKey));
      
      properties.put(hibKey, "jdbc:hsqldb:file:\\whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:\\whatever", properties.get(hibKey));
      
      properties.put(hibKey, "jdbc:hsqldb:file:c:/whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:c:/whatever", properties.get(hibKey));
      
  
      //########################
      //try without file
      properties.put(hibKey, "jdbc:hsqldb:whatever");
      GrouperUtil.grouperHome = "/something";
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:/something" + File.separator + "whatever", properties.get(hibKey));
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:/something" + File.separator + "whatever", properties.get(hibKey));
  
      properties.put(hibKey, "jdbc:hsqldb:whatever");
      GrouperUtil.grouperHome = "/something/";
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:/something/whatever", properties.get(hibKey));
  
      properties.put(hibKey, "jdbc:hsqldb:/whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:/whatever", properties.get(hibKey));
      
      properties.put(hibKey, "jdbc:hsqldb:\\whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:\\whatever", properties.get(hibKey));
      
      properties.put(hibKey, "jdbc:hsqldb:c:/whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:c:/whatever", properties.get(hibKey));
    } finally {
      GrouperUtil.grouperHome = oldGrouperHome;
    }
  }
  
  /**
   * test utility method
   */
  public void testArgAfter() {
    assertEquals("arg1", GrouperUtil.argAfter(new String[]{"a", "arg0", "arg1"}, "arg0"));
    assertEquals("arg1", GrouperUtil.argAfter(new String[]{"a", "arg0", "arg1", "b"}, "arg0"));
    assertNull(GrouperUtil.argAfter(new String[]{"a", "arg0", "arg1"}, "arg1"));
    try {
      GrouperUtil.argAfter(new String[]{"a", "arg0", "arg1"}, "arg2");
      fail("Should throw exception");
    } catch (Exception e) {
      //good
    }
  }
  
  /**
   * 
   * @throws AttributeNotFoundException
   */
  public void testCopyObjectFields() throws AttributeNotFoundException {
    SessionHelper.getRootSession();

    Group groupFrom = new Group();
    Map attributes = new HashMap();
    attributes.put("a", "b");
    GrouperUtil.assignField(groupFrom, Group.FIELD_CREATOR_UUID, "abc");
    
    Group groupTo = new Group();
    GrouperUtil.cloneFields(groupFrom, groupTo, 
        GrouperUtil.toSet(Group.FIELD_CREATOR_UUID, Group.FIELD_CREATE_TIME));
    
    assertEquals("abc", groupTo.getCreatorUuid());
    assertEquals(0, groupTo.getCreateTimeLong());
    
  }
  
  /**
   * see if testing for equal maps work
   */
  public void testMapEquals() {
    
    Map<String, String> first = null;
    Map<String, String> second = null;
    
    assertTrue("nulls should be equal", GrouperUtil.mapEquals(first, second));
    
    first = new HashMap<String, String>();
    
    assertTrue("null is equal to empty", GrouperUtil.mapEquals(first, second));
    
    first.put("key1", "value1");
    
    assertFalse("null is not empty to map with size", GrouperUtil.mapEquals(first, second));
    assertTrue("map is equal to self", GrouperUtil.mapEquals(first, first));
    
    second = new HashMap<String, String>();
    second.put("key1", "value2");
    
    assertFalse("not equal if same size, different values", GrouperUtil.mapEquals(first, second));
    
    second.put("key1", "value1");
    
    assertTrue("equal if same size, same keys/values", GrouperUtil.mapEquals(first, second));
    
    first.put("key2", "value2");
    assertFalse("not equal if different size", GrouperUtil.mapEquals(first, second));
    assertFalse("not equal if different size", GrouperUtil.mapEquals(second, first));
    
    second.put("key2", "value2");
    assertTrue("equal if same size, same keys/values", GrouperUtil.mapEquals(first, second));
    
    second.put("key3", "value2");
    second.remove("key2");
    assertFalse("not equal if same size, different keys", GrouperUtil.mapEquals(first, second));
    assertFalse("not equal if same size, different keys(reversed)", GrouperUtil.mapEquals(second, first));
  }
  
  /**
   * see if testing for equal maps work
   */
  public void testMapDifferences() {
    
    Map<String, String> first = null;
    Map<String, String> second = null;
    Set<String> differences = new LinkedHashSet<String>();
    
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");

    assertTrue("nulls should be equal", differences.size() == 0);
    
    first = new LinkedHashMap<String, String>();
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    
    assertEquals("null is equal to empty", 0, differences.size());
    
    first.put("key1", "value1");
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    
    assertEquals("First should have a size of 1", 1, first.size());
    
    assertEquals("null is not equal to map with size", 1, differences.size());
    assertEquals("null is not equal to map with size", (String)differences.toArray()[0],"attribute__key1");

    differences.clear();
    //try reversed
    GrouperUtil.mapDifferences(second, first, differences, "attribute__");

    assertEquals("null is not equal to map with size(reversed)", 1, differences.size());
    assertEquals("null is not equal to map with size(reversed)", (String)differences.toArray()[0],"attribute__key1");
    
    differences.clear();
    GrouperUtil.mapDifferences(first, first, differences, "attribute__");
    assertEquals("map equal to self", 0, differences.size());
    
    GrouperUtil.mapDifferences(first, new HashMap<String, String>(first), differences, "attribute__");
    assertEquals("map equal to clone self", 0, differences.size());
    
    second = new LinkedHashMap<String, String>();
    second.put("key1", "value2");
    
    GrouperUtil.mapDifferences(first, second, differences, null);
    
    assertEquals("Second should still have size 1", 1, second.size());
    
    assertEquals("not equal if same size, different values", 1, differences.size());
    assertEquals("not equal if same size, different values", "key1", (String)differences.toArray()[0]);
    
    second.put("key1", "value1");
    differences.clear();
    GrouperUtil.mapDifferences(first, first, differences, "attribute__");
    
    assertEquals("equal if same size, same keys/values", 0, differences.size());
    
    first.put("key2", "value2");
    differences.clear();
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    assertEquals("not equal if different size", 1, differences.size());
    assertEquals("not equal if different size", "attribute__key2", (String)differences.toArray()[0]);
    
    second.put("key2", "value2");
    differences.clear();
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    assertEquals("equal if same size, same keys/values", 0, differences.size());
    
    second.put("key3", "value2");
    second.remove("key2");
    differences.clear();
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    assertEquals("not equal if same size, different keys", 2, differences.size());
    assertEquals("not equal if same size, different keys", "attribute__key2", (String)differences.toArray()[0]);
    assertEquals("not equal if same size, different keys", "attribute__key3", (String)differences.toArray()[1]);

    first.put("key4", "value4");
    differences.clear();
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    assertEquals("not equal if different size, different keys", 3, differences.size());
    assertEquals("not equal if same size, different keys.  first map keys first", "attribute__key2", (String)differences.toArray()[0]);
    assertEquals("not equal if same size, different keys.  first map keys first", "attribute__key4", (String)differences.toArray()[1]);
    assertEquals("not equal if same size, different keys.  missing from first map keys second", "attribute__key3", (String)differences.toArray()[2]);
    
  }
  
  /**
   * make sure exceptions inject
   */
  public void testInjectException() {
    Exception e = new Exception();
    GrouperUtil.injectInException(e, "hey");
    assertEquals("hey", e.getMessage());
    GrouperUtil.injectInException(e, "there");
    assertTrue(e.getMessage().contains("hey"));
    assertTrue(e.getMessage().contains("there"));
    
  }
  
  /**
   * test replacing of strings
   */
  public void testReplace() {
    //here is a test case with 3 possible replacements, one will match "Groups"
    String groupsString = "<span class=\"tooltip\" onmouseover=\"grouperTooltip('Groups are many " +
    "people or groups');\">Groups</span>";
    List<String> keysList = GrouperUtil.toList("groups", "Groups", "the hierarchy");
    List<String> valuesList = GrouperUtil.toList("<span class=\"tooltip\" onmouseover=\"grouperTooltip('Groups " +
        "are many people or groups');\">groups</span>", 
        groupsString, "<span class=\"tooltip\" " +
            "onmouseover=\"grouperTooltip('A hierarchy is where each node has " +
            "zero or one parents and zero or many children  A hierarchy is where " +
            "each node has zero or one parents and zero or many children  A hierarchy " +
            "is where each node has zero or one parents and zero or many children  A " +
            "hierarchy is where each node has zero or one parents and zero or many " +
            "children ');\">the hierarchy</span>");
    String result = GrouperUtil.replace("All Groups", 
        keysList,
        valuesList, false, true);

    assertEquals(result, "All " + groupsString);
    
    //at this point the keys and values should have one fewer item
    assertEquals(2, keysList.size());
    assertEquals(2, valuesList.size());
  }

  /**
   * test indent
   */
  public void testIndent() {
    String indented = GrouperUtil.indent("<a><b whatever=\"whatever\"><c>hey</c><d><e>there</e><f /><g / ><h></h></d></b></a>", true);
    String expected = "<a>\n  <b whatever=\"whatever\">\n    <c>hey</c>\n    <d>\n      <e>there</e>\n      <f />\n      <g / >\n      <h></h>\n    </d>\n  </b>\n</a>";
    assertEquals("\n\nExpected:\n" + expected + "\n\nresult:\n" + indented + "\n\n", expected, indented);

    indented = GrouperUtil.indent("<a><!-- comment --><!-- another comment --><b whatever=\"whatever\"><!-- hey --><c>hey</c><d><e>there</e><f /><g / ><h></h></d></b></a>", true);
    expected = "<a>\n  <!-- comment -->\n  <!-- another comment -->\n  <b whatever=\"whatever\">\n    <!-- hey -->\n    <c>hey</c>\n    <d>\n      <e>there</e>\n      <f />\n      <g / >\n      <h></h>\n    </d>\n  </b>\n</a>";
    assertEquals("\n\nExpected:\n" + expected + "\n\nresult:\n" + indented + "\n\n", expected, indented);

  }
  
  /**
   * test indent
   */
  public void testIndentDoctype() {
    String xml = "<?xml version='1.0' encoding='iso-8859-1'?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><a><b whatever=\"whatever\"><c>hey</c><d><e>there</e><f /><g / ><h></h></d></b></a>";
    String indented = GrouperUtil.indent(xml, true);
    String expected = "<?xml version='1.0' encoding='iso-8859-1'?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<a>\n  <b whatever=\"whatever\">\n    <c>hey</c>\n    <d>\n      <e>there</e>\n      <f />\n      <g / >\n      <h></h>\n    </d>\n  </b>\n</a>";
    assertEquals("\n\nExpected:\n" + expected + "\n\nresult:\n" + indented + "\n\n", expected, indented);
  }

  /**
   * indent json
   */
  public void testIndentJson() {
    String json = "{\"a\":{\"b\\\"b\":{\"c\\\\\":\"d\"},\"e\":\"f\",\"g\":[\"h\":\"i\"]}}";
    String indented = GrouperUtil.indent(json, true);
    String expected = "{\n  \"a\":{\n    \"b\\\"b\":{\n      \"c\\\\\":\"d\"\n    },\n    \"e\":\"f\",\n    \"g\":[\n      \"h\":\"i\"\n    ]\n  }\n}";
    assertEquals("\n\nExpected:\n" + expected + "\n\nresult:\n" + indented + "\n\n", expected, indented);
  }
  
  private static JsonConfig jsonConfig = new JsonConfig();  
  
  static {
  
    jsonConfig.setJsonPropertyFilter( new PropertyFilter() {
      
      public boolean apply(Object source, String name, Object value) {
        if( value != null && value instanceof Map ){
          Map map = (Map)value;
          if (map.size() > 0 && !(map.keySet().iterator().next() instanceof String)) {
            return true;
          }
        }  
        return false;
      }
    });
  }
  
  /**
   * 
   */
  public void testConvertJson() {
    Source isa = SourceManager.getInstance().getSource("g:isa");
    JSONObject jsonObject = net.sf.json.JSONObject.fromObject(isa, jsonConfig);  
    String json = jsonObject.toString();
    System.out.println(json);
  }
  
  /**
   * @param name
   */
  public GrouperUtilTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testBatching() {
    List<Integer> list = GrouperUtil.toList(0, 1, 2, 3, 4);
    assertEquals("3 batches, 0 and 1, 2 and 3, and 4", 3, GrouperUtil.batchNumberOfBatches(list, 2));
    List<Integer> listBatch = GrouperUtil.batchList(list, 2, 0);
    assertEquals(2, listBatch.size());
    assertEquals(0, (int)listBatch.get(0));
    assertEquals(1, (int)listBatch.get(1));
    
    listBatch = GrouperUtil.batchList(list, 2, 1);
    assertEquals(2, listBatch.size());
    assertEquals(2, (int)listBatch.get(0));
    assertEquals(3, (int)listBatch.get(1));
    
    listBatch = GrouperUtil.batchList(list, 2, 2);
    assertEquals(1, listBatch.size());
    assertEquals(4, (int)listBatch.get(0));
  }
 
  /**
   * 
   * @param group
   * @return string
   */
  public static String transformGroup(Group group) {
    return "hey: " + group.getNameDb();
  }
  
  /**
   * 
   */
  public void testSubstituteExpressionLanguage() {
    Group group = new Group();
    group.setNameDb("someName");
    Map<String, Object> substituteMap = new HashMap<String, Object>();
    substituteMap.put("group", group);
    String nameDb = null;
    
    nameDb = GrouperUtil.substituteExpressionLanguage("${group.nameDb} ${group.nameDb}", substituteMap);
    assertEquals("someName someName", nameDb);
    
    nameDb = GrouperUtil.substituteExpressionLanguage("${group.nameDb.endsWith('Name')}", substituteMap);
    assertEquals("true", nameDb);
    
    nameDb = GrouperUtil.substituteExpressionLanguage("${!group.nameDb.endsWith('Name')}", substituteMap);
    assertEquals("false", nameDb);
    
    try {
      nameDb = GrouperUtil.substituteExpressionLanguage("${java.lang.System.currentTimeMillis()}", substituteMap);
      fail("Shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    nameDb = GrouperUtil.substituteExpressionLanguage("${java.lang.System.currentTimeMillis()}", substituteMap, true);
    assertTrue(Long.parseLong(nameDb) > 0);
    nameDb = GrouperUtil.substituteExpressionLanguage("${edu.internet2.middleware.grouper.util.GrouperUtilTest.transformGroup(group)}", substituteMap, true);
    assertEquals("hey: someName", nameDb);

    nameDb = GrouperUtil.substituteExpressionLanguage("${if (true) { 'hello'; } } ${group.nameDb}", substituteMap);
    assertEquals("hello someName", nameDb);
    nameDb = GrouperUtil.substituteExpressionLanguage("${if (true) { if (true){ 'hello'; }}} ${group.nameDb}", substituteMap);
    assertEquals("hello someName", nameDb);
    nameDb = GrouperUtil.substituteExpressionLanguage("${if (true) { if (true){ 'hello'; }}} ${if (true) { if (true){ 'hello'; }}} ${group.nameDb}", substituteMap);
    assertEquals("hello hello someName", nameDb);
    
    
    
  }

  /**
   * some method to call
   * @param arg1
   * @param beanA
   * @param beanB
   * @param arg4
   * @return theVal
   */
  public String someMethod(String arg1, BeanA beanA, BeanB[] beanB, String arg4) {
    beanB[0] = null;
    return "success";
  }

  /**
   * see if calling method with more params works
   */
  public void testCallMethodWithMoreParams() {
    
    BeanB[] beanBs = new BeanB[]{new BeanB()};
    
    Object result = GrouperUtil.callMethodWithMoreParams(new GrouperUtilTest(), GrouperUtilTest.class, "someMethod", 
      new Object[]{"hey", new BeanA(), beanBs});
    
    assertEquals("success", result);
    
    assertNull(beanBs[0]);
    
    //should work with same number of params
    beanBs = new BeanB[]{new BeanB()};
    
    result = GrouperUtil.callMethodWithMoreParams(new GrouperUtilTest(), GrouperUtilTest.class, "someMethod", 
      new Object[]{"hey", new BeanA(), beanBs, "there"});
    
    assertEquals("success", result);
    
    assertNull(beanBs[0]);
    
    //shouldnt work with more params
    try {
      GrouperUtil.callMethodWithMoreParams(new GrouperUtilTest(), GrouperUtilTest.class, "someMethod", 
        new Object[]{"hey", new BeanA(), beanBs, "there", "you"});
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    //shouldnt work with wrong type of params
    try {
      GrouperUtil.callMethodWithMoreParams(new GrouperUtilTest(), GrouperUtilTest.class, "someMethod", 
        new Object[]{"hey", beanBs, new BeanA(), "there", "you"});
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    
    
  }

  public GrouperUtilTest() {
    super();
  }

  /**
   * test versioning
   */
  public void testVersioning() {
    BeanA beanA = null;
    
    String v2package = "edu.internet2.middleware.grouper.util.versioningV2";
  
    assertNull(GrouperUtil.changeToVersion(beanA, v2package));
    
    beanA = new BeanA();
    
    edu.internet2.middleware.grouper.util.versioningV2.BeanA v2BeanA = 
      (edu.internet2.middleware.grouper.util.versioningV2.BeanA)GrouperUtil.changeToVersion(beanA, v2package);
    
    assertNotNull(v2BeanA);
    
    assertNull(v2BeanA.getField1());
    assertNull(v2BeanA.getField1b());
    assertNull(v2BeanA.getField2());
    assertNull(v2BeanA.getField3());
    assertNull(v2BeanA.getField4());
  
    beanA.setField1("field1");
    beanA.setField1a("field1a");
    beanA.setField2(new String[]{"a", "b"});
    {
      BeanB beanB = new BeanB();
      beanB.setFieldB1("beanB");
      beanB.setFieldB2(new String[]{"beanBa", "beanBb"});
      beanA.setField3(beanB);
    }
    BeanB beanB1 = new BeanB();
    beanB1.setFieldB1("beanB1");
    beanB1.setFieldB2(new String[]{"beanBa1", "beanBb1"});
    
    BeanB beanB2 = new BeanB();
    beanB2.setFieldB1("beanB2");
    beanB2.setFieldB2(new String[]{"beanBa2", "beanBb2"});
  
    beanA.setField4(new BeanB[]{beanB1, beanB2});
    
    v2BeanA = (edu.internet2.middleware.grouper.util.versioningV2.BeanA)GrouperUtil.changeToVersion(beanA, v2package);
    
    assertEquals("field1", v2BeanA.getField1());
    assertNull(v2BeanA.getField1b());
    assertEquals(2, GrouperUtil.length(v2BeanA.getField2()));
    assertEquals("a", v2BeanA.getField2()[0]);
    assertEquals("b", v2BeanA.getField2()[1]);
    
    assertNotNull(v2BeanA.getField3());
    assertEquals("beanB", v2BeanA.getField3().getFieldB1());
    assertNull(v2BeanA.getField3().getFieldB1a());
    assertEquals(2, GrouperUtil.length(v2BeanA.getField3().getFieldB2()));
    assertEquals("beanBa", v2BeanA.getField3().getFieldB2()[0]);
    assertEquals("beanBb", v2BeanA.getField3().getFieldB2()[1]);
  
    assertEquals(2, GrouperUtil.length(v2BeanA.getField4()));
    assertEquals("beanB1", v2BeanA.getField4()[0].getFieldB1());
    assertEquals("beanBa1", v2BeanA.getField4()[0].getFieldB2()[0]);
    assertNull(v2BeanA.getField4()[0].getFieldB1a());
    assertEquals("beanBb1", v2BeanA.getField4()[0].getFieldB2()[1]);
    
    assertEquals("beanB2", v2BeanA.getField4()[1].getFieldB1());
    assertEquals("beanBa2", v2BeanA.getField4()[1].getFieldB2()[0]);
    assertNull(v2BeanA.getField4()[1].getFieldB1a());
    assertEquals("beanBb2", v2BeanA.getField4()[1].getFieldB2()[1]);
    
  }
  
  public void testIpOnNetworks() {
    
    String ipString = "1.2.3.4";
    String networkIpStrings = "1.2.3.5,1.2.3.6";
    boolean ipOnNetworks = GrouperUtil.ipOnNetworks(ipString, networkIpStrings);
    assertFalse(ipOnNetworks);
    
    ipString = "1.2.3.4";
    networkIpStrings = "1.2.3.4,1.2.3.6";
    ipOnNetworks = GrouperUtil.ipOnNetworks(ipString, networkIpStrings);
    assertTrue(ipOnNetworks);
    
    ipString = "1.2.3.4";
    networkIpStrings = "1.2.3.0/10";
    ipOnNetworks = GrouperUtil.ipOnNetworks(ipString, networkIpStrings);
    assertTrue(ipOnNetworks);
    
    ipString = "1.2.3.4";
    networkIpStrings = "0.0.0.0/0";
    ipOnNetworks = GrouperUtil.ipOnNetworks(ipString, networkIpStrings);
    assertTrue(ipOnNetworks);
    
    ipString = "1.2.3.4";
    networkIpStrings = "0.0.0.0.0.0/0";
    ipOnNetworks = GrouperUtil.ipOnNetworks(ipString, networkIpStrings);
    assertTrue(ipOnNetworks);

    ipString = "1.2.3.4";
    networkIpStrings = "2001:db8:3333:4444:CCCC:DDDD:EEEE:FFFF";
    ipOnNetworks = GrouperUtil.ipOnNetworks(ipString, networkIpStrings);
    assertFalse(ipOnNetworks);
    
    ipString = "0.0.0.0.0.1";
    networkIpStrings = "0.0.0.0.0.0/0";
    ipOnNetworks = GrouperUtil.ipOnNetworks(ipString, networkIpStrings);
    assertTrue(ipOnNetworks);

    ipString = "2001:db8:3333:4444:CCCC:DDDD:EEEE:FFFF";
    networkIpStrings = "2001:db8:3333:4444:CCCC:DDDD:EEEE:FFFF";
    ipOnNetworks = GrouperUtil.ipOnNetworks(ipString, networkIpStrings);
    assertTrue(ipOnNetworks);
    
  }
  
}
