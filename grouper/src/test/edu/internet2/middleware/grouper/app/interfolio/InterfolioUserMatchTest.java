package edu.internet2.middleware.grouper.app.interfolio;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * The Interfolio provisioner looks users up with byc users/search, a free-text search over name and
 * email whose results do not include the UID.  A hit may only be linked to a Grouper user when its
 * email equals an email known to belong to that user (never just because it was the only hit), an
 * update that changes the email must resolve the account by the OLD email, and the search term must
 * not be able to add query parameters.  InterfolioUserMatch holds those rules; the target DAO and
 * GrouperInterfolioApiCommands call it.  No database or Interfolio server needed.
 */
public class InterfolioUserMatchTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new InterfolioUserMatchTest("testLoneFreeTextHitIsNotLinked"));
  }

  /**
   * @param name
   */
  public InterfolioUserMatchTest(String name) {
    super(name);
  }

  /**
   * @param pid
   * @param email
   * @return a search hit
   */
  private static InterfolioUser hit(String pid, String email) {
    InterfolioUser user = new InterfolioUser();
    user.setPid(pid);
    user.setEmail(email);
    return user;
  }

  /**
   * searching UID "avik" returns exactly one account, someone else whose name contains "avik": it
   * must not be linked (this was the single-hit shortcut)
   */
  public void testLoneFreeTextHitIsNotLinked() {
    List<InterfolioUser> hits = new ArrayList<InterfolioUser>();
    hits.add(hit("8000001", "rmaddipati@example.edu"));
    assertNull(InterfolioUserMatch.matchByEmail(hits, "avik@example.edu"));
  }

  /**
   * a lone hit whose email is the user's email is the user's account
   */
  public void testLoneHitWithSameEmailIsLinked() {
    List<InterfolioUser> hits = new ArrayList<InterfolioUser>();
    hits.add(hit("8000002", "Avik@Example.edu"));
    InterfolioUser match = InterfolioUserMatch.matchByEmail(hits, "avik@example.edu");
    assertNotNull(match);
    assertEquals("8000002", match.getPid());
  }

  /**
   * several hits: only the exact (case-insensitive) email matches
   */
  public void testMultipleHitsPickExactEmail() {
    List<InterfolioUser> hits = new ArrayList<InterfolioUser>();
    hits.add(hit("8000001", "rmaddipati@example.edu"));
    hits.add(hit("8000002", "avik@example.edu"));
    hits.add(hit("8000003", "daviking@example.edu"));
    assertEquals("8000002", InterfolioUserMatch.matchByEmail(hits, " AVIK@example.edu ").getPid());
    // a substring is not a match
    assertNull(InterfolioUserMatch.matchByEmail(hits, "vik@example.edu"));
  }

  /**
   * no expected email: nothing can be compared, so nothing is linked, even a single hit
   */
  public void testNoTrustedEmailNoMatch() {
    List<InterfolioUser> hits = new ArrayList<InterfolioUser>();
    hits.add(hit("8000001", "avik@example.edu"));
    assertNull(InterfolioUserMatch.matchByEmail(hits, null));
    assertNull(InterfolioUserMatch.matchByEmail(hits, "  "));
    assertNull(InterfolioUserMatch.matchByEmail(null, "avik@example.edu"));
  }

  /**
   * two hits with the same email (should not happen, emails are unique): ambiguous, no match
   */
  public void testDuplicateEmailIsAmbiguous() {
    List<InterfolioUser> hits = new ArrayList<InterfolioUser>();
    hits.add(hit("8000001", "avik@example.edu"));
    hits.add(hit("8000002", "avik@example.edu"));
    assertNull(InterfolioUserMatch.matchByEmail(hits, "avik@example.edu"));
  }

  /**
   * an update that changes the email resolves the account by the email Interfolio holds now (the
   * old value), not the new one another account may already hold
   */
  public void testEmailBeforeChangeUsesOldValue() {
    ProvisioningEntity entity = new ProvisioningEntity();
    entity.assignAttributeValue("institution_user_id", "avik");
    entity.assignAttributeValue("email", "new@example.edu");
    entity.addInternal_objectChange(new ProvisioningObjectChange("email",
        ProvisioningObjectChangeAction.update, "old@example.edu", "new@example.edu"));
    assertEquals("old@example.edu", InterfolioUserMatch.emailBeforeChange(entity));
  }

  /**
   * no email change: the entity's email is the account's email
   */
  public void testEmailBeforeChangeNoChange() {
    ProvisioningEntity entity = new ProvisioningEntity();
    entity.assignAttributeValue("email", "same@example.edu");
    entity.addInternal_objectChange(new ProvisioningObjectChange("first_name",
        ProvisioningObjectChangeAction.update, "Av", "Avik"));
    assertEquals("same@example.edu", InterfolioUserMatch.emailBeforeChange(entity));
  }

  /**
   * email changing but the old value is not known: no email, so the pid is not resolved
   */
  public void testEmailBeforeChangeUnknownOldValue() {
    ProvisioningEntity entity = new ProvisioningEntity();
    entity.assignAttributeValue("email", "new@example.edu");
    entity.addInternal_objectChange(new ProvisioningObjectChange("email",
        ProvisioningObjectChangeAction.update, null, "new@example.edu"));
    assertNull(InterfolioUserMatch.emailBeforeChange(entity));
    assertNull(InterfolioUserMatch.emailBeforeChange(null));
  }

  /**
   * UIDs and emails go out unchanged (same HMAC-signed string as before); query syntax is encoded
   */
  public void testEncodeSearchTerm() {
    assertEquals("", InterfolioUserMatch.encodeSearchTerm(null));
    assertEquals("avik", InterfolioUserMatch.encodeSearchTerm("avik"));
    assertEquals("a.b-c_d+e~f@example.edu", InterfolioUserMatch.encodeSearchTerm("a.b-c_d+e~f@example.edu"));
    assertEquals("x%26limit%3D1000%23", InterfolioUserMatch.encodeSearchTerm("x&limit=1000#"));
    assertEquals("a%20b%25", InterfolioUserMatch.encodeSearchTerm("a b%"));
    assertEquals("%C3%A9", InterfolioUserMatch.encodeSearchTerm("\u00e9"));
  }
}
