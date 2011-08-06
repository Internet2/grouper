/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegateOptions;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotAllowed;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author mchyzer
 *
 */
public class AttributeAssignTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignTest("testFindAnyMembershipAttributeAssignments"));
  }
  
  /**
   * 
   */
  public AttributeAssignTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeAssignTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** top stem */
  private Stem top;

  /** top stem */
  private Role role;

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
    this.role = this.top.addChildRole("role", "role");
    this.role.addMember(SubjectTestHelper.SUBJ0, false);
    this.role.addMember(SubjectTestHelper.SUBJ1, false);
    this.role.addMember(SubjectTestHelper.SUBJ2, false);
    this.role.addMember(SubjectTestHelper.SUBJ7, false);

    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrAdmin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptout", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrRead", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrUpdate", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrView", "false");

    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");

    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "-1");
  }

  /**
   * attribute def
   */
  public void testHibernateFail() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    
    try {
      attributeAssign.saveOrUpdate();
      fail("Should throw exception");
    } catch (AttributeAssignNotAllowed aana) {
      //good
    }
    
  }

  /**
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    
    //should work now
    attributeAssign.saveOrUpdate();
  }

  /**
   * attribute def
   */
  public void testDelegation() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName0 = this.top.addChildAttributeDefName(attributeDef, "testName0", "test name0");
    AttributeDefName attributeDefName1 = this.top.addChildAttributeDefName(attributeDef, "testName1", "test name1");
    AttributeDefName attributeDefName2 = this.top.addChildAttributeDefName(attributeDef, "testName2", "test name2");

    //assign a direct assignment to the user
    //subj0 can delegate
    //subj1 cannot delegate
    //subj2 can grant delegation
    AttributeAssignResult attributeAssignResult0 = this.role.getPermissionRoleDelegate()
      .assignSubjectRolePermission(attributeDefName0, SubjectTestHelper.SUBJ0);
    this.role.getPermissionRoleDelegate()
      .assignSubjectRolePermission(attributeDefName1, SubjectTestHelper.SUBJ1);
    AttributeAssignResult attributeAssignResult2 = this.role.getPermissionRoleDelegate()
    .assignSubjectRolePermission(attributeDefName2, SubjectTestHelper.SUBJ2);
    
    AttributeAssign attributeAssign0 = attributeAssignResult0.getAttributeAssign();
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    attributeAssign0.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
    attributeAssign0.saveOrUpdate();
    
    attributeAssign2.setAttributeAssignDelegatable(AttributeAssignDelegatable.GRANT);
    attributeAssign2.saveOrUpdate();
    
    Member member7 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ7, true);
    
    //lets try to delegate with permissions
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName0, 
        member7, true, null);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    
    PermissionEntry permissionEntry = GrouperDAOFactory.getFactory().getPermissionEntry()
      .findByMemberIdAndAttributeDefNameId(member7.getUuid(), attributeDefName0.getId()).iterator().next();
    assertTrue(permissionEntry != null);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //try to grant as delegatable, cant
    try {
      AttributeAssignDelegateOptions attributeAssignDelegateOptions = new AttributeAssignDelegateOptions();
      attributeAssignDelegateOptions.setAssignAttributeAssignDelegatable(true);
      attributeAssignDelegateOptions.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
      this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName0, 
          member7, true, attributeAssignDelegateOptions);
      fail();
    } catch (Exception e) {
      //e.printStackTrace();
      //ok
    }
    
    //ungrant
    this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName0, 
        member7, false, null);
    assertEquals(0, GrouperDAOFactory.getFactory().getPermissionEntry()
      .findByMemberIdAndAttributeDefNameId(member7.getUuid(), attributeDefName0.getId()).size());

    //#############################################
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    //try to delegate, cant
    try {
      this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName1, 
          member7, true, null);
      fail(); 
    } catch (Exception e) {
      //ok
    }

    //#############################################
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    AttributeAssignDelegateOptions attributeAssignDelegateOptions = new AttributeAssignDelegateOptions();
    attributeAssignDelegateOptions.setAssignAttributeAssignDelegatable(true);
    attributeAssignDelegateOptions.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);

    //try to grant, can
    this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName2, 
        member7, true, attributeAssignDelegateOptions);

    permissionEntry = GrouperDAOFactory.getFactory().getPermissionEntry()
      .findByMemberIdAndAttributeDefNameId(member7.getUuid(), attributeDefName2.getId()).iterator().next();
    assertEquals(AttributeAssignDelegatable.TRUE, permissionEntry.getAttributeAssignDelegatable());
  }

  /**
   * attribute def
   */
  public void testHibernateDelegate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
    attributeAssign.saveOrUpdate();
    
    
    
  }

  /**
   * make an example attribute assign for testing
   * @return an example attribute assign
   */
  public static AttributeAssign exampleAttributeAssign() {
    AttributeAssign attributeAssign = new AttributeAssign();
    attributeAssign.setAttributeAssignActionId("attributeAssignActionId");
    attributeAssign.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
    attributeAssign.setAttributeAssignType(AttributeAssignType.any_mem);
    attributeAssign.setAttributeDefNameId("attributeDefNameId");
    attributeAssign.setContextId("contextId");
    attributeAssign.setCreatedOnDb(5L);
    attributeAssign.setDisabledTimeDb(7L);
    attributeAssign.setEnabledDb("T");
    attributeAssign.setEnabledTimeDb(8L);
    attributeAssign.setHibernateVersionNumber(3L);
    attributeAssign.setLastUpdatedDb(6L);
    attributeAssign.setNotes("notes");
    attributeAssign.setOwnerAttributeAssignId("ownerAttributeAssignId");
    attributeAssign.setOwnerAttributeDefId("ownerAttributeDefId");
    attributeAssign.setOwnerGroupId("ownerGroupId");
    attributeAssign.setOwnerMemberId("ownerMemberId");
    attributeAssign.setOwnerMembershipId("ownerMembershipId");
    attributeAssign.setOwnerStemId("ownerStemId");
    attributeAssign.setId("uuid");
    
    return attributeAssign;
  }
  
  /**
   * make an example attribute assign from db for testing
   * @return an example group
   */
  public static AttributeAssign exampleAttributeAssignDb() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    return attributeAssignResult.getAttributeAssign();
  }

  
  /**
   * retrieve example group from db for testing
   * @return an example group
   */
  public static AttributeAssign exampleRetrieveAttributeAssignDb() {
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("test:testAttributeAssignDefName", true);
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:groupTestAttrAssign", true);
    return group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    AttributeDefName attributeDefInsertName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefInsertName");
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrInsertAssign").assignName("test:groupTestAttrInsertAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefInsertName);
    AttributeAssign attributeAssignOriginal = attributeAssignResult.getAttributeAssign();
    
    //do this because last membership update isnt there, only in db
    attributeAssignOriginal = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignOriginal.getId(), true);
    AttributeAssign attributeAssignCopy = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignOriginal.getId(), true);
    AttributeAssign atrtibuteAssignCopy2 = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignOriginal.getId(), true);
    attributeAssignCopy.delete();
    
    //lets insert the original
    atrtibuteAssignCopy2.xmlSaveBusinessProperties(null);
    atrtibuteAssignCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    attributeAssignCopy = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignOriginal.getId(), true);
    
    assertFalse(attributeAssignCopy == attributeAssignOriginal);
    assertFalse(attributeAssignCopy.xmlDifferentBusinessProperties(attributeAssignOriginal));
    assertFalse(attributeAssignCopy.xmlDifferentUpdateProperties(attributeAssignOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeAssign attributeAssign = null;
    AttributeAssign exampleAttributeAssign = null;

    
    //TEST UPDATE PROPERTIES
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();
      
      attributeAssign.setContextId("abc");
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertTrue(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setContextId(exampleAttributeAssign.getContextId());
      attributeAssign.xmlSaveUpdateProperties();

      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
      
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setCreatedOnDb(99L);
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertTrue(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setCreatedOnDb(exampleAttributeAssign.getCreatedOnDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setLastUpdatedDb(99L);
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertTrue(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setLastUpdatedDb(exampleAttributeAssign.getLastUpdatedDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setHibernateVersionNumber(99L);
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertTrue(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setHibernateVersionNumber(exampleAttributeAssign.getHibernateVersionNumber());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setAttributeAssignActionId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setAttributeAssignActionId(exampleAttributeAssign.getAttributeAssignActionId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setAttributeDefNameId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setAttributeDefNameId(exampleAttributeAssign.getAttributeDefNameId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      //this is set based on times...
      attributeAssign.setEnabledDb("F");
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setEnabledDb(exampleAttributeAssign.getEnabledDb());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setEnabledTimeDb(99L);
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setEnabledTimeDb(exampleAttributeAssign.getEnabledTimeDb());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setId(exampleAttributeAssign.getId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
    
    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setNotes("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setNotes(exampleAttributeAssign.getNotes());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerAttributeAssignId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerAttributeAssignId(exampleAttributeAssign.getOwnerAttributeAssignId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerAttributeDefId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerAttributeDefId(exampleAttributeAssign.getOwnerAttributeDefId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerGroupId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerGroupId(exampleAttributeAssign.getOwnerGroupId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerMemberId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerMemberId(exampleAttributeAssign.getOwnerMemberId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerMembershipId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerMembershipId(exampleAttributeAssign.getOwnerMembershipId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }

    {
      attributeAssign = exampleAttributeAssignDb();
      exampleAttributeAssign = exampleRetrieveAttributeAssignDb();

      attributeAssign.setOwnerStemId("abc");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));

      attributeAssign.setOwnerStemId(exampleAttributeAssign.getOwnerStemId());
      attributeAssign.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignDb());
      attributeAssign.xmlSaveUpdateProperties();
      
      attributeAssign = exampleRetrieveAttributeAssignDb();
      
      assertFalse(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
      assertFalse(attributeAssign.xmlDifferentUpdateProperties(exampleAttributeAssign));
    
    }
  }

  /**
   * make sure update properties are detected correctly
   */
  public void testRetrieveMultiple() {
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();

    //this one is the same
    AttributeAssign attributeAssign1 = group.getAttributeDelegate().internal_assignAttributeHelper(null, attributeDefName, true, null, null).getAttributeAssign();
    attributeAssign1.saveOrUpdate();

    //this one has different notes
    AttributeAssign attributeAssign2 = group.getAttributeDelegate().internal_assignAttributeHelper(null, attributeDefName, true, null, null).getAttributeAssign();
    attributeAssign2.setNotes("abc");
    attributeAssign2.saveOrUpdate();
    
    //this one has different notes and date
    AttributeAssign attributeAssign3 = group.getAttributeDelegate().internal_assignAttributeHelper(null, attributeDefName, true, null, null).getAttributeAssign();
    attributeAssign3.setNotes("abc");
    attributeAssign3.setEnabledTimeDb(9L);
    attributeAssign3.saveOrUpdate();
    
    //get by id
    attributeAssign = attributeAssign1.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssign1.getId(), attributeAssign.getId());

    attributeAssign = attributeAssign2.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssign2.getId(), attributeAssign.getId());

    attributeAssign = attributeAssign3.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssign3.getId(), attributeAssign.getId());

    
  }

  /**
   * 
   */
  public void testFindGroupAttributeAssignments() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    //test subject 0 can view and read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 1 can view not read
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW);

    //test subject 2 can read not view
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 3 can not read or view

    //test subject 4 can read and read
    group.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 5 can update and read
    group.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 6 can admin and read
    group.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 7 can view and update
    group.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);

    //test subject 8 can view and admin
    group.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view and view
    group.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);

    
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }

    //Search for group, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet("abc"), null, true, false);
  
    assertEquals(0, attributeAssigns.size());

    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());

    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);

    assertEquals(0, attributeAssigns.size());

    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);

    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }

    ApiConfig.testConfig.remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);

    assertEquals(0, attributeAssigns.size());


    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());

    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can view and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    
    //test subject 1 can view not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
    
    //test subject 2 can read not view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(1 == attributeDefs.size() && attributeDefs.contains(attributeDef));

    //test subject 3 can not read or view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());

    //test subject 4 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 4 cannot read the attribute assignment on assignment
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 6 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 7 can view and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());

    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 8 can view and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 9 can view and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findGroupAttributeAssignments(null, null, null, GrouperUtil.toSet(group.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    group.delete();
    GrouperSession.stopQuietly(this.grouperSession);

  }

  /**
   * 
   */
  public void testFindStemAttributeAssignments() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignStemNameToEdit("test:stemTestAttrAssign").assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    //test subject 0 can read
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read
  
    //test subject 7 can update
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can admin
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);

    
    AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);

    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet("abc"), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    ApiConfig.testConfig.remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
 
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 7 can update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);

    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findStemAttributeAssignments(null, null, null, GrouperUtil.toSet(stem.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());

    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    stem.delete();
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testFindMemberAttributeAssignments() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToMember(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ0, true);

    //test subject 0 can read
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 3 can not read

    //test subject 7 can update
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can admin
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = member.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);

    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findMemberAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for stem, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet("abc"), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    ApiConfig.testConfig.remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    //test subject 3 can not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 7 can update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //test subject 9 can view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, GrouperUtil.toSet(member.getUuid()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
  }

  /**
   * 
   */
  public void testFindAttrDefAttributeAssignments() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    AttributeDef attributeDefAssignTo = AttributeDefTest.exampleAttributeDefDb("test", "testAttributeDefAssignTo");
    

    //test subject 0 can view and read
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can view not read
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_VIEW, false);
  
    //test subject 2 can read not view
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read or view
  
    //test subject 4 can read and read
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 5 can update and read
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 6 can admin and read
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can view and update
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can view and admin
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view and view
    attributeDefAssignTo.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = attributeDefAssignTo.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);

    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for group, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet("abc"), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    ApiConfig.testConfig.remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can view and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    //test subject 1 can view not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
    
    //test subject 2 can read not view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(1 == attributeDefs.size() && attributeDefs.contains(attributeDef));
  
    //test subject 3 can not read or view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 4 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 6 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 7 can view and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can view and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 9 can view and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefAttributeAssignments(null, null, null, GrouperUtil.toSet(attributeDefAssignTo.getId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();

    attributeDef.delete();
    
    attributeDef2.delete();
    
  }

  /**
   * 
   */
  public void testFindMembershipAttributeAssignments() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:membershipTestAttrAssign").assignName("test:membershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    
    Membership membership = group1.getMemberships(FieldFinder.find("members", true)).iterator().next();
    
    //test subject 0 can read and read
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can read not read
    group1.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
  
    //test subject 2 can read not read
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read or read
  
    //test subject 4 can view and read
    group1.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 5 can update and read
    group1.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 6 can admin and read
    group1.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can read and update
    group1.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can read and admin
    group1.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can read and view
    group1.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
  
    
    AttributeAssignResult attributeAssignResult = membership.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
      
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);

    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findMembershipAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    //Search for membership, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet("abc"), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    ApiConfig.testConfig.remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    //test subject 1 can read not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
    
    //test subject 2 can read not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(1 == attributeDefs.size() && attributeDefs.contains(attributeDef));
  
    //test subject 3 can not read or read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can view and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 6 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 7 can read and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can read and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, true);
    assertEquals(1, attributeAssigns.size());
    assertTrue(attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //test subject 9 can read and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(membership.getImmediateMembershipId()), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.startRootSession();
    
    //make sure this cascades right
    group1.deleteMember(SubjectTestHelper.SUBJ0);
    
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * note, this wont find immediate ones, only the any kind which works on effective memberships or immediate memberships
   */ 
  public void testFindAnyMembershipAttributeAssignments() {

    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign").assignName("test:anyMembershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign2").assignName("test:anyMembershipTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    //add one group to another to make effective membership and add attribute to that membership
    group1.addMember(group2.toSubject());
    group2.addMember(SubjectTestHelper.SUBJ0);
    
    //test subject 0 can read and read
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 1 can read not read
    group1.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
  
    //test subject 2 can read not read
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 3 can not read or read
  
    //test subject 4 can view and read
    group1.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 5 can update and read
    group1.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 6 can admin and read
    group1.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);
  
    //test subject 7 can read and update
    group1.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);
  
    //test subject 8 can read and admin
    group1.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can read and view
    group1.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);
    
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ0, false);

    Membership membership = (Membership)MembershipFinder.findMemberships(GrouperUtil.toSet(group1.getId()), 
        GrouperUtil.toSet(member.getUuid()), null, null, FieldFinder.find("members", true), null, null, null, null, null).iterator().next()[0];
    
    AttributeAssignResult attributeAssignResult = membership.getAttributeDelegateEffMship().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    Set<AttributeAssign> attributeAssignsBase2 = GrouperUtil.toSet(attributeAssign, attributeAssign2);

    //Search for all, should be an error:
    try {
      GrouperDAOFactory.getFactory().getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, null, null, null, false);
      fail();
    } catch (Exception e) {
      //good
    }

    //Search for membership, should find it
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search for not it, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), "abc")), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey("abc", member.getUuid())), null, true, false);
  
    assertEquals(0, attributeAssigns.size());

    //search by attributeDefId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("assign"), true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by attributeDefId and action (wrong), shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet(attributeDef.getId()), null, null, GrouperUtil.toSet("abc"), true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    //search by not attributeDefNameId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, GrouperUtil.toSet("abc"), null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
    //search by attributeAssignId, should find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
    
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "1");
    
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
  
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    ApiConfig.testConfig.put("ws.findAttrAssignments.maxResultSize", "0");
    
    try {
      attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet(attributeAssign.getId()), null, null, null, null, true, false);
      fail();
    } catch (Exception e) {
      //good
    }
  
    ApiConfig.testConfig.remove("ws.findAttrAssignments.maxResultSize");
    
    //search by not attributeAssignId, should not find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(GrouperUtil.toSet("abc"), null, null, null, null, true, false);
  
    assertEquals(0, attributeAssigns.size());
  
  
    //search by not attributeDefId, shouldnt find it
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, GrouperUtil.toSet("abc"), null, null, null, true, false);
    
    assertEquals(0, attributeAssigns.size());
  
    Set<AttributeAssign> attributeAssignsBase = GrouperUtil.toSet(attributeAssign);
    Set<AttributeDef> attributeDefsBase = GrouperUtil.toSet(attributeDef);
    
    //temp var for checking
    Set<AttributeDef> attributeDefs;
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
    
    //#####################  CHECK SECURITY #############################
    //test subject 0 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));

    //test subject 0 can read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, true);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 2 && attributeAssigns.contains(attributeAssign) && attributeAssigns.contains(attributeAssign2));

    //test subject 1 can read not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
  
    assertEquals(0, attributeAssigns.size());
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
    
    //test subject 2 can read not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(1 == attributeDefs.size() && attributeDefs.contains(attributeDef));
  
    //test subject 3 can not read or read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertEquals(0, attributeDefs.size());
  
    //test subject 4 can view and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 6 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 7 can read and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 can read and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    //test subject 8 cannot read the attribute assignment on assignment
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, true);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));
    
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase2, false);
    assertTrue(attributeAssigns.size() == 1 && attributeAssigns.contains(attributeAssign));

    //test subject 9 can read and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
  
    attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(group1.getId(), member.getUuid())), null, true, false);
    assertEquals(0, attributeAssigns.size());
  
    attributeAssigns = PrivilegeHelper.canViewAttributeAssigns(this.grouperSession, attributeAssignsBase, false);
    assertEquals(0, attributeAssigns.size());
    
    attributeDefs = PrivilegeHelper.canViewAttributeDefs(this.grouperSession, attributeDefsBase);
    assertTrue(attributeDefs.size() == 1 && attributeDefs.contains(attributeDef));
  
    attributeAssign.delete();
    
  }
  
}
