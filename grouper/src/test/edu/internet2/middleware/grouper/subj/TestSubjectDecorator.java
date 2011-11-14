/**
 * 
 */
package edu.internet2.middleware.grouper.subj;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * @author mchyzer
 *
 */
public class TestSubjectDecorator extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestSubjectDecorator("testFilterAnotherCollabGroup"));
  }

  /**
   * 
   */
  public TestSubjectDecorator() {
    super();
  }

  /**
   * 
   * @param name
   */
  public TestSubjectDecorator(String name) {
    super(name);
  }

  /**
   * 
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    ApiConfig.testConfig.remove("subjects.customizer.className");
  }

  /**
   * 
   */
  public void testHideStudentData() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    //if we are hiding student data
    //create the groups and add some people
    Group studentGroup = new GroupSave(grouperSession)
      .assignName(SubjectCustomizerForDecoratorTestingHideStudentData.STUDENT_GROUP_NAME)
      .assignCreateParentStemsIfNotExist(true).save();
    Group privilegedGroup = new GroupSave(grouperSession)
      .assignName(SubjectCustomizerForDecoratorTestingHideStudentData.PRIVILEGED_EMPLOYEE_GROUP_NAME)
      .assignCreateParentStemsIfNotExist(true).save();

    //subject0 is privileged, subject1 is not
    privilegedGroup.addMember(SubjectTestHelper.SUBJ0, true);
    
    //subjects 345 are students, all others are not
    studentGroup.addMember(SubjectTestHelper.SUBJ3, true);
    studentGroup.addMember(SubjectTestHelper.SUBJ4, true);
    studentGroup.addMember(SubjectTestHelper.SUBJ5, true);

    //now, configure the subject decorator
    ApiConfig.testConfig.put("subjects.customizer.className", SubjectCustomizerForDecoratorTestingHideStudentData.class.getName());
    SubjectFinder.internalClearSubjectCustomizerCache();
    GrouperSession.stopQuietly(grouperSession);
    
    Subject subject = null;
    
    //####################################
    //do a search as subject 0 (privileged)

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ2_ID, true);
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
    
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ3_ID, true);
    assertEquals("Should see proper name for student", SubjectTestHelper.SUBJ3_NAME, subject.getName());

    subject = SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ2_IDENTIFIER, true);
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
    
    subject = SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ3_IDENTIFIER, true);
    assertEquals("Should see proper name for student", SubjectTestHelper.SUBJ3_NAME, subject.getName());

    subject = SubjectFinder.findAll(SubjectTestHelper.SUBJ2_IDENTIFIER).iterator().next();
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());

    subject = SubjectFinder.findAll(SubjectTestHelper.SUBJ3_IDENTIFIER).iterator().next();
    assertEquals("Should see proper name for student", SubjectTestHelper.SUBJ3_NAME, subject.getName());

    subject = SubjectFinder.findPage(SubjectTestHelper.SUBJ2_IDENTIFIER).getResults().iterator().next();
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());

    subject = SubjectFinder.findPage(SubjectTestHelper.SUBJ3_IDENTIFIER).getResults().iterator().next();
    assertEquals("Should see proper name for student", SubjectTestHelper.SUBJ3_NAME, subject.getName());

    GrouperSession.stopQuietly(grouperSession);

    //####################################
    //do a search as subject 1 (not privileged)

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ2_ID, true);
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
    
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ3_ID, true);
    assertEquals("Should see loginid for student", SubjectTestHelper.SUBJ3_IDENTIFIER, subject.getName());

    subject = SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ2_IDENTIFIER, true);
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
    
    subject = SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ3_IDENTIFIER, true);
    assertEquals("Should see loginid for student", "id.test.subject.3", subject.getName());

    subject = SubjectFinder.findAll(SubjectTestHelper.SUBJ2_IDENTIFIER).iterator().next();
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
    
    subject = SubjectFinder.findAll(SubjectTestHelper.SUBJ3_IDENTIFIER).iterator().next();
    assertEquals("Should see loginid for student", "id.test.subject.3", subject.getName());

    subject = SubjectFinder.findPage(SubjectTestHelper.SUBJ2_IDENTIFIER).getResults().iterator().next();
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
    
    subject = SubjectFinder.findPage(SubjectTestHelper.SUBJ3_IDENTIFIER).getResults().iterator().next();
    assertEquals("Should see loginid for student", "id.test.subject.3", subject.getName());

    GrouperSession.stopQuietly(grouperSession);
    
  }

  /**
   * 
   */
  public void testFilterAnotherCollabGroup() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
  
    //if we are hiding student data
    //create the groups and add some people
    Group collab1 = new GroupSave(grouperSession)
      .assignName(SubjectCustomizerForDecoratorTestingCollabGroup.COLLAB_STEM_NAME + ":collab1")
      .assignCreateParentStemsIfNotExist(true).save();
    Group collab2 = new GroupSave(grouperSession)
      .assignName(SubjectCustomizerForDecoratorTestingCollabGroup.COLLAB_STEM_NAME + ":collab2")
      .assignCreateParentStemsIfNotExist(true).save();
    Group privilegedGroup = new GroupSave(grouperSession)
      .assignName(SubjectCustomizerForDecoratorTestingCollabGroup.PRIVILEGED_ADMIN_GROUP_NAME)
      .assignCreateParentStemsIfNotExist(true).save();
  
    //subject0 is privileged, subject1 is not
    privilegedGroup.addMember(SubjectTestHelper.SUBJ0, true);
    
    //subjects 345 are students, all others are not
    collab1.addMember(SubjectTestHelper.SUBJ3, true);
    collab1.addMember(SubjectTestHelper.SUBJ4, true);
    collab2.addMember(SubjectTestHelper.SUBJ4, true);
    collab2.addMember(SubjectTestHelper.SUBJ5, true);
  
    //now, configure the subject decorator
    ApiConfig.testConfig.put("subjects.customizer.className", SubjectCustomizerForDecoratorTestingCollabGroup.class.getName());
    SubjectFinder.internalClearSubjectCustomizerCache();
    GrouperSession.stopQuietly(grouperSession);
    
    Subject subject = null;
    Set<Subject> subjects = null;
    
    //####################################
    //do a search as subject 0 (privileged)
  
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ2_ID, true);
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
    
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ3_ID, true);
    assertEquals("Should see proper name for student", SubjectTestHelper.SUBJ3_NAME, subject.getName());
  
    subject = SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ2_IDENTIFIER, true);
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
    
    subject = SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ3_IDENTIFIER, true);
    assertEquals("Should see proper name for student", SubjectTestHelper.SUBJ3_NAME, subject.getName());
  
    subject = SubjectFinder.findAll(SubjectTestHelper.SUBJ2_IDENTIFIER).iterator().next();
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
  
    subject = SubjectFinder.findAll(SubjectTestHelper.SUBJ3_IDENTIFIER).iterator().next();
    assertEquals("Should see proper name for student", SubjectTestHelper.SUBJ3_NAME, subject.getName());
  
    subject = SubjectFinder.findPage(SubjectTestHelper.SUBJ2_IDENTIFIER).getResults().iterator().next();
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
  
    subject = SubjectFinder.findPage(SubjectTestHelper.SUBJ3_IDENTIFIER).getResults().iterator().next();
    assertEquals("Should see proper name for student", SubjectTestHelper.SUBJ3_NAME, subject.getName());
  
    GrouperSession.stopQuietly(grouperSession);
  
    //####################################
    //do a search as subject 1 (has no rights)
  
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ2_ID, true);
    assertNull("cant see anyone, not in collab group", subject);
    
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ3_ID, false);
    assertNull("cant see anyone, not in collab group", subject);
  
    subject = SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ2_IDENTIFIER, false);
    assertNull("cant see anyone, not in collab group", subject);
    
    subject = SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ3_IDENTIFIER, false);
    assertNull("cant see anyone, not in collab group", subject);
  
    subjects = SubjectFinder.findAll(SubjectTestHelper.SUBJ2_IDENTIFIER);
    assertEquals("cant see anyone, not in collab group", 0, GrouperUtil.length(subjects));
    
    subjects = SubjectFinder.findAll(SubjectTestHelper.SUBJ3_IDENTIFIER);
    assertEquals("cant see anyone, not in collab group", 0, GrouperUtil.length(subjects));
  
    subjects = SubjectFinder.findPage(SubjectTestHelper.SUBJ2_IDENTIFIER).getResults();
    assertEquals("cant see anyone, not in collab group", 0, GrouperUtil.length(subjects));
    
    subjects = SubjectFinder.findPage(SubjectTestHelper.SUBJ3_IDENTIFIER).getResults();
    assertEquals("cant see anyone, not in collab group", 0, GrouperUtil.length(subjects));
  
    GrouperSession.stopQuietly(grouperSession);
    
    //####################################
    //do a search as subject 3 (collab1)
  
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ4_ID, true);
    assertNotNull("can see in collab group", subject);
    
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ5_ID, false);
    assertNull("cant see anyone, not in collab group", subject);
  
    subject = SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ4_IDENTIFIER, false);
    assertNotNull("can see in collab group", subject);
    
    subject = SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ5_IDENTIFIER, false);
    assertNull("cant see anyone, not in collab group", subject);
  
    subjects = SubjectFinder.findAll(SubjectTestHelper.SUBJ4_IDENTIFIER);
    assertEquals("can see in collab group", 1, GrouperUtil.length(subjects));
    
    subjects = SubjectFinder.findAll(SubjectTestHelper.SUBJ5_IDENTIFIER);
    assertEquals("cant see anyone, not in collab group", 0, GrouperUtil.length(subjects));
  
    subjects = SubjectFinder.findPage(SubjectTestHelper.SUBJ4_IDENTIFIER).getResults();
    assertEquals("can see in collab group", 1, GrouperUtil.length(subjects));
    
    subjects = SubjectFinder.findPage(SubjectTestHelper.SUBJ5_IDENTIFIER).getResults();
    assertEquals("cant see anyone, not in collab group", 0, GrouperUtil.length(subjects));
  
    GrouperSession.stopQuietly(grouperSession);
    
  }
  
  
}
