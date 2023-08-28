package edu.internet2.middleware.grouper.stem;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.LoadData;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;


public class StemViewPrivilegeTest extends GrouperTest {

  public StemViewPrivilegeTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new StemViewPrivilegeTest("testPrivilegeAddedByGroupIncrementalPublic"));
  }
  
  @Override
  protected void setUp() {
    
    super.setUp();
    
    // these should be clear but make sure
    new GcDbAccess().sql("delete from grouper_stem_view_privilege").executeSql();
    new GcDbAccess().sql("delete from grouper_last_login").executeSql();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");


  }

  public void testPrivilegeAddedOnFolderCreateNotCalulatingForUser() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stemA = new StemSave().assignName("a").assignCreateParentStemsIfNotExist(true).save();
    
    stemA.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    
    GrouperSession.stopQuietly(grouperSession);

    // this user has not checked view privileges recently
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());

    new StemSave().assignName("a:b").save();
    
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());

    
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());

  }

  public void testPrivilegeAddedByGroupIncremental() {
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    long now = System.currentTimeMillis();
    GrouperUtil.sleep(100);
    
    Set<Stem> stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login gll, grouper_members gm where gll.member_uuid = gm.id "
        + " and gll.last_stem_view_need >= ?  and gll.last_stem_view_compute >= ? and gm.subject_source = ? and gm.subject_id = ?")
        .addBindVar(now).addBindVar(now)
        .addBindVar("jdbc").addBindVar(SubjectTestHelper.SUBJ0.getId()).select(int.class).intValue());
    
    assertEquals(0, stems.size());

    grouperSession = GrouperSession.startRootSession();

    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    Stem stemA = new StemSave().assignName("a").assignCreateParentStemsIfNotExist(true).save();
    
    Stem stemB = new StemSave().assignName("a:b").save();
    
    Group groupAb = new GroupSave().assignName("a:b:c").save();
    groupAb.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_stemViewPrivileges");

    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());

    stemB.grantPriv(groupAb.toSubject(), NamingPrivilege.CREATE, false);

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_stemViewPrivileges");

    assertNull((String)StemViewPrivilegeEsbListener.test_debugMapLast.get("exception"), StemViewPrivilegeEsbListener.test_debugMapLast.get("exception"));
    assertNull((String)StemViewPrivilegeEsbListener.test_debugMapLast.get("exception2"), StemViewPrivilegeEsbListener.test_debugMapLast.get("exception2"));

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
    Set<MultiKey> grouperStemViewPrivileges = GrouperUtil.multiKeySet(new GcDbAccess()
        .sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege").selectList(Object[].class));
    Set<MultiKey> grouperStemViewPrivilegesExpected = GrouperUtil.toSet(new MultiKey(member0.getId(), stemB.getId(), "S"));
    assertEqualsMultiKey(grouperStemViewPrivilegesExpected, grouperStemViewPrivileges);
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());
    
    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(3, stems.size());
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, ":", true)));
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, "a", true)));
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, "a:b", true)));

  }

  public void testPrivilegeAddedByGroupFull() {
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    long now = System.currentTimeMillis();
    GrouperUtil.sleep(100);
    
    Set<Stem> stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login gll, grouper_members gm where gll.member_uuid = gm.id "
        + " and gll.last_stem_view_need >= ?  and gll.last_stem_view_compute >= ? and gm.subject_source = ? and gm.subject_id = ?")
        .addBindVar(now).addBindVar(now)
        .addBindVar("jdbc").addBindVar(SubjectTestHelper.SUBJ0.getId()).select(int.class).intValue());
    
    assertEquals(0, stems.size());
    
    grouperSession = GrouperSession.startRootSession();

    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    Stem stemA = new StemSave().assignName("a").assignCreateParentStemsIfNotExist(true).save();
    
    Stem stemB = new StemSave().assignName("a:b").save();
    
    Group groupAb = new GroupSave().assignName("a:b:c").save();
    groupAb.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_stemViewPrivilegesFull");

    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());

    stemB.grantPriv(groupAb.toSubject(), NamingPrivilege.CREATE, false);

    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_stemViewPrivilegesFull");

    assertNull((String)StemViewPrivilegeFullDaemonLogic.test_debugMapLast.get("exception"), StemViewPrivilegeFullDaemonLogic.test_debugMapLast.get("exception"));
    assertNull((String)StemViewPrivilegeFullDaemonLogic.test_debugMapLast.get("exception2"), StemViewPrivilegeFullDaemonLogic.test_debugMapLast.get("exception2"));

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
    Set<MultiKey> grouperStemViewPrivileges = GrouperUtil.multiKeySet(new GcDbAccess()
        .sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege").selectList(Object[].class));
    Set<MultiKey> grouperStemViewPrivilegesExpected = GrouperUtil.toSet(new MultiKey(member0.getId(), stemB.getId(), "S"));
    assertEqualsMultiKey(grouperStemViewPrivilegesExpected, grouperStemViewPrivileges);
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());
    
    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(3, stems.size());
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, ":", true)));
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, "a", true)));
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, "a:b", true)));
  }

  public void testPrivilegeAddedOnFolderCreateYesCalulatingForUser() {

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    grouperSession = GrouperSession.startRootSession();

    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    Stem stemA = new StemSave().assignName("a").assignCreateParentStemsIfNotExist(true).save();
    
    stemA.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    
    GrouperSession.stopQuietly(grouperSession);

    // this user has not checked view privileges recently
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    Set<Stem> stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());
    Set<MultiKey> grouperStemViewPrivileges = GrouperUtil.multiKeySet(new GcDbAccess().sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege").selectList(Object[].class));
    Set<MultiKey> grouperStemViewPrivilegesExpected = GrouperUtil.toSet(
        new MultiKey(member0.getId(), stemA.getId(), "S")
        );
    assertEqualsMultiKey(grouperStemViewPrivilegesExpected, grouperStemViewPrivileges);

    Stem stemB = new StemSave().assignName("a:b").save();
    
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
    grouperStemViewPrivileges = GrouperUtil.multiKeySet(new GcDbAccess().sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege").selectList(Object[].class));
    grouperStemViewPrivilegesExpected = GrouperUtil.toSet(
        new MultiKey(member0.getId(), stemB.getId(), "S"),
        new MultiKey(member0.getId(), stemA.getId(), "S")
        );
    assertEqualsMultiKey(grouperStemViewPrivilegesExpected, grouperStemViewPrivileges);
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());

  }

  public void testRecalculateStemViewPrivilegesForUsers() {
    
    StemViewPrivilegeLogic.testingWaitForAttributes = true;
    try {
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());

      GrouperSession grouperSession = GrouperSession.startRootSession();
      Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
      Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
      
      new StemViewPrivilegeLogic().recalculateStemViewPrivilegesForUsers(GrouperUtil.toSet(member0.getId()));
      
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
      assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());
      
      Stem stemE = new StemSave().assignName("a:b:c:d:e").assignCreateParentStemsIfNotExist(true).save();
      Stem stemD = StemFinder.findByName(grouperSession, "a:b:c:d", true);
      Stem stemC = StemFinder.findByName(grouperSession, "a:b:c", true);
      Stem stemB = StemFinder.findByName(grouperSession, "a:b", true);
      Stem stemA = StemFinder.findByName(grouperSession, "a", true);
      
      Group groupE = new GroupSave().assignName("a:b:c:d:e:groupE").save();
      Group groupD = new GroupSave().assignName("a:b:c:d:groupD").save();

      AttributeDef attributeDefC = new AttributeDefSave().assignName("a:b:c:attributeDefC").assignAttributeDefType(AttributeDefType.attr).save();
      
      stemC.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
      groupE.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.OPTIN);
      attributeDefC.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_OPTOUT, false);
      
      new StemViewPrivilegeLogic().recalculateStemViewPrivilegesForUsers(GrouperUtil.toSet(member0.getId()));
      
      assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
      Set<MultiKey> grouperStemViewPrivileges = GrouperUtil.multiKeySet(new GcDbAccess().sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege").selectList(Object[].class));
      Set<MultiKey> grouperStemViewPrivilegesExpected = GrouperUtil.toSet(new MultiKey(member0.getId(), stemE.getId(), "G"),
          new MultiKey(member0.getId(), stemC.getId(), "S"),
          new MultiKey(member0.getId(), stemC.getId(), "A"));
      assertEqualsMultiKey(grouperStemViewPrivilegesExpected, grouperStemViewPrivileges);
      assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());

      // try another user
      stemD.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ATTR_READ);
      groupD.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
      attributeDefC.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_UPDATE, false);
      
      new StemViewPrivilegeLogic().recalculateStemViewPrivilegesForUsers(GrouperUtil.toSet(member0.getId(), member1.getId(), member2.getId()));
      
      assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
      grouperStemViewPrivileges = GrouperUtil.multiKeySet(new GcDbAccess().sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege").selectList(Object[].class));
      grouperStemViewPrivilegesExpected = GrouperUtil.toSet(
          new MultiKey(member0.getId(), stemE.getId(), "G"),
          new MultiKey(member0.getId(), stemC.getId(), "S"),
          new MultiKey(member0.getId(), stemC.getId(), "A"),
          new MultiKey(member2.getId(), stemD.getId(), "G"),
          new MultiKey(member1.getId(), stemD.getId(), "S"),
          new MultiKey(member1.getId(), stemC.getId(), "A")
          ) ;
      assertEqualsMultiKey(grouperStemViewPrivilegesExpected, grouperStemViewPrivileges);
      assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());

      stemD.revokePriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ATTR_READ);
      groupD.revokePriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
      attributeDefC.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_UPDATE, false);
      
      new StemViewPrivilegeLogic().recalculateStemViewPrivilegesForUsers(GrouperUtil.toSet(member0.getId(), member1.getId(), member2.getId()));
      
      assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
      grouperStemViewPrivileges = GrouperUtil.multiKeySet(new GcDbAccess().sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege").selectList(Object[].class));
      grouperStemViewPrivilegesExpected = GrouperUtil.toSet(
          new MultiKey(member0.getId(), stemE.getId(), "G"),
          new MultiKey(member0.getId(), stemC.getId(), "S"),
          new MultiKey(member0.getId(), stemC.getId(), "A")
          );
      assertEqualsMultiKey(grouperStemViewPrivilegesExpected, grouperStemViewPrivileges);
      assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());

    } finally {
      StemViewPrivilegeLogic.testingWaitForAttributes = false;
    }

  }

  public void testPrivilegeAddedOnStemViewCreateYesCalulatingForUser() {
  
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    grouperSession = GrouperSession.startRootSession();
  
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
  
    Stem stemA = new StemSave().assignName("a").assignCreateParentStemsIfNotExist(true).save();
    
    stemA.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_VIEW);
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
    
    Set<MultiKey> grouperStemViewPrivileges = GrouperUtil.multiKeySet(new GcDbAccess().sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege").selectList(Object[].class));
    Set<MultiKey> grouperStemViewPrivilegesExpected = GrouperUtil.toSet(new MultiKey(member0.getId(), stemA.getId(), "S"));
    assertEqualsMultiKey(grouperStemViewPrivilegesExpected, grouperStemViewPrivileges);
    
  }

  public void testPrivilegeAddedOnStemViewCreateNoCalulatingForUser() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
  
    MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
  
    Stem stemA = new StemSave().assignName("a").assignCreateParentStemsIfNotExist(true).save();
    
    stemA.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_VIEW);
    
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());
    
  }

  public void testPrivilegeAddedByGroupFullPublic() {
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    long now = System.currentTimeMillis();
    GrouperUtil.sleep(100);
    
    Set<Stem> stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login gll, grouper_members gm where gll.member_uuid = gm.id "
        + " and gll.last_stem_view_need >= ?  and gll.last_stem_view_compute >= ? and gm.subject_source = ? and gm.subject_id = ?")
        .addBindVar(now).addBindVar(now)
        .addBindVar("jdbc").addBindVar(SubjectTestHelper.SUBJ0.getId()).select(int.class).intValue());
    
    assertEquals(0, stems.size());
    
    grouperSession = GrouperSession.startRootSession();
  
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
  
    Stem stemA = new StemSave().assignName("a").assignCreateParentStemsIfNotExist(true).save();
    
    Stem stemB = new StemSave().assignName("a:b").save();
    
    Group groupAb = new GroupSave().assignName("a:b:c").save();
    groupAb.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_stemViewPrivilegesFull");
  
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege where stem_uuid in (?, ?)")
        .addBindVar(stemA.getId()).addBindVar(stemB.getId()).select(int.class).intValue());
    // test0 and grouperall
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());
  
    groupAb.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
  
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_stemViewPrivilegesFull");
  
    assertNull((String)StemViewPrivilegeFullDaemonLogic.test_debugMapLast.get("exception"), StemViewPrivilegeFullDaemonLogic.test_debugMapLast.get("exception"));
    assertNull((String)StemViewPrivilegeFullDaemonLogic.test_debugMapLast.get("exception2"), StemViewPrivilegeFullDaemonLogic.test_debugMapLast.get("exception2"));
  
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege where stem_uuid in (?, ?)")
        .addBindVar(stemA.getId()).addBindVar(stemB.getId()).select(int.class).intValue());
    Set<MultiKey> grouperStemViewPrivileges = GrouperUtil.multiKeySet(new GcDbAccess()
        .sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege where stem_uuid in (?, ?)")
        .addBindVar(stemA.getId()).addBindVar(stemB.getId()).selectList(Object[].class));
    Set<MultiKey> grouperStemViewPrivilegesExpected = GrouperUtil.toSet(new MultiKey(MemberFinder.internal_findAllMember().getId(), stemB.getId(), "G"));
    assertEqualsMultiKey(grouperStemViewPrivilegesExpected, grouperStemViewPrivileges);
    
    // test0 and grouperall
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());
    
    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertTrue(stems.size() >= 3);
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, ":", true)));
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, "a", true)));
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, "a:b", true)));
  }

  public void testPrivilegeAddedByGroupIncrementalPublic() {
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    long now = System.currentTimeMillis();
    GrouperUtil.sleep(100);
    
    Set<Stem> stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login gll, grouper_members gm where gll.member_uuid = gm.id "
        + " and gll.last_stem_view_need >= ?  and gll.last_stem_view_compute >= ? and gm.subject_source = ? and gm.subject_id = ?")
        .addBindVar(now).addBindVar(now)
        .addBindVar("jdbc").addBindVar(SubjectTestHelper.SUBJ0.getId()).select(int.class).intValue());
    
    assertEquals(0, stems.size());
  
    grouperSession = GrouperSession.startRootSession();
  
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
  
    Stem stemA = new StemSave().assignName("a").assignCreateParentStemsIfNotExist(true).save();
    
    Stem stemB = new StemSave().assignName("a:b").save();
    
    Group groupAb = new GroupSave().assignName("a:b:c").save();
    groupAb.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_stemViewPrivileges");
  
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege where stem_uuid in (?, ?)")
        .addBindVar(stemA.getId()).addBindVar(stemB.getId()).select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());
  
    groupAb.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
  
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_stemViewPrivileges");
  
    assertNull((String)StemViewPrivilegeEsbListener.test_debugMapLast.get("exception"), StemViewPrivilegeEsbListener.test_debugMapLast.get("exception"));
    assertNull((String)StemViewPrivilegeEsbListener.test_debugMapLast.get("exception2"), StemViewPrivilegeEsbListener.test_debugMapLast.get("exception2"));
  
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_stem_view_privilege where stem_uuid in (?, ?)")
        .addBindVar(stemA.getId()).addBindVar(stemB.getId()).select(int.class).intValue());
    Set<MultiKey> grouperStemViewPrivileges = GrouperUtil.multiKeySet(new GcDbAccess()
        .sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege where stem_uuid in (?, ?)")
        .addBindVar(stemA.getId()).addBindVar(stemB.getId()).selectList(Object[].class));
    Set<MultiKey> grouperStemViewPrivilegesExpected = GrouperUtil.toSet(new MultiKey(MemberFinder.internal_findAllMember().getId(), stemB.getId(), "G"));
    assertEqualsMultiKey(grouperStemViewPrivilegesExpected, grouperStemViewPrivileges);
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_last_login").select(int.class).intValue());
    
    stems = new StemFinder().assignSubject(SubjectTestHelper.SUBJ0).findStems();
    
    assertTrue(stems.size() >= 3);
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, ":", true)));
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, "a", true)));
    assertTrue(stems.contains(StemFinder.findByName(grouperSession, "a:b", true)));
  
  }
}

