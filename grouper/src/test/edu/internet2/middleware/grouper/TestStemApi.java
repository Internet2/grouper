/**
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

package edu.internet2.middleware.grouper;

import java.sql.Timestamp;
import java.util.Date;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntityFinder;
import edu.internet2.middleware.grouper.entity.EntitySave;
import edu.internet2.middleware.grouper.entity.EntityUtils;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemModifyException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Test {@link Stem}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: TestStemApi.java,v 1.14 2009-12-10 08:54:15 mchyzer Exp $
 * @since   1.2.1
 */
public class TestStemApi extends GrouperTest {


  private Group           child_group, top_group, admin, wheel;
  private GrouperSession  s;
  private Stem            child, root, top, top_new, etc, stem_copy_source, stem_copy_target;
  private GroupType       type1;
  @SuppressWarnings("unused")
  private AttributeDefName type1attr1;

  /**
   * 
   */
  public TestStemApi() {
    super();
  }

  /**
   * @param name
   */
  public TestStemApi(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestStemApi("test_getChildGroups_PrivilegeArrayAndScope_emptyArray"));
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
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
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
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  public void tearDown() {
    super.tearDown();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_no_attr_changes() throws Exception {
    Stem top2 = root.addChildStem("top2", "top2 display name");
    Stem child2 = top2.addChildStem("child2", "child2 display name");
    
    AttributeDef topAttributeDef = top.addChildAttributeDef("top attr def", AttributeDefType.attr);
    AttributeDef childAttributeDef = child.addChildAttributeDef("child attr def", AttributeDefType.attr);
    AttributeDef childAttributeDef2 = child.addChildAttributeDef("child attr def2", AttributeDefType.attr);
    
    top.addChildAttributeDefName(topAttributeDef, "top attr def name", "top attr def name display name");
    child.addChildAttributeDefName(childAttributeDef, "child attr def name", "child attr def name display name");
    child.addChildAttributeDefName(childAttributeDef2, "child attr def name2a", "child attr def name2a display name");
    child.addChildAttributeDefName(childAttributeDef2, "child attr def name2b", "child attr def name2b display name");
    
    child2.setExtension("child3");
    child2.store();
    
    top2.setExtension("top3");
    top2.store();
    
    // verify attr def and attr def
    topAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:top attr def", true);
    childAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child:child attr def", true);
    childAttributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child:child attr def2", true);
    
    AttributeDefName topAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:top attr def name", true);
    AttributeDefName childAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name", true);
    AttributeDefName childAttributeDefName2a = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name2a", true);
    AttributeDefName childAttributeDefName2b = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name2b", true);
    
    assertEquals("top display name:top attr def name display name", topAttributeDefName.getDisplayName());
    assertEquals("top display name:child display name:child attr def name display name", childAttributeDefName.getDisplayName());
    assertEquals("top display name:child display name:child attr def name2a display name", childAttributeDefName2a.getDisplayName());
    assertEquals("top display name:child display name:child attr def name2b display name", childAttributeDefName2b.getDisplayName());
  }
  
  /**
   * @throws Exception
   */
  public void test_move_no_attr_changes() throws Exception {
    Stem top2 = root.addChildStem("top2", "top2 display name");
    Stem child2 = top2.addChildStem("child2", "child2 display name");
    Stem childofChild2 = child2.addChildStem("child of child2", "child of child2 display name");
    
    AttributeDef topAttributeDef = top.addChildAttributeDef("top attr def", AttributeDefType.attr);
    AttributeDef childAttributeDef = child.addChildAttributeDef("child attr def", AttributeDefType.attr);
    AttributeDef childAttributeDef2 = child.addChildAttributeDef("child attr def2", AttributeDefType.attr);
    
    top.addChildAttributeDefName(topAttributeDef, "top attr def name", "top attr def name display name");
    child.addChildAttributeDefName(childAttributeDef, "child attr def name", "child attr def name display name");
    child.addChildAttributeDefName(childAttributeDef2, "child attr def name2a", "child attr def name2a display name");
    child.addChildAttributeDefName(childAttributeDef2, "child attr def name2b", "child attr def name2b display name");
    
    childofChild2.move(root);
    top2.move(top);
    
    // verify attr def and attr def
    topAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:top attr def", true);
    childAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child:child attr def", true);
    childAttributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child:child attr def2", true);
    
    AttributeDefName topAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:top attr def name", true);
    AttributeDefName childAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name", true);
    AttributeDefName childAttributeDefName2a = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name2a", true);
    AttributeDefName childAttributeDefName2b = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name2b", true);
    
    assertEquals("top display name:top attr def name display name", topAttributeDefName.getDisplayName());
    assertEquals("top display name:child display name:child attr def name display name", childAttributeDefName.getDisplayName());
    assertEquals("top display name:child display name:child attr def name2a display name", childAttributeDefName2a.getDisplayName());
    assertEquals("top display name:child display name:child attr def name2b display name", childAttributeDefName2b.getDisplayName());
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_with_attr_changes() throws Exception {
    AttributeDef topAttributeDef = top.addChildAttributeDef("top attr def", AttributeDefType.attr);
    AttributeDef childAttributeDef = child.addChildAttributeDef("child attr def", AttributeDefType.attr);
    AttributeDef childAttributeDef2 = child.addChildAttributeDef("child attr def2", AttributeDefType.attr);
    
    top.addChildAttributeDefName(topAttributeDef, "top attr def name", "top attr def name display name");
    child.addChildAttributeDefName(childAttributeDef, "child attr def name", "child attr def name display name");
    child.addChildAttributeDefName(childAttributeDef2, "child attr def name2a", "child attr def name2a display name");
    child.addChildAttributeDefName(childAttributeDef2, "child attr def name2b", "child attr def name2b display name");
    
    // set both extension and display extension
    child.setExtension("child2");
    child.setDisplayExtension("child2 display name");
    child.store();
    
    // verify attr def and attr def
    topAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:top attr def", true);
    childAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child2:child attr def", true);
    childAttributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child2:child attr def2", true);
    
    AttributeDefName topAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:top attr def name", true);
    AttributeDefName childAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child2:child attr def name", true);
    AttributeDefName childAttributeDefName2a = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child2:child attr def name2a", true);
    AttributeDefName childAttributeDefName2b = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child2:child attr def name2b", true);
    
    assertEquals("top display name:top attr def name display name", topAttributeDefName.getDisplayName());
    assertEquals("top display name:child2 display name:child attr def name display name", childAttributeDefName.getDisplayName());
    assertEquals("top display name:child2 display name:child attr def name2a display name", childAttributeDefName2a.getDisplayName());
    assertEquals("top display name:child2 display name:child attr def name2b display name", childAttributeDefName2b.getDisplayName());
    
    // set just extension
    child.setExtension("child3");
    child.store();
    
    // verify attr def and attr def
    topAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:top attr def", true);
    childAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child3:child attr def", true);
    childAttributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child3:child attr def2", true);
    
    topAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:top attr def name", true);
    childAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child3:child attr def name", true);
    childAttributeDefName2a = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child3:child attr def name2a", true);
    childAttributeDefName2b = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child3:child attr def name2b", true);
    
    assertEquals("top display name:top attr def name display name", topAttributeDefName.getDisplayName());
    assertEquals("top display name:child2 display name:child attr def name display name", childAttributeDefName.getDisplayName());
    assertEquals("top display name:child2 display name:child attr def name2a display name", childAttributeDefName2a.getDisplayName());
    assertEquals("top display name:child2 display name:child attr def name2b display name", childAttributeDefName2b.getDisplayName());
    
    // set just display extension
    child.setDisplayExtension("child3 display name");
    child.store();
    
    // verify attr def and attr def
    topAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:top attr def", true);
    childAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child3:child attr def", true);
    childAttributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child3:child attr def2", true);
    
    topAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:top attr def name", true);
    childAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child3:child attr def name", true);
    childAttributeDefName2a = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child3:child attr def name2a", true);
    childAttributeDefName2b = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child3:child attr def name2b", true);
    
    assertEquals("top display name:top attr def name display name", topAttributeDefName.getDisplayName());
    assertEquals("top display name:child3 display name:child attr def name display name", childAttributeDefName.getDisplayName());
    assertEquals("top display name:child3 display name:child attr def name2a display name", childAttributeDefName2a.getDisplayName());
    assertEquals("top display name:child3 display name:child attr def name2b display name", childAttributeDefName2b.getDisplayName());
    
    // now rename one level higher
    top.setExtension("top3");
    top.setDisplayExtension("top3 display name");
    top.store();
    
    // verify attr def and attr def
    topAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top3:top attr def", true);
    childAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top3:child3:child attr def", true);
    childAttributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top3:child3:child attr def2", true);
    
    topAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top3:top attr def name", true);
    childAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top3:child3:child attr def name", true);
    childAttributeDefName2a = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top3:child3:child attr def name2a", true);
    childAttributeDefName2b = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top3:child3:child attr def name2b", true);
    
    assertEquals("top3 display name:top attr def name display name", topAttributeDefName.getDisplayName());
    assertEquals("top3 display name:child3 display name:child attr def name display name", childAttributeDefName.getDisplayName());
    assertEquals("top3 display name:child3 display name:child attr def name2a display name", childAttributeDefName2a.getDisplayName());
    assertEquals("top3 display name:child3 display name:child attr def name2b display name", childAttributeDefName2b.getDisplayName());
  }
  

  /**
   * @throws Exception
   */
  public void test_move_with_attr_changes() throws Exception {
    Stem newLocation = root.addChildStem("new", "new display name");
    
    AttributeDef topAttributeDef = top.addChildAttributeDef("top attr def", AttributeDefType.attr);
    AttributeDef childAttributeDef = child.addChildAttributeDef("child attr def", AttributeDefType.attr);
    AttributeDef childAttributeDef2 = child.addChildAttributeDef("child attr def2", AttributeDefType.attr);
    
    top.addChildAttributeDefName(topAttributeDef, "top attr def name", "top attr def name display name");
    child.addChildAttributeDefName(childAttributeDef, "child attr def name", "child attr def name display name");
    child.addChildAttributeDefName(childAttributeDef2, "child attr def name2a", "child attr def name2a display name");
    child.addChildAttributeDefName(childAttributeDef2, "child attr def name2b", "child attr def name2b display name");
    
    // move child to different stem
    child.move(newLocation);
    
    // verify attr def and attr def
    topAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:top attr def", true);
    childAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("new:child:child attr def", true);
    childAttributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("new:child:child attr def2", true);
    
    AttributeDefName topAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:top attr def name", true);
    AttributeDefName childAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("new:child:child attr def name", true);
    AttributeDefName childAttributeDefName2a = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("new:child:child attr def name2a", true);
    AttributeDefName childAttributeDefName2b = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("new:child:child attr def name2b", true);
    
    assertEquals("top display name:top attr def name display name", topAttributeDefName.getDisplayName());
    assertEquals("new display name:child display name:child attr def name display name", childAttributeDefName.getDisplayName());
    assertEquals("new display name:child display name:child attr def name2a display name", childAttributeDefName2a.getDisplayName());
    assertEquals("new display name:child display name:child attr def name2b display name", childAttributeDefName2b.getDisplayName());
    
    // move child back
    child.move(top);
    
    // verify attr def and attr def
    topAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:top attr def", true);
    childAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child:child attr def", true);
    childAttributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child:child attr def2", true);
    
    topAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:top attr def name", true);
    childAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name", true);
    childAttributeDefName2a = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name2a", true);
    childAttributeDefName2b = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name2b", true);
    
    assertEquals("top display name:top attr def name display name", topAttributeDefName.getDisplayName());
    assertEquals("top display name:child display name:child attr def name display name", childAttributeDefName.getDisplayName());
    assertEquals("top display name:child display name:child attr def name2a display name", childAttributeDefName2a.getDisplayName());
    assertEquals("top display name:child display name:child attr def name2b display name", childAttributeDefName2b.getDisplayName());
    
    // move top to different stem
    top.move(newLocation);
    
    // verify attr def and attr def
    topAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("new:top:top attr def", true);
    childAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("new:top:child:child attr def", true);
    childAttributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("new:top:child:child attr def2", true);
    
    topAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("new:top:top attr def name", true);
    childAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("new:top:child:child attr def name", true);
    childAttributeDefName2a = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("new:top:child:child attr def name2a", true);
    childAttributeDefName2b = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("new:top:child:child attr def name2b", true);
    
    assertEquals("new display name:top display name:top attr def name display name", topAttributeDefName.getDisplayName());
    assertEquals("new display name:top display name:child display name:child attr def name display name", childAttributeDefName.getDisplayName());
    assertEquals("new display name:top display name:child display name:child attr def name2a display name", childAttributeDefName2a.getDisplayName());
    assertEquals("new display name:top display name:child display name:child attr def name2b display name", childAttributeDefName2b.getDisplayName());
    
    // move top back
    top.move(root);
    
    // verify attr def and attr def
    topAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:top attr def", true);
    childAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child:child attr def", true);
    childAttributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:child:child attr def2", true);
    
    topAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:top attr def name", true);
    childAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name", true);
    childAttributeDefName2a = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name2a", true);
    childAttributeDefName2b = GrouperDAOFactory.getFactory().getAttributeDefName()
      .findByNameSecure("top:child:child attr def name2b", true);
    
    assertEquals("top display name:top attr def name display name", topAttributeDefName.getDisplayName());
    assertEquals("top display name:child display name:child attr def name display name", childAttributeDefName.getDisplayName());
    assertEquals("top display name:child display name:child attr def name2a display name", childAttributeDefName2a.getDisplayName());
    assertEquals("top display name:child display name:child attr def name2b display name", childAttributeDefName2b.getDisplayName());
  }

  /**
   * @throws Exception
   */
  public void test_copy_with_disabled_memberships() throws Exception {
    R r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    Subject c = r.getSubject("c");
    
    Stem source = root.addChildStem("source", "source");
    Stem target = root.addChildStem("target", "target");
    Group group1 = source.addChildGroup("group1", "group1");
    Group group2 = top.addChildGroup("group2", "group2");
    Group otherGroup = top.addChildGroup("otherGroup", "otherGroup");
    Stem otherStem = top.addChildStem("otherStem", "otherStem");
    
    source.grantPriv(group2.toSubject(), NamingPrivilege.CREATE);
    group1.addMember(a);
    group1.addMember(group2.toSubject());
    group1.grantPriv(b, AccessPrivilege.UPDATE);
    group2.addMember(c);
    otherGroup.addMember(group1.toSubject());
    otherGroup.grantPriv(group1.toSubject(), AccessPrivilege.UPDATE);
    otherStem.grantPriv(group1.toSubject(), NamingPrivilege.CREATE);
    
    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group1.getUuid(), group2.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group1.getUuid(), MemberFinder.findBySubject(r.rs, b, true).getUuid(), FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), MembershipType.IMMEDIATE.getTypeString(), true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        otherGroup.getUuid(), group1.toMember().getUuid(), FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), MembershipType.IMMEDIATE.getTypeString(), true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        otherGroup.getUuid(), group1.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        otherStem.getUuid(), group1.toMember().getUuid(), FieldFinder.find(Field.FIELD_NAME_CREATORS, true), MembershipType.IMMEDIATE.getTypeString(), true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        source.getUuid(), group2.toMember().getUuid(), FieldFinder.find(Field.FIELD_NAME_CREATORS, true), MembershipType.IMMEDIATE.getTypeString(), true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperUtil.sleep(100);
    Date pre = new Date();
    GrouperUtil.sleep(100);
    
    Stem newStem = source.copy(target);
    Group newGroup = (Group) newStem.getChildGroups().iterator().next();
    
    assertEquals(1, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, Group.getDefaultList(), true).size());
    assertEquals(0, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), true).size());
    assertEquals(0, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, FieldFinder.find(Field.FIELD_NAME_CREATORS, true), true).size());
    
    assertEquals(3, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, Group.getDefaultList(), false).size());
    assertEquals(2, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), false).size());
    assertEquals(2, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, FieldFinder.find(Field.FIELD_NAME_CREATORS, true), false).size());

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
      this.root.getChildGroups(null, (Scope)null);
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
      this.root.getChildStems(null, (Scope)null);
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
   * 
   */
  public void test_copy_group_name_exists() {
    
    Stem one = this.root.addChildStem("one", "one");
    Stem two = this.root.addChildStem("two", "two");
    one.addChildGroup("group", "group");
    Group twoGroup = two.addChildGroup("group", "group");
    
    twoGroup.addAlternateName("two:one:group");
    twoGroup.store();

    one.copy(two);

    // these should not throw exceptions
    GroupFinder.findByAlternateName(s, "two:one:group", true);
    GroupFinder.findByCurrentName(s, "two:one:group.2", true);
  }
  
  /**
   * 
   */
  public void test_copy_stem_name_exists() {
    
    Stem one = this.root.addChildStem("one", "one");
    Stem two = this.root.addChildStem("two", "two");
    Stem three = this.root.addChildStem("three", "three");
    
    three.addAlternateName("two:one");
    three.store();

    try {
      one.copy(two);
      fail("failed to throw RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
  }
  
  /**
   * 
   */
  public void test_move_group_name_exists() {
    
    Stem one = this.root.addChildStem("one", "one");
    Stem two = this.root.addChildStem("two", "two");
    one.addChildGroup("group", "group");
    Group twoGroup = two.addChildGroup("group", "group");
    
    twoGroup.addAlternateName("two:one:group");
    twoGroup.store();

    try {
      one.move(two);
      fail("failed to throw RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(true);
    }
  } 
  
  /**
   * 
   */
  public void test_move_stem_name_exists() {
    
    Stem one = this.root.addChildStem("one", "one");
    Stem two = this.root.addChildStem("two", "two");
    Stem three = this.root.addChildStem("three", "three");
    
    three.addAlternateName("two:one");
    three.store();

    try {
      one.move(two);
      fail("failed to throw RuntimeException");
    } catch (RuntimeException e) {
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
    this.etc          = new StemSave(this.s).assignStemNameToEdit("etc").assignName("etc").save();
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToRenameStem", admin.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

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
    this.etc          = new StemSave(this.s).assignStemNameToEdit("etc").assignName("etc").save();
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToRenameStem", admin.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

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
    this.etc          = new StemSave(this.s).assignStemNameToEdit("etc").assignName("etc").save();
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToMoveStem", admin.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

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
    this.etc          = new StemSave(this.s).assignStemNameToEdit("etc").assignName("etc").save();
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToMoveStem", admin.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

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
  public void test_move_insufficientPrivileges_with_wheel_group_in_self_mode() throws Exception {
    this.etc          = new StemSave(this.s).assignStemNameToEdit("etc").assignName("etc").save();
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToMoveStem", admin.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    
    this.top_new = this.root.addChildStem("top new", "top new display name");
    
    wheel.addMember(a);
    
    nrs = GrouperSession.start(a);
    nrs.setConsiderIfWheelMember(false);
    try {
      top.move(top_new);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.setConsiderIfWheelMember(true);
    top.move(top_new);
    assertTrue(true);
    nrs.stop();
            
    r.rs.stop();
  }

  
  /**
   * @throws Exception
   */
  public void testStemMoveAudit() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");

    this.top_new = this.root.addChildStem("top new", "top new display name");

    child_group.addMember(a);

    child_group.grantPriv(a, AccessPrivilege.UPDATE);
    child.grantPriv(b, NamingPrivilege.CREATE);
    top.grantPriv(b, NamingPrivilege.CREATE);


    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");

    assertEquals(0, auditCount);
    
    // first move to a non-root stem
    top.move(top_new);

    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    assertTrue("durationMicros should exist", auditEntry.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 2: " + auditEntry.getQueryCount(), 2 <= auditEntry.getQueryCount());
  
    assertEquals("Context id's should match", auditEntry.getContextId(), top.getContextId());
  
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

    top = StemFinder.findByName(s, "top new:top", true);
    child = StemFinder.findByName(s, "top new:top:child", true);
    child_group = GroupFinder.findByName(s, "top new:top:child:child group", true);
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

    top = StemFinder.findByName(s, "top", true);
    child = StemFinder.findByName(s, "top:child", true);
    child_group = GroupFinder.findByName(s, "top:child:child group", true);
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
  public void test_move2() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");

    this.top_new = this.root.addChildStem("top new", "top new display name");

    child_group.addMember(a);
    child_group.grantPriv(a, AccessPrivilege.UPDATE);
    child.grantPriv(b, NamingPrivilege.CREATE);
    top.grantPriv(b, NamingPrivilege.CREATE);

    // first move to a non-root stem
    new StemMove(top, top_new).save();

    top = StemFinder.findByName(s, "top new:top", true);
    child = StemFinder.findByName(s, "top new:top:child", true);
    child_group = GroupFinder.findByName(s, "top new:top:child:child group", true);
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
    new StemMove(top, root).save();

    top = StemFinder.findByName(s, "top", true);
    child = StemFinder.findByName(s, "top:child", true);
    child_group = GroupFinder.findByName(s, "top:child:child group", true);
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
  public void test_move_alternate_name() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");
    Stem top2 = root.addChildStem("top2", "top2");

    // verify alternate name gets added
    top.grantPriv(a, NamingPrivilege.STEM);
    top2.grantPriv(a, NamingPrivilege.STEM);
    nrs = GrouperSession.start(a);
    top.move(top2);
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top2:top", true);
    child = StemFinder.findByName(s, "top2:top:child", true);
    child_group = GroupFinder.findByName(s, "top2:top:child:child group", true);
    top_group = GroupFinder.findByName(s, "top2:top:top group", true);
    assertTrue(top.getAlternateNameDb().equals("top"));
    assertTrue(child.getAlternateNameDb().equals("top:child"));
    assertTrue(top_group.getAlternateNameDb().equals("top:top group"));
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_move_no_alternate_name() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");
    Stem top2 = root.addChildStem("top2", "top2");

    // verify alternate name does not get added
    top.grantPriv(a, NamingPrivilege.STEM);
    top2.grantPriv(a, NamingPrivilege.STEM);
    nrs = GrouperSession.start(a);
    new StemMove(top, top2).assignAlternateName(false).save();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top2:top", true);
    child = StemFinder.findByName(s, "top2:top:child", true);
    child_group = GroupFinder.findByName(s, "top2:top:child:child group", true);
    top_group = GroupFinder.findByName(s, "top2:top:top group", true);
    assertNull(top.getAlternateNameDb());
    assertNull(child.getAlternateNameDb());
    assertNull(top_group.getAlternateNameDb());
    assertNull(child_group.getAlternateNameDb());
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_move_alternate_name_when_alternate_name_already_exists() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");
    Stem top2 = root.addChildStem("top2", "top2");
    top.addAlternateName("stem1");
    top.store();
    child.addAlternateName("stem1:stem2");
    child.store();
    child_group.addAlternateName("test1:test2");
    child_group.store();
    top_group.addAlternateName("test1a:test2a");
    top_group.store();

    // verify alternate name gets replaced
    top.grantPriv(a, NamingPrivilege.STEM);
    top2.grantPriv(a, NamingPrivilege.STEM);
    nrs = GrouperSession.start(a);
    new StemMove(top, top2).assignAlternateName(true).save();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top2:top", true);
    child = StemFinder.findByName(s, "top2:top:child", true);
    child_group = GroupFinder.findByName(s, "top2:top:child:child group", true);
    top_group = GroupFinder.findByName(s, "top2:top:top group", true);
    assertTrue(top.getAlternateNameDb().equals("top"));
    assertTrue(child.getAlternateNameDb().equals("top:child"));
    assertTrue(top_group.getAlternateNameDb().equals("top:top group"));
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_move_no_alternate_name_when_alternate_name_already_exists() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");
    Stem top2 = root.addChildStem("top2", "top2");
    top.addAlternateName("stem1");
    top.store();
    child.addAlternateName("stem1:stem2");
    child.store();
    child_group.addAlternateName("test1:test2");
    child_group.store();
    top_group.addAlternateName("test1a:test2a");
    top_group.store();

    // verify alternate name doesn't get replaced
    top.grantPriv(a, NamingPrivilege.STEM);
    top2.grantPriv(a, NamingPrivilege.STEM);
    nrs = GrouperSession.start(a);
    new StemMove(top, top2).assignAlternateName(false).save();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top2:top", true);
    child = StemFinder.findByName(s, "top2:top:child", true);
    child_group = GroupFinder.findByName(s, "top2:top:child:child group", true);
    top_group = GroupFinder.findByName(s, "top2:top:top group", true);
    assertTrue(top.getAlternateNameDb().equals("stem1"));
    assertTrue(child.getAlternateNameDb().equals("stem1:stem2"));
    assertTrue(top_group.getAlternateNameDb().equals("test1a:test2a"));
    assertTrue(child_group.getAlternateNameDb().equals("test1:test2"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_alternate_name() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");

    // verify alternate name gets added
    top.grantPriv(a, NamingPrivilege.STEM);
    nrs = GrouperSession.start(a);
    top.setExtension("top2");
    top.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top2", true);
    child = StemFinder.findByName(s, "top2:child", true);
    child_group = GroupFinder.findByName(s, "top2:child:child group", true);
    top_group = GroupFinder.findByName(s, "top2:top group", true);
    assertTrue(top.getAlternateNameDb().equals("top"));
    assertTrue(child.getAlternateNameDb().equals("top:child"));
    assertTrue(top_group.getAlternateNameDb().equals("top:top group"));
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_alternate_name_same_name() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");

    // verify alternate name does not get added
    top.grantPriv(a, NamingPrivilege.STEM);
    nrs = GrouperSession.start(a);
    top.setExtension("top");
    top.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top", true);
    child = StemFinder.findByName(s, "top:child", true);
    child_group = GroupFinder.findByName(s, "top:child:child group", true);
    top_group = GroupFinder.findByName(s, "top:top group", true);
    assertNull(top.getAlternateNameDb());
    assertNull(child.getAlternateNameDb());
    assertNull(top_group.getAlternateNameDb());
    assertNull(child_group.getAlternateNameDb());
    nrs.stop();
    
    nrs = GrouperSession.startRootSession();
    top.addAlternateName("stem1");
    top.store();
    child.addAlternateName("stem1:stem2");
    child.store();
    
    // verify again
    nrs = GrouperSession.start(a);
    top.setExtension("top");
    top.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top", true);
    child = StemFinder.findByName(s, "top:child", true);
    child_group = GroupFinder.findByName(s, "top:child:child group", true);
    top_group = GroupFinder.findByName(s, "top:top group", true);
    assertTrue(top.getAlternateNameDb().equals("stem1"));
    assertTrue(child.getAlternateNameDb().equals("stem1:stem2"));
    assertNull(top_group.getAlternateNameDb());
    assertNull(child_group.getAlternateNameDb());
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_and_displayname_alternate_name() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");

    // verify alternate name gets added
    top.grantPriv(a, NamingPrivilege.STEM);
    nrs = GrouperSession.start(a);
    top.setExtension("top2");
    top.setDisplayExtension("top2 display name");
    top.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top2", true);
    child = StemFinder.findByName(s, "top2:child", true);
    child_group = GroupFinder.findByCurrentName(s, "top2:child:child group", true);
    top_group = GroupFinder.findByCurrentName(s, "top2:top group", true);
    assertTrue(top.getAlternateNameDb().equals("top"));
    assertTrue(child.getAlternateNameDb().equals("top:child"));
    assertTrue(top_group.getAlternateNameDb().equals("top:top group"));
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    assertTrue(top.getDisplayName().equals("top2 display name"));
    assertTrue(child.getDisplayName().equals("top2 display name:child display name"));
    assertTrue(top_group.getDisplayName().equals("top2 display name:top group display name"));
    assertTrue(child_group.getDisplayName().equals("top2 display name:child display name:child group display name"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_and_displayname_no_alternate_name() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");

    // verify alternate name gets added
    top.grantPriv(a, NamingPrivilege.STEM);
    nrs = GrouperSession.start(a);
    top.setExtension("top2", false);
    top.setDisplayExtension("top2 display name");
    top.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top2", true);
    child = StemFinder.findByName(s, "top2:child", true);
    child_group = GroupFinder.findByCurrentName(s, "top2:child:child group", true);
    top_group = GroupFinder.findByCurrentName(s, "top2:top group", true);
    assertNull(top.getAlternateNameDb());
    assertNull(child.getAlternateNameDb());
    assertNull(top_group.getAlternateNameDb());
    assertNull(child_group.getAlternateNameDb());
    assertTrue(top.getDisplayName().equals("top2 display name"));
    assertTrue(child.getDisplayName().equals("top2 display name:child display name"));
    assertTrue(top_group.getDisplayName().equals("top2 display name:top group display name"));
    assertTrue(child_group.getDisplayName().equals("top2 display name:child display name:child group display name"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_no_alternate_name() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");

    // verify alternate name doesn't get added
    top.grantPriv(a, NamingPrivilege.STEM);
    nrs = GrouperSession.start(a);
    top.setExtension("top2", false);
    top.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top2", true);
    child = StemFinder.findByName(s, "top2:child", true);
    child_group = GroupFinder.findByName(s, "top2:child:child group", true);
    top_group = GroupFinder.findByName(s, "top2:top group", true);
    assertNull(top.getAlternateNameDb());
    assertNull(child.getAlternateNameDb());
    assertNull(top_group.getAlternateNameDb());
    assertNull(child_group.getAlternateNameDb());
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_alternate_name_when_alternate_name_already_exists() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");
    top.addAlternateName("stem1");
    top.store();
    child.addAlternateName("stem1:stem2");
    child.store();
    child_group.addAlternateName("test1:test2");
    child_group.store();
    top_group.addAlternateName("test1a:test2a");
    top_group.store();

    // verify alternate name gets replaced
    top.grantPriv(a, NamingPrivilege.STEM);
    nrs = GrouperSession.start(a);
    top.setExtension("top2", true);
    top.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top2", true);
    child = StemFinder.findByName(s, "top2:child", true);
    child_group = GroupFinder.findByName(s, "top2:child:child group", true);
    top_group = GroupFinder.findByName(s, "top2:top group", true);
    assertTrue(top.getAlternateNameDb().equals("top"));
    assertTrue(child.getAlternateNameDb().equals("top:child"));
    assertTrue(top_group.getAlternateNameDb().equals("top:top group"));
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_no_alternate_name_when_alternate_name_already_exists() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");
    top.addAlternateName("stem1");
    top.store();
    child.addAlternateName("stem1:stem2");
    child.store();
    child_group.addAlternateName("test1:test2");
    child_group.store();
    top_group.addAlternateName("test1a:test2a");
    top_group.store();

    // verify alternate name doesn't get replaced
    top.grantPriv(a, NamingPrivilege.STEM);
    nrs = GrouperSession.start(a);
    top.setExtension("top2", false);
    top.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    top = StemFinder.findByName(s, "top2", true);
    child = StemFinder.findByName(s, "top2:child", true);
    child_group = GroupFinder.findByName(s, "top2:child:child group", true);
    top_group = GroupFinder.findByName(s, "top2:top group", true);
    assertTrue(top.getAlternateNameDb().equals("stem1"));
    assertTrue(child.getAlternateNameDb().equals("stem1:stem2"));
    assertTrue(top_group.getAlternateNameDb().equals("test1a:test2a"));
    assertTrue(child_group.getAlternateNameDb().equals("test1:test2"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * 
   */
  public void test_rename_with_duplicate_name() {

    Stem test1 = top.addChildStem("test1", "test1");
    Stem test2 = top.addChildStem("test2", "test2");
    
    test1.addAlternateName("top:altname");
    test1.store();
    test2.addAlternateName("top:conflict");
    test2.store();
    
    try {
      // this should fail because "test2" is in use.
      test1.setExtension("test2");
      test1.store();
      fail("failed to throw StemModifyException");
    } catch (StemModifyException e) {
      assertTrue(true);
    }
    
    try {
      // this should fail because "conflict" is in use.
      test1.setExtension("conflict");
      test1.store();
      fail("failed to throw StemModifyException");
    } catch (StemModifyException e) {
      assertTrue(true);
    }
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_all() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    stem_copy_setup(r);
    Stem newStem = stem_copy_source.copy(stem_copy_target);
    verify_copy(r, newStem, true, true, true, true, true, true);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_should_not_copy_alternate_name() throws Exception {
    child.addAlternateName("test1");
    child.store();
    child_group.addAlternateName("test1:test2");
    child_group.store();
    
    child.copy(root);
    
    Group existingGroup = GroupFinder.findByName(s, "top:child:child group", true);
    assertTrue(existingGroup.getAlternateNameDb().equals("test1:test2"));
    
    Group newGroup = GroupFinder.findByName(s, "child:child group", true);
    assertNull(newGroup.getAlternateNameDb());
    
    Stem existingStem = StemFinder.findByName(s, "top:child", true);
    assertTrue(existingStem.getAlternateNameDb().equals("test1"));
    
    Stem newStem = StemFinder.findByName(s, "child", true);
    assertNull(newStem.getAlternateNameDb());
  }
  
  /**
   * @throws Exception
   */
  public void testStemCopyAudit() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    stem_copy_setup(r);
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");

    assertEquals(0, auditCount);
    
    Stem newStem = stem_copy_source.copy(stem_copy_target);
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    assertTrue("durationMicros should exist", auditEntry.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 2: " + auditEntry.getQueryCount(), 2 <= auditEntry.getQueryCount());
  
    assertEquals("Context id's should match", auditEntry.getContextId(), newStem.getContextId());
    
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_all2() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    stem_copy_setup(r);
    StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
    Stem newStem = stemCopy.copyPrivilegesOfStem(true).copyPrivilegesOfGroup(true)
        .copyGroupAsPrivilege(true).copyListMembersOfGroup(true)
        .copyListGroupAsMember(true).copyAttributes(true).save();  
    verify_copy(r, newStem, true, true, true, true, true, true);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_stem_privs_only() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    stem_copy_setup(r);
    StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
    Stem newStem = stemCopy.copyPrivilegesOfStem(true).copyPrivilegesOfGroup(false)
        .copyGroupAsPrivilege(false).copyListMembersOfGroup(false)
        .copyListGroupAsMember(false).copyAttributes(false).save();  
    verify_copy(r, newStem, true, false, false, false, false, false);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_group_privs_only() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    stem_copy_setup(r);
    StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
    Stem newStem = stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(true)
        .copyGroupAsPrivilege(true).copyListMembersOfGroup(false)
        .copyListGroupAsMember(false).copyAttributes(false).save();  
    verify_copy(r, newStem, false, true, true, false, false, false);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_members_only() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    stem_copy_setup(r);
    StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
    Stem newStem = stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(false)
        .copyGroupAsPrivilege(false).copyListMembersOfGroup(true)
        .copyListGroupAsMember(true).copyAttributes(false).save();  
    verify_copy(r, newStem, false, false, false, true, true, false);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_attrs_only() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    stem_copy_setup(r);
    StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
    Stem newStem = stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(false)
        .copyGroupAsPrivilege(false).copyListMembersOfGroup(false)
        .copyListGroupAsMember(false).copyAttributes(true).save();  
    verify_copy(r, newStem, false, false, false, false, false, true);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_minimum_nonadmin() throws Exception {
    R r = R.populateRegistry(0, 0, 13);
    Subject c = r.getSubject("c");
    GrouperSession nrs;

    stem_copy_setup(r);
    stem_copy_source.grantPriv(c, NamingPrivilege.STEM);

    nrs = GrouperSession.start(c);

    StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
    stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(false)
        .copyGroupAsPrivilege(false).copyListMembersOfGroup(false)
        .copyListGroupAsMember(false).copyAttributes(false).save();  
    assertTrue(true);
    
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_with_roles() throws Exception {
    Stem stem1 = root.addChildStem("stem1", "stem1");
    Stem stem2 = root.addChildStem("stem2", "stem2");
    Role role = stem1.addChildRole("role", "role");
    role.addMember(SubjectTestHelper.SUBJ0, true);    

    stem1.copy(stem2);
    
    assertEquals(TypeOfGroup.role, GroupFinder.findByName(s, "stem2:stem1:role", true).getTypeOfGroup());
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_with_entities() throws Exception {
    Stem stem1 = root.addChildStem("stem1", "stem1");
    Stem stem2 = root.addChildStem("stem2", "stem2");
    Entity entity = new EntitySave(this.s).assignName(stem1.getName() + ":entity").save();
    entity.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), "stem1:x:y:z");

    stem1.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
    stem2.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);

    GrouperSession session = null;
    try {
      session = GrouperSession.start(SubjectTestHelper.SUBJ1);
      stem1.copy(stem2);
    } finally {
      GrouperSession.stopQuietly(session);
    }
    
    GrouperSession.startRootSession();
    Entity entityCopy = new EntityFinder().addName("stem2:stem1:entity").findEntity(true);
    
    assertEquals(TypeOfGroup.entity, ((Group)entityCopy).getTypeOfGroup());
    assertEquals("stem2:stem1:x:y:z", entityCopy.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName()));
  }
  
  /**
   * 
   */
  public void test_move_with_entities() {
    Stem stem1 = root.addChildStem("stem1", "stem1");
    Stem stem2 = root.addChildStem("stem2", "stem2");
    Stem stem3 = root.addChildStem("stem3", "stem3");
    Entity entity = new EntitySave(this.s).assignName(stem1.getName() + ":entity").save();
    
    entity.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, true);
    stem1.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
    stem2.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
    stem3.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    stem1.move(stem2);
    assertNull(entity.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName()));
    
    entity.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), "stem2:stem1:x:y:z");
    stem2.move(stem3);
    assertEquals("stem3:stem2:stem1:x:y:z", entity.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName()));
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_insufficient_privilege_listGroupAsMember() throws Exception {
    R r = R.populateRegistry(0, 0, 13);
    Subject c = r.getSubject("c");
    GrouperSession nrs;

    stem_copy_setup(r);
    stem_copy_source.grantPriv(c, NamingPrivilege.STEM);

    nrs = GrouperSession.start(c);
    try {

      StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
      stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(true)
          .copyGroupAsPrivilege(false).copyListMembersOfGroup(false)
          .copyListGroupAsMember(true).copyAttributes(false).save();  
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException eExpected) {
      assertTrue(true);
    }

    nrs.stop();
    
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    top_group.grantPriv(c, AccessPrivilege.ADMIN);
    nrs.stop();
        
    nrs = GrouperSession.start(c);
    StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
    stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(true)
        .copyGroupAsPrivilege(false).copyListMembersOfGroup(false)
        .copyListGroupAsMember(true).copyAttributes(false).save();  
    assertTrue(true);
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_insufficient_privilege_groupAsPrivilege_naming() throws Exception {
    R r = R.populateRegistry(0, 0, 13);
    Subject c = r.getSubject("c");
    GrouperSession nrs;

    stem_copy_setup(r);
    top_group.grantPriv(c, AccessPrivilege.ADMIN);
    stem_copy_source.grantPriv(c, NamingPrivilege.STEM);

    nrs = GrouperSession.start(c);
    try {
      StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
      stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(true)
          .copyGroupAsPrivilege(true).copyListMembersOfGroup(false)
          .copyListGroupAsMember(false).copyAttributes(false).save();  
      fail("failed to throw exception");
    } catch (Exception eExpected) {
      if (eExpected.getCause() instanceof InsufficientPrivilegeException) {
        assertTrue(true);
      } else {
        fail("failed to throw exception whose cause is InsufficientPrivilegeException");
      }
    }
        
    nrs.stop();
    
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    top.grantPriv(c, NamingPrivilege.STEM);
    nrs.stop();
        
    nrs = GrouperSession.start(c);
    StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
    stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(true)
        .copyGroupAsPrivilege(true).copyListMembersOfGroup(false)
        .copyListGroupAsMember(false).copyAttributes(false).save();  
    assertTrue(true);
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_insufficient_privilege_groupAsPrivilege_access() throws Exception {
    R r = R.populateRegistry(0, 0, 13);
    Subject c = r.getSubject("c");
    GrouperSession nrs;

    stem_copy_setup(r);
    top.grantPriv(c, NamingPrivilege.STEM);
    stem_copy_source.grantPriv(c, NamingPrivilege.STEM);

    nrs = GrouperSession.start(c);
    try {
      StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
      stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(true)
          .copyGroupAsPrivilege(true).copyListMembersOfGroup(false)
          .copyListGroupAsMember(false).copyAttributes(false).save();  
      fail("failed to throw exception");
    } catch (Exception eExpected) {
      if (eExpected.getCause() instanceof InsufficientPrivilegeException) {
        assertTrue(true);
      } else {
        fail("failed to throw exception whose cause is InsufficientPrivilegeException");
      }
    }
        
    nrs.stop();
    
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    top_group.grantPriv(c, AccessPrivilege.ADMIN);
    nrs.stop();
        
    nrs = GrouperSession.start(c);
    StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
    stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(true)
        .copyGroupAsPrivilege(true).copyListMembersOfGroup(false)
        .copyListGroupAsMember(false).copyAttributes(false).save();  
    assertTrue(true);
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_no_create_priv() throws Exception {
    R r = R.populateRegistry(0, 0, 13);
    Subject e = r.getSubject("e");
    GrouperSession nrs;

    stem_copy_setup(r);

    nrs = GrouperSession.start(e);

    try {
      StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
      stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(false)
          .copyGroupAsPrivilege(false).copyListMembersOfGroup(false)
          .copyListGroupAsMember(false).copyAttributes(false).save();  
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
    this.etc          = new StemSave(this.s).assignStemNameToEdit("etc").assignName("etc").save();
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToCopyStem", admin.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 13);

    stem_copy_setup(r);
    
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    stem_copy_target.grantPriv(a, NamingPrivilege.STEM, false);
    stem_copy_target.grantPriv(b, NamingPrivilege.STEM, false);
    
    admin.addMember(b);
    
    nrs = GrouperSession.start(a);
    try {
      StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
      stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(false)
          .copyGroupAsPrivilege(false).copyListMembersOfGroup(false)
          .copyListGroupAsMember(false).copyAttributes(false).save();  
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
         
    nrs = GrouperSession.start(b);
    StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
    stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(false)
        .copyGroupAsPrivilege(false).copyListMembersOfGroup(false)
        .copyListGroupAsMember(false).copyAttributes(false).save();  
    assertTrue(true);
    nrs.stop();
            
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_insufficientPrivileges_with_wheel_group() throws Exception {
    this.etc          = new StemSave(this.s).assignStemNameToEdit("etc").assignName("etc").save();
    this.admin        = this.etc.addChildGroup("admin", "admin");
    this.wheel        = this.etc.addChildGroup("wheel","wheel");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToCopyStem", admin.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 13);

    stem_copy_setup(r);
    
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    wheel.addMember(b);
    
    nrs = GrouperSession.start(a);
    try {
      StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
      stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(false)
          .copyGroupAsPrivilege(false).copyListMembersOfGroup(false)
          .copyListGroupAsMember(false).copyAttributes(false).save();  
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
         
    nrs = GrouperSession.start(b);
    StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target);
    stemCopy.copyPrivilegesOfStem(false).copyPrivilegesOfGroup(false)
        .copyGroupAsPrivilege(false).copyListMembersOfGroup(false)
        .copyListGroupAsMember(false).copyAttributes(false).save();  
    assertTrue(true);
    nrs.stop();
            
    r.rs.stop();
  }
  
  private void stem_copy_setup(R r) throws Exception {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    
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
    Subject l = r.getSubject("l");
    Subject m = r.getSubject("m");
    
    type1 = GroupType.createType(s, "type1");
    type1attr1 = type1.addAttribute(s, "type1attr1", true);
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
    level3Stem1.grantPriv(l, NamingPrivilege.STEM_ATTR_READ);
    level3Stem1.grantPriv(m, NamingPrivilege.STEM_ATTR_UPDATE);
    
    level3Group3.addType(type1);
    level3Group3.setAttribute("type1attr1", "test");
    
    level1Group1.addCompositeMember(CompositeType.UNION, level1Group2, level1Group3);
    level2Group1.addCompositeMember(CompositeType.UNION, level2Group2, level2Group3);
    level3Group1.addCompositeMember(CompositeType.UNION, child_group, level3Group2);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "true");
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
    Subject l = r.getSubject("l");
    Subject m = r.getSubject("m");
    
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
    Stem level1Stem1 = StemFinder.findByName(r.rs, "target:source:level1Stem1", true);
    Stem level1Stem2 = StemFinder.findByName(r.rs, "target:source:level1Stem2", true);
    Stem level2Stem1 = StemFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem1", true);
    Stem level2Stem2 = StemFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem2", true);
    Stem level3Stem1 = StemFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem1:level3Stem1", true);
    Stem level3Stem2 = StemFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem1:level3Stem2", true);
    assertTrue(level1Stem1.getDisplayExtension().equals("level1Stem1 display name"));
    assertTrue(level1Stem1.getStemmers().size() == 0);
    assertTrue(level1Stem1.getCreators().size() == 0);
    assertTrue(level3Stem2.getDisplayExtension().equals("level3Stem2 display name"));
    assertTrue(level3Stem2.getStemmers().size() == 0);
    assertTrue(level3Stem2.getCreators().size() == 0);
    
    // verify other groups
    Group level1Group1 = GroupFinder.findByName(r.rs, "target:source:level1Group1", true);
    Group level1Group2 = GroupFinder.findByName(r.rs, "target:source:level1Group2", true);
    Group level1Group3 = GroupFinder.findByName(r.rs, "target:source:level1Group3", true);
    Group level2Group1 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Group1", true);
    Group level2Group2 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Group2", true);
    Group level2Group3 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Group3", true);
    Group level3Group1 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem1:level3Group1", true);
    Group level3Group2 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem1:level3Group2", true);
    Group level3Group3 = GroupFinder.findByName(r.rs, "target:source:level1Stem1:level2Stem2:level3Group3", true);
    assertTrue(level1Group1.getDisplayExtension().equals("level1Group1 display name"));
    assertTrue(level1Group1.getAdmins().size() == 0);
    assertTrue(level1Group1.getUpdaters().size() == 0);
    assertTrue(level1Group1.getViewers().size() == 0);
    assertTrue(level1Group1.getReaders().size() == 0);
    assertTrue(level1Group1.getOptins().size() == 0);
    assertTrue(level1Group1.getOptouts().size() == 0);
    assertTrue(level1Group1.getGroupAttrReaders().size() == 0);
    assertTrue(level1Group1.getGroupAttrUpdaters().size() == 0);
    assertTrue(level1Group1.getMembers().size() == 0);
    assertTrue(level1Group1.getTypes().size() == 0);
    assertTrue(level3Group2.getDisplayExtension().equals("level3Group2 display name"));
    assertTrue(level3Group2.getAdmins().size() == 0);
    assertTrue(level3Group2.getUpdaters().size() == 0);
    assertTrue(level3Group2.getViewers().size() == 0);
    assertTrue(level3Group2.getReaders().size() == 0);
    assertTrue(level3Group2.getOptins().size() == 0);
    assertTrue(level3Group2.getOptouts().size() == 0);
    assertTrue(level3Group2.getGroupAttrReaders().size() == 0);
    assertTrue(level3Group2.getGroupAttrUpdaters().size() == 0);
    assertTrue(level3Group2.getMembers().size() == 0);
    assertTrue(level3Group2.getTypes().size() == 0);
    assertTrue(level3Group3.getTypes().size() == 1);

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
    assertTrue(level1Group1.getComposite(true).getLeftGroup().getName().equals("target:source:level1Group2"));
    assertTrue(level1Group1.getComposite(true).getRightGroup().getName().equals("target:source:level1Group3"));
    assertTrue(level2Group1.getComposite(true).getLeftGroup().getName().equals("target:source:level1Stem1:level2Group2"));
    assertTrue(level2Group1.getComposite(true).getRightGroup().getName().equals("target:source:level1Stem1:level2Group3"));
    assertTrue(level3Group1.getComposite(true).getLeftGroup().getName().equals("top:child:child group"));
    assertTrue(level3Group1.getComposite(true).getRightGroup().getName().equals("source:level1Stem1:level2Stem1:level3Group2"));
    
    // stem privilege checks
    if (privilegesOfStem) {
      assertTrue(newStem.getStemmers().size() == 1);
      assertTrue(newStem.getCreators().size() == 1);
      assertTrue(newStem.hasCreate(a) == true);
      assertTrue(newStem.hasStem(b) == true);
      assertTrue(level3Stem1.getStemmers().size() == 1);
      assertTrue(level3Stem1.getCreators().size() == 2);
      assertTrue(level3Stem1.getStemAttrReaders().size() == 1);
      assertTrue(level3Stem1.getStemAttrUpdaters().size() == 1);
      assertTrue(level3Stem1.hasCreate(i) == true);
      assertTrue(level3Stem1.hasCreate(k) == true);
      assertTrue(level3Stem1.hasStem(j) == true);
      assertTrue(level3Stem1.hasStemAttrRead(l) == true);
      assertTrue(level3Stem1.hasStemAttrUpdate(m) == true);
    } else {
      assertTrue(newStem.getStemmers().size() == 0);
      assertTrue(newStem.getCreators().size() == 0);
      assertTrue(level3Stem1.getStemAttrReaders().size() == 0);
      assertTrue(level3Stem1.getStemAttrUpdaters().size() == 0);
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
      assertTrue(level3Group3.getAttributeValue("type1attr1", false, true).equals("test"));
    } else {
      assertTrue(level3Group3.getAttributeValue("type1attr1", false, false).equals(""));
    }
  }
  
  /**
   * @throws Exception
   */
  public void test_option_to_disable_last_membership_change() throws Exception {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    Group first = top.addChildGroup("first", "first");
    Group second = top.addChildGroup("second", "second");
    Stem test = top.addChildStem("test", "test");
    
    test.grantPriv(first.toSubject(), NamingPrivilege.STEM);
    test.grantPriv(a, NamingPrivilege.CREATE);
    test.grantPriv(b, NamingPrivilege.CREATE);
    test.revokePriv(a, NamingPrivilege.CREATE);
    test.revokePriv(NamingPrivilege.CREATE);
    
    first.addMember(second.toSubject());
    
    second.addMember(a);
    second.addMember(b);
    second.deleteMember(a);

    // after all this, the last_membership_change should still be null for the stem
    first = GroupFinder.findByName(r.rs, "top:first", true);
    second = GroupFinder.findByName(r.rs, "top:second", true);
    test = StemFinder.findByName(r.rs, "top:test", true);

    assertNotNull(first.getLastMembershipChange());
    assertNotNull(second.getLastMembershipChange());
    assertNull(test.getLastMembershipChange());
  }
  
  /**
   * @throws Exception
   */
  public void test_alternateName() throws Exception {
    assertTrue(top.getAlternateNames().size() == 0);

    // add invalid stem name
    try {
      top.addAlternateName(null);
      top.store();
      fail("failed to throw StemModifyException");
    } catch (StemModifyException e) {
      assertTrue(true);
    }
    
    // add invalid stem name
    try {
      top.addAlternateName("");
      top.store();
      fail("failed to throw StemModifyException");
    } catch (StemModifyException e) {
      assertTrue(true);
    }
    
    // add invalid stem name
    try {
      top.addAlternateName("top:");
      top.store();
      fail("failed to throw StemModifyException");
    } catch (StemModifyException e) {
      assertTrue(true);
    }
    
    // add invalid stem name
    try {
      top.addAlternateName("top::top2");
      top.store();
      fail("failed to throw StemModifyException");
    } catch (StemModifyException e) {
      assertTrue(true);
    }
    
    // add invalid stem name
    try {
      top.addAlternateName("top:  :top2");
      top.store();
      fail("failed to throw StemModifyException");
    } catch (StemModifyException e) {
      assertTrue(true);
    }
    
    assertTrue(top.getAlternateNames().size() == 0);
    
    // add an alternate name and verify it gets stored in the db
    top.addAlternateName("top:top2");
    top.store();
    assertTrue(top.getAlternateNames().size() == 1);
    assertTrue(top.getAlternateNames().contains("top:top2"));
    top = StemFinder.findByName(s, "top", true);
    assertTrue(top.getAlternateNames().size() == 1);
    assertTrue(top.getAlternateNames().contains("top:top2"));
    
    // add another alternate name.  it should overwrite the last one.
    top.addAlternateName("top:top3");
    top.store();
    assertTrue(top.getAlternateNames().size() == 1);
    assertTrue(top.getAlternateNames().contains("top:top3"));
    top = StemFinder.findByName(s, "top", true);
    assertTrue(top.getAlternateNames().size() == 1);
    assertTrue(top.getAlternateNames().contains("top:top3"));
    
    // try deleting first alternate name
    assertFalse(top.deleteAlternateName("top:top2"));
    top.store();
    assertTrue(top.getAlternateNames().size() == 1);
    assertTrue(top.getAlternateNames().contains("top:top3"));
    top = StemFinder.findByName(s, "top", true);
    assertTrue(top.getAlternateNames().size() == 1);
    assertTrue(top.getAlternateNames().contains("top:top3"));
    
    // delete alternate name
    assertTrue(top.deleteAlternateName("top:top3"));
    top.store();
    assertTrue(top.getAlternateNames().size() == 0);
    top = StemFinder.findByName(s, "top", true);
    assertTrue(top.getAlternateNames().size() == 0);
    
    // add an alternate name using a location that doesn't exist.
    top.addAlternateName("top2:top3");
    top.store();
    assertTrue(top.getAlternateNames().size() == 1);
    assertTrue(top.getAlternateNames().contains("top2:top3"));
    top = StemFinder.findByName(s, "top", true);
    assertTrue(top.getAlternateNames().size() == 1);
    assertTrue(top.getAlternateNames().contains("top2:top3"));
    
    // delete alternate name
    assertTrue(top.deleteAlternateName("top2:top3"));
    top.store();
    assertTrue(top.getAlternateNames().size() == 0);
    top = StemFinder.findByName(s, "top", true);
    assertTrue(top.getAlternateNames().size() == 0);
    
    // add alternate name again so we can verify that the name cannot be used again for a stem.
    top.addAlternateName("top:test");
    top.store();
    
    // add alternate name that already exists
    try {
      assertTrue(child.getAlternateNames().size() == 0);
      child.addAlternateName("top:test");
      child.store();
      fail("failed to throw StemModifyException");
    } catch (StemModifyException e) {
      assertTrue(true);
    }
    
    // add stem name that already exists
    try {
      top.addChildStem("test", "test");
      fail("failed to throw StemAddAlreadyExistsException");
    } catch (StemAddAlreadyExistsException e) {
      assertTrue(true);
    }
    
    // now try adding an alternate name where the name is an existing stem name
    try {
      top.addAlternateName("top:child");
      top.store();
      fail("failed to throw StemModifyException");
    } catch (StemModifyException e) {
      assertTrue(true);
    }
  }
  

  /**
   * @throws Exception
   */
  public void test_alternateNameSecurityCheck() throws Exception {
    
    Stem top2 = root.addChildStem("top2", "top2");
    Group securityGroup = top.addChildGroup("securityGroup", "securityGroup");

    GrouperSession session;
    R r = R.populateRegistry(0, 0, 1);
    Subject subjA = r.getSubject("a");

    session = GrouperSession.start(subjA);

    // subjA doesn't have stem access to top or top2
    try {
      top.addAlternateName("top2:test");
      top.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    top.grantPriv(subjA, NamingPrivilege.STEM);
    session.stop();
    session = GrouperSession.start(subjA);

    // subjA doesn't have stem access to top2
    try {
      top.addAlternateName("top2:test");
      top.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    top.revokePriv(subjA, NamingPrivilege.STEM);
    top2.grantPriv(subjA, NamingPrivilege.STEM);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA doesn't have stem access to top
    try {
      top.addAlternateName("top2:test");
      top.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    top.grantPriv(subjA, NamingPrivilege.STEM);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA has the appropriate privileges now
    top.addAlternateName("top2:test");
    top.store();
    assertTrue(true);
    
    // now we're requiring that the user must be a member of a group that's allowed to do renames
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToRenameStem", "top:securityGroup");
    
    // subjA is not in the security group
    try {
      top.addAlternateName("top2:test2");
      top.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    // add subjA to the security group and try again
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    securityGroup.addMember(subjA);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA has the appropriate privileges now
    top.addAlternateName("top2:test2");
    top.store();
    assertTrue(true);

    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    top.revokePriv(subjA, NamingPrivilege.STEM);
    top2.revokePriv(subjA, NamingPrivilege.STEM);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA doesn't have access to delete the alternate name
    try {
      top.deleteAlternateName("top2:test2");
      top.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    top.grantPriv(subjA, NamingPrivilege.STEM);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA can delete the alternate name now
    top.deleteAlternateName("top2:test2");
    top.store();
    assertTrue(true);
    
    // remove subjA from the security group and try again
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    securityGroup.deleteMember(subjA);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA doesn't have access to delete the alternate name (alternate name doesn't exist but should still get exception)
    try {
      top.deleteAlternateName("top2:test2");
      top.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    // done using security group
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.stem.groupAllowedToRenameStem");
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    Stem test1 = child.addChildStem("test1", "test1");
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA doesn't have stem access on top:child:test1
    try {
      top.addAlternateName("top:child:test1:test2");
      top.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    test1.grantPriv(subjA, NamingPrivilege.STEM);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA has stem access on top:child:test1
    top.addAlternateName("top:child:test1:test2");
    top.store();
    top.deleteAlternateName("top:child:test1:test2");
    top.store();
    assertTrue(true);
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    test1.delete();
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA doesn't have stem access on top:child
    try {
      top.addAlternateName("top:child:test1:test2");
      top.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    child.grantPriv(subjA, NamingPrivilege.STEM);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA has stem access on child
    top.addAlternateName("top:child:test1:test2");
    top.store();
    top.deleteAlternateName("top:child:test1:test2");
    top.store();
    assertTrue(true);
    
    // subjA should be able to set an alternate name of a stem at the same level without having access to the parent
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    top.revokePriv(subjA, NamingPrivilege.STEM);
    session.stop();
    session = GrouperSession.start(subjA);
    child.addAlternateName("top:child2");
    child.store();
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    top.grantPriv(subjA, NamingPrivilege.STEM);
    session.stop();
    session = GrouperSession.start(subjA);
    top.addAlternateName("top3");
    top.store();
    
    // subjA doesn't have stem access on the root stem
    try {
      top.addAlternateName("test3:test4:test5");
      top.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    root.grantPriv(subjA, NamingPrivilege.STEM);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA has stem access on the root stem
    top.addAlternateName("test3:test4:test5");
    top.store();
    top.deleteAlternateName("test3:test4:test5");
    top.store();
    assertTrue(true);
  }
}

