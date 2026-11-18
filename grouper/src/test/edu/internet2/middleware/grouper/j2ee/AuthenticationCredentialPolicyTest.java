package edu.internet2.middleware.grouper.j2ee;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordCredentialPolicy;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security fix 141 (basicAuthenticationIgnoresCredentialRestrictions): built-in Basic
 * authentication (Authentication.authenticate) must refuse a database-backed grouper_password
 * credential, even with the right password, when its expiration is in the past or the request is
 * not from one of its allowed_from_cidrs networks, also on an authentication-cache hit, and must
 * record the refusal as a failed recently-used attempt.  Changing only a Basic credential's
 * expiration through GrouperPasswordSave must persist (the compare was inverted).
 *
 * Before the fix: the expired and wrong-network cases return true, and the expiration-only edit
 * keeps the old value.
 * </pre>
 */
public class AuthenticationCredentialPolicyTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AuthenticationCredentialPolicyTest("testCacheHitStillChecked"));
  }

  /** */
  public AuthenticationCredentialPolicyTest() {
    super();
  }

  /**
   * @param name
   */
  public AuthenticationCredentialPolicyTest(String name) {
    super(name);
  }

  /** password for every test credential (no colon, see grouper.authentication.splitBasicAuthOnFirstColon) */
  private static final String PASS = "secPatch141Pass";

  /** an address inside 10.0.0.0/8 */
  private static final String IP_INSIDE = "10.1.2.3";

  /** an address outside 10.0.0.0/8 (TEST-NET-3) */
  private static final String IP_OUTSIDE = "203.0.113.40";

  /** one day */
  private static final long DAY = 24L * 60 * 60 * 1000;

  /** cache config key for UI */
  private static final String UI_CACHE = "grouper.authentication.UI.cache";

  /** root session */
  private GrouperSession rootSession;

  /** makes usernames unique in this JVM */
  private static int counter = 0;

  /** usernames to delete in tearDown */
  private Set<String> usernames = new HashSet<String>();

  @Override
  protected void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();
    // default: no authentication cache, so authenticate() reads the row every time
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(UI_CACHE, "false");
  }

  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(UI_CACHE);
    for (String username : this.usernames) {
      try {
        GrouperPassword grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword()
            .findByUsernameApplication(username, GrouperPassword.Application.UI.name());
        if (grouperPassword != null) {
          GrouperDAOFactory.getFactory().getGrouperPasswordRecentlyUsed().deleteGrouperPasswordRecentlyUsedEntries(grouperPassword.getId());
          new GrouperPasswordSave().assignUsername(username).assignApplication(GrouperPassword.Application.UI)
            .assignSaveMode(SaveMode.DELETE).save();
        }
      } catch (RuntimeException re) {
        System.out.println("could not clean up " + username + ": " + re);
      }
    }
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------- helpers

  /**
   * create a UI Basic credential
   * @param label part of the username
   * @param expiresAt null for none
   * @param allowedFromCidrs null for none
   * @return the username
   */
  private String createCredential(String label, Long expiresAt, String allowedFromCidrs) {
    String username = "secPatch141_" + label + "_" + System.currentTimeMillis() + "_" + (counter++);
    this.usernames.add(username);
    new GrouperPasswordSave().assignApplication(GrouperPassword.Application.UI)
      .assignUsername(username).assignPassword(PASS)
      .assignExpiresAt(expiresAt).assignAllowedFromCidrs(allowedFromCidrs).save();
    assertNotNull(row(username));
    return username;
  }

  /**
   * @param username
   * @return the row or null
   */
  private static GrouperPassword row(String username) {
    return GrouperDAOFactory.getFactory().getGrouperPassword()
        .findByUsernameApplication(username, GrouperPassword.Application.UI.name());
  }

  /**
   * authenticate like the UI filter does, inside an audited hibernate callback so the
   * recently-used row can be written on every release
   * @param username
   * @param password
   * @param ip
   * @return the result
   */
  private static boolean authn(String username, String password, final String ip) {
    final String header;
    try {
      header = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes("UTF-8"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return (Boolean)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {
          public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
            return new Authentication().authenticate(header, GrouperPassword.Application.UI, ip);
          }
        });
  }

  /**
   * number of recently used rows with this status
   * @param username
   * @param status S or F
   * @return count
   */
  private static int recentlyUsed(String username, char status) {
    Set<Character> statuses = new HashSet<Character>();
    statuses.add(status);
    return GrouperUtil.length(GrouperDAOFactory.getFactory().getGrouperPasswordRecentlyUsed()
        .findByGrouperPasswordIdAndStatus(row(username).getId(), statuses, null));
  }

  // ---------------------------------------------------------------- tests

  /**
   * valid credentials still work: no restrictions, a future expiration, an allowed network
   */
  public void testValidCredentialsAccepted() {

    String plain = createCredential("plain", null, null);
    assertTrue(authn(plain, PASS, IP_OUTSIDE));
    assertFalse("wrong password", authn(plain, PASS + "x", IP_OUTSIDE));

    String restricted = createCredential("ok", System.currentTimeMillis() + DAY, "10.0.0.0/8, 192.168.0.0/16");
    assertTrue(authn(restricted, PASS, IP_INSIDE));
    assertTrue(authn(restricted, PASS, "192.168.5.6"));
    assertEquals(2, recentlyUsed(restricted, 'S'));
  }

  /**
   * an expired credential is refused with the right password and recorded as a failed attempt
   */
  public void testExpiredRefused() {

    String expired = createCredential("expired", System.currentTimeMillis() - 60000L, null);
    assertFalse("expired credential must be refused", authn(expired, PASS, IP_INSIDE));
    assertEquals(1, recentlyUsed(expired, 'F'));
    assertEquals(0, recentlyUsed(expired, 'S'));

    String expiredInNetwork = createCredential("expiredNet", System.currentTimeMillis() - 60000L, "10.0.0.0/8");
    assertFalse("expired credential from an allowed network must be refused", authn(expiredInNetwork, PASS, IP_INSIDE));
  }

  /**
   * a credential used from outside its networks is refused; fail closed on no IP and bad CIDRs
   */
  public void testCidrNotMatchingRefused() {

    String restricted = createCredential("cidr", null, "10.0.0.0/8");
    assertFalse("outside the allowed network must be refused", authn(restricted, PASS, IP_OUTSIDE));
    assertEquals(1, recentlyUsed(restricted, 'F'));
    assertEquals(0, recentlyUsed(restricted, 'S'));
    // no requester IP (grouper_password_recently_used.ip_address is not null, so check the policy directly)
    assertEquals(GrouperPasswordCredentialPolicy.DENIED_REQUESTER_IP_MISSING,
        GrouperPasswordCredentialPolicy.deniedReason(row(restricted), null, System.currentTimeMillis()));
    assertNull(GrouperPasswordCredentialPolicy.deniedReason(row(restricted), IP_INSIDE, System.currentTimeMillis()));
    assertTrue("inside the allowed network", authn(restricted, PASS, IP_INSIDE));

    String badCidr = createCredential("badCidr", null, "notACidr");
    assertFalse("unparseable allowedFromCidrs must be refused", authn(badCidr, PASS, IP_INSIDE));
  }

  /**
   * with the UI authentication cache on, a cached password match is still checked against the
   * row: a different network is refused, and an expiration set after the first login applies
   */
  public void testCacheHitStillChecked() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(UI_CACHE, "true");

    String cached = createCredential("cache", System.currentTimeMillis() + DAY, "10.0.0.0/8");
    // first call fills the authentication cache, second is a cache hit
    assertTrue(authn(cached, PASS, IP_INSIDE));
    assertTrue(authn(cached, PASS, IP_INSIDE));
    assertFalse("cache hit from outside the allowed network must be refused", authn(cached, PASS, IP_OUTSIDE));

    // expire it (expiration only, like an edit screen) while the password is cached
    new GrouperPasswordSave().assignUuid(row(cached).getId()).assignSaveMode(SaveMode.UPDATE)
      .assignReplaceAllSettings(false).assignExpiresAt(System.currentTimeMillis() - 60000L).save();
    assertFalse("cache hit after expiration must be refused", authn(cached, PASS, IP_INSIDE));
    assertTrue(recentlyUsed(cached, 'F') >= 2);
  }

  /**
   * changing only the expiration of a Basic credential is saved, and clearing it saves null
   */
  public void testSaveExpirationOnly() {

    long first = System.currentTimeMillis() + DAY;
    String username = createCredential("save", first, null);
    String id = row(username).getId();
    assertEquals(Long.valueOf(first), row(username).getExpiresMillis());

    long second = first + DAY;
    new GrouperPasswordSave().assignUuid(id).assignSaveMode(SaveMode.UPDATE)
      .assignReplaceAllSettings(false).assignExpiresAt(second).save();
    assertEquals("changed expiration must persist", Long.valueOf(second),
        GrouperDAOFactory.getFactory().getGrouperPassword().findById(id, true).getExpiresMillis());

    new GrouperPasswordSave().assignUuid(id).assignSaveMode(SaveMode.UPDATE)
      .assignReplaceAllSettings(false).assignExpiresAt(null).save();
    assertNull("cleared expiration must persist",
        GrouperDAOFactory.getFactory().getGrouperPassword().findById(id, true).getExpiresMillis());

    // the password still works after the edits
    assertTrue(authn(username, PASS, IP_OUTSIDE));
  }

}
