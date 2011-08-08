/*
 * @author mchyzer
 * $Id: AuditTest.java,v 1.18 2009-08-18 23:11:39 shilen Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.sql.Timestamp;
import java.util.List;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GroupTypeTupleDAO;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class AuditTest extends GrouperTest {
  /**
   * @param name
   */
  public AuditTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new AuditTest("testGroupPrivileges"));
    TestRunner.run(new AuditTest("testAttributeAssignsMember"));
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    ApiConfig.testConfig.put("grouper.env.name", "testEnv");
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    ApiConfig.testConfig.remove("grouper.env.name");

  }

  /**
   * @throws Exception 
   * 
   */
  public void testTypes() throws Exception {
    
    //delete all audit records
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    //add a type
    GrouperSession grouperSession = SessionHelper.getRootSession();
    GroupType groupType = GroupType.createType(grouperSession, "test1");
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    assertTrue("durationMicros should exist", auditEntry.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 2, one for select, one for insert: " + auditEntry.getQueryCount(), 2 <= auditEntry.getQueryCount());

    assertEquals("Context id's should match", auditEntry.getContextId(), groupType.getContextId());
    
    assertEquals("engine should be junit", GrouperEngineBuiltin.JUNIT.getGrouperEngine(), auditEntry.getGrouperEngine());

    assertNotNull("createdOn should exist", auditEntry.getCreatedOn());

    assertEquals("testEnv", auditEntry.getEnvName());
    
    assertNotNull(auditEntry.getLastUpdated());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    assertTrue("grouper version is blank", StringUtils.isNotBlank(auditEntry.getGrouperVersion()));

    assertTrue("server host is blank", StringUtils.isNotBlank(auditEntry.getServerHost()));

    assertTrue("server user name is blank", StringUtils.isNotBlank(auditEntry.getServerUserName()));

    GroupType.createType(grouperSession, "test1", false);
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");

    assertEquals("Shouldnt have changed since type didnt change", auditCount+1, newAuditCount);
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    groupType.delete(grouperSession);

    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
  
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    AuditEntry auditEntry2 = auditEntries.get(1);

    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertTrue("durationMicros should exist", auditEntry2.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 1, one for delete: " + auditEntry2.getQueryCount(), 2 <= auditEntry2.getQueryCount());
  
    assertEquals("Context id's should match", auditEntry2.getContextId(), groupType.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
    
    
  }

  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession = SessionHelper.getRootSession();
  
  /** root stem */
  private Stem root;

  /**
   * @throws Exception 
   */
  public void testTypeTuples() throws Exception {
    GroupType groupType = GroupType.createType(grouperSession, "test1");
  
    Group group = StemHelper.addChildGroup(this.edu, "test1", "the test1");
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    group.addType(groupType);
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
  
    GroupTypeTuple groupTypeTuple = Hib3GroupTypeTupleDAO.findByGroupAndType(group, groupType);
    
    assertEquals("Context id's should match", auditEntry.getContextId(), groupTypeTuple.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    group.addType(groupType, false);
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Shouldnt have changed since type didnt change", auditCount+1, newAuditCount);
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    group.deleteType(groupType);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
  
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    AuditEntry auditEntry2 = auditEntries.get(1);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testStems() throws Exception {

    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    Stem stem = this.edu.addChildStem("test1", "test1");
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), stem.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    //make sure date is different
    GrouperUtil.sleep(1000);

    stem.setDisplayExtension("newVal");
    stem.store();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), stem.getContextId());

    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));

    assertTrue("description should contain diffs", auditEntry2.getDescription().contains("newVal")
        && auditEntry2.getDescription().contains("displayName"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    stem.delete();

    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    AuditEntry auditEntry3 = auditEntries.get(2);

    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testGroups() throws Exception {

    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    Group group = this.edu.addChildGroup("test1", "test1");
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), group.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    //make sure date is different
    GrouperUtil.sleep(1000);

    group.setDisplayExtension("newVal");
    group.store();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), group.getContextId());

    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));

    assertTrue("description should contain diffs", auditEntry2.getDescription().contains("newVal")
        && auditEntry2.getDescription().contains("displayName"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    group.delete();

    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    AuditEntry auditEntry3 = auditEntries.get(2);

    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testComposites() throws Exception {

    Group group = this.edu.addChildGroup("test1", "test1");
    Group groupLeft = this.edu.addChildGroup("test1left", "test1left");
    Group groupRight = this.edu.addChildGroup("test1right", "test1right");

    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    group.addCompositeMember(CompositeType.UNION, groupLeft, groupRight);
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    Composite composite = group.getComposite(true);
    
    assertEquals("Context id's should match", auditEntry.getContextId(), composite.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    //make sure date is different
    GrouperUtil.sleep(1000);

    group.assignCompositeMember(CompositeType.COMPLEMENT, groupLeft, groupRight);
    composite = group.getComposite(true);
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match: ", auditEntry2.getContextId(), composite.getContextId());

    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));

    //make sure date is different
    GrouperUtil.sleep(1000);
    
    group.deleteCompositeMember();

    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    AuditEntry auditEntry3 = auditEntries.get(2);

    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
  }

  /**
   * @throws Exception 
   */
  public void testMemberships() throws Exception {
  
    Group group = this.edu.addChildGroup("test1", "test1");
    Group group2 = this.edu.addChildGroup("test2", "test2");
    Subject subject = SubjectFinder.findRootSubject();
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    group.addMember(subject);
  
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    Membership membership = MembershipFinder.findImmediateMembership(this.grouperSession, group, subject, Group.getDefaultList(), true);
    
    assertEquals("Context id's should match", auditEntry.getContextId(), membership.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
  
    group2.addMember(subject);
  
    Membership membership2 = MembershipFinder.findImmediateMembership(this.grouperSession, group2, subject, 
        Group.getDefaultList(), true);
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match: ", auditEntry2.getContextId(), membership2.getContextId());
  
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
  
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    group.deleteMember(subject);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
  }

  /**
   */
  public void testGroupPrivileges() {

    Group group = this.edu.addChildGroup("test1", "test1");
    Group group2 = this.edu.addChildGroup("test2", "test2");
    Subject subject = SubjectFinder.findRootSubject();
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    group.grantPriv(subject, AccessPrivilege.OPTIN);

    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    Membership membership = MembershipFinder.findImmediateMembership(this.grouperSession, group, subject, 
        FieldFinder.find("optins", true), true);
    
    assertEquals("Context id's should match", auditEntry.getContextId(), membership.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    //make sure date is different
    GrouperUtil.sleep(1000);

    group2.grantPriv(subject, AccessPrivilege.OPTIN);

    Membership membership2 = MembershipFinder.findImmediateMembership(this.grouperSession, group2, subject, 
        FieldFinder.find("optins", true), true);
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match: ", auditEntry2.getContextId(), membership2.getContextId());

    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));

    //make sure date is different
    GrouperUtil.sleep(1000);
    
    group.revokePriv(subject, AccessPrivilege.OPTIN);

    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    AuditEntry auditEntry3 = auditEntries.get(2);

    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
  }

  /**
   */
  public void testStemPrivileges() {

    Stem stem = this.edu.addChildStem("test1", "test1");
    Stem stem2 = this.edu.addChildStem("test2", "test2");
    Subject subject = SubjectTestHelper.SUBJ0;
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    stem.grantPriv(subject, NamingPrivilege.CREATE);

    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    Member member = MemberFinder.findBySubject(this.grouperSession, subject, false);
    
    Membership membership = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(stem.getUuid(), 
        member.getUuid(), FieldFinder.find("creators", true), "immediate", true, true);
    
    assertEquals("Context id's should match", auditEntry.getContextId(), membership.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    //make sure date is different
    GrouperUtil.sleep(1000);

    stem2.grantPriv(subject, NamingPrivilege.CREATE);

    Membership membership2 = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(stem2.getUuid(), 
        member.getUuid(), FieldFinder.find("creators", true), "immediate", true, true);
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match: ", auditEntry2.getContextId(), membership2.getContextId());

    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));

    //make sure date is different
    GrouperUtil.sleep(1000);
    
    stem.revokePriv(subject, NamingPrivilege.CREATE);

    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    AuditEntry auditEntry3 = auditEntries.get(2);

    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
  }

  /**
   * @throws Exception 
   * 
   */
  public void testFields() throws Exception {

    //add a type
    GrouperSession grouperSession = SessionHelper.getRootSession();
    GroupType groupType = GroupType.createType(grouperSession, "test1");
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");

    assertEquals(0, auditCount);
    
    Field field = groupType.addAttribute(grouperSession, "test1attr", AccessPrivilege.READ, AccessPrivilege.ADMIN,true);
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    assertTrue("durationMicros should exist", auditEntry.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 2, one for select, one for insert: " + auditEntry.getQueryCount(), 2 <= auditEntry.getQueryCount());
  
    assertEquals("Context id's should match", auditEntry.getContextId(), field.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    groupType.addAttribute(grouperSession, "test1attr", AccessPrivilege.READ, AccessPrivilege.ADMIN,true, false);
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Shouldnt have changed since type didnt change", auditCount+1, newAuditCount);
    
    //make sure date is different on mysql
    GrouperUtil.sleep(1000);

    //try an update
    field = groupType.addOrUpdateAttribute(grouperSession, "test1attr", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, true);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
  
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    AuditEntry auditEntry2 = auditEntries.get(1);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertTrue("durationMicros should exist", auditEntry2.getDurationMicroseconds() > 0);
  
    assertEquals("Context id's should match", auditEntry2.getContextId(), field.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
    
    //make sure date is different on mysql
    GrouperUtil.sleep(1000);

    //try a delete
    groupType.deleteField(grouperSession, "test1attr");
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
     auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
  }

  /**
   * @throws Exception 
   */
  public void testAttributeDefs() throws Exception {
  
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    AttributeDef attributeDef = this.edu.addChildAttributeDef("test1", AttributeDefType.attr);
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), attributeDef.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
  
    attributeDef.setDescription("newVal");
    attributeDef.store();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), attributeDef.getContextId());
  
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
  
    assertTrue("description should contain diffs", auditEntry2.getDescription().contains("newVal")
        && auditEntry2.getDescription().contains("description"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    attributeDef.delete();
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);

    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testAttributeDefNames() throws Exception {
  
    AttributeDef attributeDef = this.edu.addChildAttributeDef("test1", AttributeDefType.attr);
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    AttributeDefName attributeDefName = this.edu.addChildAttributeDefName(attributeDef, "test1", "test1");
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), attributeDefName.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
  
    attributeDefName.setDescription("newVal");
    attributeDefName.store();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), attributeDefName.getContextId());
  
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
  
    assertTrue("description should contain diffs", auditEntry2.getDescription().contains("newVal")
        && auditEntry2.getDescription().contains("description"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    attributeDefName.delete();
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
  
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testAttributeAssignsGroup() throws Exception {
  
    AttributeDef attributeDef = this.edu.addChildAttributeDef("test1", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName = this.edu.addChildAttributeDefName(attributeDef, "test1", "test1");
    Group group = new GroupSave(this.grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), attributeAssign.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));

    assertTrue(auditEntry.getAuditType().getActionName(), auditEntry.getAuditType().getActionName().toLowerCase().contains("group"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
  
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis()));
    attributeAssign.saveOrUpdate();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), attributeAssign.getContextId());
  
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
  
    assertTrue("description should contain diffs: " + auditEntry2.getDescription(), 
        auditEntry2.getDescription().contains("disabledTime"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    group.getAttributeDelegate().removeAttribute(attributeDefName);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
  
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testAttributeAssignsStem() throws Exception {
  
    AttributeDef attributeDef = this.edu.addChildAttributeDef("test1", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    AttributeDefName attributeDefName = this.edu.addChildAttributeDefName(attributeDef, "test1", "test1");
    Stem stem = new StemSave(this.grouperSession).assignName("test:testStem").assignCreateParentStemsIfNotExist(true).save();
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), attributeAssign.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
  
    assertTrue(auditEntry.getAuditType().getActionName(), auditEntry.getAuditType().getActionName().toLowerCase().contains("stem"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
  
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis()));
    attributeAssign.saveOrUpdate();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), attributeAssign.getContextId());
  
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
  
    assertTrue("description should contain diffs: " + auditEntry2.getDescription(), 
        auditEntry2.getDescription().contains("disabledTime"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    stem.getAttributeDelegate().removeAttribute(attributeDefName);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
  
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testAttributeAssignsMember() throws Exception {
  
    AttributeDef attributeDef = this.edu.addChildAttributeDef("test1", AttributeDefType.attr);
    attributeDef.setAssignToMember(true);
    attributeDef.store();
    AttributeDefName attributeDefName = this.edu.addChildAttributeDefName(attributeDef, "test1", "test1");
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectFinder.findAllSubject(), true);
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    AttributeAssign attributeAssign = member.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), attributeAssign.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
  
    assertTrue(auditEntry.getAuditType().getActionName(), auditEntry.getAuditType().getActionName().toLowerCase().contains("member"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
  
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis()));
    attributeAssign.saveOrUpdate();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), attributeAssign.getContextId());
  
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
  
    assertTrue("description should contain diffs: " + auditEntry2.getDescription(), 
        auditEntry2.getDescription().contains("disabledTime"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    member.getAttributeDelegate().removeAttribute(attributeDefName);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
  
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testAttributeAssignsAttributeDef() throws Exception {
  
    AttributeDef attributeDef = this.edu.addChildAttributeDef("test1", AttributeDefType.attr);
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.store();
    AttributeDefName attributeDefName = this.edu.addChildAttributeDefName(attributeDef, "test1", "test1");
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    AttributeAssign attributeAssign = attributeDef.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), attributeAssign.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
  
    assertTrue(auditEntry.getAuditType().getActionName(), auditEntry.getAuditType().getActionName().toLowerCase().contains("attrdef"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
  
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis()));
    attributeAssign.saveOrUpdate();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), attributeAssign.getContextId());
  
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
  
    assertTrue("description should contain diffs: " + auditEntry2.getDescription(), 
        auditEntry2.getDescription().contains("disabledTime"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    attributeDef.getAttributeDelegate().removeAttribute(attributeDefName);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
  
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testAttributeAssignsImmMship() throws Exception {
  
    AttributeDef attributeDef = this.edu.addChildAttributeDef("test1", AttributeDefType.attr);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName = this.edu.addChildAttributeDefName(attributeDef, "test1", "test1");

    Group group = new GroupSave(this.grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    group.addMember(SubjectFinder.findAllSubject());
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectFinder.findAllSubject(), true);
    Membership membership = group.getImmediateMembership(Group.getDefaultList(), member, true, true);
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    AttributeAssign attributeAssign = membership.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), attributeAssign.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
  
    assertTrue(auditEntry.getAuditType().getActionName(), auditEntry.getAuditType().getActionName().toLowerCase().contains("immmship"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
  
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis()));
    attributeAssign.saveOrUpdate();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), attributeAssign.getContextId());
  
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
  
    assertTrue("description should contain diffs: " + auditEntry2.getDescription(), 
        auditEntry2.getDescription().contains("disabledTime"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    membership.getAttributeDelegate().removeAttribute(attributeDefName);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
  
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testAttributeAssignsAnyMship() throws Exception {
  
    AttributeDef attributeDef = this.edu.addChildAttributeDef("test1", AttributeDefType.attr);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName = this.edu.addChildAttributeDefName(attributeDef, "test1", "test1");
  
    Group group = new GroupSave(this.grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    group.addMember(SubjectFinder.findAllSubject());
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectFinder.findAllSubject(), true);

    GroupMember groupMember = new GroupMember(group, member);
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    AttributeAssign attributeAssign = groupMember.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), attributeAssign.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
  
    assertTrue(auditEntry.getAuditType().getActionName(), auditEntry.getAuditType().getActionName().toLowerCase().contains("anymship"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
  
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis()));
    attributeAssign.saveOrUpdate();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), attributeAssign.getContextId());
  
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
  
    assertTrue("description should contain diffs: " + auditEntry2.getDescription(), 
        auditEntry2.getDescription().contains("disabledTime"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    groupMember.getAttributeDelegate().removeAttribute(attributeDefName);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
  
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testAttributeAssignsAssign() throws Exception {
  
    AttributeDef attributeDef = this.edu.addChildAttributeDef("test1", AttributeDefType.attr);
    attributeDef.setAssignToGroupAssn(true);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName = this.edu.addChildAttributeDefName(attributeDef, "test1", "test1");
    Group group = new GroupSave(this.grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    AttributeAssign attributeAssignOwner = group.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    AttributeAssign attributeAssign = attributeAssignOwner.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), attributeAssign.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
  
    assertTrue(auditEntry.getAuditType().getActionName(), auditEntry.getAuditType().getActionName().toLowerCase().contains("assignassign"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
  
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis()));
    attributeAssign.saveOrUpdate();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), attributeAssign.getContextId());
  
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
  
    assertTrue("description should contain diffs: " + auditEntry2.getDescription(), 
        auditEntry2.getDescription().contains("disabledTime"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    attributeAssignOwner.getAttributeDelegate().removeAttribute(attributeDefName);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
  
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   */
  public void testAttributeAssignValue() throws Exception {
  
    AttributeDef attributeDef = this.edu.addChildAttributeDef("test1", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();

    AttributeDefName attributeDefName = this.edu.addChildAttributeDefName(attributeDef, "test1", "test1");
    Group group = new GroupSave(this.grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.getAttributeDelegate().assignAttribute(attributeDefName);
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
  
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
   AttributeAssignValue attributeAssignValue = group.getAttributeValueDelegate()
     .assignValue(attributeDefName.getName(), "something").getAttributeAssignValueResult().getAttributeAssignValue();
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), attributeAssignValue.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
  
    assertTrue(auditEntry.getAuditType().getActionName(), auditEntry.getAuditType().getActionName().toLowerCase().contains("addattributeassignvalue"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
  
    attributeAssignValue.setValueString("anotherString");
    attributeAssignValue.saveOrUpdate();
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
    
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
    
    AuditEntry auditEntry2 = auditEntries.get(1);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertEquals("Context id's should match", auditEntry2.getContextId(), attributeAssignValue.getContextId());
  
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
  
    assertTrue("description should contain diffs: " + auditEntry2.getDescription(), 
        auditEntry2.getDescription().contains("anotherString"));
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    group.getAttributeValueDelegate().deleteValue(attributeDefName.getName(), "anotherString");
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
    auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOnDb").list(AuditEntry.class);
  
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }

}
