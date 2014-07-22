/**
 * Copyright 2014 Internet2
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
 */
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
    externalSubject.setIdentifier("e@i.p");
    externalSubject.setEmail("ea");
    externalSubject.setInstitution("in");
    ExternalSubjectAttribute externalSubjectAttribute = new ExternalSubjectAttribute();
    externalSubjectAttribute.setAttributeSystemName("jabber");
    externalSubjectAttribute.setAttributeValue("w@e.r");
    externalSubject.store(GrouperUtil.toSet(externalSubjectAttribute), null, true, true, false);

    //should work now
    externalSubject.store();

    assertEquals("e@i.p", ExternalSubjectHooksImpl.lastIdentifier);
    
    
    externalSubject = new ExternalSubject();
    externalSubject.setName("my name2");
    externalSubject.setIdentifier("vetome@school.edu");
    externalSubject.setEmail("ea2");
    externalSubject.setInstitution("in2");
    externalSubjectAttribute = new ExternalSubjectAttribute();
    externalSubjectAttribute.setAttributeSystemName("jabber");
    externalSubjectAttribute.setAttributeValue("w@e.r2");

    try {
      //should not work now
      externalSubject.store(GrouperUtil.toSet(externalSubjectAttribute), null, true, true, false);
    } catch (HookVeto hv) {
      assertEquals("hook.veto.external.subject.cant.be.vetome", hv.getReasonKey());
      assertEquals("name cannot be vetome", hv.getReason());
    }
      
  }
}
