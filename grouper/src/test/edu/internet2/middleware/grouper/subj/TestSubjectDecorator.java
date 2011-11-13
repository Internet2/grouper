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
    TestRunner.run(new TestSubjectDecorator("testHideStudentData"));
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
    
    Set<Subject> subjects = null;
    
    //####################################
    //do a search as subject 0 (privileged)

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ2_ID, true);
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
    
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ3_ID, true);
    assertEquals("Should see proper name for student", SubjectTestHelper.SUBJ3_NAME, subject.getName());

    GrouperSession.stopQuietly(grouperSession);
    
    //####################################
    //do a search as subject 1 (not privileged)

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ2_ID, true);
    assertEquals("Should see proper name for non-student", SubjectTestHelper.SUBJ2_NAME, subject.getName());
    
    subject = SubjectFinder.findById(SubjectTestHelper.SUBJ3_ID, true);
    assertEquals("Should see loginid for student", "id.test.subject.3", subject.getName());

    GrouperSession.stopQuietly(grouperSession);
    
  }
  
  
}
