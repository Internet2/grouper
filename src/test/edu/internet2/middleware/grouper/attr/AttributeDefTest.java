/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefAddException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;

/**
 * @author mchyzer
 *
 */
public class AttributeDefTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefTest("testHibernateSecurityAdmin"));
  }
  
  /**
   * 
   */
  public AttributeDefTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeDefTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** top stem */
  private Stem top;

  /** some group */
  private Group group;
  
  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
    this.group = this.top.addChildGroup("group", "group");
  }

  /**
   * make sure security is there
   */
  public void testHibernateSecurity() {
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    try {
    
      this.top.addChildAttributeDef("test", AttributeDefType.attr);
      fail("This shouldnt be allowed");
    } catch (AttributeDefAddException e) {
      //acceptable
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    //####################################
    //grant that subject stem on that stem
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    this.top.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    try {
    
      this.top.addChildAttributeDef("test", AttributeDefType.attr);
      fail("This shouldnt be allowed");
    } catch (AttributeDefAddException e) {
      //acceptable
    } catch (InsufficientPrivilegeException e) {
      //good
    }
    
    //####################################
    //grant that subject create on that stem
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    this.top.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);

    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    //this should work
    this.top.addChildAttributeDef("test", AttributeDefType.attr);
    
    
  }
  
  /**
   * make sure security is there
   */
  public void testHibernateSecurityAdmin() {
    
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrAdmin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptout", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrRead", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrUpdate", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrView", "false");

    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");

//    this.group.addMember(SubjectTestHelper.SUBJ3);
//    this.group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
//    this.grouperSession.stop();
//    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
//    System.out.println(this.group.hasAdmin(SubjectTestHelper.SUBJ1));
//    System.out.println(this.group.hasRead(SubjectTestHelper.SUBJ0));
//    System.out.println(this.group.hasAdmin(SubjectTestHelper.SUBJ0));
//    System.out.println(PrivilegeHelper.isWheelOrRoot(SubjectTestHelper.SUBJ0));
//    System.out.println(this.group.getMembers());
    
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    this.group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);

    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    System.out.println(this.group.hasAdmin(SubjectTestHelper.SUBJ1));
    
    try {
      attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    try {
      attributeDef.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ1);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    try {
      attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (RevokePrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    //####################################
    //grant that subject update on that attributeDef, not enough
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, true);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    try {
    
      attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }
    
    try {
      attributeDef.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ1);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    try {
      attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (RevokePrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    //####################################
    //grant that subject update on that attributeDef, not enough
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, true);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);
    
    attributeDef.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ1);

    assertTrue(attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true));
  }
  
  /**
   * 
   */
  public void testHibernateSecurity2() {
    
    //dont default any
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrAdmin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptout", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrRead", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrUpdate", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrView", "false");

    int grouperGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    int grouperMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_memberships");
    
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    
    int newGrouperGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    int newGrouperMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_memberships");
    
    assertEquals("Should make 6 group sets for permissions", grouperGroupSetCount + 6, newGrouperGroupSetCount);
    assertEquals("Should make a memberships for owner", grouperMembershipCount + 1, newGrouperMembershipCount);

    //#############################################
    
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrAdmin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptout", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrRead", "true");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrUpdate", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrView", "true");
    
    grouperGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    grouperMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_memberships");
    
    attributeDef = this.top.addChildAttributeDef("test2", AttributeDefType.attr);
    
    newGrouperGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    newGrouperMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_memberships");
    
    assertEquals("Should make 6 group sets for permissions", grouperGroupSetCount + 6, newGrouperGroupSetCount);
    assertEquals("Should make a memberships for owner, and 2 for grouperAll", grouperMembershipCount + 3, newGrouperMembershipCount);
    
    //################################################
    // make sure user can admin to same an attribute
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );

    attributeDef.setDescription("whatever");
    try {
      attributeDef.store();
      fail("Shouldnt succeed");
    } catch (Exception e) {
      //good
    }
    
    //make the user an admin
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );

    attributeDef.setDescription("whatever");
    //should succeed
    attributeDef.store();
    
    
    //#################################################
    //make sure if in group, then still allowed
    //TODO check group

  }
  
  /**
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    assertNotNull(attributeDef.getId());

    //lets retrieve by id
    AttributeDef attributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByIdSecure(attributeDef.getId(), true);

    assertEquals(attributeDef.getId(), attributeDef2.getId());
    
    //lets retrieve by name
    attributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure("top:test", true);
    
    assertEquals("top:test", attributeDef2.getName());
    assertEquals(attributeDef.getId(), attributeDef2.getId());

    //try to add another
    try {
      attributeDef2 = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    } catch (AttributeDefAddException adae) {
      assertTrue(adae.getMessage(), adae.getMessage().contains("attribute def already exists"));
    }

    attributeDef2 = this.top.addChildAttributeDef("test2", AttributeDefType.attr);
    
    
  }

  /**
   * make sure security is there
   */
  public void testHibernateGroup() {
    
//    this.grouperSession.stop();
//    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
  
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
  
    assertFalse(this.group.getAttributeDelegate().hasAttributeByName("top:testName"));
    
    assertTrue(this.group.getAttributeDelegate().assignAttribute(attributeDefName));
  
    assertTrue(this.group.getAttributeDelegate().hasAttributeByName("top:testName"));

    assertFalse(this.group.getAttributeDelegate().assignAttribute(attributeDefName));
    
  }

  /**
   * 
   */
  public void testHibernateSecurityWheel() {
    
    Stem etc          = this.root.addChildStem("etc", "etc");
    Group wheel        = etc.addChildGroup("wheel","wheel");
    
    ApiConfig.testConfig.put("groups.wheel.use", "true");
    ApiConfig.testConfig.put("groups.wheel.group", wheel.getName());

    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    //################################################
    // make sure user can admin to same an attribute
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
  
    attributeDef.setDescription("whatever");
    try {
      attributeDef.store();
      fail("Shouldnt succeed");
    } catch (Exception e) {
      //good
    }
    
    //make the user an admin
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    
    wheel.addMember(SubjectTestHelper.SUBJ0);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
  
    attributeDef.setDescription("whatever");
    //should succeed
    attributeDef.store();
    
    
  }

}
