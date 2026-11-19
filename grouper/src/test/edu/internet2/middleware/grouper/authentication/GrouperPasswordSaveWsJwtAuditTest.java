package edu.internet2.middleware.grouper.authentication;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * <pre>
 * Advisory H7 (wsJwtKeyCreateDeleteNotAudited): creating, editing, rotating and deleting a local
 * entity's WS JWT key must write an audit entry naming the subject who did it (not root), and
 * the delete must go through GrouperPasswordSave (access check, recently used rows removed).
 *
 * Also covers the folded-in canAccessWsJwtKeys bug: with
 * grouper.selfService.jwt.groupNameAllowedToManage set, an entity admin who is in that group
 * must be allowed (it was always denied), and a missing entity must be a clean false (it threw
 * a NullPointerException).
 *
 * Before the fix: no audit rows, the DELETE does not reach the public key path or remove the
 * recently used rows, the manage group case is always false, and the missing entity NPEs.
 * </pre>
 */
public class GrouperPasswordSaveWsJwtAuditTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperPasswordSaveWsJwtAuditTest("testCreateUpdateDeleteAudited"));
  }

  /** */
  public GrouperPasswordSaveWsJwtAuditTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperPasswordSaveWsJwtAuditTest(String name) {
    super(name);
  }

  /** root session */
  private GrouperSession rootSession;

  /** local entity, SUBJ0 is ADMIN */
  private Group localEntity;

  /** member id of the local entity */
  private String localEntityMemberId;

  @Override
  protected void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();
    this.localEntity = new GroupSave(this.rootSession).assignName("test:jwtAuditEntity")
        .assignTypeOfGroup(TypeOfGroup.entity).assignCreateParentStemsIfNotExist(true).save();
    this.localEntity.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    this.localEntityMemberId = MemberFinder.findBySubject(this.rootSession, this.localEntity.toSubject(), true).getId();
  }

  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.selfService.jwt.groupNameAllowedToManage");
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------- helpers

  /**
   * audit entries about WS JWT keys for this password row
   * @param passwordId
   * @return the entries
   */
  private static List<AuditEntry> jwtAudits(String passwordId) {
    return HibernateSession.byHqlStatic()
        .createQuery("from AuditEntry ae where ae.description like :theDescription")
        .setString("theDescription", "%WS JWT key%passwordId: " + passwordId + "%")
        .list(AuditEntry.class);
  }

  /**
   * count audit entries with a description prefix
   * @param audits
   * @param prefix e.g. Created
   * @return count
   */
  private static int countWithPrefix(List<AuditEntry> audits, String prefix) {
    int count = 0;
    for (AuditEntry auditEntry : GrouperUtil.nonNull(audits)) {
      if (auditEntry.getDescription() != null && auditEntry.getDescription().startsWith(prefix + " WS JWT key")) {
        count++;
      }
    }
    return count;
  }

  /**
   * number of recently used rows for a password
   * @param passwordId
   * @return count
   */
  private static long recentlyUsedCount(String passwordId) {
    return HibernateSession.byHqlStatic()
        .createQuery("select count(*) from GrouperPasswordRecentlyUsed r where r.grouperPasswordId = :thePasswordId")
        .setString("thePasswordId", passwordId).uniqueResult(Long.class);
  }

  /**
   * create a key as SUBJ0 the way the UI does
   * @return the password id
   */
  private String createKeyAsSubj0() {
    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      new GrouperPasswordSave()
        .assignAllowedFromCidrs("10.0.0.0/8")
        .assignApplication(GrouperPassword.Application.WS)
        .assignEncryptionType(GrouperPassword.EncryptionType.RS_2048)
        .assignEntityType("localEntity")
        .assignExpiresAt(System.currentTimeMillis() + 24L * 60 * 60 * 1000)
        .assignPublicKey(GrouperUtil.generateRsaKeypair(2048)[0])
        .assignUsername(this.localEntityMemberId)
        .assignMemberId(this.localEntityMemberId)
        .save();
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
    GrouperPassword grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword()
        .findByUsernameApplication(this.localEntityMemberId, GrouperPassword.Application.WS.name());
    assertNotNull(grouperPassword);
    return grouperPassword.getId();
  }

  // ---------------------------------------------------------------- tests

  /**
   * create, edit, rotate and delete as a non-wheel entity admin: each writes one audit entry
   * that names SUBJ0, and the delete removes the recently used rows
   */
  public void testCreateUpdateDeleteAudited() {

    Member subj0Member = MemberFinder.findBySubject(this.rootSession, SubjectTestHelper.SUBJ0, true);
    String rootMemberId = this.rootSession.getMember().getId();

    // create
    String passwordId = createKeyAsSubj0();
    List<AuditEntry> audits = jwtAudits(passwordId);
    assertEquals(1, countWithPrefix(audits, "Created"));
    AuditEntry created = audits.get(0);
    assertTrue(created.getDescription(), created.getDescription().contains(this.localEntity.getName()));
    assertTrue(created.getDescription(), created.getDescription().contains("10.0.0.0/8"));
    // the actor is SUBJ0, never root
    String actor = GrouperUtil.defaultIfBlank(created.getActAsMemberId(), created.getLoggedInMemberId());
    assertEquals(subj0Member.getId(), actor);
    assertFalse(rootMemberId.equals(actor));

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {

      // partial edit like UiV2LocalEntity.editWsJwtKeySubmit: no public key, no member id
      Long newExpires = System.currentTimeMillis() + 2L * 24 * 60 * 60 * 1000;
      new GrouperPasswordSave()
        .assignAllowedFromCidrs("192.168.0.0/16")
        .assignExpiresAt(newExpires)
        .assignUuid(passwordId)
        .assignSaveMode(SaveMode.UPDATE)
        .assignReplaceAllSettings(false)
        .save();
      GrouperPassword edited = GrouperDAOFactory.getFactory().getGrouperPassword().findById(passwordId, true);
      assertEquals("192.168.0.0/16", edited.getAllowedFromCidrs());
      // the inverted compare used to drop a changed expiration
      assertEquals(newExpires, edited.getExpiresMillis());
      assertEquals(1, countWithPrefix(jwtAudits(passwordId), "Updated"));

      // rotate like UiV2LocalEntity.deleteWsJwtKeyCreateNew
      new GrouperPasswordSave()
        .assignPublicKey(GrouperUtil.generateRsaKeypair(2048)[0])
        .assignSaveMode(SaveMode.UPDATE)
        .assignMemberId(this.localEntityMemberId)
        .assignReplaceAllSettings(false)
        .assignUuid(passwordId)
        .save();
      assertEquals(1, countWithPrefix(jwtAudits(passwordId), "Updated (new public key)"));

      // a recently used row, as written by WS JWT authentication
      GrouperPasswordRecentlyUsed recentlyUsed = new GrouperPasswordRecentlyUsed();
      recentlyUsed.setId(GrouperUuid.getUuid());
      recentlyUsed.setGrouperPasswordId(passwordId);
      recentlyUsed.setAttemptMillis(System.currentTimeMillis());
      recentlyUsed.setIpAddress("10.1.2.3");
      recentlyUsed.setStatus('S');
      GrouperDAOFactory.getFactory().getGrouperPasswordRecentlyUsed().saveOrUpdate(recentlyUsed);
      assertEquals(1L, recentlyUsedCount(passwordId));

      // delete like UiV2LocalEntity.deleteWsJwtKey after the fix
      new GrouperPasswordSave()
        .assignUuid(passwordId)
        .assignMemberId(this.localEntityMemberId)
        .assignSaveMode(SaveMode.DELETE)
        .save();
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }

    assertNull(GrouperDAOFactory.getFactory().getGrouperPassword().findById(passwordId, false));
    assertEquals(0L, recentlyUsedCount(passwordId));
    audits = jwtAudits(passwordId);
    assertEquals(1, countWithPrefix(audits, "Deleted"));
    for (AuditEntry auditEntry : audits) {
      assertEquals(subj0Member.getId(), GrouperUtil.defaultIfBlank(auditEntry.getActAsMemberId(), auditEntry.getLoggedInMemberId()));
      // never key material in the description
      assertFalse(auditEntry.getDescription(), auditEntry.getDescription().contains("MII"));
    }
  }

  /**
   * a subject without ADMIN on the entity cannot delete the key, and nothing is audited;
   * an admin of another entity cannot delete this entity's key by uuid
   */
  public void testDeleteDeniedNotAudited() {

    String passwordId = createKeyAsSubj0();

    // SUBJ1 has no privileges on the entity
    GrouperSession subj1Session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      new GrouperPasswordSave()
        .assignUuid(passwordId)
        .assignMemberId(this.localEntityMemberId)
        .assignSaveMode(SaveMode.DELETE)
        .save();
      fail("SUBJ1 should not be able to delete the key");
    } catch (RuntimeException re) {
      // expected
    } finally {
      GrouperSession.stopQuietly(subj1Session);
    }

    // SUBJ1 admins another entity and passes that member id with this key's uuid
    Group otherEntity = new GroupSave(this.rootSession).assignName("test:jwtAuditOtherEntity")
        .assignTypeOfGroup(TypeOfGroup.entity).assignCreateParentStemsIfNotExist(true).save();
    otherEntity.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    String otherMemberId = MemberFinder.findBySubject(this.rootSession, otherEntity.toSubject(), true).getId();
    subj1Session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      new GrouperPasswordSave()
        .assignUuid(passwordId)
        .assignMemberId(otherMemberId)
        .assignSaveMode(SaveMode.DELETE)
        .save();
      fail("SUBJ1 should not be able to delete another entity's key by uuid");
    } catch (RuntimeException re) {
      // expected
    } finally {
      GrouperSession.stopQuietly(subj1Session);
    }

    assertNotNull(GrouperDAOFactory.getFactory().getGrouperPassword().findById(passwordId, false));
    assertEquals(0, countWithPrefix(jwtAudits(passwordId), "Deleted"));
  }

  /**
   * folded-in bug: with the manage group set, an entity admin in the group is allowed, one not
   * in the group is denied, and a missing entity is a clean false
   */
  public void testCanAccessWsJwtKeysWithManageGroup() {

    Group manageGroup = new GroupSave(this.rootSession).assignName("test:jwtManagers")
        .assignCreateParentStemsIfNotExist(true).save();
    manageGroup.addMember(SubjectTestHelper.SUBJ0, false);
    this.localEntity.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.selfService.jwt.groupNameAllowedToManage", manageGroup.getName());

    // SUBJ0: admin and in the group (was always false before the fix)
    assertTrue(GrouperPasswordSave.canAccessWsJwtKeys(SubjectTestHelper.SUBJ0, this.localEntity.toSubject()));
    // SUBJ1: admin, not in the group
    assertFalse(GrouperPasswordSave.canAccessWsJwtKeys(SubjectTestHelper.SUBJ1, this.localEntity.toSubject()));
    // SUBJ2: neither
    assertFalse(GrouperPasswordSave.canAccessWsJwtKeys(SubjectTestHelper.SUBJ2, this.localEntity.toSubject()));

    // a subject that is not a group (no local entity found): false, not a NullPointerException
    assertFalse(GrouperPasswordSave.canAccessWsJwtKeys(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ3));

    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.selfService.jwt.groupNameAllowedToManage");

    // blank setting: admin is enough; missing entity is still false, not an NPE
    assertTrue(GrouperPasswordSave.canAccessWsJwtKeys(SubjectTestHelper.SUBJ1, this.localEntity.toSubject()));
    assertFalse(GrouperPasswordSave.canAccessWsJwtKeys(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ3));
  }

}
