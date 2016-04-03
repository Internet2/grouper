/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.changeLog;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.changeLog.consumer.PrintChangeLogConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class ChangeLogConsumerBaseImplTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ChangeLogConsumerBaseImplTest("testRemoveAttributeFromFolder"));
  }
  
  /**
   * @param name
   */
  public ChangeLogConsumerBaseImplTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public ChangeLogConsumerBaseImplTest() {
    
  }

  /**
   * 
   */
  private AttributeDef provisioningMarkerAttributeDef = null;
  
  /**
   * 
   */
  private AttributeDefName provisioningMarkerAttributeName = null;
  
  /**
   * 
   */
  private final String JOB_NAME = "testing123123";
  
  /**
   * 
   */
  @Override
  public void setUp() {
    super.setUp();

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".class", 
        "edu.internet2.middleware.grouper.changeLog.consumer.PrintChangeLogConsumer");
    
    //something that will never fire
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".quartzCron", 
        "0 0 5 * * 2000");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    this.provisioningMarkerAttributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("attr:someAttrDef").assignToStem(true).assignToGroup(true).save();
    
    this.provisioningMarkerAttributeName = new AttributeDefNameSave(grouperSession, this.provisioningMarkerAttributeDef)
        .assignName("attr:provisioningMarker").save();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".syncAttributeName", 
        this.provisioningMarkerAttributeName.getName());
    
    GrouperSession.stopQuietly(grouperSession);
    
    PrintChangeLogConsumer.eventsProcessed.clear();
  }
  
  /**
   * 
   */
  public void testMarkParentFolder() {
    // Grouper action: 1.0 Place a marker on a folder
    // Target outcome: add all the groups under that folder and any subfolder, and all the group memberships
    // Test 1.0.1: Marking a parent folder
    // 1) setup folder structure with groups, sub folders, and groups in sub folders
    // 2) place syncAttribute marker on parent folder
    // Outcome:
    // 1) all groups within folder structure added to the target
    
    GrouperSession gs = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add group1 and membership to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String group1Name = parentFolderName + ":group1";
    Group group1 = new GroupSave(gs).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    group1.addMember(bill);

    // add group2 and membership to subfolder
    String subFolderName = parentFolderName + ":subFolder";
    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(gs).assignName(group2Name)
        .assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);

    //wait for grouper_debug.log: changeLog.consumer.print skipping addMembership for subject Bill Brown 
    //since group testFolder:parentFolder:subFolder:group2 is not marked for sync
    runJobs();

    assertEquals(0, PrintChangeLogConsumer.eventsProcessed.size());
    
    // add syncAttribute mark to parent folder
    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    Stem parentFolder = StemFinder.findByName(gs, parentFolderName, true);
    parentFolder.getAttributeDelegate().assignAttribute(syncAttr);

    // added syncAttribute to parent folder")
    // wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships");
    // wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships");
    // print("end of Test 1.0.1 Marking a parent folder");
    runJobs();
    
    assertEquals(2, PrintChangeLogConsumer.eventsProcessed.size());
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " add group " + group1.getName() + " and memberships."));
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " add group " + group2.getName() + " and memberships."));
  }

  /**
   * test removing marker from parent
   */
  public void testRemoveAttributeFromFolder() {
 
    // Grouper action: 1.1 Remove a marker from a folder
    // Target outcome: remove groups under that folder and any subfolder (and implicitly all the memberships), unless otherwise marked from a parent folder or has a direct assignment 
    // Test 1.1.1: Removing mark from parent folder with subfolders and groups (and no other marks)
    // 1) Test 1.0.1
    // 2) Remove syncAttribute marker from parent folder
    // Outcome:
    // 1) all groups within folder structure removed from target
    // GSH:
    // Test 1.1.1 Removing mark from parent folder with subfolders and groups (and no other marks)
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add group1 and membership to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String group1Name = parentFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    group1.addMember(bill);

    // add group2 and membership to subfolder
    String subFolderName = parentFolderName + ":subFolder";
    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);

    // add syncAttribute mark to parent folder
    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    Stem parentFolder = StemFinder.findByName(grouperSession, parentFolderName, true);
    parentFolder.getAttributeDelegate().assignAttribute(syncAttr);

    //add syncAttribute mark to parent folder
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships.
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships.
    //hit return to continue
    runJobs();
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    parentFolder.getAttributeDelegate().removeAttribute(syncAttr);
    runJobs();

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(2, PrintChangeLogConsumer.eventsProcessed.size());
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " remove group " + group1.getName()));
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " remove group " + group2.getName()));

  }
  
  /**
   * 
   */
  private void runJobs() {
    
    ChangeLogTempToEntity.convertRecords();
    
    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    hib3GrouploaderLog.setHost(GrouperUtil.hostname());
    hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_" + JOB_NAME);
    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
    ChangeLogHelper.processRecords(JOB_NAME, hib3GrouploaderLog, new PrintChangeLogConsumer());
    
  }
  
}
