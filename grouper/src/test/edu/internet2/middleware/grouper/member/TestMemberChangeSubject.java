/*
 * @author mchyzer
 * $Id: TestMemberChangeSubject.java,v 1.3 2009-08-29 15:57:59 shilen Exp $
 */
package edu.internet2.middleware.grouper.member;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * test changing a member's subject id
 */
public class TestMemberChangeSubject extends GrouperTest {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(TestMemberChangeSubject.class);

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMemberChangeSubject("testChangeSubjectDidExistAudit"));
    //TestRunner.run(new TestMemberChangeSubject(""));
    //TestRunner.run(TestMemberChangeSubject.class);
  }
  
  /**
   * @param name
   */
  public TestMemberChangeSubject(String name) {
    super(name);
  }

  /**
   * grouper session 
   */
  private GrouperSession grouperSession;

  /** root grouper sesion */
  private GrouperSession rootGrouperSession;

  /**
   * edu stem 
   */
  private Stem edu;
  
  /**
   * test group 
   */
  private Group group;
  
  /**
   * test group 
   */
  private Group group2;
  
  /**
   * group type
   */
  private GroupType groupType;
  
  /**
   * test composite group 
   */
  private Group groupComposite;

  /**
   * subj0 membership
   */
  private Membership membershipSubj0;

  /**
   * composite
   */
  private Composite composite;
  
  /**
   * root stem 
   */
  private Stem root; 

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp() {
    super.setUp();
    try {
      RegistryReset.internal_resetRegistryAndAddTestSubjects();
      GrouperTest.initGroupsAndAttributes();

      rootGrouperSession = SessionHelper.getRootSession();
      root  = StemHelper.findRootStem(rootGrouperSession);
      
      //add a stem privilege
      root.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
      root.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
      root.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
      root.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
      
      grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
      edu   = StemHelper.addChildStem(root, "edu", "education");
      
      //make it modified
      edu.setDescription("Education");
      edu.store();
      
      edu.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
      group = StemHelper.addChildGroup(this.edu, "group", "the group");
      group.addMember(SubjectTestHelper.SUBJ0);
      //this group has subject0 and subject1 so we see that the subject0
      //membership is deleted since the renamed subejct exists
      group.addMember(SubjectTestHelper.SUBJ1);
      
      group2 = StemHelper.addChildGroup(this.edu, "group2", "group2");
      group2.addMember(SubjectTestHelper.SUBJ0);
      
      groupComposite = StemHelper.addChildGroup(this.edu, "groupComposite", "groupComposite");
      
      groupComposite.addCompositeMember(CompositeType.UNION, group, group2);
      
      composite = CompositeFinder.findAsOwner(groupComposite, true);
      
      membershipSubj0 = MembershipFinder.findImmediateMembership(grouperSession, 
          group, SubjectTestHelper.SUBJ0, Group.getDefaultList(), true);

      final Group sysadmingroup = Group.saveGroup(rootGrouperSession, null, 
          null, "etc:sysadmingroup", "sysadmingroup", "sysadmingroup", 
          SaveMode.INSERT, true);
      ApiConfig.testConfig.put("groups.wheel.use", "true");
      ApiConfig.testConfig.put("groups.wheel.group", "etc:sysadmingroup");
      
      GrouperSession.callbackGrouperSession(rootGrouperSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            sysadmingroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE);
            sysadmingroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          return null;
        }
        
      });
      sysadmingroup.addMember(SubjectTestHelper.SUBJ0);
      groupType = GroupType.createType(grouperSession, "groupType");
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * test when the member is changing subjects which did not exist as members
   * @throws Exception 
   */
  public void testChangeSubjectSameSubject() throws Exception {
    Member member0 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ0, true);
    String member0uuid = member0.getUuid();
    
    GrouperSession nonRootSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
    
    try {
      member0.changeSubject(SubjectTestHelper.SUBJ9);
      fail("Should throw exception");
    } catch (Exception e) {
      //good
    }

    GrouperSession.stopQuietly(nonRootSession);
    this.rootGrouperSession = GrouperSession.startRootSession();
    
    @SuppressWarnings("unused")
    String report = member0.changeSubjectReport(SubjectTestHelper.SUBJ0, true);
    int sameSubjects = Member.changeSubjectSameSubject;

    member0.changeSubject(SubjectTestHelper.SUBJ0);
    assertEquals("subject id should not change", member0.getSubjectId(), SubjectTestHelper.SUBJ0_ID);
    assertEquals("source id should not change", member0.getSubjectSourceId(), SubjectTestHelper.SUBJ0.getSource().getId());
    assertEquals("should have detected same subject", sameSubjects+1, Member.changeSubjectSameSubject);
    
    member0 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ0, true);
    
    assertEquals("subject uuid should not change", member0uuid, member0.getUuid());
  }
  
  /**
   * test when the member is changing subjects which did not exist as members
   * @throws Exception 
   */
  public void testChangeSubjectDidntExist() throws Exception {
    Member member0 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ0, true);
    
    //lets set the source so we can see it change
    member0.setSubjectSourceIdDb("abc");
    
    HibernateSession.byObjectStatic().saveOrUpdate(member0);
    
    String member0uuid = member0.getUuid();
    String eduStemCreateUuid = this.edu.getCreatorUuid();
    Member member2 = null;
    //lets delete this member
    member2 = MemberFinder.internal_findBySubject(SubjectTestHelper.SUBJ2, null, false);
    if (member2 != null) {
      HibernateSession.byObjectStatic().delete(member2);
    }

    //make sure member doesnt exist
    try {
      GrouperDAOFactory.getFactory().getMember().findBySubject(
          SubjectTestHelper.SUBJ2, true);
      fail("Should not find this member!");
    }catch (MemberNotFoundException mnfe) {
      //good
    }
    this.rootGrouperSession = GrouperSession.startRootSession();
    @SuppressWarnings("unused")
    String report = member0.changeSubjectReport(SubjectTestHelper.SUBJ2, true);
    int subjectsDidntExist = Member.changeSubjectDidntExist;
    member0.changeSubject(SubjectTestHelper.SUBJ2);
    
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);

    assertEquals("subject id should change", member0.getSubjectId(), SubjectTestHelper.SUBJ2_ID);
    assertEquals("source id should change", member0.getSubjectSourceId(), SubjectTestHelper.SUBJ2.getSource().getId());
    assertEquals("should have detected that it didnt change", subjectsDidntExist+1, Member.changeSubjectDidntExist);
    
    try {
      member0 = GrouperDAOFactory.getFactory().getMember().findBySubject(SubjectTestHelper.SUBJ0, true);
      fail("Should not find this subject anymore");
    } catch (MemberNotFoundException mnfe) {
      
    }
    
    member2 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ2, true);
    
    assertEquals("uuid should not have changed", member2.getUuid(), member0uuid);
    
    //refresh the stem
    this.edu = StemFinder.findByName(this.rootGrouperSession, "edu", true);
    
    assertEquals("the member uuid should not have changed", eduStemCreateUuid, this.edu.getCreatorUuid());
    
  }

  /**
   * test when the member is changing subjects which did not exist as members
   * @throws Exception 
   */
  public void testChangeSubjectDidExist() throws Exception {
    Member member0 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ0, true);
    
    //lets set the source so we can see it change
    member0.setSubjectSourceIdDb("abc");
    
    HibernateSession.byObjectStatic().saveOrUpdate(member0);
    
    String member0uuid = member0.getUuid();

    //grouper_composites.creator_id
    String compositeUuid = this.composite.getCreatorUuid();
    assertEquals("existing uuid", member0uuid, compositeUuid);
    
    //find each memberid of each case where it is used
    String eduStemCreateUuid = this.edu.getCreatorUuid();
    assertEquals("existing uuid", member0uuid, eduStemCreateUuid);
    
    //grouper_groups.creator_id, 
    //  modifier_id
    String groupCreatorId = this.group.getCreatorUuid();
    assertEquals("existing uuid", member0uuid, groupCreatorId);
    String groupModifierId = this.group.getModifierUuid();
    assertEquals("existing uuid", member0uuid, groupModifierId);
    
    //grouper_memberships.member_id, 
    //  creator_id
    String membershipMemberId = this.membershipSubj0.getMemberUuid();
    assertEquals("existing uuid", member0uuid, membershipMemberId);
    String membershipCreatorId = this.membershipSubj0.getCreatorUuid();
    assertEquals("existing uuid", member0uuid, membershipCreatorId);

    //grouper_stems.creator_id, 
    //  modifier_id
    String stemCreatorId = this.edu.getCreatorUuid();
    assertEquals("existing uuid", member0uuid, stemCreatorId);
    String stemModifierId = this.edu.getModifierUuid();
    assertEquals("existing uuid", member0uuid, stemModifierId);
    
    //grouper_types.creator_uuid
    String groupTypeId = this.groupType.getCreatorUuid();
    assertEquals("existing uuid", member0uuid, groupTypeId);

    Member member1 = GrouperDAOFactory.getFactory().getMember().findBySubject(
        SubjectTestHelper.SUBJ1, true);
    
    String member1uuid = member1.getUuid();
    
    this.rootGrouperSession = GrouperSession.startRootSession();
    
    @SuppressWarnings("unused")
    String report = member0.changeSubjectReport(SubjectTestHelper.SUBJ1, true);

    int subjectsExist = Member.changeSubjectExist;
    int subjectAddCount = Member.changeSubjectMembershipAddCount;
    int subjectDeleteCount = Member.changeSubjectMembershipDeleteCount;

    member0.changeSubject(SubjectTestHelper.SUBJ1);

    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);

    assertEquals("subject id should change", member0.getSubjectId(), SubjectTestHelper.SUBJ1_ID);
    assertEquals("source id should change", member0.getSubjectSourceId(), SubjectTestHelper.SUBJ1.getSource().getId());
    assertEquals("should have detected that it didnt change", subjectsExist+1, Member.changeSubjectExist);
    assertTrue("should have detected at least one delete", Member.changeSubjectMembershipDeleteCount > subjectDeleteCount+1);
    assertTrue("should have detected at least one update", Member.changeSubjectMembershipAddCount > subjectAddCount+1);

    try {
      member0 = GrouperDAOFactory.getFactory().getMember().findBySubject(SubjectTestHelper.SUBJ0, true);
      fail("Should not find this subject anymore");
    } catch (MemberNotFoundException mnfe) {
      //good
    }
    
    member1 = GrouperDAOFactory.getFactory().getMember().findBySubject(
        SubjectTestHelper.SUBJ1, true);
    
    assertEquals("uuid should not have changed", member1.getUuid(), member1uuid);
    assertTrue("uuid should not be the same", 
        !StringUtils.equals(member1.getUuid(), member0uuid));
      
    //grouper_composites.creator_id
    this.composite = CompositeFinder.findAsOwner(this.groupComposite, true);
    compositeUuid = this.composite.getCreatorUuid();
    assertEquals("new uuid", member1uuid, compositeUuid);
    
    //grouper_groups.creator_id, 
    //  modifier_id
    this.group = GroupFinder.findByName(this.grouperSession, this.group.getName(), true);
    groupCreatorId = this.group.getCreatorUuid();
    assertEquals("new uuid", member1uuid, groupCreatorId);
    groupModifierId = this.group.getModifierUuid();
    assertEquals("new uuid", member1uuid, groupModifierId);
    
    //grouper_memberships.member_id, 
    //  creator_id
    Membership membershipSubj1 = MembershipFinder.findImmediateMembership(grouperSession, 
        group, SubjectTestHelper.SUBJ1, Group.getDefaultList(), true);
    membershipMemberId = membershipSubj1.getMemberUuid();
    assertEquals("new uuid", member1uuid, membershipMemberId);
    membershipCreatorId = membershipSubj1.getCreatorUuid();
    assertEquals("new uuid", member1uuid, membershipCreatorId);

    //grouper_stems.creator_id, 
    //  modifier_id
    //refresh the stem
    this.edu = StemFinder.findByName(this.rootGrouperSession, "edu", true);
    stemCreatorId = this.edu.getCreatorUuid();
    assertEquals("new uuid", member1uuid, stemCreatorId);
    stemModifierId = this.edu.getModifierUuid();
    assertEquals("new uuid", member1uuid, stemModifierId);
    
    //grouper_types.creator_uuid
    this.groupType = GroupTypeFinder.find(this.groupType.getName(), true);
    groupTypeId = this.groupType.getCreatorUuid();
    assertEquals("new uuid", member1uuid, groupTypeId);
    
  }

  /**
   * test when the member is changing subjects which did not exist as members
   * @throws Exception 
   */
  public void testChangeSubjectDidExistAudit() throws Exception {
    
    Member member0 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ0, true);
    
    //lets set the source so we can see it change
    member0.setSubjectSourceIdDb("abc");
    
    HibernateSession.byObjectStatic().saveOrUpdate(member0);
    
    this.rootGrouperSession = GrouperSession.startRootSession();
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
    
    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");

    @SuppressWarnings("unused")
    String report = member0.changeSubjectReport(SubjectTestHelper.SUBJ1, true);
  
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("that shouldnt audit", auditCount, newAuditCount);
    
    member0.changeSubject(SubjectTestHelper.SUBJ1);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("that should audit", auditCount+1, newAuditCount);
    
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);

    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    
    assertEquals("Context id's should match", auditEntry.getContextId(), member0.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));

    //grouper_composites.creator_id
    this.composite = CompositeFinder.findAsOwner(this.groupComposite, true);
    assertEquals("Context id's should match", auditEntry.getContextId(), this.composite.getContextId());
    
    //grouper_groups.creator_id, 
    //  modifier_id
    this.group = GroupFinder.findByName(this.grouperSession, this.group.getName(), true);
    assertEquals("Context id's should match", auditEntry.getContextId(), this.group.getContextId());
    
    //grouper_memberships.member_id, 
    //  creator_id
    Membership membershipSubj1 = MembershipFinder.findImmediateMembership(grouperSession, 
        group, SubjectTestHelper.SUBJ1, Group.getDefaultList(), true);
    assertEquals("Context id's should match", auditEntry.getContextId(), membershipSubj1.getContextId());
  
    //grouper_stems.creator_id, 
    //  modifier_id
    //refresh the stem
    this.edu = StemFinder.findByName(this.rootGrouperSession, "edu", true);
    assertEquals("Context id's should match", auditEntry.getContextId(), this.edu.getContextId());
    
    //grouper_types.creator_uuid
    this.groupType = GroupTypeFinder.find(this.groupType.getName(), true);
    assertEquals("Context id's should match", auditEntry.getContextId(), this.edu.getContextId());
    
  }
  
}
