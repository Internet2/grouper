/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegateOptions;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotAllowed;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.role.Role;

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
    TestRunner.run(new AttributeAssignTest("testRetrieveMultiple"));
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
    
    //try to grant, cant
    try {
      AttributeAssignDelegateOptions attributeAssignDelegateOptions = new AttributeAssignDelegateOptions();
      attributeAssignDelegateOptions.setAssignAttributeAssignDelegatable(true);
      attributeAssignDelegateOptions.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
      this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName0, 
          member7, true, attributeAssignDelegateOptions);
      fail();
    } catch (Exception e) {
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

      attributeAssign.setEnabledDb("F");
      
      assertTrue(attributeAssign.xmlDifferentBusinessProperties(exampleAttributeAssign));
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
    AttributeAssign attributeAssign1 = group.getAttributeDelegate().internal_assignAttributeHelper(null, attributeDefName, true, null).getAttributeAssign();
    attributeAssign1.saveOrUpdate();

    //this one has different notes
    AttributeAssign attributeAssign2 = group.getAttributeDelegate().internal_assignAttributeHelper(null, attributeDefName, true, null).getAttributeAssign();
    attributeAssign2.setNotes("abc");
    attributeAssign2.saveOrUpdate();
    
    //this one has different notes and date
    AttributeAssign attributeAssign3 = group.getAttributeDelegate().internal_assignAttributeHelper(null, attributeDefName, true, null).getAttributeAssign();
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

  
}
