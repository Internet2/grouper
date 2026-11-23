package edu.internet2.middleware.grouper.authentication;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Optional grouper.jwt.trusted.&lt;configId&gt;.issuer and
 * .audiences bind a trusted JWT to this relying party.  A token signed by the right key but
 * issued by another issuer or for another audience is refused before any subject lookup.
 * Blank keys (the default) do not check iss / aud.
 *
 * The test makes its own RSA key pair and signs tokens with it.
 */
public class GrouperTrustedJwtIssuerAudienceTest extends GrouperTest {

  /** config id used by this test */
  private static final String CONFIG_ID = "testCfgIssAud";

  /** property prefix of this test's config */
  private static final String PREFIX = "grouper.jwt.trusted." + CONFIG_ID + ".";

  /** properties stored in the database config, deleted again in tearDown */
  private static final String[] PROPERTY_SUFFIXES = new String[] {
      "enabled", "key.0.publicKey", "key.0.encryptionType", "subjectSourceIds", "issuer", "audiences"};

  /** the issuer the config trusts */
  private static final String ISSUER = "https://idp.example.edu/grouper";

  /** the audience the config accepts */
  private static final String AUDIENCE = "https://grouper.example.edu/grouper-ws";

  /** private key that signs the tokens */
  private RSAPrivateKey privateKey;

  /** matching public key */
  private RSAPublicKey publicKey;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperTrustedJwtIssuerAudienceTest("testIssuerAndAudienceChecked"));
  }

  /**
   * @param name
   */
  public GrouperTrustedJwtIssuerAudienceTest(String name) {
    super(name);
  }

  /**
   * make a key pair and store the trusted jwt config
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
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
    storeProperty("key.0.publicKey", Base64.getEncoder().encodeToString(this.publicKey.getEncoded()));
    storeProperty("key.0.encryptionType", "RS-256");
    storeProperty("subjectSourceIds", "jdbc");
    GrouperTrustedJwtConfig.clearCache();
  }

  /**
   * remove the config again
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    for (String suffix : PROPERTY_SUFFIXES) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(PREFIX + suffix);
      new GrouperDbConfig().configFileName("grouper.properties").propertyName(PREFIX + suffix).delete();
    }
    GrouperTrustedJwtConfig.clearCache();
    super.tearDown();
  }

  /**
   * store one property in the database config and in memory (so this JVM sees it now)
   * @param suffix after the prefix
   * @param value
   */
  private static void storeProperty(String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper.properties").propertyName(PREFIX + suffix).value(value).store();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + suffix, value);
  }

  /**
   * sign a token for test.subject.0
   * @param issuer iss claim or null for none
   * @param audiences aud claim values, or null for none
   * @return the bearer header value
   */
  private String bearer(String issuer, String... audiences) {
    long now = System.currentTimeMillis();
    JWTCreator.Builder builder = JWT.create()
        .withClaim("subjectId", SubjectTestHelper.SUBJ0_ID)
        .withIssuedAt(new Date(now))
        .withExpiresAt(new Date(now + 300 * 1000L));
    if (issuer != null) {
      builder.withIssuer(issuer);
    }
    if (audiences != null) {
      builder.withAudience(audiences);
    }
    String jwt = builder.sign(Algorithm.RSA256(this.publicKey, this.privateKey));
    return "Bearer jwtTrusted_" + CONFIG_ID + "_" + jwt;
  }

  /**
   * decode and check the token is refused as invalid
   * @param message
   * @param bearerHeader
   */
  private static void assertRefused(String message, String bearerHeader) {
    GrouperTrustedJwt grouperTrustedJwt = new GrouperTrustedJwt().assignBearerTokenHeader(bearerHeader);
    assertNull(message, grouperTrustedJwt.decode());
    assertEquals(message, GrouperTrustedJwtResult.ERROR_TOKEN_INVALID, grouperTrustedJwt.getGrouperTrustedJwtResult());
  }

  /**
   * decode and check the token resolves test.subject.0 in jdbc
   * @param message
   * @param bearerHeader
   */
  private static void assertAccepted(String message, String bearerHeader) {
    GrouperTrustedJwt grouperTrustedJwt = new GrouperTrustedJwt().assignBearerTokenHeader(bearerHeader);
    Subject subject = grouperTrustedJwt.decode();
    assertNotNull(message + ", result: " + grouperTrustedJwt.getGrouperTrustedJwtResult(), subject);
    assertEquals(message, SubjectTestHelper.SUBJ0_ID, subject.getId());
    assertEquals(message, "jdbc", subject.getSourceId());
  }

  /**
   * blank issuer / audiences (the default): iss and aud are not checked, as before
   */
  public void testBlankKeysNotChecked() {
    assertAccepted("no iss / aud", bearer(null, (String[]) null));
    assertAccepted("other iss / aud", bearer("https://issuer.example/other-service", "unrelated-service"));
  }

  /**
   * configured issuer and audiences are both enforced, before the subject lookup, and the
   * source ceiling still applies after them
   */
  public void testIssuerAndAudienceChecked() {
    storeProperty("issuer", ISSUER);
    storeProperty("audiences", "someOtherAudience, " + AUDIENCE);
    GrouperTrustedJwtConfig.clearCache();

    assertAccepted("right iss and aud", bearer(ISSUER, AUDIENCE));
    assertAccepted("aud array containing a configured audience", bearer(ISSUER, "unrelated-service", AUDIENCE));

    assertRefused("token for another application (the report's replay)", bearer("https://issuer.example/other-service", "unrelated-service"));
    assertRefused("wrong issuer", bearer("https://issuer.example/other-service", AUDIENCE));
    assertRefused("issuer differs only by a trailing slash", bearer(ISSUER + "/", AUDIENCE));
    assertRefused("wrong audience", bearer(ISSUER, "unrelated-service"));
    assertRefused("missing issuer", bearer(null, AUDIENCE));
    assertRefused("missing audience", bearer(ISSUER, (String[]) null));

    // the subject source ceiling is still enforced after issuer / audience pass
    long now = System.currentTimeMillis();
    String jwt = JWT.create().withClaim("subjectId", "GrouperSystem").withClaim("subjectSourceId", "g:isa")
        .withIssuer(ISSUER).withAudience(AUDIENCE)
        .withIssuedAt(new Date(now)).withExpiresAt(new Date(now + 300 * 1000L))
        .sign(Algorithm.RSA256(this.publicKey, this.privateKey));
    assertRefused("g:isa claim outside subjectSourceIds", "Bearer jwtTrusted_" + CONFIG_ID + "_" + jwt);
  }

  /**
   * only the issuer configured: aud is not checked
   */
  public void testIssuerOnly() {
    storeProperty("issuer", ISSUER);
    GrouperTrustedJwtConfig.clearCache();

    assertAccepted("right iss, no aud", bearer(ISSUER, (String[]) null));
    assertRefused("wrong iss", bearer("https://issuer.example/other-service", (String[]) null));
  }

  /**
   * the config reads and trims the keys
   */
  public void testConfigParsed() {
    storeProperty("issuer", "  " + ISSUER + "  ");
    storeProperty("audiences", " a1 ,a2 ");
    GrouperTrustedJwtConfig.clearCache();
    GrouperTrustedJwtConfig config = GrouperTrustedJwtConfig.retrieveFromConfigOrCache(CONFIG_ID);
    assertEquals(ISSUER, config.getIssuer());
    assertEquals(2, config.getAudiences().size());
    assertTrue(config.getAudiences().contains("a1"));
    assertTrue(config.getAudiences().contains("a2"));
  }
}
