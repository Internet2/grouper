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
import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup0.java,v 1.5 2008-02-08 07:37:31 mchyzer Exp $
 */
public class TestGroup0 extends TestCase {

  /** */
  private static final Log LOG = LogFactory.getLog(TestGroup0.class);
  
  /**
   * 
   * @param name
   */
  public TestGroup0(String name) {
    super(name);
  }

  /**
   * 
   */
  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  /**
   * 
   */
  protected void tearDown () {
    LOG.debug("tearDown");
  }

  /**
   * 
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
   * helper method to delete group if not exist
   * @param grouperSession
   * @param name
   * @throws Exception 
   */
  public static void deleteGroupIfExists(GrouperSession grouperSession, String name) throws Exception {
    
    try {
      Group group = GroupFinder.findByName(grouperSession, name);
      //hopefully this will succeed
      group.delete();
    } catch (GroupNotFoundException gnfe) {
      //this is good
    }
    
  }
  
  /**
   * 
   * @param names
   * @param length
   * @return stem name based on array and length
   */
  public static String stemName(String[] names, int length) {
    StringBuilder result = new StringBuilder();
    for (int i=0;i<length;i++) {
      result.append(names[i]);
      if (i<length-1) {
        result.append(":");
      }
    }
    return result.toString();
  }
  
  /**
   * helper method to delete stems if not exist
   * @param grouperSession
   * @param name
   * @throws Exception 
   */
  public static void deleteStemsIfExists(GrouperSession grouperSession, String name) throws Exception {
    //this isnt good, it exists
    String[] stems = StringUtils.split(name, ':');
    Stem currentStem = null;
    for (int i=stems.length-1;i>-0;i--) {
      String currentName = stemName(stems, i+1);
      try {
        currentStem = StemFinder.findByName(grouperSession, currentName);
      } catch (StemNotFoundException snfe1) {
        continue;
      }
      currentStem.delete();
    }
    
  }

  /**
   * @throws Exception
   */
  public void testStaticSaveGroup() throws Exception {
    
    R.populateRegistry(1, 2, 0);
    
    String displayExtension = "testing123 display";
    GrouperSession rootSession = SessionHelper.getRootSession();
    String groupDescription = "description";
    try {
      String groupNameNotExist = "whatever:whatever:testing123";
      
      deleteGroupIfExists(rootSession, groupNameNotExist);
      
      Group.saveGroup(rootSession, groupDescription, 
          displayExtension, groupNameNotExist, 
          null, false, true, false);
      fail("this should fail, since stem doesnt exist");
    } catch (StemNotFoundException e) {
      //good, caught an exception
      //e.printStackTrace();
    }
    
    /////////////////////////////////
    String groupName = "i2:a:testing123";
    deleteGroupIfExists(rootSession, groupName);
    try {
      Group.saveGroup(rootSession, groupDescription, 
          displayExtension, groupName, 
          null, false, false, false);
      fail("if not create if not exist, and doesnt exist, then fail");
    } catch (GroupNotFoundException e) {
      //good, caught an exception
      e.printStackTrace();
    }
    
    //////////////////////////////////
    //this should insert
    Group createdGroup = Group.saveGroup(rootSession, groupDescription, 
        displayExtension, groupName, 
        null, false, true, false);
    
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
        createdGroup.getUuid(), false, false, false);
    assertEquals("this should update by uuid", groupDescription + "1", createdGroup.getDescription());
    
    //this should update by name
    createdGroup = Group.saveGroup(rootSession, groupDescription + "2", 
        displayExtension, groupName, 
        null, true, false, false);
    assertEquals("this should update by name", groupDescription + "2", createdGroup.getDescription());
    
    /////////////////////////////////////
    //create a group that creates a bunch of stems
    String stemsNotExist = "whatever:heythere:another";
    String groupNameCreateStems = stemsNotExist + ":" + groupName;
    deleteGroupIfExists(rootSession, groupNameCreateStems);
    deleteStemsIfExists(rootSession, stemsNotExist);
    //lets also delete those stems
    createdGroup = Group.saveGroup(rootSession, groupDescription, 
        displayExtension, groupNameCreateStems, 
        null, false, true, true);
    
    assertEquals(groupDescription, createdGroup.getDescription());
    
    rootSession.stop();
    
  }
  
}

