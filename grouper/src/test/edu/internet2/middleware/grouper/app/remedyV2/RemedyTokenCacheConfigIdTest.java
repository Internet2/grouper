package edu.internet2.middleware.grouper.app.remedyV2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace.GrouperDigitalMarketplaceApiCommands;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import junit.textui.TestRunner;

/**
 * The Remedy and Digital Marketplace login token caches must be keyed by external system config
 * id: before the fix both cached one token under Boolean.TRUE, so a second configured system
 * reused the first system's token and sent it to its own URL.  The caches and
 * retrieveJwtToken are private, so the test uses reflection: it seeds a token for config A,
 * checks config A gets it from the cache, and checks config B (not configured) does not get it
 * (it tries a real login, which fails on the missing config).  Security issue
 * advisory M1.
 */
public class RemedyTokenCacheConfigIdTest extends GrouperTest {

  /** config that has a cached token (no real config needed: the token comes from the cache) */
  private static final String CONFIG_A = "testTokenIsolationCfgA";

  /** a second config that must not see config A's token (deliberately not configured) */
  private static final String CONFIG_B = "testTokenIsolationCfgB";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RemedyTokenCacheConfigIdTest("testRemedyTokenCacheKeyedByConfigId"));
  }

  /**
   * @param name
   */
  public RemedyTokenCacheConfigIdTest(String name) {
    super(name);
  }

  /**
   * the private static token cache of a class (raw type: before the fix its key type was Boolean)
   * @param theClass
   * @return the cache
   */
  @SuppressWarnings("rawtypes")
  private static ExpirableCache tokenCache(Class<?> theClass) {
    try {
      Field field = theClass.getDeclaredField("retrieveJwtTokenCache");
      field.setAccessible(true);
      return (ExpirableCache) field.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * call the private static retrieveJwtToken(String configId, Map debugMap)
   * @param theClass
   * @param configId
   * @return the token, or null if the call threw (a real login was tried and failed)
   */
  private static String retrieveJwtToken(Class<?> theClass, String configId) {
    try {
      Method method = theClass.getDeclaredMethod("retrieveJwtToken", String.class, Map.class);
      method.setAccessible(true);
      return (String) method.invoke(null, configId, new LinkedHashMap<String, Object>());
    } catch (InvocationTargetException ite) {
      // fine: config B is not configured, so the login fails; it did not use another config's token
      return null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * seed config A's token and check config A gets it and config B does not
   * @param theClass
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static void assertTokenCacheKeyedByConfigId(Class<?> theClass) {
    ExpirableCache cache = tokenCache(theClass);
    String tokenA = "tokenFor_" + CONFIG_A;
    cache.put(CONFIG_A, tokenA);
    try {
      assertEquals(theClass.getSimpleName() + ": config A reads its own cached token", tokenA,
          retrieveJwtToken(theClass, CONFIG_A));
      String tokenB = retrieveJwtToken(theClass, CONFIG_B);
      assertFalse(theClass.getSimpleName() + ": config B must not get config A's token", tokenA.equals(tokenB));
    } finally {
      cache.clear();
    }
  }

  /**
   * Remedy (remedyV2) token cache
   */
  public void testRemedyTokenCacheKeyedByConfigId() {
    assertTokenCacheKeyedByConfigId(GrouperRemedyApiCommands.class);
  }

  /**
   * Digital Marketplace token cache
   */
  public void testDigitalMarketplaceTokenCacheKeyedByConfigId() {
    assertTokenCacheKeyedByConfigId(GrouperDigitalMarketplaceApiCommands.class);
  }
}
