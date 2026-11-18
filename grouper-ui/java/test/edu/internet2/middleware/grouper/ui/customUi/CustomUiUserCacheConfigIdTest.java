package edu.internet2.middleware.grouper.ui.customUi;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * The Custom UI Azure and Duo user caches must be keyed by connector config id as well as by
 * subject: two configs (e.g. two tenants or two Duo accounts) must not share one cached answer.
 * The caches are private, so the test seeds them by reflection with an entry for config A and
 * checks that config A gets it and config B does not.  Security issue
 * advisory M1.  The same file runs on every patched release: CustomUiDuo
 * is not in 2.6.19 or 5.0.0 to 5.12.2, so the Duo part loads it by name and is skipped there.
 */
public class CustomUiUserCacheConfigIdTest extends GrouperTest {

  /** config that has a cached answer (no real config needed: the answer comes from the cache) */
  private static final String CONFIG_A = "testCacheIsolationCfgA";

  /** a second config that must not see config A's answer (deliberately not configured) */
  private static final String CONFIG_B = "testCacheIsolationCfgB";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new CustomUiUserCacheConfigIdTest("testAzureUserCacheKeyedByConfigId"));
  }

  /**
   * @param name
   */
  public CustomUiUserCacheConfigIdTest(String name) {
    super(name);
  }

  /**
   * the private static user cache of a class
   * @param theClass
   * @param fieldName
   * @return the cache
   */
  @SuppressWarnings("unchecked")
  private static ExpirableCache<MultiKey, Map<String, Object>> userCache(Class<?> theClass, String fieldName) {
    try {
      Field field = theClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (ExpirableCache<MultiKey, Map<String, Object>>) field.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * an answer only config A could have produced
   * @return the map
   */
  private static Map<String, Object> configAAnswer() {
    Map<String, Object> answer = new HashMap<String, Object>();
    answer.put("userFound", true);
    answer.put("summary", "answer from " + CONFIG_A);
    return answer;
  }

  /**
   * Azure: config A's cached user is not served for config B
   */
  public void testAzureUserCacheKeyedByConfigId() {
    Subject subject = SubjectTestHelper.SUBJ0;
    ExpirableCache<MultiKey, Map<String, Object>> cache = userCache(CustomUiAzure.class, "azureUserCache");
    Map<String, Object> answerA = configAAnswer();
    cache.put(new MultiKey(CONFIG_A, subject.getSourceId(), subject.getId()), answerA);
    try {
      assertSame("config A reads its own cache entry (key includes the config id)", answerA,
          new CustomUiAzure().retrieveAzureUserOrFromCache(CONFIG_A, subject));

      Map<String, Object> answerB = null;
      try {
        answerB = new CustomUiAzure().retrieveAzureUserOrFromCache(CONFIG_B, subject);
      } catch (RuntimeException e) {
        // fine: config B is not configured, so a real lookup fails; it did not use A's entry
      }
      assertNotSame("config B must not get config A's cached user", answerA, answerB);
    } finally {
      cache.clear();
    }
  }

  /** class name of the Duo connector (not in 2.6.19 or 5.0.0 to 5.12.2) */
  private static final String CUSTOM_UI_DUO_CLASS = "edu.internet2.middleware.grouper.ui.customUi.CustomUiDuo";

  /**
   * call retrieveDuoUserOrFromCache(configId, subject) by reflection
   * @param duoClass
   * @param configId
   * @param subject
   * @return the answer, or null if the call threw (a real lookup was tried and failed)
   */
  @SuppressWarnings("unchecked")
  private static Map<String, Object> retrieveDuoUserOrFromCache(Class<?> duoClass, String configId, Subject subject) {
    try {
      Method method = duoClass.getMethod("retrieveDuoUserOrFromCache", String.class, Subject.class);
      return (Map<String, Object>) method.invoke(duoClass.newInstance(), configId, subject);
    } catch (InvocationTargetException ite) {
      // fine: the config is not configured, so a real lookup fails; it did not use another entry
      return null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Duo: config A's cached user is not served for config B
   */
  public void testDuoUserCacheKeyedByConfigId() {
    Class<?> duoClass;
    try {
      duoClass = Class.forName(CUSTOM_UI_DUO_CLASS);
    } catch (ClassNotFoundException cnfe) {
      System.out.println("skip testDuoUserCacheKeyedByConfigId: this release has no CustomUiDuo");
      return;
    }
    Subject subject = SubjectTestHelper.SUBJ0;
    ExpirableCache<MultiKey, Map<String, Object>> cache = userCache(duoClass, "duoUserCache");
    Map<String, Object> answerA = configAAnswer();
    cache.put(new MultiKey(CONFIG_A, subject.getSourceId(), subject.getId()), answerA);
    try {
      assertSame("config A reads its own cache entry (key includes the config id)", answerA,
          retrieveDuoUserOrFromCache(duoClass, CONFIG_A, subject));

      Map<String, Object> answerB = retrieveDuoUserOrFromCache(duoClass, CONFIG_B, subject);
      assertNotSame("config B must not get config A's cached user", answerA, answerB);
    } finally {
      cache.clear();
    }
  }
}
