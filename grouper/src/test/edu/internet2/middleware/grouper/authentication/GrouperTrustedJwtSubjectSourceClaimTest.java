package edu.internet2.middleware.grouper.authentication;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security issue trustedJwtSubjectSourceClaim: a trusted JWT's subjectSourceId claim used to
 * replace the configured grouper.jwt.trusted.&lt;configId&gt;.subjectSourceIds (so a token could
 * name g:isa and log in as GrouperSystem), and it did so by changing the live set of the cached
 * config, so later tokens on the same config were looked up in the attacker's source too.
 *
 * The test makes its own RSA key pair, configures a trusted JWT config with that public key, and
 * signs tokens with the private key, so it does not depend on a fixed pre-signed token.
 */
public class GrouperTrustedJwtSubjectSourceClaimTest extends GrouperTest {

  /** config id used by this test */
  private static final String CONFIG_ID = "testCfgSourceClaim";

  /** property prefix of this test's config */
  private static final String PREFIX = "grouper.jwt.trusted." + CONFIG_ID + ".";

  /** properties this test stores, deleted again in tearDown */
  private static final String[] PROPERTY_SUFFIXES = new String[] {
      "enabled", "key.0.publicKey", "key.0.encryptionType", "subjectSourceIds", "subjectIdType"};

  /** private key that signs the tokens */
  private RSAPrivateKey privateKey;

  /** matching public key */
  private RSAPublicKey publicKey;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperTrustedJwtSubjectSourceClaimTest("testSubjectSourceClaimCannotExceedConfig"));
  }

  /**
   *
   */
  public GrouperTrustedJwtSubjectSourceClaimTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperTrustedJwtSubjectSourceClaimTest(String name) {
    super(name);
  }

  /**
   * make a key pair and store the trusted jwt config
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    // GrouperDbConfig.store needs an open session on 4.x (GrouperDbConfig.seeIfAllowed); harmless later
    GrouperSession.startRootSession();
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
      this.publicKey = (RSAPublicKey) keyPair.getPublic();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    storeProperty("enabled", "true");
    // X.509 SubjectPublicKeyInfo, base64, the same format as the existing GrouperTrustedJwtTest key
    storeProperty("key.0.publicKey", Base64.getEncoder().encodeToString(this.publicKey.getEncoded()));
    storeProperty("key.0.encryptionType", "RS-256");
    // leave subjectIdType blank: then decode reads the "subjectId" claim. subjectIdType=subjectId
    // without subjectIdClaimName makes GrouperTrustedJwt.decode read no subject claim at all
    // (pre-existing behavior, not part of this fix), so every token would resolve to null
    storeProperty("subjectSourceIds", "jdbc");
    GrouperTrustedJwtConfig.clearCache();
  }

  /**
   * remove the config again
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(PREFIX + "subjectSourceIds");
    for (String suffix : PROPERTY_SUFFIXES) {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName(PREFIX + suffix).delete();
    }
    GrouperTrustedJwtConfig.clearCache();
    super.tearDown();
  }

  /**
   * store one property of this test's config
   * @param suffix after the prefix
   * @param value
   */
  private static void storeProperty(String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper.properties").propertyName(PREFIX + suffix).value(value).store();
  }

  /**
   * sign a token
   * @param subjectId subjectId claim
   * @param subjectSourceId subjectSourceId claim, or null for none
   * @return the bearer header value
   */
  private String bearer(String subjectId, String subjectSourceId) {
    long now = System.currentTimeMillis();
    JWTCreator.Builder builder = JWT.create()
        .withClaim("subjectId", subjectId)
        .withIssuedAt(new Date(now))
        .withExpiresAt(new Date(now + 300 * 1000L));
    if (subjectSourceId != null) {
      builder.withClaim("subjectSourceId", subjectSourceId);
    }
    String jwt = builder.sign(Algorithm.RSA256(this.publicKey, this.privateKey));
    return "Bearer jwtTrusted_" + CONFIG_ID + "_" + jwt;
  }

  /**
   * decode a token as WS would
   * @param bearerHeader
   * @return the subject or null
   */
  private static Subject decode(String bearerHeader) {
    return new GrouperTrustedJwt().assignBearerTokenHeader(bearerHeader).decode();
  }

  /**
   * a claim outside the configured sources is refused and does not change the cached config
   */
  public void testSubjectSourceClaimCannotExceedConfig() {

    // sanity: the key pair and config work for an ordinary token
    Subject subject = decode(bearer(SubjectTestHelper.SUBJ0_ID, null));
    assertNotNull(subject);
    assertEquals(SubjectTestHelper.SUBJ0_ID, subject.getId());
    assertEquals("jdbc", subject.getSourceId());

    // the attack: a signed token names g:isa to become GrouperSystem
    GrouperTrustedJwt grouperTrustedJwt = new GrouperTrustedJwt().assignBearerTokenHeader(bearer("GrouperSystem", "g:isa"));
    assertNull("subjectSourceId claim outside the configured sources must be refused", grouperTrustedJwt.decode());
    assertEquals(GrouperTrustedJwtResult.ERROR_TOKEN_INVALID, grouperTrustedJwt.getGrouperTrustedJwtResult());

    // the cached config was not changed by the claim (before the fix it became {g:isa})
    assertEquals(GrouperUtil.toSet("jdbc"),
        new HashSet<String>(GrouperTrustedJwtConfig.retrieveFromConfigOrCache(CONFIG_ID).getSubjectSourceIds()));

    // so a later token without the claim, inside the same cache minute, still resolves in jdbc
    subject = decode(bearer(SubjectTestHelper.SUBJ0_ID, null));
    assertNotNull("later token must still be looked up in the configured source", subject);
    assertEquals("jdbc", subject.getSourceId());

    // a claim inside the ceiling still narrows the lookup and works
    subject = decode(bearer(SubjectTestHelper.SUBJ0_ID, "jdbc"));
    assertNotNull(subject);
    assertEquals(SubjectTestHelper.SUBJ0_ID, subject.getId());
  }

  /**
   * with no configured sources there is no ceiling: unchanged behavior, the claim picks the source
   */
  public void testNoConfiguredSourcesClaimStillPicksSource() {
    new GrouperDbConfig().configFileName("grouper.properties").propertyName(PREFIX + "subjectSourceIds").delete();
    // older releases (6.x and earlier) do not refresh the database config cache in this JVM right
    // after a delete, so also blank it in memory; tearDown removes the override
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "subjectSourceIds", "");
    GrouperTrustedJwtConfig.clearCache();

    GrouperTrustedJwt grouperTrustedJwt = new GrouperTrustedJwt().assignBearerTokenHeader(bearer("GrouperSystem", "g:isa"));
    Subject subject = grouperTrustedJwt.decode();
    assertNotNull("no subject; decode result: " + grouperTrustedJwt.getGrouperTrustedJwtResult()
        + ", config subjectSourceIds: " + GrouperTrustedJwtConfig.retrieveFromConfigOrCache(CONFIG_ID).getSubjectSourceIds(), subject);
    assertEquals("g:isa", subject.getSourceId());

    // still no change to the cached config
    assertEquals(0, GrouperUtil.nonNull(GrouperTrustedJwtConfig.retrieveFromConfigOrCache(CONFIG_ID).getSubjectSourceIds()).size());
  }

}
