package edu.internet2.middleware.grouper.app.interfolio;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Decides which Interfolio account a byc users/search result belongs to.
 *
 * byc users/search is a free-text search over name, email and UID, and its results do not carry the
 * UID (institution_user_id).  So the fact that we searched BY a UID proves nothing about any single
 * hit: searching "avik" can return exactly one other person whose name or email contains "avik".
 * Interfolio enforces a unique email per institution, so the one identifier we can compare exactly
 * on a search hit is the email.  A hit is accepted only when its email equals (case-insensitive) an
 * email we already know belongs to the user, never because it was the only hit.
 *
 * Kept in its own class (same file in every release) so the matching rule is in one place and can
 * be unit tested without an Interfolio server.
 */
public class InterfolioUserMatch {

  /**
   * no instances
   */
  private InterfolioUserMatch() {
  }

  // ------------------------------------------------------------------------------------------------
  // matching

  /**
   * Find the one search hit whose email equals the trusted email.
   * @param hits users returned by byc users/search
   * @param trustedEmail an email known to belong to the user (Grouper's value, or the value
   *   Interfolio held before this update)
   * @return the matching user, or null if the email is blank, nothing matches, or (defensively) more
   *   than one hit has that email
   */
  public static InterfolioUser matchByEmail(List<InterfolioUser> hits, String trustedEmail) {
    String email = StringUtils.trimToNull(trustedEmail);
    if (email == null) {
      // no exact identifier to compare on: a lone hit is not proof, so do not link
      return null;
    }
    InterfolioUser match = null;
    for (InterfolioUser hit : GrouperUtil.nonNull(hits)) {
      if (hit != null && StringUtils.equalsIgnoreCase(email, StringUtils.trim(hit.getEmail()))) {
        if (match != null) {
          // emails are unique in Interfolio, so two hits means we cannot tell them apart
          return null;
        }
        match = hit;
      }
    }
    return match;
  }

  /**
   * The email the user's Interfolio account holds right now, i.e. before this update is written.
   * For an update that changes the email, the entity carries the NEW (desired) email; resolving the
   * pid with that value could find a different account that already holds the new email.  The old
   * value is in the email attribute's object change (the compare puts the target value there).
   * @param targetEntity entity being updated or deleted
   * @return the pre-change email, the entity's email if the email is not changing, or null if the
   *   email is changing and the old value is not known (the caller then refuses to resolve)
   */
  public static String emailBeforeChange(ProvisioningEntity targetEntity) {
    if (targetEntity == null) {
      return null;
    }
    for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
      if (change != null && StringUtils.equals("email", change.getAttributeName())) {
        // email is changing: only the old value identifies the existing account
        return StringUtils.trimToNull(GrouperUtil.stringValue(change.getOldValue()));
      }
    }
    return targetEntity.retrieveAttributeValueString("email");
  }

  // ------------------------------------------------------------------------------------------------
  // request building

  /**
   * Make a search term safe to put in the query string.  Letters, digits and . _ - @ + ~ are kept
   * as is (so UIDs and emails are sent, and HMAC signed, exactly as before); anything else is
   * percent-encoded as UTF-8 so a value cannot add query parameters (&amp;, =), cut the url (#) or
   * break the request line (space).
   * @param searchTerm raw term, may be null
   * @return encoded term, empty string for null
   */
  public static String encodeSearchTerm(String searchTerm) {
    if (searchTerm == null) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    byte[] bytes;
    try {
      bytes = searchTerm.getBytes("UTF-8");
    } catch (java.io.UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }
    for (byte b : bytes) {
      int c = b & 0xff;
      boolean safe = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
          || c == '.' || c == '_' || c == '-' || c == '@' || c == '+' || c == '~';
      if (safe) {
        result.append((char) c);
      } else {
        result.append('%');
        String hex = Integer.toHexString(c).toUpperCase();
        if (hex.length() == 1) {
          result.append('0');
        }
        result.append(hex);
      }
    }
    return result.toString();
  }
}
