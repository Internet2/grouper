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
import edu.internet2.middleware.grouper.StemSave;
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
    TestRunner.run(new ChangeLogConsumerBaseImplTest("testRemoveGroupMarkerKeepFolderMarker"));
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
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    assertEquals(0, PrintChangeLogConsumer.eventsProcessed.size());
    
    // add syncAttribute mark to parent folder
    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    Stem parentFolder = StemFinder.findByName(gs, parentFolderName, true);
    parentFolder.getAttributeDelegate().assignAttribute(syncAttr);

    // added syncAttribute to parent folder")
    // wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships");
    // wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships");
    // print("end of Test 1.0.1 Marking a parent folder");
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    assertEquals(2, PrintChangeLogConsumer.eventsProcessed.size());
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " add group " + group1.getName() + " and memberships"));
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " add group " + group2.getName() + " and memberships"));
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
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    parentFolder.getAttributeDelegate().removeAttribute(syncAttr);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

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
  public void testAddParentChildRemoveParent() {

    // Test 1.1.2: Marked folder with marked subfolders
    // 1) Test 1.0.1
    // 2) Mark subfolder with syncAttribute
    // 3) Remove syncAttribute from parent folder
    // Outcome:
    // 1) all groups within parent folder structure removed from target, except those within marked subfolder
    //Test 1.1.2 Removing mark from parent folder with subfolders and groups, with a mark on a subfolder
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

    Stem subFolder = StemFinder.findByName(grouperSession, subFolderName, true);
    subFolder.getAttributeDelegate().assignAttribute(syncAttr);
    
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    parentFolder.getAttributeDelegate().removeAttribute(syncAttr);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " remove group " + group1.getName()));
    
    
  }
  
  /**
   * 
   */
  public void testMarkGroupRemoveParent() {
    
    // Test 1.1.3: Marked folder with marked groups
    // 1) Test 1.0.1
    // 2) Mark group in parent folder and sub folder structure
    // 3) Remove syncAttribute from parent folder
    // Outcome:
    // 1) all groups within parent folder structure removed from target, except directly marked groups
    //Test 1.1.3 Removing mark from parent folder with subfolders and groups, with a direct mark on a group
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

    //add syncAttribute mark to group2
    group2.getAttributeDelegate().assignAttribute(syncAttr);
    
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    parentFolder.getAttributeDelegate().removeAttribute(syncAttr);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " remove group " + group1.getName()));
    

  }
  
  /**
   * 
   */
  public void testMarkGroup() {
    
    // Grouper action: 1.2 Place a marker on a group
    // Target outcome: add the group and all its effective memberships (direct and indirect)
    // Test 1.2.1:
    // 1) Set up folder and a group with memberships, and no syncAttribute marks
    // 2) Mark group with syncAttribute
    // Outcome:
    // 1) group and its memberships added to the target
    // Test 1.2.1 Place a marker on a group, add the group and all its effective memberships to the target
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

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();


    //add syncAttribute mark to group1
    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    group1.getAttributeDelegate().assignAttribute(syncAttr);

    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " add group " + group1.getName() + " and memberships"));

  }
  
  /**
   * 
   */
  public void testRemoveMarkerFromGroup() {
    
    // Grouper action: 1.3 Remove a marker from a group
    // Target outcome: remove the group (and implicitly all the memberships), unless otherwise marked by a parent folder
    // Test 1.3.1: Remove marker from a group that doesn't have parent folders marked
    // 1) Test 1.2.1
    // 2) Remove marker from the group
    // Outcome:
    // 1) group removed from target
    // Test 1.3.1: Remove marker from a group that doesn't have parent folders marked
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

    //add syncAttribute mark to group2
    group1.getAttributeDelegate().assignAttribute(syncAttr);
    
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    group1.getAttributeDelegate().removeAttribute(syncAttr);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " remove group " + group1.getName()));
    
  }

  /**
   * 
   */
  public void testRemoveGroupMarkerKeepFolderMarker() {
    //Test 1.3.2: Remove mark from group that has an indirect mark from a parent folder
    // 1) Test 1.2.1
    // 2) Mark parent folder (adding an indirect syncAttribute mark)
    // 3) Remove the direct marker from the group
    // Outcome:
    // 1) Group is *not* removed from target, since it has an indirect mark from parent folder
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

    //add syncAttribute mark to group1
    group1.getAttributeDelegate().assignAttribute(syncAttr);
    
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    group1.getAttributeDelegate().removeAttribute(syncAttr);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(0, PrintChangeLogConsumer.eventsProcessed.size());
    

  }
  
  /**
   * 
   */
  public void testAddGroupToMarkedFolder() {
    
    // Grouper action: 2.0 Add indirectly marked group (i.e. add a group under a folder that is already marked)
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";
    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);

    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    Stem parentFolder = StemFinder.findByName(grouperSession, parentFolderName, true);
    parentFolder.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // add group1 and membership to parent folder
    String group1Name = parentFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    
    // remove syncAttribute mark
    group1.getAttributeDelegate().removeAttribute(syncAttr);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " add group " + group1.getName()));


  }
  
  /**
   * 
   */
  public void testMoveGroupToFolder() {
    
    // Test 2.0.2: Move group that has memberships to a folder that is already marked
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";

    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    Stem subFolder = StemFinder.findByName(grouperSession, subFolderName, true);
    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);

    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    subFolder.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // add group1 and membership to parent folder
    String group1Name = parentFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    group1.addMember(bill);

    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    assertEquals(0, PrintChangeLogConsumer.eventsProcessed.size());

    // remove syncAttribute mark
    group1.move(subFolder);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " rename group " + group1Name + " to " + group1.getName()));

  }
  
  /**
   * 
   */
  public void moveGroupToUnmarkedFolder() {
    
    //Test 2.0.3: Move unmarked group that has memberships to a folder that is also not marked
    // 1) Set up folder without syncAttribute mark
    // 2) Set up group with membership outside of marked folder
    // 3) Move group to unmarked folder (or subfolder)
    // Outcome:
    // 1) Nothing to do since group still unmarked

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";

    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    Stem subFolder = StemFinder.findByName(grouperSession, subFolderName, true);
    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // add group1 and membership to parent folder
    String group1Name = parentFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    group1.addMember(bill);

    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    assertEquals(0, PrintChangeLogConsumer.eventsProcessed.size());

    // remove syncAttribute mark
    group1.move(subFolder);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(0, PrintChangeLogConsumer.eventsProcessed.size());
    

  }
  
  /**
   * 
   */
  public void renameMarkedGroupToMarkedFolder() {
    //Test 2.0.4: Move marked group that has memberships to a folder that is also marked
    // 1) Set up folder with syncAttribute mark
    // 2) Set up marked group with membership outside of marked folder
    // 3) Move marked group to marked folder (or subfolder)
    // Outcome:
    // 1) Group rename at the target

    // Test 2.0.2: Move group that has memberships to a folder that is already marked
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";

    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    Stem subFolder = StemFinder.findByName(grouperSession, subFolderName, true);
    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);

    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    subFolder.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // add group1 and membership to parent folder
    String group1Name = parentFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    group1.addMember(bill);
    
    group1.getAttributeDelegate().assignAttribute(syncAttr);

    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    group1.move(subFolder);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " rename group " + group1Name + " to " + group1.getName()));

  }
  
  /**
   * 
   */
  public void testMoveMarkedGroupToUnarkedFolder() {
    
    //Test 2.0.4: Move marked group that has memberships to a folder that is also marked
    // 1) Set up folder with syncAttribute mark
    // 2) Set up marked group with membership outside of marked folder
    // 3) Move marked group to marked folder (or subfolder)
    // Outcome:
    // 1) Group rename at the target

    // Test 2.0.2: Move group that has memberships to a folder that is already marked
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";
    String subFolder2Name = parentFolderName + ":subFolder2";
    
    Stem subFolder2 = new StemSave(grouperSession).assignName(subFolder2Name).assignCreateParentStemsIfNotExist(true).save();

    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    Stem subFolder = StemFinder.findByName(grouperSession, subFolderName, true);
    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);

    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    subFolder.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // add group1 and membership to parent folder
    String group1Name = subFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    group1.addMember(bill);

    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    group1.move(subFolder2);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " remove moved group " + group1Name));

  }
  
  /**
   * 
   */
  public void testRemoveGroup() {

    //Grouper action: 3.0 Delete a directly marked group
    //Target outcome: remove the group
    //Test 3.0.1: delete a directly marked group, with no other parent folder marks

    // Test 2.0.2: Move group that has memberships to a folder that is already marked
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";

    // add group1 and membership to parent folder
    String group1Name = subFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    group1.addMember(bill);
    
    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);

    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    group1.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    group1.delete();
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " remove deleted group " + group1Name));

  }
  
  /**
   * 
   */
  public void testDeleteGroupMarkedFromFolder() {

    
    //Grouper action: 3.1 Delete an indirectly marked group
    //Target outcome: remove the group
    //Test 3.1.1: Delete a group that has sync mark on a parent folder

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";

    // add group1 and membership to parent folder
    String group1Name = subFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    group1.addMember(bill);
    
    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    Stem parentFolder = StemFinder.findByName(grouperSession, parentFolderName, true);
    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);

    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    parentFolder.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    group1.delete();
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " remove deleted group " + group1Name));

  }
  
  /**
   * 
   */
  public void testAddMembershipToDirectlyMarkedGroup() {
    
    //Grouper action: 4.0 Membership add on a marked group (directly or indirectly marked)");
    //Target outcome: add membership");
    //Test 4.0.1: Membership add to directly marked group");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";

    // add group1 and membership to parent folder
    String group1Name = subFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    
    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);

    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    group1.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    group1.addMember(bill);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " add subject " + bill.getId() + " to group " + group1Name));
    
    
  }
  
  /**
   * 
   */
  public void testAddMembershipToIndirectlyMarkedGroup() {
    //Test 4.0.2: Membership add to indirectly marked group (i.e. parent folder is marked)");
    // 1) Test 4.0.2
    // 2) Add member to group
    // Outcome:
    // 1) members added to group at target
    // GSH:
    // Test 4.0.2
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";

    // add group1 and membership to parent folder
    String group1Name = subFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    
    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    Stem parentFolder = StemFinder.findByName(grouperSession, parentFolderName, true);
    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);

    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    parentFolder.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    group1.addMember(bill);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " add subject " + bill.getId() + " to group " + group1Name));
    
  }
  
  /**
   * 
   */
  public void testRemoveSubgroupFromMarkedGroup() {
    
    //Test 4.1.3 Membership delete by grouper effective membership (via sub groups or group math)
    // 1) Test 4.0.3
    // 2) Remove subgroup from marked group
    // Outcome:
    // 1) Indirect memberships due to subgroup removed from target

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";

    // add group1 and membership to parent folder
    String group1Name = subFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    
    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group2.addMember(bill);
    group1.addMember(group2.toSubject());
    
    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    group1.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    // remove syncAttribute mark
    group1.deleteMember(group2.toSubject());
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " remove subject " + bill.getId() + " from group " + group1Name));

  }
  
  /**
   * 
   */
  public void testAddGroupToMarkedGroup() {
    
    //Test 4.0.3: Membership add by grouper effective membership (via sub groups or group math)
    // 1) Test 4.0.1
    // 2) Add a sub group with membership to marked group
    // Outcome:
    // 1) New effective members via subgroup are added to group at target
    // GSH:
    // Test 4.0.3

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";

    // add group1 and membership to parent folder
    String group1Name = subFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    
    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group2.addMember(bill);
    
    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    group1.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    group1.addMember(group2.toSubject());
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " add subject " + bill.getId() + " to group " + group1Name));

  }
  
  /**
   * 
   */
  public void testRemoveMembershipFromDirectGroup() {
    
    //Test 4.0.3: Membership add by grouper effective membership (via sub groups or group math)
    // 1) Test 4.0.1
    // 2) Add a sub group with membership to marked group
    // Outcome:
    // 1) New effective members via subgroup are added to group at target
    // GSH:
    // Test 4.0.3

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";

    // add group1 and membership to parent folder
    String group1Name = subFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(bob);
    group1.addMember(ann);
    group1.addMember(bill);
    
    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    group1.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    group1.deleteMember(bill);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " remove subject " + bill.getId() + " from group " + group1Name));

  }
  
  /**
   * 
   */
  public void testRemoveMembershipFromIndirectlyMarkedGroup() {
    //Test 4.1.2 Membership delete to indirectly marked group (i.e. parent folder is marked
    // 1) Test 4.0.2
    // 2) Remove member from indirectly marked group
    // Outcome:
    // 1) membership removed from target
    // GSH:
    // Test 4.1.2
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject bob = SubjectTestHelper.SUBJ0;
    Subject ann = SubjectTestHelper.SUBJ1;
    Subject bill = SubjectTestHelper.SUBJ2;
    String testFolderName = "testFolder";

    // add syncAttribute mark to parent folder
    String parentFolderName = testFolderName + ":parentFolder";
    String subFolderName = parentFolderName + ":subFolder";

    // add group1 and membership to parent folder
    String group1Name = subFolderName + ":group1";
    Group group1 = new GroupSave(grouperSession).assignName(group1Name).assignGroupNameToEdit(group1Name)
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();

    String group2Name = subFolderName + ":group2";
    Group group2 = new GroupSave(grouperSession).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();

    group2.addMember(bob);
    group2.addMember(ann);
    group2.addMember(bill);
    group1.addMember(group2.toSubject());
    
    AttributeDefName syncAttr = AttributeDefNameFinder.findByName(this.provisioningMarkerAttributeName.getName(), true);
    group1.getAttributeDelegate().assignAttribute(syncAttr);

    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships
    //wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships
    //hit return to continue
    Hib3GrouperLoaderLog hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
    PrintChangeLogConsumer.eventsProcessed.clear();

    group2.deleteMember(bill);
    hib3GrouploaderLog  = runJobs();
    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());

    //removed syncAttribute mark");
    //wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
    assertEquals(1, PrintChangeLogConsumer.eventsProcessed.size());
    
    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " remove subject " + bill.getId() + " from group " + group1Name));

  }

  /**
   * 
   */
  private Hib3GrouperLoaderLog runJobs() {
    
    ChangeLogTempToEntity.convertRecords();
    
    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    hib3GrouploaderLog.setHost(GrouperUtil.hostname());
    hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_" + JOB_NAME);
    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
    ChangeLogHelper.processRecords(JOB_NAME, hib3GrouploaderLog, new PrintChangeLogConsumer());

    return hib3GrouploaderLog;
  }
  
}
