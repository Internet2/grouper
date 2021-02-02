package edu.internet2.middleware.grouper.app.subjectSource;

import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectSave;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

public class SubjectSourceConfigurationTest extends GrouperTest {

  public static void main(String[] args) {
   
    TestRunner.run(new SubjectSourceConfigurationTest("testLdapSubjectSourceConfig"));
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
    
    try {      
      Subject subject = SubjectFinder.findByIdentifierAndSource("vivek@test.com", "sqlSubjectSourceIdAnother", true);
      assertEquals("vivek ("+subject.getId()+") - vivek@test.com", subject.getDescription());
      assertEquals("vivek", subject.getName());
    } catch (Exception e) {
      e.printStackTrace();
      org.junit.Assert.fail("Should never get here.");
    }
    
  }
  

}
