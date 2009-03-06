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

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemModifyException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;


/**
 * Test {@link Stem}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_Stem.java,v 1.16 2009-03-06 17:48:56 shilen Exp $
 * @since   1.2.1
 */
public class Test_api_Stem extends GrouperTest {


  private Group           child_group, top_group, admin, wheel;
  private GrouperSession  s;
  private Stem            child, root, top, top_new, etc, stem_copy_source, stem_copy_target;
  private GroupType       type1;
  private Field           type1attr1;

  /**
   * 
   */
  public Test_api_Stem() {
    super();
  }

  /**
   * @param name
   */
  public Test_api_Stem(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new Test_api_Stem("test_getChildGroups_PrivilegeArrayAndScope_viewPrivAndOneScope"));
    TestRunner.run(Test_api_Stem.class);
  }

  /** size before getting started */
  private int originalRootGroupSubSize = -1;
  
  /** original chld stem size */
  private int originalRootChildStemSize = -1;
  
  /** original */
  private int originalRootChildStemOneSize = -1;
  
  /** original */
  private int originalRootChildStemSubSize = -1;
  
  /** original */
  private int originalRootCreateOne = -1;
  
  /** original */
  private int originalRootCreateSub = -1;
  
  /** original */
  private int originalRootViewOne = -1;
  
  /** original */
  private int originalRootViewSub = -1;
  
  /** original */
  private int originalRootCreateAndViewOne = -1;
  
  /** original */
  private int originalRootCreateAndViewSub = -1;
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  public void setUp() {
    super.setUp();
    try {
      this.s            = GrouperSession.start( SubjectFinder.findRootSubject() );
      this.root         = StemFinder.findRootStem(this.s);
      
      this.originalRootGroupSubSize = this.root.getChildGroups(Stem.Scope.SUB).size();
      this.originalRootChildStemSize = this.root.getChildStems().size();
      this.originalRootChildStemOneSize = this.root.getChildStems(Stem.Scope.ONE).size();
      this.originalRootChildStemSubSize = this.root.getChildStems(Stem.Scope.SUB).size();
      
      this.originalRootCreateOne =  this.root.getChildStems( 
          new Privilege[]{NamingPrivilege.CREATE}, Stem.Scope.ONE ).size();
      this.originalRootCreateSub =  this.root.getChildStems( 
          new Privilege[]{NamingPrivilege.CREATE}, Stem.Scope.SUB ).size();
      this.originalRootViewOne =  this.root.getChildStems( 
          new Privilege[]{AccessPrivilege.VIEW}, Stem.Scope.ONE ).size();
      this.originalRootViewSub =  this.root.getChildStems( 
          new Privilege[]{AccessPrivilege.VIEW}, Stem.Scope.SUB ).size();
      this.originalRootCreateAndViewOne =  this.root.getChildStems( 
          new Privilege[]{NamingPrivilege.CREATE, AccessPrivilege.VIEW}, Stem.Scope.ONE ).size();
      this.originalRootCreateAndViewSub =  this.root.getChildStems( 
          new Privilege[]{NamingPrivilege.CREATE, AccessPrivilege.VIEW}, Stem.Scope.SUB ).size();
      
      this.top          = this.root.addChildStem("top", "top display name");
      this.top_group    = this.top.addChildGroup("top group", "top group display name");
      this.child        = this.top.addChildStem("child", "child display name");
      this.child_group  = this.child.addChildGroup("child group", "child group display name");
    }
    catch (Exception e) {
      throw new GrouperRuntimeException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
   */
  public void tearDown() {
    super.tearDown();
  }



  public void test_getChildGroups_fromRoot() {
    assertEquals( 0, this.root.getChildGroups().size() );
  }

  public void test_getChildGroups_fromTop() {
    assertEquals( 1, this.top.getChildGroups().size() );
  }

  public void test_getChildGroups_fromChild() {
    assertEquals( 1, this.child.getChildGroups().size() );
  }

  public void test_getChildGroups_Scope_nullScope() {
    try {
      this.root.getChildGroups(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }



  public void test_getChildGroups_PrivilegeArrayAndScope_nullArray() {
    try {
      this.root.getChildGroups(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  public void test_getChildGroups_PrivilegeArrayAndScope_nullScope() {
    try {
      this.root.getChildGroups( new Privilege[0], null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  public void test_getChildGroups_PrivilegeArrayAndScope_emptyArray() {
    assertEquals( 0, this.top.getChildGroups( new Privilege[0], Stem.Scope.SUB ).size() );
  }
  public void test_getChildGroups_PrivilegeArrayAndScope_createPrivAndOneScope() {
    Privilege[] privs = { NamingPrivilege.CREATE };
    assertEquals( 0, this.top.getChildGroups( privs, Stem.Scope.ONE ).size() );
  }
  public void test_getChildGroups_PrivilegeArrayAndScope_createPrivAndSubScope() {
    Privilege[] privs = { NamingPrivilege.CREATE };
    assertEquals( 0, this.top.getChildGroups( privs, Stem.Scope.SUB ).size() );
  }
  public void test_getChildGroups_PrivilegeArrayAndScope_viewPrivAndOneScope() {
    Privilege[] privs = { AccessPrivilege.VIEW };
    assertEquals( 1, this.top.getChildGroups( privs, Stem.Scope.ONE ).size() );
  }
  public void test_getChildGroups_PrivilegeArrayAndScope_viewPrivAndSubScope() {
    Privilege[] privs = { AccessPrivilege.VIEW };
    assertEquals( 2, this.top.getChildGroups( privs, Stem.Scope.SUB ).size() );
  }
  public void test_getChildGroups_PrivilegeArrayAndScope_createAndViewPrivsAndOneScope() {
    Privilege[] privs = { NamingPrivilege.CREATE, AccessPrivilege.VIEW };
    assertEquals( 1, this.top.getChildGroups( privs, Stem.Scope.ONE ).size() );
  }
  public void test_getChildGroups_PrivilegeArrayAndScope_createAndViewPrivsAndSubScope() {
    Privilege[] privs = { NamingPrivilege.CREATE, AccessPrivilege.VIEW };
    assertEquals( 2, this.top.getChildGroups( privs, Stem.Scope.SUB ).size() );
  }



  public void test_getChildGroups_Scope_fromRootScopeONE() {
    assertEquals( 0, this.root.getChildGroups(Stem.Scope.ONE).size() );
  }

  public void test_getChildGroups_Scope_fromRootScopeSUB() {
    assertEquals( this.originalRootGroupSubSize + 2, this.root.getChildGroups(Stem.Scope.SUB).size() );
  }

  public void test_getChildGroups_Scope_fromTopScopeONE() {
    assertEquals( 1, this.top.getChildGroups(Stem.Scope.ONE).size() );
  }

  public void test_getChildGroups_Scope_fromTopScopeSUB() {
    assertEquals( 2, this.top.getChildGroups(Stem.Scope.SUB).size() );
  }

  public void test_getChildGroups_Scope_fromChildScopeONE() {
    assertEquals( 1, this.child.getChildGroups(Stem.Scope.ONE).size() );
  }

  public void test_getChildGroups_Scope_fromChildScopeSUB() {
    assertEquals( 1, this.child.getChildGroups(Stem.Scope.SUB).size() );
  }



  public void test_getChildStems_fromRoot() {
    assertEquals( this.originalRootChildStemSize + 1, this.root.getChildStems().size() );
  }

  public void test_getChildStems_fromTop() {
    assertEquals( 1, this.top.getChildStems().size() );
  }

  public void test_getChildStems_fromChild() {
    assertEquals( 0, this.child.getChildStems().size() );
  }



  public void test_getChildStems_Scope_nullScope() {
    try {
      this.root.getChildStems(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getChildStems_Scope_fromRootScopeONE() {
    assertEquals( this.originalRootChildStemOneSize + 1, this.root.getChildStems(Stem.Scope.ONE).size() );
  }

  public void test_getChildStems_Scope_fromRootScopeSUB() {
    assertEquals( this.originalRootChildStemSubSize + 2, this.root.getChildStems(Stem.Scope.SUB).size() );
  }

  public void test_getChildStems_Scope_fromTopScopeONE() {
    assertEquals( 1, this.top.getChildStems(Stem.Scope.ONE).size() );
  }

  public void test_getChildStems_Scope_fromTopScopeSUB() {
    assertEquals( 1, this.top.getChildStems(Stem.Scope.SUB).size() );
  }

  public void test_getChildStems_Scope_fromChildScopeONE() {
    assertEquals( 0, this.child.getChildStems(Stem.Scope.ONE).size() );
  }

  public void test_getChildStems_Scope_fromChildScopeSUB() {
    assertEquals( 0, this.child.getChildStems(Stem.Scope.SUB).size() );
  }



  public void test_getChildStems_PrivilegeArrayAndScope_nullArray() {
    try {
      this.root.getChildStems(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  public void test_getChildStems_PrivilegeArrayAndScope_nullScope() {
    try {
      this.root.getChildStems( new Privilege[0], null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  public void test_getChildStems_PrivilegeArrayAndScope_emptyArray() {
    assertEquals( 0, this.root.getChildStems( new Privilege[0], Stem.Scope.SUB ).size() );
  }
  public void test_getChildStems_PrivilegeArrayAndScope_createPrivAndOneScope() {
    Privilege[] privs = { NamingPrivilege.CREATE };
    assertEquals( this.originalRootCreateOne + 1, this.root.getChildStems( privs, Stem.Scope.ONE ).size() );
  }
  public void test_getChildStems_PrivilegeArrayAndScope_createPrivAndSubScope() {
    Privilege[] privs = { NamingPrivilege.CREATE };
    assertEquals( this.originalRootCreateSub + 2, this.root.getChildStems( privs, Stem.Scope.SUB ).size() );
  }
  public void test_getChildStems_PrivilegeArrayAndScope_viewPrivAndOneScope() {
    Privilege[] privs = { AccessPrivilege.VIEW };
    assertEquals( this.originalRootViewOne + 1, this.root.getChildStems( privs, Stem.Scope.ONE ).size() );
  }
  public void test_getChildStems_PrivilegeArrayAndScope_viewPrivAndSubScope() {
    Privilege[] privs = { AccessPrivilege.VIEW };
    assertEquals( this.originalRootViewSub + 2, this.root.getChildStems( privs, Stem.Scope.SUB ).size() );
  }
  public void test_getChildStems_PrivilegeArrayAndScope_createAndViewPrivsAndOneScope() {
    Privilege[] privs = { NamingPrivilege.CREATE, AccessPrivilege.VIEW };
    assertEquals( this.originalRootCreateAndViewOne + 1, this.root.getChildStems( privs, Stem.Scope.ONE ).size() );
  }
  public void test_getChildStems_PrivilegeArrayAndScope_createAndViewPrivsAndSubScope() {
    Privilege[] privs = { NamingPrivilege.CREATE, AccessPrivilege.VIEW };
    assertEquals( this.originalRootCreateAndViewSub + 2, this.root.getChildStems( privs, Stem.Scope.SUB ).size() );
  }
  /**
   * @since   1.2.1
   */
  public void test_getChildStems_PrivilegeArrayAndScope_OneScopeDoNotReturnThisStem() {
    Privilege[] privs = { AccessPrivilege.VIEW };
    assertEquals( 1, this.top.getChildStems( privs, Stem.Scope.ONE ).size() );
  }



  public void test_isChildGroup_nullChild() {
    try {
      this.root.isChildGroup(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    } 
  }

  public void test_isChildGroup_rootAsPotentialParent() {
    assertTrue( this.root.isChildGroup( this.child_group ) );
  }

  public void test_isChildGroup_immediateChild() {
    assertTrue( this.child.isChildGroup( this.child_group ) );
  }

  public void test_isChildGroup_notChild() {
    assertFalse( this.child.isChildGroup( this.top_group ) );
  }


  public void test_isChildStem_nullChild() {
    try {
      this.root.isChildStem(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_isChildStem_rootAsPotentialParent() {
    assertTrue( this.root.isChildStem( this.child ) );
  }

  public void test_isChildStem_rootAsChild() {
    assertFalse( this.child.isChildStem( this.root ) );
  }

  public void test_isChildStem_selfAsChild() {
    assertFalse( this.child.isChildStem( this.child ) );
  }

  public void test_isChildStem_isChild() {
    assertTrue( this.top.isChildStem( this.child ) );
  }

  public void test_isChildStem_notChild() 
    throws  InsufficientPrivilegeException,
            StemAddException
  {
    Stem otherTop = this.root.addChildStem("other top", "other top");
    assertFalse( otherTop.isChildStem( this.child ) );
  }



  public void test_isRootStem_root() {
    assertTrue( this.root.isRootStem() );
  }

  public void test_isRootStem_notRootStem() {
    assertFalse( this.top.isRootStem() );
  }



  /**
   * @since   1.2.1
   */
  public void test_revokePriv_Priv_accessPrivilege() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException
  {
    try {
      this.root.revokePriv(AccessPrivilege.ADMIN);
      fail("failed to throw expected SchemaException");
    }
    catch (SchemaException eExpected) {
      assertTrue(true);
    }
  }
  
  
  /**
   * @throws InsufficientPrivilegeException 
   */
  public void test_move_rootStem() throws InsufficientPrivilegeException {
    try {
      root.move(top);
      fail("failed to throw StemModifyException");
    } catch (StemModifyException e) {
      assertTrue(true);
    }
  }
  
  /**
   * @throws InsufficientPrivilegeException 
   */
  public void test_move_toSubStem() throws InsufficientPrivilegeException {
    try {
      top.move(child);
      fail("failed to throw StemModifyException");
    } catch (StemModifyException e) {
      assertTrue(true);
    }
  }


  /**
   * @throws Exception
   */
  public void test_move_insufficientPrivileges_without_admin_or_wheel_group() throws Exception {
    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    Subject c = r.getSubject("c");
    
    this.top_new = this.root.addChildStem("top new", "top new display name");
    
    top.grantPriv(a, NamingPrivilege.STEM);
    top_new.grantPriv(a, NamingPrivilege.CREATE);
    top.grantPriv(b, NamingPrivilege.CREATE);
    top_new.grantPriv(b, NamingPrivilege.STEM);
    top.grantPriv(c, NamingPrivilege.STEM);
    top_new.grantPriv(c, NamingPrivilege.STEM);
    
    nrs = GrouperSession.start(a);
    try {
      top.move(top_new);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
    
    nrs = GrouperSession.start(b);
    try {
      top.move(top_new);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
        
    nrs = GrouperSession.start(c);
    top.move(top_new);
    assertTrue(true);
    nrs.stop();
            
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_insufficientPrivileges_with_admin_group() throws Exception {
    this.etc          = this.root.addChildStem("etc", "etc");
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    ApiConfig.testConfig.put("security.stem.groupAllowedToRenameStem", admin.getName());
    ApiConfig.testConfig.put("groups.wheel.use", "true");
    ApiConfig.testConfig.put("groups.wheel.group", wheel.getName());

    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    top.grantPriv(a, NamingPrivilege.STEM);
    top.grantPriv(b, NamingPrivilege.STEM);
    
    admin.addMember(b);
    
    nrs = GrouperSession.start(a);
    try {
      top.setExtension("top_new");
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
         
    nrs = GrouperSession.start(b);
    top.setExtension("top_new");
    assertTrue(true);
    nrs.stop();
            
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_insufficientPrivileges_with_wheel_group() throws Exception {
    this.etc          = this.root.addChildStem("etc", "etc");
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    ApiConfig.testConfig.put("security.stem.groupAllowedToRenameStem", admin.getName());
    ApiConfig.testConfig.put("groups.wheel.use", "true");
    ApiConfig.testConfig.put("groups.wheel.group", wheel.getName());

    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    top.grantPriv(a, NamingPrivilege.STEM);
    top.grantPriv(b, NamingPrivilege.STEM);
    
    wheel.addMember(b);
    
    nrs = GrouperSession.start(a);
    try {
      top.setExtension("top_new");
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
         
    nrs = GrouperSession.start(b);
    top.setExtension("top_new");
    assertTrue(true);
    nrs.stop();
            
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_move_insufficientPrivileges_with_admin_group() throws Exception {
    this.etc          = this.root.addChildStem("etc", "etc");
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    ApiConfig.testConfig.put("security.stem.groupAllowedToMoveStem", admin.getName());
    ApiConfig.testConfig.put("groups.wheel.use", "true");
    ApiConfig.testConfig.put("groups.wheel.group", wheel.getName());

    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    this.top_new = this.root.addChildStem("top new", "top new display name");
    
    top.grantPriv(a, NamingPrivilege.STEM);
    top_new.grantPriv(a, NamingPrivilege.STEM);
    top.grantPriv(b, NamingPrivilege.STEM);
    top_new.grantPriv(b, NamingPrivilege.STEM);
    
    admin.addMember(b);
    
    nrs = GrouperSession.start(a);
    try {
      top.move(top_new);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
         
    nrs = GrouperSession.start(b);
    top.move(top_new);
    assertTrue(true);
    nrs.stop();
            
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_move_insufficientPrivileges_with_wheel_group() throws Exception {
    this.etc          = this.root.addChildStem("etc", "etc");
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    ApiConfig.testConfig.put("security.stem.groupAllowedToMoveStem", admin.getName());
    ApiConfig.testConfig.put("groups.wheel.use", "true");
    ApiConfig.testConfig.put("groups.wheel.group", wheel.getName());

    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    this.top_new = this.root.addChildStem("top new", "top new display name");
    
    top.grantPriv(a, NamingPrivilege.STEM);
    top_new.grantPriv(a, NamingPrivilege.STEM);
    top.grantPriv(b, NamingPrivilege.STEM);
    top_new.grantPriv(b, NamingPrivilege.STEM);
    
    wheel.addMember(b);
    
    nrs = GrouperSession.start(a);
    try {
      top.move(top_new);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
         
    nrs = GrouperSession.start(b);
    top.move(top_new);
    assertTrue(true);
    nrs.stop();
            
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_move() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");

    this.top_new = this.root.addChildStem("top new", "top new display name");

    child_group.addMember(a);
    child_group.grantPriv(a, AccessPrivilege.UPDATE);
    child.grantPriv(b, NamingPrivilege.CREATE);
    top.grantPriv(b, NamingPrivilege.CREATE);

    // first move to a non-root stem
    top.move(top_new);

    top = StemFinder.findByName(s, "top new:top");
    child = StemFinder.findByName(s, "top new:top:child");
    child_group = GroupFinder.findByName(s, "top new:top:child:child group");
    assertStemName(top, "top new:top");
    assertStemDisplayName(top, "top new display name:top display name");
    assertStemName(child, "top new:top:child");
    assertStemDisplayName(child,
        "top new display name:top display name:child display name");
    assertGroupName(child_group, "top new:top:child:child group");
    assertGroupDisplayName(
        child_group,
        "top new display name:top display name:child display name:child group display name");
    assertGroupHasMember(child_group, a, true);
    assertGroupHasMember(child_group, b, false);
    assertGroupHasUpdate(child_group, a, true);
    assertStemHasCreate(child, a, false);
    assertStemHasCreate(child, b, true);
    assertStemHasCreate(top, b, true);
    assertTrue(child_group.getParentStem().getUuid().equals(child.getUuid()));
    assertTrue(child.getParentStem().getUuid().equals(top.getUuid()));
    assertTrue(top.getParentStem().getUuid().equals(top_new.getUuid()));
    assertTrue(top_new.getChildStems().size() == 1);

    // second move to a root stem
    top.move(root);

    top = StemFinder.findByName(s, "top");
    child = StemFinder.findByName(s, "top:child");
    child_group = GroupFinder.findByName(s, "top:child:child group");
    assertStemName(top, "top");
    assertStemDisplayName(top, "top display name");
    assertStemName(child, "top:child");
    assertStemDisplayName(child, "top display name:child display name");
    assertGroupName(child_group, "top:child:child group");
    assertGroupDisplayName(child_group,
        "top display name:child display name:child group display name");
    assertGroupHasMember(child_group, a, true);
    assertGroupHasMember(child_group, b, false);
    assertGroupHasUpdate(child_group, a, true);
    assertStemHasCreate(child, a, false);
    assertStemHasCreate(child, b, true);
    assertStemHasCreate(top, b, true);
    assertTrue(child_group.getParentStem().getUuid().equals(child.getUuid()));
    assertTrue(child.getParentStem().getUuid().equals(top.getUuid()));
    assertTrue(top.getParentStem().getUuid().equals(root.getUuid()));
    assertTrue(top_new.getChildStems().size() == 0);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_all() throws Exception {
    R r = R.populateRegistry(0, 0, 11);

    stem_copy_setup(r);
    Stem newStem = stem_copy_source.copy(stem_copy_target);
    verify_copy(r, newStem, true, true, true, true, true, true);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_all2() throws Exception {
    R r = R.populateRegistry(0, 0, 11);

    stem_copy_setup(r);
    Stem newStem = stem_copy_source.copy(stem_copy_target, true, true, true, true, true, true);
    verify_copy(r, newStem, true, true, true, true, true, true);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_stem_privs_only() throws Exception {
    R r = R.populateRegistry(0, 0, 11);

    stem_copy_setup(r);
    Stem newStem = stem_copy_source.copy(stem_copy_target, true, false, false, false, false, false);
    verify_copy(r, newStem, true, false, false, false, false, false);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_group_privs_only() throws Exception {
    R r = R.populateRegistry(0, 0, 11);

    stem_copy_setup(r);
    Stem newStem = stem_copy_source.copy(stem_copy_target, false, true, true, false, false, false);
    verify_copy(r, newStem, false, true, true, false, false, false);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_members_only() throws Exception {
    R r = R.populateRegistry(0, 0, 11);

    stem_copy_setup(r);
    Stem newStem = stem_copy_source.copy(stem_copy_target, false, false, false, true, true, false);
    verify_copy(r, newStem, false, false, false, true, true, false);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_attrs_only() throws Exception {
    R r = R.populateRegistry(0, 0, 11);

    stem_copy_setup(r);
    Stem newStem = stem_copy_source.copy(stem_copy_target, false, false, false, false, false, true);
    verify_copy(r, newStem, false, false, false, false, false, true);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_minimum_nonadmin() throws Exception {
    R r = R.populateRegistry(0, 0, 11);
    Subject c = r.getSubject("c");
    GrouperSession nrs;

    stem_copy_setup(r);

    nrs = GrouperSession.start(c);

    stem_copy_source.copy(stem_copy_target, false, false, false, false, false, false);
    assertTrue(true);
    
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_insufficient_privilege_listGroupAsMember() throws Exception {
    R r = R.populateRegistry(0, 0, 11);
    Subject c = r.getSubject("c");
    GrouperSession nrs;

    stem_copy_setup(r);

    nrs = GrouperSession.start(c);
    try {
      stem_copy_source.copy(stem_copy_target, false, true, false, false, true, false);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException eExpected) {
      assertTrue(true);
    }
        
    nrs.stop();
    
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    top_group.grantPriv(c, AccessPrivilege.ADMIN);
    nrs.stop();
        
    nrs = GrouperSession.start(c);
    stem_copy_source.copy(stem_copy_target, false, true, false, false, true, false);
    assertTrue(true);
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_insufficient_privilege_groupAsPrivilege_naming() throws Exception {
    R r = R.populateRegistry(0, 0, 11);
    Subject c = r.getSubject("c");
    GrouperSession nrs;

    stem_copy_setup(r);
    top_group.grantPriv(c, AccessPrivilege.ADMIN);

    nrs = GrouperSession.start(c);
    try {
      stem_copy_source.copy(stem_copy_target, false, true, true, false, false, false);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException eExpected) {
      assertTrue(true);
    }
        
    nrs.stop();
    
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    top.grantPriv(c, NamingPrivilege.STEM);
    nrs.stop();
        
    nrs = GrouperSession.start(c);
    stem_copy_source.copy(stem_copy_target, false, true, true, false, false, false);
    assertTrue(true);
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_insufficient_privilege_groupAsPrivilege_access() throws Exception {
    R r = R.populateRegistry(0, 0, 11);
    Subject c = r.getSubject("c");
    GrouperSession nrs;

    stem_copy_setup(r);
    top.grantPriv(c, NamingPrivilege.STEM);

    nrs = GrouperSession.start(c);
    try {
      stem_copy_source.copy(stem_copy_target, false, true, true, false, false, false);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException eExpected) {
      assertTrue(true);
    }
        
    nrs.stop();
    
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    top_group.grantPriv(c, AccessPrivilege.ADMIN);
    nrs.stop();
        
    nrs = GrouperSession.start(c);
    stem_copy_source.copy(stem_copy_target, false, true, true, false, false, false);
    assertTrue(true);
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_no_create_priv() throws Exception {
    R r = R.populateRegistry(0, 0, 11);
    Subject e = r.getSubject("e");
    GrouperSession nrs;

    stem_copy_setup(r);

    nrs = GrouperSession.start(e);

    try {
      stem_copy_source.copy(stem_copy_target, false, false, false, false, false, false);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException eExpected) {
      assertTrue(true);
    }
    
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_insufficientPrivileges_with_admin_group() throws Exception {
    this.etc          = this.root.addChildStem("etc", "etc");
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    ApiConfig.testConfig.put("security.stem.groupAllowedToCopyStem", admin.getName());
    ApiConfig.testConfig.put("groups.wheel.use", "true");
    ApiConfig.testConfig.put("groups.wheel.group", wheel.getName());

    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 11);

    stem_copy_setup(r);
    
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    stem_copy_target.grantPriv(a, NamingPrivilege.STEM, false);
    stem_copy_target.grantPriv(b, NamingPrivilege.STEM, false);
    
    admin.addMember(b);
    
    nrs = GrouperSession.start(a);
    try {
      stem_copy_source.copy(stem_copy_target, false, false, false, false, false, false);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
         
    nrs = GrouperSession.start(b);
    stem_copy_source.copy(stem_copy_target, false, false, false, false, false, false);
    assertTrue(true);
    nrs.stop();
            
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_insufficientPrivileges_with_wheel_group() throws Exception {
    this.etc          = this.root.addChildStem("etc", "etc");
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    ApiConfig.testConfig.put("security.stem.groupAllowedToCopyStem", admin.getName());
    ApiConfig.testConfig.put("groups.wheel.use", "true");
    ApiConfig.testConfig.put("groups.wheel.group", wheel.getName());

    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 11);

    stem_copy_setup(r);
    
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    wheel.addMember(b);
    
    nrs = GrouperSession.start(a);
    try {
      stem_copy_source.copy(stem_copy_target, false, false, false, false, false, false);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
         
    nrs = GrouperSession.start(b);
    stem_copy_source.copy(stem_copy_target, false, false, false, false, false, false);
    assertTrue(true);
    nrs.stop();
            
    r.rs.stop();
  }
  
  private void stem_copy_setup(R r) throws Exception {
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    Subject c = r.getSubject("c");
    Subject d = r.getSubject("d");
    Subject e = r.getSubject("e");
    Subject f = r.getSubject("f");
    Subject g = r.getSubject("g");
    Subject h = r.getSubject("h");
    Subject i = r.getSubject("i");
    Subject j = r.getSubject("j");
    Subject k = r.getSubject("k");
    
    type1 = GroupType.createType(s, "type1");
    type1attr1 = type1.addAttribute(s, "type1attr1", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, true);
    stem_copy_source = root.addChildStem("source", "source display name");
    stem_copy_target = root.addChildStem("target", "target display name");
    
    Group level1Group1 = stem_copy_source.addChildGroup("level1Group1", "level1Group1 display name");
    Group level1Group2 = stem_copy_source.addChildGroup("level1Group2", "level1Group2 display name");
    Group level1Group3 = stem_copy_source.addChildGroup("level1Group3", "level1Group3 display name");
    Stem level1Stem1 = stem_copy_source.addChildStem("level1Stem1", "level1Stem1 display name");
    Stem level1Stem2 = stem_copy_source.addChildStem("level1Stem2", "level1Stem2 display name");
    
    Group level2Group1 = level1Stem1.addChildGroup("level2Group1", "level2Group1 display name");
    Group level2Group2 = level1Stem1.addChildGroup("level2Group2", "level2Group2 display name");
    Group level2Group3 = level1Stem1.addChildGroup("level2Group3", "level2Group3 display name");
    Stem level2Stem1 = level1Stem1.addChildStem("level2Stem1", "level2Stem1 display name");
    Stem level2Stem2 = level1Stem1.addChildStem("level2Stem2", "level2Stem2 display name");
    
    Group level3Group1 = level2Stem1.addChildGroup("level3Group1", "level3Group1 display name");
    Group level3Group2 = level2Stem1.addChildGroup("level3Group2", "level3Group2 display name");
    Stem level3Stem1 = level2Stem1.addChildStem("level3Stem1", "level3Stem1 display name");
    Stem level3Stem2 = level2Stem1.addChildStem("level3Stem2", "level3Stem2 display name");
    
    Group level3Group3 = level2Stem2.addChildGroup("level3Group3", "level3Group3 display name");
    
    stem_copy_source.revokePriv(NamingPrivilege.STEM);
    stem_copy_source.revokePriv(NamingPrivilege.CREATE);
    stem_copy_target.revokePriv(NamingPrivilege.STEM);
    stem_copy_target.revokePriv(NamingPrivilege.CREATE);
    level1Stem1.revokePriv(NamingPrivilege.STEM);
    level1Stem1.revokePriv(NamingPrivilege.CREATE);    
    level1Stem2.revokePriv(NamingPrivilege.STEM);
    level1Stem2.revokePriv(NamingPrivilege.CREATE);   
    level2Stem1.revokePriv(NamingPrivilege.STEM);
    level2Stem1.revokePriv(NamingPrivilege.CREATE);    
    level2Stem2.revokePriv(NamingPrivilege.STEM);
    level2Stem2.revokePriv(NamingPrivilege.CREATE);   
    level3Stem1.revokePriv(NamingPrivilege.STEM);
    level3Stem1.revokePriv(NamingPrivilege.CREATE);    
    level3Stem2.revokePriv(NamingPrivilege.STEM);
    level3Stem2.revokePriv(NamingPrivilege.CREATE);   
    level1Group1.revokePriv(AccessPrivilege.ADMIN);
    level1Group2.revokePriv(AccessPrivilege.ADMIN);
    level1Group3.revokePriv(AccessPrivilege.ADMIN);
    level2Group1.revokePriv(AccessPrivilege.ADMIN);
    level2Group2.revokePriv(AccessPrivilege.ADMIN);
    level2Group3.revokePriv(AccessPrivilege.ADMIN);
    level3Group1.revokePriv(AccessPrivilege.ADMIN);
    level3Group2.revokePriv(AccessPrivilege.ADMIN);
    level3Group3.revokePriv(AccessPrivilege.ADMIN);


    stem_copy_source.grantPriv(a, NamingPrivilege.CREATE);
    stem_copy_source.grantPriv(b, NamingPrivilege.STEM);
    stem_copy_target.grantPriv(c, NamingPrivilege.CREATE);
    stem_copy_target.grantPriv(c, NamingPrivilege.STEM);
    
    level3Group3.addMember(d);
    level3Group3.grantPriv(c, AccessPrivilege.READ);
    level3Group3.grantPriv(e, AccessPrivilege.ADMIN);

    top_group.addMember(f);
    top_group.addMember(level3Group3.toSubject());
    top_group.revokePriv(AccessPrivilege.ADMIN);
    top_group.revokePriv(AccessPrivilege.VIEW);
    top_group.revokePriv(AccessPrivilege.READ);
    top_group.grantPriv(g, AccessPrivilege.ADMIN);
    top_group.grantPriv(level3Group3.toSubject(), AccessPrivilege.ADMIN);
    
    top.revokePriv(NamingPrivilege.STEM);
    top.revokePriv(NamingPrivilege.CREATE);
    top.grantPriv(h, NamingPrivilege.STEM);
    top.grantPriv(level3Group3.toSubject(), NamingPrivilege.STEM);
    
    level3Stem1.grantPriv(i, NamingPrivilege.CREATE);
    level3Stem1.grantPriv(j, NamingPrivilege.STEM);
    level3Stem1.grantPriv(k, NamingPrivilege.CREATE);
    
    level3Group3.addType(type1);
    level3Group3.setAttribute("type1attr1", "test");
    level3Group3.store();
    
    level1Group1.addCompositeMember(CompositeType.UNION, level1Group2, level1Group3);
    level2Group1.addCompositeMember(CompositeType.UNION, level2Group2, level2Group3);
    level3Group1.addCompositeMember(CompositeType.UNION, child_group, level3Group2);
    
    ApiConfig.testConfig.put("groups.create.grant.all.read", "true");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "true");
  }
  
  private void verify_copy(R r, Stem newStem, boolean privilegesOfStem,
      boolean privilegesOfGroup, boolean groupAsPrivilege, boolean listMembersOfGroup,
      boolean listGroupAsMember, boolean attributes) throws Exception {

    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    Subject c = r.getSubject("c");
    Subject d = r.getSubject("d");
    Subject e = r.getSubject("e");
    Subject f = r.getSubject("f");
    Subject g = r.getSubject("g");
    Subject i = r.getSubject("i");
    Subject j = r.getSubject("j");
    Subject k = r.getSubject("k");
    
    // verify the new stem
    assertTrue(newStem.getChildGroups(Scope.SUB).size() == stem_copy_source.getChildGroups(Scope.SUB).size());
    assertTrue(newStem.getChildStems(Scope.SUB).size() == stem_copy_source.getChildStems(Scope.SUB).size());
    assertTrue(newStem.getExtension().equals("source"));
    assertTrue(newStem.getDisplayExtension().equals("source display name"));
    assertTrue(newStem.getName().equals("target:source"));
    assertTrue(newStem.getDisplayName().equals("target display name:source display name"));
    
    // verify target stem
    assertTrue(stem_copy_target.getChildGroups().size() == 0);
    assertTrue(stem_copy_target.getChildStems().size() == 1);
    assertTrue(stem_copy_target.getStemmers().size() == 1);
    assertTrue(stem_copy_target.hasStem(c) == true);
    assertTrue(stem_copy_target.getCreators().size() == 1);
    assertTrue(stem_copy_target.hasCreate(c) == true);

    
    // verify other stems
    Stem level1Stem1 = StemFinder.findByName(r.rs, "target:source:level1Stem1");
    Stem level1Stem2 = StemFinder.findByName(r.rs, "target:source:level1Stem2");
    Stem level2Stem1 = StemFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem1");
    Stem level2Stem2 = StemFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem2");
    Stem level3Stem1 = StemFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem1:level3Stem1");
    Stem level3Stem2 = StemFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem1:level3Stem2");
    assertTrue(level1Stem1.getDisplayExtension().equals("level1Stem1 display name"));
    assertTrue(level1Stem1.getStemmers().size() == 0);
    assertTrue(level1Stem1.getCreators().size() == 0);
    assertTrue(level3Stem2.getDisplayExtension().equals("level3Stem2 display name"));
    assertTrue(level3Stem2.getStemmers().size() == 0);
    assertTrue(level3Stem2.getCreators().size() == 0);
    
    // verify other groups
    Group level1Group1 = GroupFinder.findByName(r.rs, "target:source:level1Group1");
    Group level1Group2 = GroupFinder.findByName(r.rs, "target:source:level1Group2");
    Group level1Group3 = GroupFinder.findByName(r.rs, "target:source:level1Group3");
    Group level2Group1 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Group1");
    Group level2Group2 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Group2");
    Group level2Group3 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Group3");
    Group level3Group1 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem1:level3Group1");
    Group level3Group2 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem1:level3Group2");
    Group level3Group3 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem2:level3Group3");
    assertTrue(level1Group1.getDisplayExtension().equals("level1Group1 display name"));
    assertTrue(level1Group1.getAdmins().size() == 0);
    assertTrue(level1Group1.getUpdaters().size() == 0);
    assertTrue(level1Group1.getViewers().size() == 0);
    assertTrue(level1Group1.getReaders().size() == 0);
    assertTrue(level1Group1.getOptins().size() == 0);
    assertTrue(level1Group1.getOptouts().size() == 0);
    assertTrue(level1Group1.getMembers().size() == 0);
    assertTrue(level1Group1.getTypes().size() == 1);
    assertTrue(level3Group2.getDisplayExtension().equals("level3Group2 display name"));
    assertTrue(level3Group2.getAdmins().size() == 0);
    assertTrue(level3Group2.getUpdaters().size() == 0);
    assertTrue(level3Group2.getViewers().size() == 0);
    assertTrue(level3Group2.getReaders().size() == 0);
    assertTrue(level3Group2.getOptins().size() == 0);
    assertTrue(level3Group2.getOptouts().size() == 0);
    assertTrue(level3Group2.getMembers().size() == 0);
    assertTrue(level3Group2.getTypes().size() == 1);

    // composite checks
    assertTrue(level1Group1.hasComposite() == true);
    assertTrue(level1Group2.hasComposite() == false);
    assertTrue(level1Group3.hasComposite() == false);
    assertTrue(level2Group1.hasComposite() == true);
    assertTrue(level2Group2.hasComposite() == false);
    assertTrue(level2Group3.hasComposite() == false);
    assertTrue(level3Group1.hasComposite() == true);
    assertTrue(level3Group2.hasComposite() == false);
    assertTrue(level3Group3.hasComposite() == false);
    assertTrue(level1Group1.getComposite().getLeftGroup().getName().equals("target:source:level1Group2"));
    assertTrue(level1Group1.getComposite().getRightGroup().getName().equals("target:source:level1Group3"));
    assertTrue(level2Group1.getComposite().getLeftGroup().getName().equals("target:source:level1Stem1:level2Group2"));
    assertTrue(level2Group1.getComposite().getRightGroup().getName().equals("target:source:level1Stem1:level2Group3"));
    assertTrue(level3Group1.getComposite().getLeftGroup().getName().equals("top:child:child group"));
    assertTrue(level3Group1.getComposite().getRightGroup().getName().equals("source:level1Stem1:level2Stem1:level3Group2"));
    
    // stem privilege checks
    if (privilegesOfStem) {
      assertTrue(newStem.getStemmers().size() == 1);
      assertTrue(newStem.getCreators().size() == 1);
      assertTrue(newStem.hasCreate(a) == true);
      assertTrue(newStem.hasStem(b) == true);
      assertTrue(level3Stem1.getStemmers().size() == 1);
      assertTrue(level3Stem1.getCreators().size() == 2);
      assertTrue(level3Stem1.hasCreate(i) == true);
      assertTrue(level3Stem1.hasCreate(k) == true);
      assertTrue(level3Stem1.hasStem(j) == true);
    } else {
      assertTrue(newStem.getStemmers().size() == 0);
      assertTrue(newStem.getCreators().size() == 0);
      assertTrue(level3Stem1.getStemmers().size() == 0);
      assertTrue(level3Stem1.getCreators().size() == 0);
    }
    
    // group member checks
    if (listMembersOfGroup) {
      assertTrue(level3Group3.hasMember(d) == true);
      assertTrue(level3Group3.getMembers().size() == 1);
    } else {
      assertTrue(level3Group3.getMembers().size() == 0);
    }
    
    // group privilege checks
    if (privilegesOfGroup) {
      assertTrue(level3Group3.hasAdmin(e) == true);
      assertTrue(level3Group3.getAdmins().size() == 1);
      assertTrue(level3Group3.hasRead(c) == true);
      assertTrue(level3Group3.getReaders().size() == 1);
    } else {
      assertTrue(level3Group3.getAdmins().size() == 0);
      assertTrue(level3Group3.getReaders().size() == 0);
    }
    
    // groups with copied group as a member checks
    if (listGroupAsMember) {
      assertTrue(top_group.hasImmediateMember(level3Group3.toSubject()));
      assertTrue(top_group.getImmediateMembers().size() == 3);
    } else {
      assertTrue(top_group.getImmediateMembers().size() == 2);
    }

    // groups with copied group as a privilege checks
    if (groupAsPrivilege) {
      assertTrue(top_group.hasAdmin(level3Group3.toSubject()) == true);
    } else {
      assertTrue(top_group.hasAdmin(level3Group3.toSubject()) == false);
    }
    
    // stems with copied group as a privilege checks
    if (groupAsPrivilege) {
      assertTrue(top.hasStem(level3Group3.toSubject()) == true);
    } else {
      assertTrue(top.hasStem(level3Group3.toSubject()) == false);
    }
    
    // attribute checks
    if (attributes) {
      assertTrue(level3Group3.getAttribute("type1attr1").equals("test"));
    } else {
      assertTrue(level3Group3.getAttribute("type1attr1").equals(""));
    }
  }
}

