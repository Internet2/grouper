/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.subj;

import java.sql.Types;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.subj.decoratorExamples.SubjectCustomizerForDecoratorExtraAttributes;
import edu.internet2.middleware.grouper.subj.decoratorExamples.SubjectCustomizerForDecoratorTestingCollabGroup;
import edu.internet2.middleware.grouper.subj.decoratorExamples.SubjectCustomizerForDecoratorTestingHideStudentData;
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
    TestRunner.run(new TestSubjectDecorator("testDecoratorExtraAttributes"));
    
//    Connection con = null;
//    try {
//
//      Class.forName(
//        "com.microsoft.sqlserver.jdbc.SQLServerDriver");
//      con = DriverManager.getConnection(
//          "jdbc:sqlserver://localhost:50090;databaseName=groupertest;"
//        + "user=groupertest;password=<pass>;"
//        + "database=groupertest");
//
//      DatabaseMetaData meta = con.getMetaData();
//      ResultSet res = meta.getTables(null, null, null, 
//         new String[] {"TABLE"});
//      System.out.println("List of tables: "); 
//      while (res.next()) {
//         System.out.println(
//            "   "+res.getString("TABLE_CAT") 
//           + ", "+res.getString("TABLE_SCHEM")
//           + ", "+res.getString("TABLE_NAME")
//           + ", "+res.getString("TABLE_TYPE")
//           + ", "+res.getString("REMARKS")); 
//      }
//      res.close();
//
//      con.close();
//    } catch (java.lang.ClassNotFoundException e) {
//      System.err.println("ClassNotFoundException: "
//        +e.getMessage());
//    } catch (SQLException e) {
//      System.err.println("SQLException: "
//        +e.getMessage());
//    }

    
  }

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(TestSubjectDecorator.class);

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
  
  /**
   * 
   */
  public void testDecoratorExtraAttributes() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    try {
      //create a table which has the extra attributes
      ensureExtraAttributeTables();
      
      //add some attributes
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperSubjAttr").executeUpdate();
  
      TestgrouperSubjAttr test1attr = new TestgrouperSubjAttr(SubjectTestHelper.SUBJ1_ID, "major1", "title1");
      TestgrouperSubjAttr test2attr = new TestgrouperSubjAttr(SubjectTestHelper.SUBJ2_ID, "major2", "title2");
      
      HibernateSession.byObjectStatic().save(GrouperUtil.toSet(test1attr, test2attr));
      
      //define the permissions
      
      Group privilegedGroup = new GroupSave(grouperSession)
        .assignName(SubjectCustomizerForDecoratorExtraAttributes.PRIVILEGED_ADMIN_GROUP_NAME)
        .assignCreateParentStemsIfNotExist(true).save();
      Role role = new GroupSave(grouperSession)
        .assignName("subjectAttributes:roles:subjectAttributeRole").assignTypeOfGroup(TypeOfGroup.role)
        .assignCreateParentStemsIfNotExist(true).save();
  
      AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName(SubjectCustomizerForDecoratorExtraAttributes.SUBJECT_ATTRIBUTES_PERMISSIONS_ATTRIBUTE_DEF).assignAttributeDefType(AttributeDefType.perm)
        .assignToEffMembership(true).assignToGroup(true).assignCreateParentStemsIfNotExist(true).save();
  
      permissionDef.getAttributeDefActionDelegate().configureActionList("read");
      
      AttributeDefName permissionNameTitle = new AttributeDefNameSave(grouperSession, permissionDef)
        .assignCreateParentStemsIfNotExist(true).assignName("subjectAttributes:permissions:columnNames:title").save();
      AttributeDefName permissionNameMajor = new AttributeDefNameSave(grouperSession, permissionDef)
        .assignCreateParentStemsIfNotExist(true).assignName("subjectAttributes:permissions:columnNames:major").save();
      
      //lets assign a permission
      role.addMember(SubjectTestHelper.SUBJ1, true);
      role.addMember(SubjectTestHelper.SUBJ2, true);
      role.addMember(SubjectTestHelper.SUBJ3, true);
      privilegedGroup.addMember(SubjectTestHelper.SUBJ4, true);
      
      //subj 1 can see title
      role.getPermissionRoleDelegate().assignSubjectRolePermission("read", permissionNameTitle, SubjectTestHelper.SUBJ1, PermissionAllowed.ALLOWED);
      
      //subj 2 can see major
      role.getPermissionRoleDelegate().assignSubjectRolePermission("read", permissionNameMajor, SubjectTestHelper.SUBJ2, PermissionAllowed.ALLOWED);
  
      //subj 3 can see title and major
      role.getPermissionRoleDelegate().assignSubjectRolePermission("read", permissionNameMajor, SubjectTestHelper.SUBJ3, PermissionAllowed.ALLOWED);
      role.getPermissionRoleDelegate().assignSubjectRolePermission("read", permissionNameTitle, SubjectTestHelper.SUBJ3, PermissionAllowed.ALLOWED);
      
      //subject 4 is an admin
      
      //assign the customizer
      ApiConfig.testConfig.put("subjects.customizer.className", SubjectCustomizerForDecoratorExtraAttributes.class.getName());
      SubjectFinder.internalClearSubjectCustomizerCache();
  
      {
        //##################################################################
        //search for subjects and decorate them as someone who cant see them
        GrouperSession.stopQuietly(grouperSession);
    
        grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
        
        Set<Subject> subjects = SubjectFinder.findAll("test.subject");
        SubjectFinder.decorateSubjects(grouperSession, subjects, GrouperUtil.toSet("title", "major"));
        
        {
          Subject subject0 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ0_ID, true);
          
          //shouldnt have a title or major
          String title = subject0.getAttributeValueSingleValued("title", false);
          assertTrue(title, StringUtils.isBlank(title));
      
          String major = subject0.getAttributeValueSingleValued("major", false);
          assertTrue(major, StringUtils.isBlank(major));
        }
        
        {
          Subject subject1 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ1_ID, true);
          
          //shouldnt have a title or major
          String title = subject1.getAttributeValueSingleValued("title", false);
          assertTrue(title, StringUtils.isBlank(title));
      
          String major = subject1.getAttributeValueSingleValued("major", false);
          assertTrue(major, StringUtils.isBlank(major));
        }
    
        {
          Subject subject2 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ2_ID, true);
          
          //shouldnt have a title or major
          String title = subject2.getAttributeValueSingleValued("title", false);
          assertTrue(title, StringUtils.isBlank(title));
      
          String major = subject2.getAttributeValueSingleValued("major", false);
          assertTrue(major, StringUtils.isBlank(major));
        }
      }
      
      {
        //###################################################################
        //search for subjects and decorate them as someone who can see title
        GrouperSession.stopQuietly(grouperSession);
    
        grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
        
        Set<Subject> subjects = SubjectFinder.findAll("test.subject");
        SubjectFinder.decorateSubjects(grouperSession, subjects, GrouperUtil.toSet("title", "major"));
        
        {
          Subject subject0 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ0_ID, true);
          
          //shouldnt have a title or major
          String title = subject0.getAttributeValueSingleValued("title", false);
          assertTrue(title, StringUtils.isBlank(title));
      
          String major = subject0.getAttributeValueSingleValued("major", false);
          assertTrue(major, StringUtils.isBlank(major));
        }
        
        {
          Subject subject1 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ1_ID, true);
          
          //should have a title, not major
          String title = subject1.getAttributeValueSingleValued("title", false);
          assertEquals("title1", title);
      
          String major = subject1.getAttributeValueSingleValued("major", false);
          assertTrue(major, StringUtils.isBlank(major));
        }
    
        {
          Subject subject2 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ2_ID, true);
          
          //shouldnt have a title or major
          String title = subject2.getAttributeValueSingleValued("title", false);
          assertEquals("title2", title);
      
          String major = subject2.getAttributeValueSingleValued("major", false);
          assertTrue(major, StringUtils.isBlank(major));
        }
      }
      {
        //###################################################################
        //search for subjects and decorate them as someone who can see major
        GrouperSession.stopQuietly(grouperSession);
    
        grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
        
        Set<Subject> subjects = SubjectFinder.findAll("test.subject");
        SubjectFinder.decorateSubjects(grouperSession, subjects, GrouperUtil.toSet("title", "major"));
        
        {
          Subject subject0 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ0_ID, true);
          
          //shouldnt have a title or major
          String title = subject0.getAttributeValueSingleValued("title", false);
          assertTrue(title, StringUtils.isBlank(title));
      
          String major = subject0.getAttributeValueSingleValued("major", false);
          assertTrue(major, StringUtils.isBlank(major));
        }
  
        {
          Subject subject1 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ1_ID, true);
          
          //should have a title, not major
          String title = subject1.getAttributeValueSingleValued("title", false);
          assertTrue(title, StringUtils.isBlank(title));
      
          String major = subject1.getAttributeValueSingleValued("major", false);
          assertEquals("major1", major);
        }
  
        {
          Subject subject2 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ2_ID, true);
          
          //shouldnt have a title or major
          String title = subject2.getAttributeValueSingleValued("title", false);
          assertTrue(title, StringUtils.isBlank(title));
      
          String major = subject2.getAttributeValueSingleValued("major", false);
          assertEquals("major2", major);
        }
      }
  
      {
        //###################################################################
        //search for subjects and decorate them as someone who can see both
        GrouperSession.stopQuietly(grouperSession);
    
        grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
        
        Set<Subject> subjects = SubjectFinder.findAll("test.subject");
        SubjectFinder.decorateSubjects(grouperSession, subjects, GrouperUtil.toSet("title", "major"));
        
        {
          Subject subject0 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ0_ID, true);
          
          //shouldnt have a title or major
          String title = subject0.getAttributeValueSingleValued("title", false);
          assertTrue(title, StringUtils.isBlank(title));
      
          String major = subject0.getAttributeValueSingleValued("major", false);
          assertTrue(major, StringUtils.isBlank(major));
        }
  
        {
          Subject subject1 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ1_ID, true);
          
          //should have a title, not major
          String title = subject1.getAttributeValueSingleValued("title", false);
          assertEquals("title1", title);
      
          String major = subject1.getAttributeValueSingleValued("major", false);
          assertEquals("major1", major);
        }
  
        {
          Subject subject2 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ2_ID, true);
          
          //shouldnt have a title or major
          String title = subject2.getAttributeValueSingleValued("title", false);
          assertEquals("title2", title);
      
          String major = subject2.getAttributeValueSingleValued("major", false);
          assertEquals("major2", major);
        }
      }
  
      {
        //###################################################################
        //search for subjects and decorate them as someone who is an admin (can see all)
        GrouperSession.stopQuietly(grouperSession);
    
        grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
        
        Set<Subject> subjects = SubjectFinder.findAll("test.subject");
        SubjectFinder.decorateSubjects(grouperSession, subjects, GrouperUtil.toSet("title", "major"));
        
        {
          Subject subject0 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ0_ID, true);
          
          //shouldnt have a title or major
          String title = subject0.getAttributeValueSingleValued("title", false);
          assertTrue(title, StringUtils.isBlank(title));
      
          String major = subject0.getAttributeValueSingleValued("major", false);
          assertTrue(major, StringUtils.isBlank(major));
        }
  
        {
          Subject subject1 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ1_ID, true);
          
          //should have a title, not major
          String title = subject1.getAttributeValueSingleValued("title", false);
          assertEquals("title1", title);
      
          String major = subject1.getAttributeValueSingleValued("major", false);
          assertEquals("major1", major);
        }
  
        {
          Subject subject2 = SubjectHelper.findInList(subjects, "jdbc", SubjectTestHelper.SUBJ2_ID, true);
          
          //shouldnt have a title or major
          String title = subject2.getAttributeValueSingleValued("title", false);
          assertEquals("title2", title);
      
          String major = subject2.getAttributeValueSingleValued("major", false);
          assertEquals("major2", major);
        }
      }
    } finally {
      try {
        dropExtraAttributeTables();
      } catch (Throwable t) {
        LOG.error(t);
      }
    }

  }

  
  
  /**
   * 
   */
  public void dropExtraAttributeTables() {
    //we need to delete the test table if it is there, and create a new one
    //drop field id col, first drop foreign keys
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
  
      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();
  
        {
          Table subjectAttributeTable = database.findTable("testgrouper_subj_attr");
          
          if (subjectAttributeTable != null) {
            database.removeTable(subjectAttributeTable);
          }
        }
        
      }
      
    });
  }

  /**
   * 
   */
  public void ensureExtraAttributeTables() {
    //we need to delete the test table if it is there, and create a new one
    //drop field id col, first drop foreign keys
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
  
      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();
 
        {
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"testgrouper_subj_attr");
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
              Types.VARCHAR, "255", true, true);
  
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "hibernate_version_number", 
              Types.BIGINT, "12", false, true);
  
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "title", 
              Types.VARCHAR, "255", false, false);
      
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "major", 
              Types.VARCHAR, "255", false, false);
      
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id", 
              Types.VARCHAR, "255", false, false);
      
          GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
              "testgrouper_subj_attr", "sample table that can be used by attribute decorator");
      
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_subj_attr", "id", 
              "uuid");
      
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_subj_attr", "hibernate_version_number", 
            "number so hibernate does not update a row that another process has recently updated");
  
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_subj_attr", "title", 
            "job title");
  
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_subj_attr", "major", 
            "student major");
  
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_subj_attr", "subject_id", 
            "subject_id that this attribute involves");
  
        }
      }
      
    });
  }
  
}
