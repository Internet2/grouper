package edu.internet2.middleware.grouper.authentication;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <pre>
 * Advisory H7 (disabledLocalEntityJwtAuthentication): a self-service WS JWT must stop working as
 * soon as its local entity is disabled (flag or disabled date), deleted, or is not a local
 * entity at all.  Calls the public entry point GrouperPublicPrivateKeyJwt.decode() with real
 * signed tokens.
 *
 * The caches in GrouperPublicPrivateKeyJwt are deliberately NOT cleared between steps: the
 * entity check must be uncached, so the denial has to happen while the credential and subject
 * are still cached.
 *
 * Before the fix every "denied" case below returns the subject.
 * </pre>
 */
public class GrouperPublicPrivateKeyJwtDisabledEntityTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperPublicPrivateKeyJwtDisabledEntityTest("testDisabledFlagDenied"));
  }

  /** */
  public GrouperPublicPrivateKeyJwtDisabledEntityTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperPublicPrivateKeyJwtDisabledEntityTest(String name) {
    super(name);
  }

  /** root session */
  private GrouperSession grouperSession;

  @Override
  protected void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.startRootSession();
    GrouperPublicPrivateKeyJwt.clearCache();
  }

  @Override
  protected void tearDown() {
    GrouperPublicPrivateKeyJwt.clearCache();
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------- helpers

  /**
   * signs with a generated key pair
   */
  private static class KeyProvider implements RSAKeyProvider {

    /** public key base64 */
    private final String publicKeyString;

    /** private key base64 */
    private final String privateKeyString;

    /**
     * @param keyPair from GrouperUtil.generateRsaKeypair
     */
    KeyProvider(String[] keyPair) {
      this.publicKeyString = keyPair[0];
      this.privateKeyString = keyPair[1];
    }

    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
      try {
        return (RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(
            new X509EncodedKeySpec(Base64.decodeBase64(this.publicKeyString)));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
      try {
        return (RSAPrivateKey)KeyFactory.getInstance("RSA").generatePrivate(
            new PKCS8EncodedKeySpec(Base64.decodeBase64(this.privateKeyString)));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public String getPrivateKeyId() {
      return null;
    }
  }

  /** a credential holder and its signing key */
  private static class Holder {
    /** member id of the credential holder */
    String memberId;
    /** signs tokens */
    Algorithm algorithm;
  }

  /**
   * make a local entity
   * @param name
   * @return the entity group
   */
  private Group newEntity(String name) {
    return new GroupSave(this.grouperSession).assignName(name).assignTypeOfGroup(TypeOfGroup.entity)
        .assignCreateParentStemsIfNotExist(true).save();
  }

  /**
   * store a WS JWT key the way the local entity UI does (UiV2LocalEntity)
   * @param subject credential holder
   * @param entityType entity type to store, normally localEntity
   * @return the holder
   */
  private Holder newCredential(Subject subject, String entityType) {
    Member member = MemberFinder.findBySubject(this.grouperSession, subject, true);
    String[] keyPair = GrouperUtil.generateRsaKeypair(2048);
    new GrouperPasswordSave()
      .assignAllowedFromCidrs("0.0.0.0/0")
      .assignApplication(GrouperPassword.Application.WS)
      .assignEncryptionType(GrouperPassword.EncryptionType.RS_2048)
      .assignEntityType("localEntity")
      .assignExpiresAt(System.currentTimeMillis() + 24L * 60 * 60 * 1000)
      .assignMemberIdWhoSetPassword(this.grouperSession.getMember().getId())
      .assignPublicKey(keyPair[0])
      .assignUsername(member.getId())
      .assignMemberId(member.getId())
      .save();
    // GrouperPasswordSave refuses any other entity type for a public key, so a row with another
    // type (legacy data, or written directly to the db) is simulated with sql
    if (!"localEntity".equals(entityType)) {
      HibernateSession.bySqlStatic().executeSql(
          "update grouper_password set entity_type = ? where username = ? and application = ?",
          GrouperUtil.toListObject(entityType, member.getId(), GrouperPassword.Application.WS.name()),
          HibUtils.listType(StringType.INSTANCE, StringType.INSTANCE, StringType.INSTANCE));
    }
    Holder holder = new Holder();
    holder.memberId = member.getId();
    holder.algorithm = Algorithm.RSA256(new KeyProvider(keyPair));
    return holder;
  }

  /**
   * sign a fresh token and authenticate
   * @param holder
   * @return subject or null if denied
   */
  private static Subject authenticate(Holder holder) {
    try {
      String jwt = JWT.create().withIssuedAt(new Date()).sign(holder.algorithm);
      String b64 = new String(new Base64().encode(holder.memberId.getBytes("UTF-8")), "UTF-8");
      return new GrouperPublicPrivateKeyJwt().assignBearerTokenHeader("Bearer jwtUser_" + b64 + "_" + jwt)
          .decode("127.0.0.1");
    } catch (java.io.UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * set the entity timestamps / flag in the database directly, as if the enabled/disabled
   * daemon had not run yet (the Group setters would also flip the enabled flag)
   * @param group
   * @param enabled T or F
   * @param enabledTime millis or null
   * @param disabledTime millis or null
   */
  private static void setEntityStateInDb(Group group, String enabled, Long enabledTime, Long disabledTime) {
    HibernateSession.bySqlStatic().executeSql(
        "update grouper_groups set enabled = ?, enabled_timestamp = ?, disabled_timestamp = ? where id = ?",
        GrouperUtil.toListObject(enabled, enabledTime, disabledTime, group.getId()),
        HibUtils.listType(StringType.INSTANCE, LongType.INSTANCE, LongType.INSTANCE, StringType.INSTANCE));
  }

  /**
   * @param holder
   * @param status S, F or E
   * @return number of recently-used rows with that status for the holder's credential
   */
  private static int recentlyUsedCount(Holder holder, char status) {
    GrouperPassword grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword()
        .findByUsernameApplication(holder.memberId, GrouperPassword.Application.WS.name());
    Set<GrouperPasswordRecentlyUsed> rows = GrouperDAOFactory.getFactory().getGrouperPasswordRecentlyUsed()
        .findByGrouperPasswordIdAndStatus(grouperPassword.getId(), GrouperUtil.toSet(status), null);
    return GrouperUtil.length(rows);
  }

  // ---------------------------------------------------------------- tests

  /**
   * enabled entity works; disabling it (flag and date, the way the UI saves it) denies the next
   * request, while the subject is still cached, and records a failed use
   */
  public void testDisabledFlagDenied() {
    Group entity = newEntity("test108:entityA");
    Holder holder = newCredential(entity.toSubject(), "localEntity");

    Subject subject = authenticate(holder);
    assertNotNull("enabled local entity should authenticate", subject);
    assertEquals(entity.getId(), subject.getId());
    int failedBefore = recentlyUsedCount(holder, 'F');

    entity = GroupFinder.findByUuid(this.grouperSession, entity.getId(), true);
    entity.setDisabledTime(new Timestamp(System.currentTimeMillis() - 60000));
    entity.store();

    assertNull("disabled local entity must not authenticate", authenticate(holder));
    assertEquals("denied request is recorded as a failure", failedBefore + 1, recentlyUsedCount(holder, 'F'));
  }

  /**
   * disabled date already passed but the enabled flag still T (daemon not run): denied.
   * enabled flag F with no dates: denied.
   */
  public void testDisabledDateWithoutDaemonDenied() {
    Group entity = newEntity("test108:entityB");
    Holder holder = newCredential(entity.toSubject(), "localEntity");
    assertNotNull(authenticate(holder));

    setEntityStateInDb(entity, "T", null, System.currentTimeMillis() - 60000);
    assertNull("past disabled date must deny even if the flag is still enabled", authenticate(holder));

    setEntityStateInDb(entity, "F", null, null);
    assertNull("enabled flag F must deny", authenticate(holder));

    setEntityStateInDb(entity, "T", null, null);
    assertNotNull("re-enabled entity authenticates again", authenticate(holder));
  }

  /**
   * future disabled date: works before the boundary, denied after it with no daemon run.
   * future enabled date: denied.
   * @throws Exception
   */
  public void testFutureDates() throws Exception {
    Group entity = newEntity("test108:entityC");
    Holder holder = newCredential(entity.toSubject(), "localEntity");

    setEntityStateInDb(entity, "T", null, System.currentTimeMillis() + 3000);
    assertNotNull("before the disabled date the entity authenticates", authenticate(holder));
    Thread.sleep(4000);
    assertNull("after the disabled date the entity is denied", authenticate(holder));

    setEntityStateInDb(entity, "T", System.currentTimeMillis() + 60000, null);
    assertNull("entity not yet enabled is denied", authenticate(holder));
  }

  /**
   * deleted entity: denied (even while its subject is still in the decode cache)
   */
  public void testDeletedEntityDenied() {
    Group entity = newEntity("test108:entityD");
    Holder holder = newCredential(entity.toSubject(), "localEntity");
    assertNotNull(authenticate(holder));

    GroupFinder.findByUuid(this.grouperSession, entity.getId(), true).delete();

    Subject subject = null;
    try {
      subject = authenticate(holder);
    } catch (RuntimeException re) {
      // an exception is also a denial (the subject could not be resolved)
    }
    assertNull("deleted local entity must not authenticate", subject);
  }

  /**
   * credentials whose holder is not a local entity, or whose entity type is not localEntity,
   * are denied
   */
  public void testNonEntityCredentialsDenied() {
    // a person
    Holder person = newCredential(SubjectTestHelper.SUBJ0, "localEntity");
    assertNull("a person is not a local entity", authenticate(person));

    // a normal group (g:gsa source)
    Group group = new GroupSave(this.grouperSession).assignName("test108:normalGroup")
        .assignCreateParentStemsIfNotExist(true).save();
    Holder groupHolder = newCredential(group.toSubject(), "localEntity");
    assertNull("a normal group is not a local entity", authenticate(groupHolder));

    // a local entity, but the credential row is not a localEntity key
    Group entity = newEntity("test108:entityE");
    Holder wrongType = newCredential(entity.toSubject(), "person");
    assertNull("credential entity type must be localEntity", authenticate(wrongType));
  }

}
