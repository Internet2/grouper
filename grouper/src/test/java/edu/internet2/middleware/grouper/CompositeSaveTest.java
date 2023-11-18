/**
 * 
 */
package edu.internet2.middleware.grouper;

import org.junit.Assert;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveMode;
import junit.textui.TestRunner;

public class CompositeSaveTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new CompositeSaveTest("testDeleteComposite"));
  }
  
  /**
   * @param name
   */
  public CompositeSaveTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public CompositeSaveTest() {
    super();
  }
  
  
  public void testSaveCompositeUnionType() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup3").save();
    
    new CompositeSave().assignOwnerName(group1.getName()).assignLeftFactorName(group2.getName()).assignRightFactorName(group3.getName())
    .assignType("union").save();
    
    group2.addMember(SubjectTestHelper.SUBJ1);
    group3.addMember(SubjectTestHelper.SUBJ2);
    
    Assert.assertEquals(2, group1.getMembers().size());
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ1));
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ2));
    
  }
  
  public void testSaveCompositeIntersectionType() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup3").save();
    
    new CompositeSave().assignOwnerName(group1.getName()).assignLeftFactorName(group2.getName()).assignRightFactorName(group3.getName())
    .assignType("intersection").save();
    
    group2.addMember(SubjectTestHelper.SUBJ1);
    group2.addMember(SubjectTestHelper.SUBJ2);
    group3.addMember(SubjectTestHelper.SUBJ2);
    group3.addMember(SubjectTestHelper.SUBJ3);
    
    Assert.assertEquals(1, group1.getMembers().size());
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ2));
    
  }
  
  public void testSaveCompositeComplementType() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup3").save();
    
    new CompositeSave().assignOwnerName(group1.getName()).assignLeftFactorName(group2.getName()).assignRightFactorName(group3.getName())
    .assignType("complement").save();
    
    group2.addMember(SubjectTestHelper.SUBJ1);
    group2.addMember(SubjectTestHelper.SUBJ2);
    group3.addMember(SubjectTestHelper.SUBJ2);
    
    Assert.assertEquals(1, group1.getMembers().size());
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ1));
    
  }
  
  public void testDeleteComposite() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup3").save();
    
    new CompositeSave().assignOwnerName(group1.getName()).assignLeftFactorName(group2.getName()).assignRightFactorName(group3.getName())
    .assignType("union").save();
    
    group2.addMember(SubjectTestHelper.SUBJ1);
    group3.addMember(SubjectTestHelper.SUBJ2);
    
    Assert.assertEquals(2, group1.getMembers().size());
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ1));
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ2));
    
    new CompositeSave().assignOwnerName(group1.getName()).assignLeftFactorName(group2.getName()).assignRightFactorName(group3.getName()).assignSaveMode(SaveMode.DELETE).save();
    Assert.assertEquals(0, group1.getMembers().size());
    
  }

}
