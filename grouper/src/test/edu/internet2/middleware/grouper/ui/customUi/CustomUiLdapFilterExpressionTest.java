package edu.internet2.middleware.grouper.ui.customUi;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;

/**
 * SECURITY: the LDAP session (LdaptiveSessionImpl.processSearchRequest) runs a filter that starts with ${
 * and ends with } as a script with static classes allowed.  A Custom UI LDAP filter is first filled in with
 * the group and subject (CustomUiUtil.substituteExpressionLanguage), so a filter such as
 * ${group.description} lets whoever controls that text (a group admin) hand the LDAP session an expression.
 * CustomUiLdap.ldapFilter refuses a filled-in filter that starts with ${ before any LDAP call.
 */
public class CustomUiLdapFilterExpressionTest extends GrouperTest {

  /** an LDAP config id that does not exist: a filter that got through would fail on it instead */
  private static final String NO_SUCH_LDAP = "noSuchLdapCustomUiFilterTest";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new CustomUiLdapFilterExpressionTest("testGroupControlledExpressionRefused"));
  }

  /**
   *
   */
  public CustomUiLdapFilterExpressionTest() {
    super();
  }

  /**
   * @param name
   */
  public CustomUiLdapFilterExpressionTest(String name) {
    super(name);
  }

  /**
   * what counts as an expression: anything that starts with ${ after trimming (a near miss too)
   */
  public void testFilterIsExpression() {
    assertTrue(CustomUiLdap.filterIsExpression("${1+1}"));
    assertTrue(CustomUiLdap.filterIsExpression("  ${grouperUtil.something()}  "));
    assertTrue(CustomUiLdap.filterIsExpression("${no closing brace"));

    assertFalse(CustomUiLdap.filterIsExpression(null));
    assertFalse(CustomUiLdap.filterIsExpression(""));
    assertFalse(CustomUiLdap.filterIsExpression("(uid=jsmith)"));
    assertFalse(CustomUiLdap.filterIsExpression("(&(objectClass=person)(cn=${x}))"));
  }

  /**
   * a group description that is an expression, put in the filter by ${group.description}: refused before
   * the LDAP call
   */
  public void testGroupControlledExpressionRefused() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave(grouperSession).assignName("test:customUiLdapFilter:group")
          .assignDescription("${1+1}").assignCreateParentStemsIfNotExist(true).save();

      try {
        new CustomUiLdap().ldapFilter(NO_SUCH_LDAP, "dc=example,dc=edu", "${group.description}", "cn",
            group, SubjectTestHelper.SUBJ0, CustomUiVariableType.BOOLEAN);
        fail("a filled-in filter that is an expression must be refused");
      } catch (RuntimeException re) {
        assertTrue("refused as expression syntax before the LDAP call, got: " + re,
            String.valueOf(re.getMessage()).contains("expression syntax"));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * control: a plain filter is not refused by the check, it goes on to the LDAP session (which fails on the
   * missing LDAP config, with a different error)
   */
  public void testPlainFilterReachesLdap() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave(grouperSession).assignName("test:customUiLdapFilter:group")
          .assignDescription("plain").assignCreateParentStemsIfNotExist(true).save();
      try {
        new CustomUiLdap().ldapFilter(NO_SUCH_LDAP, "dc=example,dc=edu", "(cn=${group.extension})", "cn",
            group, SubjectTestHelper.SUBJ0, CustomUiVariableType.BOOLEAN);
      } catch (RuntimeException re) {
        assertFalse("a plain filter is not refused as an expression: " + re,
            String.valueOf(re.getMessage()).contains("expression syntax"));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
