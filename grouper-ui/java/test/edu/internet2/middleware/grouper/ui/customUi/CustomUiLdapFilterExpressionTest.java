package edu.internet2.middleware.grouper.ui.customUi;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectImpl;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * <p>Security issue customUiLdapSecondOrderExpressionInjection: CustomUiLdap.ldapFilter evaluates
 * the configured filter as an expression (with the subject and group) and passes the result to
 * LdapSession.list, which evaluates a filter that starts with ${ and ends with } AGAIN, as a
 * script with static classes allowed.  A configured filter that is one expression returning
 * subject or group data (e.g. ${subject.getId()}) therefore ran data as code.  After the fix
 * CustomUiLdap refuses a substituted filter that starts with ${ before calling the LDAP
 * session.</p>
 *
 * <p>No LDAP server and no database: the refusal happens before any LDAP call, and the
 * ordinary filter is only checked to get past the refusal (it then fails on the missing LDAP
 * config, which is expected here).  Only APIs present from 2.6.19 through 7.x.</p>
 */
public class CustomUiLdapFilterExpressionTest extends TestCase {

  /** text of the refusal */
  private static final String REFUSAL = "must be a plain LDAP filter after substitution";

  /** an LDAP config id that does not exist */
  private static final String CONFIG_ID = "secPatch184NoSuchLdap";

  /** second stage payload; harmless, it only builds a string */
  private static final String SECOND_STAGE = "${'(uid=' + 'secPatch184' + ')'}";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(CustomUiLdapFilterExpressionTest.class);
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
   * @param id subject id
   * @return a subject that is not in any source
   */
  private static Subject subject(String id) {
    return new SubjectImpl(id, "Test Subject 184", "Test Subject 184", "person", "jdbc");
  }

  /**
   * @param filter configured filter
   * @param subject subject
   * @return the exception message (null if no exception)
   */
  private static String ldapFilterError(String filter, Subject subject) {
    try {
      new CustomUiLdap().ldapFilter(CONFIG_ID, "dc=example,dc=edu", filter, "uid", null, subject,
          CustomUiVariableType.BOOLEAN);
      return null;
    } catch (RuntimeException re) {
      StringBuilder result = new StringBuilder();
      for (Throwable t = re; t != null; t = t.getCause()) {
        result.append(t.getMessage()).append(" | ");
      }
      return result.toString();
    }
  }

  /**
   * a single-expression filter whose result (subject data) is itself an expression is refused,
   * also with leading whitespace and a newline.  Before the fix it went to the LDAP session.
   */
  public void testExpressionResultIsRefused() {
    String error = ldapFilterError("${subject.getId()}", subject(SECOND_STAGE));
    assertNotNull("expected a refusal", error);
    assertTrue(error, error.contains(REFUSAL));
    // the refusal does not echo the attacker's value
    assertFalse(error, error.contains("secPatch184'"));

    error = ldapFilterError("${subject.getId()}", subject("  \n" + SECOND_STAGE));
    assertNotNull("expected a refusal", error);
    assertTrue(error, error.contains(REFUSAL));

    // a near miss without the closing brace is refused too
    error = ldapFilterError("${subject.getId()}", subject("${'x'"));
    assertNotNull("expected a refusal", error);
    assertTrue(error, error.contains(REFUSAL));
  }

  /**
   * an ordinary filter that embeds the same subject data inside parentheses is not refused here
   * (the LDAP session does not evaluate it: it does not start with ${).  It then fails on the
   * missing LDAP config, which is fine.
   */
  public void testOrdinaryFilterIsNotRefused() {
    String error = ldapFilterError("(&(objectClass=person)(uid=${subject.getId()}))", subject(SECOND_STAGE));
    if (error != null) {
      assertFalse(error, error.contains(REFUSAL));
    }
    error = ldapFilterError("(uid=${subject.getId()})", subject("jsmith"));
    if (error != null) {
      assertFalse(error, error.contains(REFUSAL));
    }
  }

  /**
   * the check itself
   */
  public void testFilterIsExpression() {
    assertTrue(CustomUiLdap.filterIsExpression("${x}"));
    assertTrue(CustomUiLdap.filterIsExpression(" \t\n${x}"));
    assertTrue(CustomUiLdap.filterIsExpression("${x"));
    assertFalse(CustomUiLdap.filterIsExpression("(uid=${x})"));
    assertFalse(CustomUiLdap.filterIsExpression("(uid=jsmith)"));
    assertFalse(CustomUiLdap.filterIsExpression(""));
    assertFalse(CustomUiLdap.filterIsExpression(null));
  }
}
