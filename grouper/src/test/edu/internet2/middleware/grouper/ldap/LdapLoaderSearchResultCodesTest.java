package edu.internet2.middleware.grouper.ldap;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.ldaptive.ResultCode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * LDAP searches the loader runs do not ignore TIME_LIMIT_EXCEEDED or PARTIAL_RESULTS unless the
 * site set ldap.&lt;id&gt;.searchIgnoreResultCodes explicitly; other searches keep the configured set.
 * Goes in grouper/src/test/edu/internet2/middleware/grouper/ldap/.
 */
public class LdapLoaderSearchResultCodesTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapLoaderSearchResultCodesTest("testLoaderSearchDropsCutOffCodes"));
  }

  /**
   *
   */
  public LdapLoaderSearchResultCodesTest() {
    super();
  }

  /**
   * @param name
   */
  public LdapLoaderSearchResultCodesTest(String name) {
    super(name);
  }

  /** an ldap id with no config, so the code default applies */
  private static final String LDAP_ID = "secPatch197ldap";

  /** the code default (LdapConfiguration) */
  private static final Set<ResultCode> DEFAULT_CODES = new HashSet<ResultCode>(GrouperUtil.toSet(
      ResultCode.TIME_LIMIT_EXCEEDED, ResultCode.SIZE_LIMIT_EXCEEDED, ResultCode.PARTIAL_RESULTS));

  /**
   * remove the override
   */
  @Override
  protected void tearDown() {
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("ldap." + LDAP_ID + ".searchIgnoreResultCodes");
    super.tearDown();
  }

  /**
   * outside the loader the default set is used as is; in a loader search time limit and partial
   * results are not ignored, size limit still is; the flag is cleared afterwards
   */
  public void testLoaderSearchDropsCutOffCodes() {
    assertFalse(LdapLoaderSearchResultCodes.isLoaderSearch());
    assertEquals("not a loader search", DEFAULT_CODES, LdapLoaderSearchResultCodes.ignoreResultCodes(LDAP_ID, DEFAULT_CODES));

    Set<ResultCode> inLoader = LdapLoaderSearchResultCodes.callForLoader(new Callable<Set<ResultCode>>() {
      public Set<ResultCode> call() {
        assertTrue(LdapLoaderSearchResultCodes.isLoaderSearch());
        return LdapLoaderSearchResultCodes.ignoreResultCodes(LDAP_ID, DEFAULT_CODES);
      }
    });
    assertEquals("loader search", GrouperUtil.toSet(ResultCode.SIZE_LIMIT_EXCEEDED), inLoader);
    assertFalse("flag cleared", LdapLoaderSearchResultCodes.isLoaderSearch());
  }

  /**
   * an explicit searchIgnoreResultCodes is the site's choice, also for the loader
   */
  public void testExplicitConfigIsKept() {
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap." + LDAP_ID + ".searchIgnoreResultCodes", "TIME_LIMIT_EXCEEDED, PARTIAL_RESULTS");
    final Set<ResultCode> configured = new HashSet<ResultCode>(GrouperUtil.toSet(ResultCode.TIME_LIMIT_EXCEEDED, ResultCode.PARTIAL_RESULTS));

    Set<ResultCode> inLoader = LdapLoaderSearchResultCodes.callForLoader(new Callable<Set<ResultCode>>() {
      public Set<ResultCode> call() {
        return LdapLoaderSearchResultCodes.ignoreResultCodes(LDAP_ID, configured);
      }
    });
    assertEquals("explicit config", configured, inLoader);
  }

  /**
   * the flag is restored after an exception and after a nested call
   */
  public void testFlagRestored() {
    try {
      LdapLoaderSearchResultCodes.callForLoader(new Callable<Object>() {
        public Object call() {
          // nested call keeps the outer flag
          LdapLoaderSearchResultCodes.callForLoader(new Callable<Object>() {
            public Object call() {
              return null;
            }
          });
          assertTrue("still in the outer loader search", LdapLoaderSearchResultCodes.isLoaderSearch());
          throw new RuntimeException("search failed");
        }
      });
      fail("exception expected");
    } catch (RuntimeException re) {
      assertEquals("search failed", re.getMessage());
    }
    assertFalse("flag cleared after the exception", LdapLoaderSearchResultCodes.isLoaderSearch());
  }
}
