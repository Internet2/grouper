/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ws;

import edu.internet2.middleware.subject.Subject;

/**
 * works out who is authenticated for the current request.
 *
 * <p>each front door registers one of these with {@link GrouperWsRequestContext}: the web service
 * resolves the subject from the servlet request and its configured authentication class, and the
 * UI resolves it from the session.  the logic which asks who is logged in does not know or care
 * which of those is in play.</p>
 */
public interface GrouperWsSubjectResolver {

  /**
   * @return the subject authenticated for the current request, never null.  throws if there is
   * no authenticated user, since that is a problem the caller needs to hear about
   */
  public Subject retrieveSubjectLoggedIn();

}
