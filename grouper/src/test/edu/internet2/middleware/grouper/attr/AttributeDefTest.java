/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefAddException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;

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
    TestRunner.run(new AttributeDefTest("testHibernateSecurity2"));
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

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
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
    AttributeDef attributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findById(attributeDef.getId(), true);

    assertEquals(attributeDef.getId(), attributeDef2.getId());
    
    //lets retrieve by name
    attributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByName("top:test", true);
    
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

}
