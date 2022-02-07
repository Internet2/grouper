package edu.internet2.middleware.grouper.app.subjectSource;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectSave;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.subj.TestSubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

public class SubjectSourceConfigurationTest extends GrouperTest {

  public static void main(String[] args) {
   
    TestRunner.run(new SubjectSourceConfigurationTest("testSqlSubjectSourceConfigHiddenData"));
  }
  
  /**
   * @param name
   */
  public SubjectSourceConfigurationTest(String name) {
    super(name);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    
    SubjectConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().clear();

  }
  
  public void testLdapSubjectSourceConfig() {
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.ldap.dinkel", false)) {
      return;
    }
    GrouperSession.startRootSession();
    
    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
    LdapProvisionerTestUtils.startLdapContainer();
    
    String subjectSourceLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/subjectSource/ldap-subject-source.properties", false);
    
    List<String> subjectSourceProperties = GrouperUtil.splitFileLines(subjectSourceLines);
    
    for (String keyValue: subjectSourceProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        SubjectConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    SourceManager.getInstance().reloadSource("ldapSubjectSource");
    
    try {      
      Subject subject = SubjectFinder.findByIdentifierAndSource("aanderson@example.edu", "ldapSubjectSource", true);
      assertEquals("Ann Anderson", subject.getName());
    } catch (Exception e) {
      e.printStackTrace();
      org.junit.Assert.fail("Should never get here.");
    }
    
  }
  
  public void testSqlSubjectSourceConfig() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group emailViewers = new GroupSave().assignName("test:emailViewers").assignCreateParentStemsIfNotExist(true).save();
    emailViewers.addMember(SubjectTestHelper.SUBJ0, false);

    String subjectSourceLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/subjectSource/jdbc-subject-source.properties", false);

    List<String> subjectSourceProperties = GrouperUtil.splitFileLines(subjectSourceLines);
    
    for (String keyValue: subjectSourceProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        SubjectConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    ExternalSubject externalSubject = new ExternalSubjectSave(grouperSession).assignName("vivek")
        .assignIdentifier("vivek@test.com").assignEmail("test@example.com").save();
    
    for (int i=0; i<100; i++) {
      new ExternalSubjectSave(grouperSession).assignName("name"+i)
        .assignIdentifier("name"+i+"@test.com").assignEmail("test"+i+"@example.com").save();
    }
    
    SourceManager.getInstance().reloadSource("sqlSubjectSourceIdAnother");

    // GrouperSystem can see email since admins can do whatever
    Subject subject0 = SubjectFinder.findByIdentifierAndSource("vivek@test.com", "sqlSubjectSourceIdAnother", true);
    System.out.println(subject0.getClass().getName() + "@" + Integer.toHexString(subject0.hashCode()));
    assertEquals("vivek ("+subject0.getId()+") - vivek@test.com", subject0.getDescription());
    assertEquals("vivek", subject0.getName());
    assertEquals("test@example.com", GrouperEmail.retrieveEmailAddress(subject0));

    GrouperSession.stopQuietly(grouperSession);
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    // subj 1 cant see email
    Subject subject1 = SubjectFinder.findByIdentifierAndSource("vivek@test.com", "sqlSubjectSourceIdAnother", true);
    System.out.println(subject1.getClass().getName() + "@" + Integer.toHexString(subject1.hashCode()));
    assertEquals("vivek ("+subject1.getId()+") - vivek@test.com", subject1.getDescription());
    assertEquals("vivek", subject1.getName());
    assertTrue(StringUtils.isBlank(GrouperEmail.retrieveEmailAddress(subject1)));

    GrouperSession.stopQuietly(grouperSession);
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    // subj 0 is in group so can see email
    Subject subject2 = SubjectFinder.findByIdentifierAndSource("vivek@test.com", "sqlSubjectSourceIdAnother", true);
    System.out.println(subject2.getClass().getName() + "@" + Integer.toHexString(subject2.hashCode()));
    assertEquals("vivek ("+subject2.getId()+") - vivek@test.com", subject2.getDescription());
    assertEquals("vivek", subject2.getName());
    assertEquals("test@example.com", GrouperEmail.retrieveEmailAddress(subject2));
  }
  
  public void testSqlSubjectSourceConfigHiddenData() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group emailViewers = new GroupSave().assignName("test:emailViewers").assignCreateParentStemsIfNotExist(true).save();
    emailViewers.addMember(SubjectTestHelper.SUBJ0, false);

    String subjectSourceLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/subjectSource/jdbc-subject-source2.properties", false);

    List<String> subjectSourceProperties = GrouperUtil.splitFileLines(subjectSourceLines);
    
    for (String keyValue: subjectSourceProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        SubjectConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    
    ExternalSubject externalSubject = new ExternalSubjectSave(grouperSession).assignName("vivek")
        .assignIdentifier("vivek@test.com").assignEmail("test@example.com").save();
    
    for (int i=0; i<100; i++) {
      new ExternalSubjectSave(grouperSession).assignName("name"+i)
        .assignIdentifier("name"+i+"@test.com").assignEmail("test"+i+"@example.com").save();
    }
    
    SourceManager.getInstance().reloadSource("sqlSubjectSourceIdAnother");

    // GrouperSystem can see email since admins can do whatever
    Subject subject0 = SubjectFinder.findByIdentifierAndSource("vivek@test.com", "sqlSubjectSourceIdAnother", true);
    System.out.println(subject0.getClass().getName() + "@" + Integer.toHexString(subject0.hashCode()));
    assertEquals(subject0.getDescription(), "vivek ("+subject0.getId()+") - vivek@test.com", subject0.getDescription());
    assertEquals("vivek", subject0.getName());
    assertEquals("test@example.com", GrouperEmail.retrieveEmailAddress(subject0));

    GrouperSession.stopQuietly(grouperSession);
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    // subj 1 cant see email
    Subject subject1 = SubjectFinder.findByIdentifierAndSource("vivek@test.com", "sqlSubjectSourceIdAnother", true);
    System.out.println(subject1.getClass().getName() + "@" + Integer.toHexString(subject1.hashCode()));
    assertEquals(subject1.getId()+" - vivek@test.com", subject1.getDescription());
    assertEquals(subject1.getId()+" - vivek@test.com", subject1.getName());
    assertTrue(StringUtils.isBlank(GrouperEmail.retrieveEmailAddress(subject1)));

    GrouperSession.stopQuietly(grouperSession);
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    // subj 0 is in group so can see email
    Subject subject2 = SubjectFinder.findByIdentifierAndSource("vivek@test.com", "sqlSubjectSourceIdAnother", true);
    System.out.println(subject2.getClass().getName() + "@" + Integer.toHexString(subject2.hashCode()));
    assertEquals("vivek ("+subject2.getId()+") - vivek@test.com", subject2.getDescription());
    assertEquals("vivek", subject2.getName());
    assertEquals("test@example.com", GrouperEmail.retrieveEmailAddress(subject2));
  }
  

}
