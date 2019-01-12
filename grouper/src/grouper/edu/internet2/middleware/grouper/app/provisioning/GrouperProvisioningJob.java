package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;

@DisallowConcurrentExecution
public class GrouperProvisioningJob extends OtherJobBase {
  
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    GrouperSession.startRootSession();
    
    updateMetadataOnDirectStemsChildren();
    updateMetadataOnIndirectGrouperObjects();
    
    return null;
  }
  
  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_grouperProvisioningDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperProvisioningJob().run(otherJobInput);
  }
  
  
  protected static List<Stem> updateMetadataOnDirectStemsChildren() {
    
    if (!GrouperProvisioningSettings.provisioningInUiEnabled()) {
      return new ArrayList<Stem>();
    }
    
    List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("true").findStems());
    
    
    for (Stem stem: stems) {
      List<GrouperProvisioningAttributeValue> attributeValues = GrouperProvisioningConfiguration.getProvisioningAttributeValues(stem);
      
      for (GrouperProvisioningAttributeValue attributeValue: attributeValues) {
        GrouperProvisioningConfiguration.saveOrUpdateProvisioningAttributes(attributeValue, stem);      
      }
      
    }
    
    return stems;
    
  }
  
  
  protected static void updateMetadataOnIndirectGrouperObjects() {
    
    if (!GrouperProvisioningSettings.provisioningInUiEnabled()) {
      return;
    }
    
    Set<GrouperObject> indirectGrouperObjects = new HashSet<GrouperObject>();
    
    List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("false").findStems());
    
    List<Group> groups = new ArrayList<Group>(new GroupFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("false").findGroups());
    
    indirectGrouperObjects.addAll(stems);
    indirectGrouperObjects.addAll(groups);
    
    for (GrouperObject grouperObject: indirectGrouperObjects) {
      GrouperProvisioningConfiguration.copyConfigFromParent(grouperObject);
    }
    
  }

}
