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
/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hooks.examples.ESCOAttributeHooks;
import edu.internet2.middleware.grouper.hooks.examples.ESCOGroupHooks;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class ESCOGroupHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ESCOGroupHooksTest("testOperations"));
  }
  
  /**
   * @param name
   */
  public ESCOGroupHooksTest(String name) {
    super(name);
  }
  
  /** The logger to use. */
  private static final Log LOGGER = GrouperUtil.getLog(ESCOGroupHooksTest.class);

  
  /**
   * run a test
   */
  public void testOperations() {
    
    LOGGER.debug("start, before addGroup");
    
    final Group theGroup = StemHelper.addChildGroup(ESCOGroupHooksTest.this.edu, "testOperation", "testOperation");

    LOGGER.debug("after addGroup, before addType");
    
    theGroup.addType(GroupTypeFinder.find("someType", true));
    
    LOGGER.debug("after addType, before addAttribute");

    theGroup.setAttribute("someAttr", "firstValue");
    theGroup.store();

    LOGGER.debug("after addAttr, before changeAttribute");

    theGroup.setAttribute("someAttr", "secondValue");
    theGroup.store();
    
    LOGGER.debug("after changeAttr, before changeField");

    theGroup.setDisplayExtension("newDisplayExtension");
    theGroup.store();

    LOGGER.debug("after changeField, before groupDelete");
    
    theGroup.delete();

    
  }
  
  /** edu stem */
  private Stem edu;
  
  /** root stem */
  private Stem root;
  
  /** grouper sesion */
  private GrouperSession grouperSession; 

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    overrideHooksRemove();
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), (Class<?>)null);
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    overrideHooksAdd();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    
    GroupType groupType = GroupType.createType(grouperSession, "someType");
    groupType.addAttribute(grouperSession, "someAttr", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), 
        GrouperUtil.toListClasses(ESCOGroupHooks.class));
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE.getPropertyFileKey(), 
        GrouperUtil.toListClasses(ESCOAttributeHooks.class));
  }

}
