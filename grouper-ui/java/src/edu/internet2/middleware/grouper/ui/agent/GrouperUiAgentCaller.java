/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.agent;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

/**
 * who the agent is working for, captured on the UI request thread so the agent can carry on in a
 * background thread, where there is no request to ask.
 *
 * <p>the session container is held rather than a copy of the scope in it, so a scope the user
 * changes while the agent is working still applies from the next tool call, the same as on a
 * request thread.</p>
 */
public class GrouperUiAgentCaller {

  /** the user logged in to the UI */
  private Subject subject;

  /** the user's session state, which holds the scope they chose */
  private SessionContainer sessionContainer;

  /** the socket address the UI request came from */
  private String remoteAddr;

  /**
   * @param theSubject the user logged in to the UI
   * @param theSessionContainer the user's session state
   * @param theRemoteAddr the socket address the request came from, may be null
   */
  public GrouperUiAgentCaller(Subject theSubject, SessionContainer theSessionContainer, String theRemoteAddr) {
    if (theSubject == null) {
      throw new RuntimeException("No user is logged in to the UI");
    }
    if (theSessionContainer == null) {
      throw new RuntimeException("No UI session");
    }
    this.subject = theSubject;
    this.sessionContainer = theSessionContainer;
    this.remoteAddr = theRemoteAddr;
  }

  /**
   * capture the caller from the current UI request.  call on a request thread, after
   * GrouperUiFilter has authenticated the user
   * @return the caller
   */
  public static GrouperUiAgentCaller fromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    return new GrouperUiAgentCaller(GrouperUiFilter.retrieveSubjectLoggedIn(),
        SessionContainer.retrieveFromSession(),
        httpServletRequest == null ? null : httpServletRequest.getRemoteAddr());
  }

  /**
   * @return the user logged in to the UI
   */
  public Subject getSubject() {
    return this.subject;
  }

  /**
   * @return the user's session state
   */
  public SessionContainer getSessionContainer() {
    return this.sessionContainer;
  }

  /**
   * @return the socket address the UI request came from, may be null
   */
  public String getRemoteAddr() {
    return this.remoteAddr;
  }

  /** the user's member internal id, looked up the first time it is needed */
  private volatile Long memberInternalId = null;

  /**
   * the user's member internal id, looked up once and kept, since every tool call and check needs
   * it and the user does not change for the life of the caller
   * @return the member internal id
   */
  public long getMemberInternalId() {
    Long theMemberInternalId = this.memberInternalId;
    if (theMemberInternalId == null) {
      theMemberInternalId = GrouperUiAgentToolRunner.retrieveMemberInternalId(this.subject);
      if (theMemberInternalId == null) {
        throw new RuntimeException("Cannot find the member for the user logged in to the UI: "
            + this.subject.getSourceId() + " / " + this.subject.getId());
      }
      this.memberInternalId = theMemberInternalId;
    }
    return theMemberInternalId;
  }

}
