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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
@DisallowConcurrentExecution
public class TierInstrumentationDaemon extends OtherJobBase {
  
  private static final Log LOG = GrouperUtil.getLog(TierInstrumentationDaemon.class);
  
  /**
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
    
    /*
    String release = "NON_PACKAGE_" + GrouperVersion.GROUPER_VERSION;

    Map<String, Set<Integer>> patchesInstalled = new java.util.TreeMap<String, Set<Integer>>();
    patchesInstalled.put("api", new java.util.HashSet<Integer>());
    patchesInstalled.get("api").add(0);
    patchesInstalled.get("api").add(1);
    patchesInstalled.get("api").add(2);
    patchesInstalled.get("api").add(5);
    
    patchesInstalled.put("pspng", new java.util.HashSet<Integer>());
    patchesInstalled.get("pspng").add(0);
    patchesInstalled.get("pspng").add(1);


    List<String> patchStrings = new ArrayList<String>();
    for (String engine : patchesInstalled.keySet()) {
      String engineLabel = engine.substring(0, 1);
      List<Integer> patchNumbers = new ArrayList<Integer>(patchesInstalled.get(engine));
      Collections.sort(patchNumbers);
      
      if (patchNumbers.size() > 0) {
        Integer maxConsecutive = null;
        boolean othersAfter = false;
        for (int i = 0; i < patchNumbers.size(); i++) {
          if (i == patchNumbers.get(i)) {
            maxConsecutive = i;
          } else {
            othersAfter = true;
            break;
          }
        }
        
        if (maxConsecutive == null) {
          patchStrings.add(engineLabel + "UnknownPatches");
        } else {
          if (othersAfter) {
            patchStrings.add(engineLabel + maxConsecutive + "AndUnknownPatches");
          } else {
            patchStrings.add(engineLabel + maxConsecutive);
          }
        }
      }
    }
        
    if (patchStrings.size() > 0) {
      release = release + "-" + String.join("-", patchStrings);
    }
    
    System.out.println(release);
    */
  }

  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_tierInstrumentationDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new TierInstrumentationDaemon().run(otherJobInput);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    Map<String, String> dataForTier = new LinkedHashMap<String, String>();
    /*
    try {
      dataForTier.put("host", InetAddress.getLocalHost().getHostAddress());
    } catch (UnknownHostException e1) {
      LOG.warn("Unable to get ip address, proceeding without it", e1);
    }*/
    
    dataForTier.put("msgType", "TIERBEACON");
    dataForTier.put("msgName", "TIER");
    dataForTier.put("msgVersion", "1.0");
    dataForTier.put("tbProduct", "Grouper");
    dataForTier.put("tbProductVersion", GrouperVersion.GROUPER_VERSION);
        
    if (!StringUtils.isEmpty(System.getenv("GROUPER_CONTAINER_VERSION"))) {
      dataForTier.put("tbTIERRelease", "PACKAGE_" + System.getenv("GROUPER_CONTAINER_VERSION"));
    } else {
      String release = "NON_PACKAGE_" + GrouperVersion.GROUPER_VERSION;
      
      Map<String, Set<Integer>> patchesInstalled = GrouperVersion.patchesInstalled();

      List<String> patchStrings = new ArrayList<String>();
      for (String engine : patchesInstalled.keySet()) {
        String engineLabel = engine.substring(0, 1);
        List<Integer> patchNumbers = new ArrayList<Integer>(patchesInstalled.get(engine));
        Collections.sort(patchNumbers);
        
        if (patchNumbers.size() > 0) {
          Integer maxConsecutive = null;
          boolean othersAfter = false;
          for (int i = 0; i < patchNumbers.size(); i++) {
            if (i == patchNumbers.get(i)) {
              maxConsecutive = i;
            } else {
              othersAfter = true;
              break;
            }
          }
          
          if (maxConsecutive == null) {
            patchStrings.add(engineLabel + "UnknownPatches");
          } else {
            if (othersAfter) {
              patchStrings.add(engineLabel + maxConsecutive + "AndUnknownPatches");
            } else {
              patchStrings.add(engineLabel + maxConsecutive);
            }
          }
        }
      }
          
      if (patchStrings.size() > 0) {
        release = release + "-" + String.join("-", patchStrings);
      }
      
      dataForTier.put("tbTIERRelease", release); 
    }
    
    // sleep for random time between 0 and 10 minutes so collector doesn't get hit all at once..
    int sleepTime = ThreadLocalRandom.current().nextInt(0, 600000);
    LOG.info("Sleeping for " + sleepTime + " milliseconds");

    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      // ignore
    }
    
    try {
      sendToTier(dataForTier);
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished successfully running TIER instrumentation daemon.");
    } catch (Exception e) {
      LOG.warn("Error while sending instrumentation data to TIER", e);
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished running TIER instrumentation daemon but received an error while sending data to TIER: " + ExceptionUtils.getFullStackTrace(e));
    } finally {
      otherJobInput.getHib3GrouperLoaderLog().store();
    }

    return null;
  }
  
  private static void sendToTier(Map<String, String> data) throws IOException {
    String dataJson = GrouperUtil.jsonConvertTo(data, false);
    //System.out.println(dataJson);
    
    HttpClient httpClient = new HttpClient();
    httpClient.getParams().setSoTimeout(60000);
    httpClient.getParams().setParameter("http.connection.timeout", 60000);
    String collectorUrl = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob.tierInstrumentationDaemon.collectorUrl");
    
    PostMethod method = new PostMethod(collectorUrl);
    method.setRequestEntity(new StringRequestEntity(dataJson, "application/json", "UTF-8"));
    int collectorReturnCode = httpClient.executeMethod(method);
    String collectorBody = IOUtils.toString(method.getResponseBodyAsStream());

    if (collectorReturnCode == 200 || collectorReturnCode == 201) {
      // we're good
      LOG.info("Successfully sent data to endpoint " + collectorUrl);
    } else {    
      throw new RuntimeException("Failed to send data to endpoint " + collectorUrl + ".  Code=" + collectorReturnCode + ", body=" + collectorBody);    
    }
  }
  
  /*
  private static void legacyInstrumentationCode(String jobName) {

    long startTime = System.currentTimeMillis();
    long startTimeMinus24Hours = startTime - 86400000L;
    
    AttributeAssign parentAssignment = InstrumentationDataUtils.grouperInstrumentationCollectorParentAttributeAssignment(jobName);

    String lastCollectorUpdateString = parentAssignment.getAttributeValueDelegate().retrieveValueString(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_LAST_UPDATE_ATTR);
    long lastCollectorUpdate = 0;
    if (!StringUtils.isEmpty(lastCollectorUpdateString)) {
      lastCollectorUpdate = Long.parseLong(lastCollectorUpdateString);
    }
    
    String collectorUuid = parentAssignment.getAttributeValueDelegate().retrieveValueString(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_UUID_ATTR);
          
    Map<String, Object> data = new LinkedHashMap<String, Object>();
    data.put("reportFormat", 1);
    data.put("uuid", collectorUuid);
    data.put("component", "grouper");
    data.put("institution", getInstitution());
    data.put("environment", GrouperConfig.retrieveConfig().getProperty("grouper.env.name", ""));
    
    if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob.tierInstrumentationDaemon.exclude.version", false)) {
      data.put("version", GrouperVersion.GROUPER_VERSION);
    }
    
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
    
    if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob.tierInstrumentationDaemon.exclude.instanceData", false)) {
      data.put("instances", getInstances(lastCollectorUpdate, startTime));
    }
    
    //System.out.println(GrouperUtil.jsonConvertTo(data, false));
    try {
      sendToTier(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // set new last updated
    parentAssignment.getAttributeValueDelegate().assignValue(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_LAST_UPDATE_ATTR, "" + startTime);
    
  }
  
  
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
  
  
  private static List<JSONObject> getInstances(long lastCollectorUpdate, long currentCollectorStart) {
    
    List<JSONObject> list = new LinkedList<JSONObject>();
    
    List<InstrumentationDataInstance> instances = InstrumentationDataInstanceFinder.findAll(true);
    for (InstrumentationDataInstance instance : instances) {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("uuid", instance.getUuid());
      jsonObject.put("engineName", instance.getEngineName());
      jsonObject.put("serverLabel", instance.getServerLabel());
      
      if (instance.getLastUpdate() != null) {
        jsonObject.put("lastUpdate", instance.getLastUpdate().getTime());
      }
      
      Set<String> newCounts = new LinkedHashSet<String>();

      List<InstrumentationDataInstanceCounts> instanceCountsList = instance.getCounts();
      for (InstrumentationDataInstanceCounts instanceCounts : instanceCountsList) {
        if (instanceCounts.getCreatedOn().getTime() >= lastCollectorUpdate && instanceCounts.getCreatedOn().getTime() < currentCollectorStart) {
          Map<String, Long> currentNewCounts = new LinkedHashMap<String, Long>();
          currentNewCounts.put("startTime", instanceCounts.getStartTime().getTime());
          currentNewCounts.put("duration", instanceCounts.getDuration());
          for (String key : instanceCounts.getCounts().keySet()) {
            currentNewCounts.put(key, instanceCounts.getCounts().get(key));
          }
          
          newCounts.add(GrouperUtil.jsonConvertTo(currentNewCounts, false));
        }
      }
      
      jsonObject.put("newCounts", newCounts);
      
      list.add(jsonObject);
    }
    
    return list;
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
    String discoveryUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob.tierInstrumentationDaemon.discoveryUrl", "https://id.internet2.edu/ti/jrd/collector");
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
        uris.add(uri);
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
  */
}
