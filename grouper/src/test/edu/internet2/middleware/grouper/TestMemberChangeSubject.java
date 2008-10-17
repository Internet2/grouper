/*
 * @author mchyzer
 * $Id: TestMemberChangeSubject.java,v 1.2 2008-10-17 12:06:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
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
    TestRunner.run(new TestMemberChangeSubject("testChangeSubjectDidExist"));
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
   * grouper sesion 
   */
  @SuppressWarnings("unused")
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
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  protected void setUp() {
    super.setUp();
    try {
      RegistryReset.internal_resetRegistryAndAddTestSubjects();
      rootGrouperSession = SessionHelper.getRootSession();
      root  = StemHelper.findRootStem(rootGrouperSession);
      
      //add a stem privilege
      root.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
      root.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
      root.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);
      root.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
      
      grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
      edu   = StemHelper.addChildStem(root, "edu", "education");
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
      
      composite = CompositeFinder.findAsOwner(groupComposite);
      
      membershipSubj0 = MembershipFinder.findImmediateMembership(grouperSession, 
          group, SubjectTestHelper.SUBJ0, Group.getDefaultList());

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
    Member member0 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ0);
    int sameSubjects = Member.changeSubjectSameSubject;
    String member0uuid = member0.getUuid();
    member0.changeSubject(SubjectTestHelper.SUBJ0);
    assertEquals("subject id should not change", member0.getSubjectId(), SubjectTestHelper.SUBJ0_ID);
    assertEquals("source id should not change", member0.getSubjectSourceId(), SubjectTestHelper.SUBJ0.getSource().getId());
    assertEquals("should have detected same subject", sameSubjects+1, Member.changeSubjectSameSubject);
    
    member0 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ0);
    
    assertEquals("subject uuid should not change", member0uuid, member0.getUuid());
  }
  
  /**
   * test when the member is changing subjects which did not exist as members
   * @throws Exception 
   */
  public void testChangeSubjectDidntExist() throws Exception {
    Member member0 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ0);
    
    //lets set the source so we can see it change
    member0.setSubjectSourceIdDb("abc");
    
    HibernateSession.byObjectStatic().saveOrUpdate(member0);
    
    int subjectsDidntExist = Member.changeSubjectDidntExist;
    String member0uuid = member0.getUuid();
    String eduStemCreateUuid = this.edu.getCreatorUuid();
    Member member2 = null;
    //lets delete this member
    try {
      member2 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ2);
      HibernateSession.byObjectStatic().delete(member2);
    }catch (MemberNotFoundException mnfe) {
      //good
    }

    //make sure member doesnt exist
    try {
      GrouperDAOFactory.getFactory().getMember().findBySubject(
          SubjectTestHelper.SUBJ2);
      fail("Should not find this member!");
    }catch (MemberNotFoundException mnfe) {
      //good
    }
    member0.changeSubject(SubjectTestHelper.SUBJ2);
    
    assertEquals("subject id should change", member0.getSubjectId(), SubjectTestHelper.SUBJ2_ID);
    assertEquals("source id should change", member0.getSubjectSourceId(), SubjectTestHelper.SUBJ2.getSource().getId());
    assertEquals("should have detected that it didnt change", subjectsDidntExist+1, Member.changeSubjectDidntExist);
    
    try {
      member0 = GrouperDAOFactory.getFactory().getMember().findBySubject(SubjectTestHelper.SUBJ0);
      fail("Should not find this subject anymore");
    } catch (MemberNotFoundException mnfe) {
      
    }
    
    member2 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ2);
    
    assertEquals("uuid should not have changed", member2.getUuid(), member0uuid);
    
    //refresh the stem
    this.edu = StemFinder.findByName(this.rootGrouperSession, "edu");
    
    assertEquals("the member uuid should not have changed", eduStemCreateUuid, this.edu.getCreatorUuid());
    
  }

  /**
   * test when the member is changing subjects which did not exist as members
   * @throws Exception 
   */
  public void testChangeSubjectDidExist() throws Exception {
    Member member0 = MemberFinder.findBySubject(this.rootGrouperSession, SubjectTestHelper.SUBJ0);
    
    //lets set the source so we can see it change
    member0.setSubjectSourceIdDb("abc");
    
    HibernateSession.byObjectStatic().saveOrUpdate(member0);
    
    int subjectsExist = Member.changeSubjectExist;
    int subjectAddCount = Member.changeSubjectMembershipAddCount;
    int subjectDeleteCount = Member.changeSubjectMembershipDeleteCount;
    
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
        SubjectTestHelper.SUBJ1);
    
    String member1uuid = member1.getUuid();
    
    member0.changeSubject(SubjectTestHelper.SUBJ1);
    
    assertEquals("subject id should change", member0.getSubjectId(), SubjectTestHelper.SUBJ1_ID);
    assertEquals("source id should change", member0.getSubjectSourceId(), SubjectTestHelper.SUBJ1.getSource().getId());
    assertEquals("should have detected that it didnt change", subjectsExist+1, Member.changeSubjectExist);
    assertTrue("should have detected at least one delete", Member.changeSubjectMembershipDeleteCount > subjectDeleteCount+1);
    assertTrue("should have detected at least one update", Member.changeSubjectMembershipAddCount > subjectAddCount+1);

    try {
      member0 = GrouperDAOFactory.getFactory().getMember().findBySubject(SubjectTestHelper.SUBJ0);
      fail("Should not find this subject anymore");
    } catch (MemberNotFoundException mnfe) {
      //good
    }
    
    member1 = GrouperDAOFactory.getFactory().getMember().findBySubject(
        SubjectTestHelper.SUBJ1);
    
    assertEquals("uuid should not have changed", member1.getUuid(), member1uuid);
    assertTrue("uuid should not be the same", 
        !StringUtils.equals(member1.getUuid(), member0uuid));
      
    //grouper_composites.creator_id
    this.composite = CompositeFinder.findAsOwner(this.groupComposite);
    compositeUuid = this.composite.getCreatorUuid();
    assertEquals("new uuid", member1uuid, compositeUuid);
    
    //grouper_groups.creator_id, 
    //  modifier_id
    this.group = GroupFinder.findByName(this.grouperSession, this.group.getName());
    groupCreatorId = this.group.getCreatorUuid();
    assertEquals("new uuid", member1uuid, groupCreatorId);
    groupModifierId = this.group.getModifierUuid();
    assertEquals("new uuid", member1uuid, groupModifierId);
    
    //grouper_memberships.member_id, 
    //  creator_id
    Membership membershipSubj1 = MembershipFinder.findImmediateMembership(grouperSession, 
        group, SubjectTestHelper.SUBJ1, Group.getDefaultList());
    membershipMemberId = membershipSubj1.getMemberUuid();
    assertEquals("new uuid", member1uuid, membershipMemberId);
    membershipCreatorId = membershipSubj1.getCreatorUuid();
    assertEquals("new uuid", member1uuid, membershipCreatorId);

    //grouper_stems.creator_id, 
    //  modifier_id
    //refresh the stem
    this.edu = StemFinder.findByName(this.rootGrouperSession, "edu");
    stemCreatorId = this.edu.getCreatorUuid();
    assertEquals("new uuid", member1uuid, stemCreatorId);
    stemModifierId = this.edu.getModifierUuid();
    assertEquals("new uuid", member1uuid, stemModifierId);
    
    //grouper_types.creator_uuid
    this.groupType = GroupTypeFinder.find(this.groupType.getName());
    groupTypeId = this.groupType.getCreatorUuid();
    assertEquals("new uuid", member1uuid, groupTypeId);
    
  }
  
}
