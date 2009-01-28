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

import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;


/**
 * Test {@link Group}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_Group.java,v 1.5 2009-01-28 20:33:09 shilen Exp $
 * @since   1.2.1
 */
public class Test_api_Group extends GrouperTest {


  private Group           top_group, child_group;
  private GrouperSession  s;
  private Stem            child, root, top;



  public void setUp() {
    super.setUp();
    try {
      this.s            = GrouperSession.start( SubjectFinder.findRootSubject() );
      this.root         = StemFinder.findRootStem(this.s);
      this.top          = this.root.addChildStem("top", "top display name");
      this.top_group    = this.top.addChildGroup("top group", "top group");
      this.child        = this.top.addChildStem("child", "child");
      this.child_group  = this.child.addChildGroup("child group", "child group display name");
    }
    catch (Exception e) {
      throw new GrouperRuntimeException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }



  /**
   * @since   1.2.1
   */
  public void test_revokePriv_Privilege_namingPrivilege() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException
  {
    try {
      this.top_group.revokePriv(NamingPrivilege.STEM);
      fail("failed to throw expected SchemaException");
    }
    catch (SchemaException eExpected) {
      assertTrue(true);
    }
  }



  /**
   * @since   1.2.1
   */
  public void test_revokePriv_SubjectAndPrivilege_namingPrivilege() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException
  {
    try {
      this.top_group.revokePriv( SubjectFinder.findAllSubject(), NamingPrivilege.STEM );
      fail("failed to throw expected SchemaException");
    }
    catch (SchemaException eExpected) {
      assertTrue(true);
    }
  }
  
  /**
   * @throws Exception
   */
  public void test_move() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");

    child_group.addMember(a);
    child_group.grantPriv(a, AccessPrivilege.UPDATE);
    child_group.move(top);

    child_group = GroupFinder.findByName(s, "top:child group");
    assertGroupName(child_group, "top:child group");
    assertGroupDisplayName(child_group, "top display name:child group display name");
    assertGroupHasMember(child_group, a, true);
    assertGroupHasMember(child_group, b, false);
    assertGroupHasUpdate(child_group, a, true);
    assertTrue(child_group.getParentStem().getUuid().equals(top.getUuid()));
    
    r.rs.stop();
  }
  
  /**
   * @throws InsufficientPrivilegeException 
   */
  public void test_move_toRootStem() throws InsufficientPrivilegeException {
    try {
      top_group.move(root);
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
  }

  /**
   * @throws Exception
   */
  public void test_move_insufficientPrivileges() throws Exception {
    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 5);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    Subject c = r.getSubject("c");
    Subject d = r.getSubject("d");
    Subject e = r.getSubject("e");
    
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    child_group.grantPriv(b, AccessPrivilege.UPDATE);
    top.grantPriv(b, NamingPrivilege.STEM);
    child_group.grantPriv(c, AccessPrivilege.UPDATE);
    top.grantPriv(c, NamingPrivilege.CREATE);
    child_group.grantPriv(d, AccessPrivilege.ADMIN);
    top.grantPriv(d, NamingPrivilege.STEM);    
    child_group.grantPriv(e, AccessPrivilege.ADMIN);
    top.grantPriv(e, NamingPrivilege.CREATE);   
    
    nrs = GrouperSession.start(a);
    try {
      child_group.move(top);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
    
    nrs = GrouperSession.start(b);
    try {
      child_group.move(top);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
    
    nrs = GrouperSession.start(c);
    try {
      child_group.move(top);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
    
    nrs = GrouperSession.start(d);
    child_group.move(top);
    assertTrue(true);
    nrs.stop();
    
    nrs = GrouperSession.start(e);
    child_group.move(top);
    assertTrue(true);
    nrs.stop();
        
    r.rs.stop();

  }
  
}

