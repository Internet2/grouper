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
package edu.internet2.middleware.grouper.grouperUi.beans.inviteExternalSubjects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister.ExternalRegisterContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.AppState;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * request container for inviting external subjects
 * @author mchyzer
 */
public class InviteExternalSubjectsContainer {

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static ExternalRegisterContainer retrieveFromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
  
    ExternalRegisterContainer externalRegisterContainer = (ExternalRegisterContainer)httpServletRequest
      .getAttribute("inviteExternalSubjectsContainer");
    if (externalRegisterContainer == null) {
      throw new NoSessionException(GrouperUiUtils.message("inviteExternalSubjects.noContainer"));
    }
    return externalRegisterContainer;
  }

  /** cache the default group once we determine it is ok to use */
  private Group defaultGroup = null;

  /**
   * store to session scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("inviteExternalSubjectsContainer", this);
  }

  /**
   * if we should allow invite by identifier (if the inviter knows the identifier, and 
   * no attributes on the external subject are mandatory by the application)
   * @return true if allow invite by identifier
   */
  public boolean isAllowInviteByIdentifier() {
    return TagUtils.mediaResourceBoolean("inviteExternalMembers.allowInviteByIdentifier", false);
  }
  
  /**
   * if we should show links to the UI
   * @return if show links
   */
  public boolean isShowLinksToUi() {
    return this.getDefaultGroup() != null;
  }

  /**
   * if there is a group passed in the URL, make sure it is ok for security, and return it
   * @return the group
   */
  public Group getDefaultGroup() {
    if (this.defaultGroup == null) {
      AppState appState = AppState.retrieveFromRequest();
  
      //lets see if there is an invite id
      final String groupId = appState.getUrlArgObjectOrParam("groupId");
      final String groupName = appState.getUrlArgObjectOrParam("groupName");
      
      //if nothing was passed in
      if (StringUtils.isBlank(groupId) && StringUtils.isBlank(groupName)) {
        return null;
      }
      
      if (!StringUtils.isBlank(groupId) && !StringUtils.isBlank(groupName)) {
        throw new RuntimeException("Dont pass in groupId and groupName");
      }
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      GrouperSession grouperSession = null;
    
      Group group = null;
      
      try {
        grouperSession = GrouperSession.start(loggedInSubject, false);
        group = (Group)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
          @Override
          public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
            Group theGroup = null;
            if (!StringUtils.isBlank(groupId)) {
              theGroup = GroupFinder.findByUuid(theGrouperSession, groupId, false);
            }
            if (!StringUtils.isBlank(groupName)) {
              theGroup = GroupFinder.findByName(theGrouperSession, groupName, false);
            }
            return theGroup;
          }
        });
  
      } finally {
        GrouperSession.stopQuietly(grouperSession); 
        }
        if (group == null) {
          String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.invalidGroupUuid");
          errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(StringUtils.defaultString(groupId, groupName), true));
          guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
          return null;
          
        }
  
      grouperSession = GrouperSession.startRootSession(false);
      try {
        final Group GROUP = group;
        boolean canEdit = (Boolean)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
          @Override
          public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
            return GROUP.hasUpdate(loggedInSubject) || GROUP.hasAdmin(loggedInSubject);
          }
        });
        if (!canEdit) {
          String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.invalidGroupPrivileges");
          errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(group.getDisplayName(), true));
          guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
          return null;
        }
        this.defaultGroup = group;
  
        
      } finally {
        GrouperSession.stopQuietly(grouperSession);
      }
      
    }
    return this.defaultGroup;      
    
  }

  /**
   * if there is a group passed in via URL, set it on the screen
   * @return the group text
   */
  public String getFirstComboDefaultText() {
    Group theDefaultGroup = this.getDefaultGroup();
    return theDefaultGroup == null ? null : theDefaultGroup.getDisplayName();
  }

  /**
   * if there is a group passed in via URL, set it on the screen
   * @return the group value
   */
  public String getFirstComboDefaultValue() {
    Group theDefaultGroup = this.getDefaultGroup();
    return theDefaultGroup == null ? null : theDefaultGroup.getUuid();
  }
  
  /**
   * 
   * @return default email subject for form
   */
  public String getDefaultEmailSubject() {
    return GrouperConfig.getProperty("externalSubjectsInviteDefaultEmailSubject");
  }

  /**
   * 
   * @return default email message for form
   */
  public String getDefaultEmailMessage() {
    String email = StringUtils.defaultString(GrouperConfig.getProperty("externalSubjectsInviteDefaultEmail"));
    email = StringUtils.replace(email, "$newline$", "\n");
    return email;
  }

}
