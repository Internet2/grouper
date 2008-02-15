/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup0.java,v 1.6 2008-02-15 09:02:00 mchyzer Exp $
 */
public class TestGroup0 extends GrouperTest {

  /** log */
  private static final Log LOG = LogFactory.getLog(TestGroup0.class);
  
  /**
   * ctor
   * @param name
   */
  public TestGroup0(String name) {
    super(name);
  }

  /**
   * setup
   */
  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  /**
   * teardown
   */
  protected void tearDown () {
    LOG.debug("tearDown");
  }

  /**
   * test
   */
  public void testAddGroupAsMemberAndThenDeleteAsMember() {
    LOG.info("testAddGroupAsMemberAndThenDeleteAsMember");
    try {
      R       r     = R.populateRegistry(1, 2, 0);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Subject bSubj = b.toSubject();
      Assert.assertFalse( "a !has b"          , a.hasMember(bSubj)  );
      a.addMember(bSubj);
      Assert.assertTrue(  "a now has b"       , a.hasMember(bSubj)  );
      a.deleteMember(bSubj); 
      Assert.assertFalse( "a no longer has b" , a.hasMember(bSubj)  );
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddGroupAsMemberAndThenDeleteAsMember()

  /**
   * test
   * @throws Exception if problem
   */
  public void testStaticSaveGroup() throws Exception {
    
    R.populateRegistry(1, 2, 0);
    
    String displayExtension = "testing123 display";
    GrouperSession rootSession = SessionHelper.getRootSession();
    String groupDescription = "description";
    try {
      String groupNameNotExist = "whatever:whatever:testing123";
      
      GrouperTest.deleteGroupIfExists(rootSession, groupNameNotExist);
      
      Group.saveGroup(rootSession, groupDescription, 
          displayExtension, groupNameNotExist, 
          null, SaveMode.UPDATE, false);
      fail("this should fail, since stem doesnt exist");
    } catch (StemNotFoundException e) {
      //good, caught an exception
      //e.printStackTrace();
    }
    
    //////////////////////////////////
    //this should insert
    String groupName = "i2:a:testing123";
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    Group createdGroup = Group.saveGroup(rootSession, groupDescription, 
        displayExtension, groupName, 
        null, SaveMode.INSERT, false);
    
    //now retrieve
    Group foundGroup = GroupFinder.findByName(rootSession, groupName);
    
    assertEquals(groupName, createdGroup.getName());
    assertEquals(groupName, foundGroup.getName());
    
    assertEquals(displayExtension, createdGroup.getDisplayExtension());
    assertEquals(displayExtension, foundGroup.getDisplayExtension());
    
    assertEquals(groupDescription, createdGroup.getDescription());
    assertEquals(groupDescription, foundGroup.getDescription());
    
    ///////////////////////////////////
    //this should update by uuid
    createdGroup = Group.saveGroup(rootSession, groupDescription + "1", 
        displayExtension, groupName, 
        createdGroup.getUuid(), SaveMode.INSERT_OR_UPDATE, false);
    assertEquals("this should update by uuid", groupDescription + "1", createdGroup.getDescription());
    
    //this should update by name
    createdGroup = Group.saveGroup(rootSession, groupDescription + "2", 
        displayExtension, groupName, 
        null, SaveMode.UPDATE, false);
    assertEquals("this should update by name", groupDescription + "2", createdGroup.getDescription());
    
    /////////////////////////////////////
    //create a group that creates a bunch of stems
    String stemsNotExist = "whatever:heythere:another";
    String groupNameCreateStems = stemsNotExist + ":" + groupName;
    GrouperTest.deleteGroupIfExists(rootSession, groupNameCreateStems);
    GrouperTest.deleteAllStemsIfExists(rootSession, stemsNotExist);
    //lets also delete those stems
    createdGroup = Group.saveGroup(rootSession, groupDescription, 
        displayExtension, groupNameCreateStems, 
        null, SaveMode.INSERT_OR_UPDATE, true);
    
    assertEquals(groupDescription, createdGroup.getDescription());
    
    rootSession.stop();
    
  }
  
}

