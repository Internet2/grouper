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

package edu.internet2.middleware.grouper.privs;
import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MemberHelper;
import edu.internet2.middleware.grouper.helper.PrivHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test use of the ADMIN {@link AccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivADMIN.java,v 1.2 2009-03-24 17:12:09 mchyzer Exp $
 */
public class TestPrivADMIN extends GrouperTest {

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new TestPrivADMIN("testRenameGroupWithoutADMIN"));
    //TestRunner.run(TestPrivADMIN.class);
  }
  
  /**
   * 
   */
  public TestPrivADMIN() {
    super();
  }

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestPrivADMIN.class);


  // Private Class Variables
  private static Group          a;
  private static Stem           edu;
  private static Group          i2;
  private static Member         m;
  private static GrouperSession nrs;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;
  private static Group          uofc;


  public TestPrivADMIN(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    super.setUp();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    //do the root session last
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    subj0 = SubjectTestHelper.SUBJ0;
    
    subj1 = SubjectTestHelper.SUBJ1;
    nrs   = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    m     = MemberHelper.getMemberBySubject(nrs, subj1);
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    super.tearDown();
  }
  
  public void testGrantedToCreator() {
    final Group[] groups = new Group[1];
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        LOG.info("testGrantedToCreator");
        PrivHelper.grantPriv(s, edu, subj0, NamingPrivilege.CREATE);
        a = GroupHelper.findByName(nrs, i2.getName());
        return null;
      }
      
    });
    GrouperSession.callbackGrouperSession(nrs, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        LOG.info("testGrantedToCreator.0");
        Stem  stem  = StemHelper.findByName(nrs, edu.getName());
        LOG.info("testGrantedToCreator.1");
        groups[0] = StemHelper.addChildGroup(stem, "group", "a group");
        LOG.info("testGrantedToCreator.2");
        PrivHelper.hasPriv(nrs, groups[0], nrs.getSubject(), AccessPrivilege.ADMIN, true);
        LOG.info("testGrantedToCreator.3");
        return null;
      }
      
    });
  } // public void testGrantedToCreator()

  public void testGrantAdminWithoutADMIN() {
    LOG.info("testGrantAdminWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testGrantAdminWithoutADMIN()

  public void testGrantAdminWithADMIN() {
    LOG.info("testGrantAdminWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testGrantAdminWithADMIN()

  public void testGrantOptinWithoutADMIN() {
    LOG.info("testGrantOptinWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testGrantAdminWithoutADMIN()

  public void testGrantOptinWithADMIN() {
    LOG.info("testGrantOptinWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testGrantOptinWithADMIN()

  public void testGrantOptoutWithoutADMIN() {
    LOG.info("testGrantOptoutWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testGrantOptoutWithoutADMIN()

  public void testGrantOptoutWithADMIN() {
    LOG.info("testGrantOptoutWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testGrantOptoutWithADMIN()

  public void testGrantReadWithoutADMIN() {
    LOG.info("testGrantReadWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testGrantReadWithoutREAD()

  public void testGrantReadWithADMIN() {
    LOG.info("testGrantReadWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testGrantReadWithADMIN()

  public void testGrantUpdateWithoutADMIN() {
    LOG.info("testGrantUpdateWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testGrantUpdateWithoutADMIN()

  public void testGrantUpdateWithADMIN() {
    LOG.info("testGrantUpdateWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testGrantUpdateWithADMIN()

  public void testGrantViewWithoutADMIN() {
    LOG.info("testGrantViewWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testGrantViewWithoutADMIN()

  public void testGrantViewWithADMIN() {
    LOG.info("testGrantViewWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testGrantViewWithADMIN()
  
  public void testRevokeAdminWithoutADMIN() {
    LOG.info("testRevokeAdminWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testRevokeAdminWithoutADMIN()

  public void testRevokeAdminWithADMIN() {
    LOG.info("testRevokeAdminWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testRevokeAdminWithADMIN()

  public void testRevokeOptinWithoutADMIN() {
    LOG.info("testRevokeOptinWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testRevokeAdminWithoutADMIN()

  public void testRevokeOptinWithADMIN() {
    LOG.info("testRevokeOptinWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testRevokeOptinWithADMIN()

  public void testRevokeOptoutWithoutADMIN() {
    LOG.info("testRevokeOptoutWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testRevokeOptoutWithoutADMIN()

  public void testRevokeOptoutWithADMIN() {
    LOG.info("testRevokeOptoutWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testRevokeOptoutWithADMIN()

  public void testRevokeReadWithoutADMIN() {
    LOG.info("testRevokeReadWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.READ);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testRevokeReadWithoutREAD()

  public void testRevokeReadWithADMIN() {
    LOG.info("testRevokeReadWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.READ);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testRevokeReadWithADMIN()

  public void testRevokeUpdateWithoutADMIN() {
    LOG.info("testRevokeUpdateWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.UPDATE);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testRevokeUpdateWithoutADMIN()

  public void testRevokeUpdateWithADMIN() {
    LOG.info("testRevokeUpdateWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testRevokeUpdateWithADMIN()

  public void testRevokeViewWithoutADMIN() {
    LOG.info("testRevokeViewWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.VIEW);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testRevokeViewWithoutADMIN()

  public void testRevokeViewWithADMIN() {
    LOG.info("testRevokeViewWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testRevokeViewWithADMIN()

  public void testDeleteGroupWithoutADMIN() {
    LOG.info("testDeleteGroupWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.deleteFail(a);
  } // public void testDeleteGroupWithoutADMIN()

  public void testDeleteGroupWithADMIN() {
    LOG.info("testDeleteGroupWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delete(nrs, a, i2.getName());
  } // public void testDeleteGroupWithADMIN()

  public void testDeleteGroupWithMemberWithoutADMIN() {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
    
        LOG.info("testDeleteGroupWithMemberWithoutADMIN");
        GroupHelper.addMember(i2, subj1, m);
        return null;
      }
    });
    GrouperSession.callbackGrouperSession(nrs, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        a = GroupHelper.findByName(nrs, i2.getName());
        GroupHelper.deleteFail(a);
        return null;
      }
    });
  } // public void testDeleteGroupWithMemberWithoutADMIN()

  public void testDeleteGroupWithMemberWithADMIN() {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        LOG.info("testDeleteGroupWithMemberWithADMIN");
        GroupHelper.addMember(i2, subj1, m);
        PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
        a = GroupHelper.findByName(nrs, i2.getName());
        try {
        	Thread.currentThread().sleep(3000);
        }catch(InterruptedException e){}
        GroupHelper.delete(nrs, a, i2.getName());
        return null;
      }
    });
  } // public void testDeleteGroupWithMemberWithADMIN()

  public void testDeleteGroupIsMemberWithoutADMIN() {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        LOG.info("testDeleteGroupIsMemberWithoutADMIN");
        GroupHelper.addMember(uofc, i2);
        return null;
      }
    });
    GrouperSession.callbackGrouperSession(nrs, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        a = GroupHelper.findByName(nrs, i2.getName());
        GroupHelper.deleteFail(a);
        return null;
      }
    });
  } // public void testDeleteGroupIsMemberWithoutADMIN()

  public void testDeleteGroupIsMemberWithADMIN() {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        LOG.info("testDeleteGroupIsMemberWithADMIN");
        GroupHelper.addMember(uofc, i2);
        PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
        a = GroupHelper.findByName(nrs, i2.getName());
        GroupHelper.delete(nrs, a, i2.getName());
        return null;
      }
    });
    
  } // public void testDeleteGroupIsMemberWithADMIN()

  public void testSetAttrsWithoutADMIN() {
    LOG.info("testSetAttrsWithoutADMIN");
    String val = "new value";
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.setAttrFail(a, ""                 , ""  );
    GroupHelper.setAttrFail(a, ""                 , null);
    GroupHelper.setAttrFail(a, null               , null);
    GroupHelper.setAttrFail(a, null               , ""  );
    GroupHelper.setAttrFail(a, "attr"             , val );
    GroupHelper.setAttrFail(a, "description"      , val );
    GroupHelper.setAttrFail(a, "displayName"      , val );
    GroupHelper.setAttrFail(a, "displayExtension" , val );
    GroupHelper.setAttrFail(a, "extension"        , val );
    GroupHelper.setAttrFail(a, "name"             , val );
  } // public void testSetAttrsWithoutADMIN()

  /**
   */
  private static final String ATTRIBUTE1 = "attribute1";
  /**
   */
  private static final String GROUP_TYPE1 = "groupType1";

  public void testSetAttrsWithADMIN() throws Exception {
    LOG.info("testSetAttrsWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    String val = "new value";
    GroupHelper.setAttrFail(a, ""                 , ""  );
    GroupHelper.setAttrFail(a, ""                 , null);
    GroupHelper.setAttrFail(a, null               , null);
    GroupHelper.setAttrFail(a, null               , ""  );
    GroupHelper.setAttrFail(a, "attr"             , val );
    a.setDescription( val );
    try {
      //fail 
      a.setDisplayName(val );
      a.store();
      
      fail();
    } catch (Exception e) {
      //ok
    }
    a.setDisplayExtension(val );
    a.setExtension( val );

    try {
      //fail 
      a.setName( val );
      a.store();
      fail();
    } catch (Exception e) {
      //ok
    }
    
    a.store();
    
    GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );

    GroupType groupType = GroupType.createType(grouperSession, GROUP_TYPE1, false); 
    groupType.addAttribute(grouperSession,ATTRIBUTE1, 
          false);
    a.addType(groupType, false);
    a.setAttribute(ATTRIBUTE1, "whatever");

  } // public void testSetAttrsWithADMIN()

  public void testDelAttrsWithoutADMIN() {
    LOG.info("testDelAttrsWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delAttrFail(a, ""                 );
    GroupHelper.delAttrFail(a, null               );
    GroupHelper.delAttrFail(a, "attr"             );
    GroupHelper.delAttrFail(a, "description"      );
    GroupHelper.delAttrFail(a, "displayName"      );
    GroupHelper.delAttrFail(a, "displayExtension" );
    GroupHelper.delAttrFail(a, "extension"        );
    GroupHelper.delAttrFail(a, "name"             );
  } // public void testDelAttrsWithoutADMIN()

  public void testRenameGroupWithoutADMIN() {
    LOG.info("testRenameGroupWithoutADMIN");
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        i2.grantPriv(nrs.getSubject(), AccessPrivilege.READ);
        return null;
      }
    });
    
    a = GroupHelper.findByName(nrs, i2.getName());
    String orig = a.getExtension();
    try {
      a.setExtension("foo");
      a.store();
      Assert.fail("set extension");
    }
    catch (Exception e) {
      Assert.assertTrue("failed to set extension", true);
      a = GroupHelper.findByName(nrs, i2.getName());
      Assert.assertTrue("extension", a.getExtension().equals(orig));
    } 
    orig = a.getDisplayExtension();
    try {
      a.setDisplayExtension("foo");
      a.store();
      Assert.fail("set displayExtension");
    }
    catch (Exception e) {
      Assert.assertTrue("failed to set displayExtension", true);
      a = GroupHelper.findByName(nrs, i2.getName());
      Assert.assertTrue("displayExtension", a.getDisplayExtension().equals(orig));
    } 
  } // public void testRenameGroupWithoutADMIN()

  public void testRenameGroupWithADMIN() {
    LOG.info("testRenameGroupWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    String  val   = "foo";
    try {
      a.setExtension(val);
      a.store();
      Assert.assertTrue("set extension", true);
      Assert.assertTrue("extension", a.getExtension().equals(val));
    }
    catch (Exception e) {
      Assert.fail("failed to set extension");
    } 
    try {
      a.setDisplayExtension("foo");
      a.store();
      Assert.assertTrue("set displayExtension", true);
      Assert.assertTrue("displayExtension", a.getDisplayExtension().equals(val));
    }
    catch (Exception e) {
      Assert.fail("failed to set displayExtension");
    } 
  } // public void testRenameGroupWithADMIN()
}

