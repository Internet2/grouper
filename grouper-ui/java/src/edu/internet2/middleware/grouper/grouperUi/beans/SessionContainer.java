/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id: SessionContainer.java,v 1.2 2009-10-11 22:04:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentConversation;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiHideShow;
import edu.internet2.middleware.grouper.mcp.GrouperToolCategory;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter.UiSection;
import edu.internet2.middleware.grouper.ui.agent.GrouperUiAgentRun;
import edu.internet2.middleware.subject.Subject;


/**
 * hold generic stuff about user in session
 */
public class SessionContainer implements Serializable {

  /**
   * if initted
   */
  private boolean initted = false;
  
  /**
   * logged in subject
   */
  private Subject subjectLoggedIn;
  
  /** allowed ui sections */
  private Set<UiSection> allowedUiSections = new HashSet<UiSection>();

  /**
   * what the AI agent may do in this session, as tool categories.  null until set, which means
   * read-only: the risk with an assistant is it doing something unintended, so writing is an
   * explicit opt-up on the AI screen.  this only ever narrows; the user still needs the group
   * membership for each category.  kept in the session and read on every tool call, so a change
   * takes effect on the next call.  volatile because the agent's background thread reads it while
   * a request thread may replace it; it is always replaced with a new set, never changed in place,
   * so volatile is enough for the next call to see the new scope
   */
  private volatile Set<GrouperToolCategory> aiAgentToolScope = null;

  /**
   * the AI agent conversation in this session, null until the user starts one.  in the session
   * rather than a static, so it follows the user's session to whichever UI node serves it.
   * volatile because the requests of one session, e.g. from two tabs, run on different threads
   */
  private volatile GrouperAiAgentConversation aiAgentConversation = null;

  /**
   * the AI agent turn running in the background for this session, or the last one, null if none.
   * transient: it is only meaningful on the node running the thread, and a copy of the session
   * elsewhere should start with none.  volatile because the requests of one session, e.g. a status
   * poll and a second tab, run on different threads
   */
  private transient volatile GrouperUiAgentRun aiAgentRun = null;

  /**
   * @return the AI agent turn running in the background for this session, or the last one
   */
  public GrouperUiAgentRun getAiAgentRun() {
    return this.aiAgentRun;
  }

  /**
   * @param aiAgentRun1 the AI agent turn running in the background for this session
   */
  public void setAiAgentRun(GrouperUiAgentRun aiAgentRun1) {
    this.aiAgentRun = aiAgentRun1;
  }

  /**
   * @return the AI agent conversation in this session, null if none
   */
  public GrouperAiAgentConversation getAiAgentConversation() {
    return this.aiAgentConversation;
  }

  /**
   * @param aiAgentConversation1 the AI agent conversation in this session, null to forget it
   */
  public void setAiAgentConversation(GrouperAiAgentConversation aiAgentConversation1) {
    this.aiAgentConversation = aiAgentConversation1;
  }

  /**
   * @return the tool categories the AI agent may use in this session, a copy.  read-only if the
   * user has not chosen otherwise
   */
  public Set<GrouperToolCategory> getAiAgentToolScope() {
    // read once, since another thread may replace it between two reads
    Set<GrouperToolCategory> theAiAgentToolScope = this.aiAgentToolScope;
    Set<GrouperToolCategory> result = new HashSet<GrouperToolCategory>();
    if (theAiAgentToolScope == null) {
      result.add(GrouperToolCategory.readonly);
    } else {
      result.addAll(theAiAgentToolScope);
    }
    return result;
  }

  /**
   * @param aiAgentToolScope1 the tool categories the AI agent may use in this session.  null puts
   * it back to the read-only default
   */
  public void setAiAgentToolScope(Set<GrouperToolCategory> aiAgentToolScope1) {
    if (aiAgentToolScope1 == null) {
      this.aiAgentToolScope = null;
    } else {
      this.aiAgentToolScope = new HashSet<GrouperToolCategory>(aiAgentToolScope1);
    }
  }

  /**
   * @return allowed ui sections
   */
  public Set<UiSection> getAllowedUiSections() {
    return this.allowedUiSections;
  }

  /**
   * logged in subject
   * @return the subjectLoggedIn
   */
  public Subject getSubjectLoggedIn() {
    return this.subjectLoggedIn;
  }

  /**
   * logged in subject
   * @param subjectLoggedIn1 the subjectLoggedIn to set
   */
  public void setSubjectLoggedIn(Subject subjectLoggedIn1) {
    this.subjectLoggedIn = subjectLoggedIn1;
  }


  /**
   * if initted
   * @return the initted
   */
  public boolean isInitted() {
    return this.initted;
  }

  
  /**
   * if initted
   * @param initted1 the initted to set
   */
  public void setInitted(boolean initted1) {
    this.initted = initted1;
  }

  /**
   * retrieveFromSession, will lazy load
   * @return the app state in request scope
   */
  public static SessionContainer retrieveFromSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    HttpSession httpSession = httpServletRequest.getSession();
    SessionContainer sessionContainer = (SessionContainer)httpSession
      .getAttribute("sessionContainer");
    if (sessionContainer == null) {
      sessionContainer = new SessionContainer();
      sessionContainer.storeToSession();
    }
    return sessionContainer;
  }

  
  
  /**
   * store to session scope
   */
  public void storeToSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.getSession().setAttribute("sessionContainer", this);
  }

  /** map of hide shows in session */
  private Map<String, GuiHideShow> hideShows = new LinkedHashMap<String, GuiHideShow>();

  /**
   * map of hide shows in session
   * @return map of hide shows in session
   */
  public Map<String, GuiHideShow> getHideShows() {
    return this.hideShows;
  }
  
}
