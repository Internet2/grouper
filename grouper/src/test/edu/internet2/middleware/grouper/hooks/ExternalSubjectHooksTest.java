/*
 * @author mchyzer
 * $Id: LifecycleHooksTest.java,v 1.2 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class ExternalSubjectHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new ExternalSubjectHooksTest("testExternalSubject"));
    TestRunner.run(ExternalSubjectHooksTest.class);
  }
  
  /**
   * 
   */
  @Override
  protected void setUp() {
    super.setUp();

    //this is the test hook implement
    GrouperHookType.addHookOverride(GrouperHookType.EXTERNAL_SUBJECT.getPropertyFileKey(), 
        GrouperUtil.toListClasses(ExternalSubjectHooksImpl.class));

  }

  /**
   * 
   */
  @Override
  protected void tearDown() {
    super.tearDown();

    //dont have the test hook implement
    GrouperHookType.addHookOverride(GrouperHookType.EXTERNAL_SUBJECT.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * @param name
   */
  public ExternalSubjectHooksTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testExternalSubject() {
    //institution is required
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setName("my name");
    externalSubject.setIdentifier("e");
    externalSubject.setEmail("ea");
    externalSubject.setInstitution("in");
    ExternalSubjectAttribute externalSubjectAttribute = new ExternalSubjectAttribute();
    externalSubjectAttribute.setAttributeSystemName("jabber");
    externalSubjectAttribute.setAttributeValue("w@e.r");
    externalSubject.store(GrouperUtil.toSet(externalSubjectAttribute), null, true);

    //should work now
    externalSubject.store();

    assertEquals("e", ExternalSubjectHooksImpl.lastIdentifier);
    
    
    externalSubject = new ExternalSubject();
    externalSubject.setName("my name2");
    externalSubject.setIdentifier("vetome@school.edu");
    externalSubject.setEmail("ea2");
    externalSubject.setInstitution("in2");
    externalSubjectAttribute = new ExternalSubjectAttribute();
    externalSubjectAttribute.setAttributeSystemName("jabber");
    externalSubjectAttribute.setAttributeValue("w@e.r2");

    try {
      //should notwork now
      externalSubject.store(GrouperUtil.toSet(externalSubjectAttribute), null, true);
    } catch (HookVeto hv) {
      assertEquals("hook.veto.external.subject.cant.be.vetome", hv.getReasonKey());
      assertEquals("name cannot be vetome", hv.getReason());
    }
      
  }
}