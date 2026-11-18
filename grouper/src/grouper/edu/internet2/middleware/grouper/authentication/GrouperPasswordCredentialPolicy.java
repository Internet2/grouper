package edu.internet2.middleware.grouper.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * Credential-level restrictions stored on a database-backed grouper_password row: the
 * expiration (expires_millis) and the networks the credential may be used from
 * (allowed_from_cidrs).  The JWT path (GrouperPublicPrivateKeyJwt.decode) has always checked
 * these; built-in Basic authentication (Authentication.authenticate) calls this class so it
 * applies the same rules.
 *
 * Semantics, the same as the JWT path:
 * - expired when expiresMillis is set and is before now
 * - when allowedFromCidrs is not blank, the requester IP must be on one of those networks
 *   (GrouperUtil.ipOnNetworks)
 *
 * This class fails closed: a missing requester IP, or an allowedFromCidrs value that cannot be
 * parsed, refuses the credential.  Callers treat a refusal exactly like a wrong password.
 * </pre>
 */
public class GrouperPasswordCredentialPolicy {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperPasswordCredentialPolicy.class);

  /** reason: no credential row */
  public static final String DENIED_NO_CREDENTIAL = "noCredential";

  /** reason: expires_millis is in the past */
  public static final String DENIED_EXPIRED = "expired";

  /** reason: allowed_from_cidrs is set but the request has no source IP */
  public static final String DENIED_REQUESTER_IP_MISSING = "requesterIpMissing";

  /** reason: the source IP is not on any of the allowed networks */
  public static final String DENIED_IP_NOT_ALLOWED = "ipNotAllowed";

  /** reason: allowed_from_cidrs could not be parsed */
  public static final String DENIED_INVALID_ALLOWED_FROM_CIDRS = "invalidAllowedFromCidrs";

  /**
   * no instances
   */
  private GrouperPasswordCredentialPolicy() {
  }

  /**
   * if the row has an expiration or a network restriction, i.e. if it can be refused by this class
   * even when the password is right
   * @param grouperPassword
   * @return true if restricted
   */
  public static boolean hasRestriction(GrouperPassword grouperPassword) {
    if (grouperPassword == null) {
      return false;
    }
    return grouperPassword.getExpiresMillis() != null || StringUtils.isNotBlank(grouperPassword.getAllowedFromCidrs());
  }

  /**
   * why the credential is refused, or null if it may be used now from this IP
   * @param grouperPassword the database row
   * @param requesterIpAddress source IP of the request
   * @param nowMillis current time
   * @return null if allowed, else one of the DENIED_* constants
   */
  public static String deniedReason(GrouperPassword grouperPassword, String requesterIpAddress, long nowMillis) {

    if (grouperPassword == null) {
      return DENIED_NO_CREDENTIAL;
    }

    // same comparison as GrouperPublicPrivateKeyJwt.decode
    Long expiresMillis = grouperPassword.getExpiresMillis();
    if (expiresMillis != null && expiresMillis < nowMillis) {
      return DENIED_EXPIRED;
    }

    String allowedFromCidrs = grouperPassword.getAllowedFromCidrs();
    if (StringUtils.isBlank(allowedFromCidrs)) {
      return null;
    }

    // fail closed: a restricted credential needs a source address to check
    if (StringUtils.isBlank(requesterIpAddress)) {
      return DENIED_REQUESTER_IP_MISSING;
    }

    boolean onNetworks = false;
    try {
      onNetworks = GrouperUtil.ipOnNetworks(StringUtils.trim(requesterIpAddress), allowedFromCidrs);
    } catch (RuntimeException re) {
      // fail closed: a value that cannot be parsed must not open the credential to everyone
      LOG.error("Cannot parse allowedFromCidrs on grouper password id '" + grouperPassword.getId() + "'", re);
      return DENIED_INVALID_ALLOWED_FROM_CIDRS;
    }
    return onNetworks ? null : DENIED_IP_NOT_ALLOWED;
  }

  /**
   * if the credential may be used now from this IP.  Logs a warning (no secrets) when refused, so
   * an admin can tell why a correct password fails
   * @param grouperPassword the database row
   * @param requesterIpAddress source IP of the request
   * @return true if allowed
   */
  public static boolean allowed(GrouperPassword grouperPassword, String requesterIpAddress) {
    String deniedReason = deniedReason(grouperPassword, requesterIpAddress, System.currentTimeMillis());
    if (deniedReason == null) {
      return true;
    }
    if (LOG.isWarnEnabled() && grouperPassword != null) {
      LOG.warn("Refusing grouper password credential username '" + grouperPassword.getUsername()
          + "', application '" + grouperPassword.getApplication() + "', id '" + grouperPassword.getId()
          + "', requester IP '" + requesterIpAddress + "': " + deniedReason);
    }
    return false;
  }

}
