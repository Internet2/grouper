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
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestStem0.java,v 1.9.2.1 2009-04-29 11:37:59 mchyzer Exp $
 */
public class TestStem0 extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestStem0("testGetChildMembershipGroups"));
  }
  
  /** log */
  private static final Log LOG = GrouperUtil.getLog(TestStem0.class);

  /**
   * ctor
   * @param name name
   */
  public TestStem0(String name) {
    super(name);
  }

  /**
   * test delete
   */
  public void testDeleteEmptyStem() {
    LOG.info("testDeleteEmptyStem");
    try {
      R r = R.populateRegistry(0, 0, 0);
      r.ns.delete();
      Assert.assertTrue("deleted stem", true);
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteEmptyStem()

  /**
   * test static save stem
   * @throws Exception if problem
   */
  public void testStaticSaveStem() throws Exception {
    
    R.populateRegistry(1, 2, 0);
    
    String displayExtension = "testing123 display";
    GrouperSession rootSession = SessionHelper.getRootSession();
    String stemDescription = "description";
    try {
      String stemNameNotExist = "whatever123:whatever:testing123";
      
      GrouperTest.deleteAllStemsIfExists(rootSession, stemNameNotExist);
      
      Stem.saveStem(rootSession, stemNameNotExist,null,  stemNameNotExist, 
          displayExtension, stemDescription, 
          SaveMode.UPDATE, false);
      fail("this should fail, since stem doesnt exist");
    } catch (StemNotFoundException e) {
      //good, caught an exception
      //e.printStackTrace();
    }
    
    /////////////////////////////////
    String stemName = "i2:a:testing123";
    GrouperTest.deleteStemIfExists(rootSession, stemName);
    
    //////////////////////////////////
    //this should insert
    Stem createdStem = Stem.saveStem(rootSession, null, null, 
        stemName,displayExtension, stemDescription, 
        SaveMode.INSERT, false);
    
    //now retrieve
    Stem foundStem = StemFinder.findByName(rootSession, stemName);
    
    assertEquals(stemName, createdStem.getName());
    assertEquals(stemName, foundStem.getName());
    
    assertEquals(displayExtension, createdStem.getDisplayExtension());
    assertEquals(displayExtension, foundStem.getDisplayExtension());
    
    assertEquals(stemDescription, createdStem.getDescription());
    assertEquals(stemDescription, foundStem.getDescription());
    
    ///////////////////////////////////
    //this should update by uuid
    createdStem = Stem.saveStem(rootSession, stemName, createdStem.getUuid(),
        stemName, displayExtension, stemDescription + "1", 
         SaveMode.INSERT_OR_UPDATE, false);
    assertEquals("this should update by uuid", stemDescription + "1", createdStem.getDescription());
    
    //this should update by name
    createdStem = Stem.saveStem(rootSession, stemName, null, stemName, 
        displayExtension, stemDescription + "2", 
        SaveMode.UPDATE, false);
    assertEquals("this should update by name", stemDescription + "2", createdStem.getDescription());
    
    /////////////////////////////////////
    //create a stem that creates a bunch of stems
    String stemsNotExist = "whatever123:heythere:another";
    //lets also delete those stems
    GrouperTest.deleteAllStemsIfExists(rootSession, stemsNotExist);
    createdStem = Stem.saveStem(rootSession, stemsNotExist, null, 
        stemsNotExist, displayExtension, stemDescription, 
        SaveMode.INSERT_OR_UPDATE, true);
    
    assertEquals(stemDescription, createdStem.getDescription());
    //clean up
    GrouperTest.deleteAllStemsIfExists(rootSession, stemsNotExist);
    
    rootSession.stop();
    
  }

  /**
   * test new performance method for child memberships
   * @throws Exception 
   */
  public void testGetChildMembershipGroups() throws Exception {
    
    GrouperSession aSession;
    R r = R.populateRegistry(0, 0, 1);
    final Subject subject = r.getSubject("a");
    
    aSession = GrouperSession.start(subject);
    final Stem[] topNew = new Stem[1];
    final Group[] group1 = new Group[1];
    
    final GrouperSession rootSession = aSession.internal_getRootSession();
    
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          topNew[0] = StemFinder.findRootStem(grouperSession).addChildStem("top new", "top new display name");
          group1[0] = topNew[0].addChildGroup("test1", "test1");
          
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });

    Set<Group> groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size());    

    //add a membership (grouper all should see
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].addMember(subject);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size());    

    //remove grouper all, should not see
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
          group1[0].revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size()); 
    
    //remove membership, add a priv
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].deleteMember(subject);
          group1[0].grantPriv(subject, AccessPrivilege.READ);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size());    

    //add membership
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].addMember(subject);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size());    
    
    
    //remove read, add list
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].revokePriv(subject, AccessPrivilege.READ);
          group1[0].grantPriv(subject, AccessPrivilege.VIEW);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });
    
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(0, groups.size());

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size());    
    
    //remove list, add update
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].revokePriv(subject, AccessPrivilege.VIEW);
          group1[0].grantPriv(subject, AccessPrivilege.UPDATE);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });
    
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(0, groups.size());

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    
  }
  
}

