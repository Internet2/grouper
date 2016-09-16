/**
 * Copyright 2016 Internet2
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
package edu.internet2.middleware.grouper.instrumentation;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.audit.AuditTypeFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

/**
 * @author shilen
 */
@DisallowConcurrentExecution
public class TierInstrumentationDaemon implements Job {
  
  private static final Log LOG = GrouperUtil.getLog(TierInstrumentationDaemon.class);
  
  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    long startTime = System.currentTimeMillis();
    long startTimeMinus24Hours = startTime - 86400000L;
    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.startRootSession();
      GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

      String jobName = context.getJobDetail().getKey().getName();

      hib3GrouploaderLog.setJobName(jobName);
      hib3GrouploaderLog.setHost(GrouperUtil.hostname());
      hib3GrouploaderLog.setStartedTime(new Timestamp(startTime));
      hib3GrouploaderLog.setJobType("OTHER_JOB");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
      hib3GrouploaderLog.store();
      
      LOG.info("Running TIER instrumentation daemon.");
      
      Map<String, Object> data = new LinkedHashMap<String, Object>();
      data.put("reportFormat", 1);
      data.put("component", "grouper");
      data.put("institution", getInstitution());
      data.put("environment", GrouperConfig.retrieveConfig().getProperty("grouper.env.name", ""));
      data.put("version", GrouperVersion.GROUPER_VERSION);
      data.put("platformWindows", SystemUtils.IS_OS_WINDOWS);
      data.put("platformLinux", SystemUtils.IS_OS_LINUX);
      data.put("platformMac", SystemUtils.IS_OS_MAC);
      data.put("platformSolaris", SystemUtils.IS_OS_SOLARIS);
      
      if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob.tierInstrumentationDaemon.exclude.transactionCounts", false)) {
        Long membershipChanges = HibernateSession.byHqlStatic().createQuery("select count(*) from AuditEntry where createdOnDb > :createdOn and auditTypeId in (:auditTypeAdd, :auditTypeUpdate, :auditTypeDelete)")
          .setLong("createdOn", startTimeMinus24Hours)
          .setString("auditTypeAdd", AuditTypeFinder.find("membership", "addGroupMembership", true).getId())
          .setString("auditTypeUpdate", AuditTypeFinder.find("membership", "updateGroupMembership", true).getId())
          .setString("auditTypeDelete", AuditTypeFinder.find("membership", "deleteGroupMembership", true).getId())
          .uniqueResult(Long.class);
        
        data.put("transactionCountMemberships", membershipChanges);
  
        Long privilegeChanges = HibernateSession.byHqlStatic().createQuery("select count(*) from AuditEntry where createdOnDb > :createdOn and auditTypeId in (:auditTypeGroupAdd, :auditTypeGroupUpdate, :auditTypeGroupDelete, :auditTypeStemAdd, :auditTypeStemUpdate, :auditTypeStemDelete)")
          .setLong("createdOn", startTimeMinus24Hours)
          .setString("auditTypeGroupAdd", AuditTypeFinder.find("privilege", "addGroupPrivilege", true).getId())
          .setString("auditTypeGroupUpdate", AuditTypeFinder.find("privilege", "updateGroupPrivilege", true).getId())
          .setString("auditTypeGroupDelete", AuditTypeFinder.find("privilege", "deleteGroupPrivilege", true).getId())
          .setString("auditTypeStemAdd", AuditTypeFinder.find("privilege", "addStemPrivilege", true).getId())
          .setString("auditTypeStemUpdate", AuditTypeFinder.find("privilege", "updateStemPrivilege", true).getId())
          .setString("auditTypeStemDelete", AuditTypeFinder.find("privilege", "deleteStemPrivilege", true).getId())
          .uniqueResult(Long.class);
        
        data.put("transactionCountPrivileges", privilegeChanges);
      }
      
      if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob.tierInstrumentationDaemon.exclude.registryCounts", false)) {
        Long totalMemberships = HibernateSession.byHqlStatic().createQuery("select count(*) from ImmediateMembershipEntry m, Field f where m.fieldId=f.uuid and f.typeString = 'list'").uniqueResult(Long.class);
        data.put("registryCountDirectMemberships", totalMemberships);
        
        Long totalPrivileges = HibernateSession.byHqlStatic().createQuery("select count(*) from ImmediateMembershipEntry m, Field f where m.fieldId=f.uuid and f.typeString in ('access', 'naming', 'attributeDef')").uniqueResult(Long.class);
        data.put("registryCountDirectPrivileges", totalPrivileges);
        
        Long totalDirectPermissions = HibernateSession.byHqlStatic().createQuery("select count(*) from AttributeAssign aa, AttributeDef ad, AttributeDefName adn where aa.attributeDefNameId=adn.id and adn.attributeDefId=ad.id and ad.attributeDefTypeDb='perm'").uniqueResult(Long.class);
        data.put("registryCountDirectPermissions", totalDirectPermissions);
      }
      
      Map<String, String> changeLogJobs = GrouperLoaderConfig.retrieveConfig().propertiesMap( 
          GrouperCheckConfig.grouperLoaderConsumerPattern);
      data.put("provisionToLdapUsingPsp", changeLogJobs.containsValue("edu.internet2.middleware.psp.grouper.PspChangeLogConsumer"));
      data.put("provisionToLdapUsingPspng", changeLogJobs.containsValue("edu.internet2.middleware.grouper.pspng.PspChangelogConsumerShim"));
      
      if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob.tierInstrumentationDaemon.exclude.patchesInstalled", false)) {
        data.put("patchesInstalled", getPatchesInstalled());
      }
      
      sendToTier(data);
            
      LOG.info("Finished running TIER instrumentation daemon.");
      hib3GrouploaderLog.appendJobMessage("Finished running TIER instrumentation daemon.");
      
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
      storeLogInDb(hib3GrouploaderLog, true, startTime);
    } catch (Exception e) {
      LOG.error("Error running job", e);
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      hib3GrouploaderLog.appendJobMessage(ExceptionUtils.getFullStackTrace(e));
      
      if (!(e instanceof JobExecutionException)) {
        e = new JobExecutionException(e);
      }
      JobExecutionException jobExecutionException = (JobExecutionException)e;
      storeLogInDb(hib3GrouploaderLog, false, startTime);
      throw jobExecutionException;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * @param hib3GrouploaderLog
   * @param throwException 
   * @param startTime
   */
  private static void storeLogInDb(Hib3GrouperLoaderLog hib3GrouploaderLog,
      boolean throwException, long startTime) {
    //store this safely
    try {
      
      long endTime = System.currentTimeMillis();
      hib3GrouploaderLog.setEndedTime(new Timestamp(endTime));
      hib3GrouploaderLog.setMillis((int)(endTime-startTime));
      
      hib3GrouploaderLog.store();
      
    } catch (RuntimeException e) {
      LOG.error("Problem storing final log", e);
      //dont preempt an existing exception
      if (throwException) {
        throw e;
      }
    }
  }
  
  private static String getInstitution() {
    String institution = GrouperConfig.retrieveConfig().getProperty("grouper.institution.name", null);
    if (!StringUtils.isBlank(institution)) {
      return institution;
    }
    
    String hostname = GrouperUtil.hostname();
    if (StringUtils.isBlank(hostname)) {
      return "";
    }
    
    int index = StringUtils.indexOf(hostname, ".");
    
    if (index == -1) {
      return "";
    }
    
    return hostname.substring(index + 1);
  }
  
  // this should probably go somewhere else since the ui/ws are also going to want to call this.
  private static Set<String> getPatchesInstalled() {
    Set<String> patchesInstalled = new TreeSet<String>();

    File log4jFile = GrouperUtil.fileFromResourceName("log4j.properties");
    File confDir = log4jFile.getParentFile();
    File grouperDir = confDir.getParentFile();
    File patchIndexFile = new File(GrouperUtil.fileCanonicalPath(grouperDir) + File.separator + "grouperPatchStatus.properties");
    if (!patchIndexFile.exists()) {
      return patchesInstalled;
    }
    
    Properties props = GrouperUtil.propertiesFromFile(patchIndexFile, false);
    
    Pattern patchStatePattern = Pattern.compile("^grouper_v" + GrouperVersion.GROUPER_VERSION.replace(".", "_") + "_api_patch_(.*)\\.state$");
    for (String key : (Set<String>)(Object)props.keySet()) {
      Matcher matcher = patchStatePattern.matcher(key);

      if (matcher.matches() && "applied".equals(props.getProperty(key))) {
        String patchNumber = matcher.group(1);
        patchesInstalled.add("api" + patchNumber);
      }
    }
        
    return patchesInstalled;
  }
  
  private static void sendToTier(Map<String, Object> data) throws HttpException, IOException {
    String dataJson = GrouperUtil.jsonConvertTo(data, false);
    
    HttpClient httpClient = new HttpClient();
    String discoveryUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob.tierInstrumentationDaemon.discoveryUrl", "https://id.internet2.edu/ti/jrd/collector.json");
    GetMethod discoveryMethod = new GetMethod(discoveryUrl);
    int discoveryReturnCode = httpClient.executeMethod(discoveryMethod);
    String discoveryBody = IOUtils.toString(discoveryMethod.getResponseBodyAsStream());

    if (discoveryReturnCode != 200) {
      throw new RuntimeException("Bad response code from discovery url " + discoveryUrl + ".  Code=" + discoveryReturnCode + ", body=" + discoveryBody);
    }
    
    TierDiscovery tierDiscovery = GrouperUtil.jsonConvertFrom(discoveryBody, TierDiscovery.class);
    if (!tierDiscovery.isServiceEnabled()) {
      LOG.warn("TIER discovery service indicates that the TIER collector is disabled currently.");
      return;
    }
    
    List<String> uris = new LinkedList<String>();
    
    for (Map<String, String> endpoint : tierDiscovery.getEndpoints()) {
      String uri = endpoint.get("uri");
      if (!StringUtils.isBlank(uri)) {
        uris.add(uri + "/dailyReport");
      }
    }
    
    if (uris.size() == 0) {
      LOG.warn("TIER discovery service doesn't have any URIs for the TIER collector.");
      return;
    }
    
    Iterator<String> uriIterator = uris.iterator();
    while (uriIterator.hasNext()) {
      String uri = uriIterator.next();
      
      // try to send results here
      httpClient = new HttpClient();
      PostMethod collectorMethod = new PostMethod(uri);
      try {
        collectorMethod.setRequestEntity(new StringRequestEntity(dataJson, "application/json", "UTF-8"));
        int collectorReturnCode = httpClient.executeMethod(collectorMethod);
        String collectorBody = IOUtils.toString(collectorMethod.getResponseBodyAsStream());

        if (collectorReturnCode == 200 || collectorReturnCode == 201) {
          // we're good
          LOG.info("Successfully sent data to endpoint " + uri);
          break;
        }
        
        throw new RuntimeException("Failed to send data to endpoint " + uri + ".  Code=" + collectorReturnCode + ", body=" + collectorBody);
      } catch (Exception e) {
        if (uriIterator.hasNext()) {
          LOG.warn("Failed to send data to endpoint " + uri + ".  Will try another endpoint", e);
        } else {
          throw new RuntimeException(e);
        }
      }
    }
  }
}
