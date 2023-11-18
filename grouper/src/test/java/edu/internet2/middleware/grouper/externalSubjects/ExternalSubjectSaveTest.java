/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.externalSubjects;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.SaveMode;


/**
 *
 */
public class ExternalSubjectSaveTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ExternalSubjectSaveTest("testSave"));
  }
  
  /**
   * 
   */
  private static boolean hasJabber = false;

  /**
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();

    hasJabber = StringUtils.equals(GrouperConfig.retrieveConfig().propertyValueString("externalSubjects.attributes.jabber.systemName"), "jabber");

  }
  
  /**
   * 
   */
  public ExternalSubjectSaveTest() {
    super();
    
  }

  /**
   * @param name
   */
  public ExternalSubjectSaveTest(String name) {
    super(name);
    
  }

  /**
   * Test method for {@link edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectSave#save()}.
   */
  public void testSave() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    ExternalSubject externalSubject = null;
    
    try {
      externalSubject = new ExternalSubjectSave(grouperSession).assignSaveMode(SaveMode.UPDATE).assignName("name")
        .assignIdentifier("abc@whatever.com").save();
    } catch (Exception e) {
      //good
    }

    externalSubject = new ExternalSubjectSave(grouperSession).assignName("name")
        .assignIdentifier("abc@whatever.com").save();
    
    assertNotNull(externalSubject);
    
    try {
      externalSubject = new ExternalSubjectSave(grouperSession).assignSaveMode(SaveMode.INSERT).assignName("name")
          .assignIdentifier("abc@whatever.com").assignEmail("abc@place.com").save();
      
    } catch (Exception e) {
      
    }

    externalSubject = new ExternalSubjectSave(grouperSession).assignName("name")
        .assignIdentifier("abc@whatever.com").assignEmail("abc@place.com").save();

    assertNotNull(externalSubject);
    assertEquals("abc@place.com", externalSubject.getEmail());
    
    if (hasJabber) {
      
      externalSubject = new ExternalSubjectSave(grouperSession).assignSaveMode(SaveMode.UPDATE).assignName("name")
          .assignIdentifier("abc@whatever.com").addAttribute("jabber", "a@b.c").save();
      
      assertEquals("a@b.c", externalSubject.retrieveFieldValue("jabber"));
      
      
      externalSubject = new ExternalSubjectSave(grouperSession).assignSaveMode(SaveMode.UPDATE).assignName("name")
          .assignIdentifier("abc@whatever.com").addAttribute("jabber", "a@b.d").save();
      
      assertEquals("a@b.d", externalSubject.retrieveFieldValue("jabber"));
      
      externalSubject = new ExternalSubjectSave(grouperSession).assignSaveMode(SaveMode.UPDATE).assignName("name")
          .assignIdentifier("abc@whatever.com").save();
      
      assertEquals("", StringUtils.trimToEmpty(externalSubject.retrieveFieldValue("jabber")));
      
      
      
    }
    
  }

}
