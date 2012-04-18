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
 * $Id: GroupHooksDbVersionTest.java,v 1.4 2009-03-21 13:35:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GroupHooksDbVersionTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupHooksDbVersionTest("testGroupHooksDbVersion"));
    //TestRunner.run(GroupHooksDbVersionTest.class);
  }
  
  /**
   * @param name
   */
  public GroupHooksDbVersionTest(String name) {
    super(name);
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
    super.tearDown();
    overrideHooksRemove();
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    super.setUp();
    overrideHooksAdd();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), 
        GrouperUtil.toListClasses(GroupHookDbVersion.class));
  }

  /**
   * @throws Exception 
   * 
   */
  public void testGroupHooksDbVersion() throws Exception {
    
    //ApiConfig.testConfig.put("groups.allow.attribute.access.1.4", "true");
    
    //try an insert
    Group group = edu.addChildGroup("myGroup", "myGroup");
    
    group = GroupFinder.findByName(this.grouperSession, "edu:myGroup", true);
    
    group.setExtension("anotherExt");
    group.store();
    
    GroupType groupType = GroupType.createType(this.grouperSession, "test1");
    
    group.addType(groupType, false);
    
    group.setExtension("anotherExt2");
    group.store();
    
  }
  
}
