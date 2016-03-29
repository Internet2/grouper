/*******************************************************************************
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Collection;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiEntity;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPrivilege;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiService;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * common elements used across the UI
 * @author mchyzer
 *
 */
public class CommonRequestContainer {

  /**
   * if we should have the root ui be the new ui
   * @return true if root ui should be the new ui
   */
  public boolean isRootUiNewUi() {
    return GrouperUiConfig.retrieveConfig().propertyValueBoolean("ui-root-is-new-ui", true);
  }
  
  /**
   * error for screen (unescaped)
   */
  private String error;
  
  /**
   * error for screen (unescaped)
   * @return the error
   */
  public String getError() {
    return this.error;
  }
  
  /**
   * error for screen (unescaped)
   * @param error1 the error to set
   */
  public void setError(String error1) {
    this.error = error1;
  }

  /** 
   * email address in error messages 
   */
  private String emailAddress;
  
  /**
   * email address in error messages 
   * @return the emailAddress
   */
  public String getEmailAddress() {
    return this.emailAddress;
  }
  
  /**
   * email address in error messages 
   * @param emailAddress1 the emailAddress to set
   */
  public void setEmailAddress(String emailAddress1) {
    this.emailAddress = emailAddress1;
  }

  /**
   * sources
   * @return set of sources
   */
  public Collection<Source> getSources() {
    return SourceManager.getInstance().getSources();
  }
  
  /**
   * subjectId
   */
  private String subjectId;
  
  /**
   * subjectId
   * @return subjectId
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * subjectId
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * gui entity
   */
  private GuiEntity guiEntity;
  
  /**
   * gui entity
   * @return entity
   */
  public GuiEntity getGuiEntity() {
    return this.guiEntity;
  }

  /**
   * gui entity
   * @param guiEntity1
   */
  public void setGuiEntity(GuiEntity guiEntity1) {
    this.guiEntity = guiEntity1;
  }

  /**
   * if link should be shown
   */
  private boolean showLink = false;

  /**
   * if should show a link
   * @return if link
   */
  public boolean isShowLink() {
    return this.showLink;
  }

  /**
   * if should show link
   * @param showLink1
   */
  public void setShowLink(boolean showLink1) {
    this.showLink = showLink1;
  }

  /**
   * if tooltip should be shown
   */
  private boolean showTooltip = false;
  
  /**
   * if tooltip should be shown
   * @return if tooltip
   */
  public boolean isShowTooltip() {
    return this.showTooltip;
  }

  /**
   * if tooltip should be shown
   * @param showTooltip1
   */
  public void setShowTooltip(boolean showTooltip1) {
    this.showTooltip = showTooltip1;
  }

  /**
   * if should show path
   */
  private boolean showPath = false;
  
  /**
   * if should show path
   * @return if path
   */
  public boolean isShowPath() {
    return this.showPath;
  }

  /**
   * if should show path
   * @param showPath1
   */
  public void setShowPath(boolean showPath1) {
    this.showPath = showPath1;
  }

  /**
   * if should show icon
   */
  private boolean showIcon = false;
  
  /**
   * if should show icon
   * @return if should show icon
   */
  public boolean isShowIcon() {
    return this.showIcon;
  }

  /**
   * if should show icon
   * @param showIcon1
   */
  public void setShowIcon(boolean showIcon1) {
    this.showIcon = showIcon1;
  }

  /**
   * gui attribute def name
   */
  private GuiAttributeDefName guiAttributeDefName;
  
  /**
   * gui attribute def
   */
  private GuiAttributeDef guiAttributeDef;
  
  /**
   * gui attribute def
   * @return gui attribute def
   */
  public GuiAttributeDef getGuiAttributeDef() {
    return this.guiAttributeDef;
  }

  /**
   * gui attribute def
   * @param guiAttributeDef1
   */
  public void setGuiAttributeDef(GuiAttributeDef guiAttributeDef1) {
    this.guiAttributeDef = guiAttributeDef1;
  }

  /**
   * gui attribute def name
   * @return gui attribute def name
   */
  public GuiAttributeDefName getGuiAttributeDefName() {
    return this.guiAttributeDefName;
  }

  /**
   * gui attribute def name
   * @param guiAttributeDefName1
   */
  public void setGuiAttributeDefName(GuiAttributeDefName guiAttributeDefName1) {
    this.guiAttributeDefName = guiAttributeDefName1;
  }

  /**
   * gui stem
   */
  private GuiStem guiStem;

  /**
   * gui stem
   * @return gui stem
   */
  public GuiStem getGuiStem() {
    return this.guiStem;
  }

  /**
   * gui stem
   * @param guiStem1
   */
  public void setGuiStem(GuiStem guiStem1) {
    this.guiStem = guiStem1;
  }

  /**
   * service
   */
  private GuiService guiService;
  
  /**
   * service
   * @return the guiService
   */
  public GuiService getGuiService() {
    return this.guiService;
  }
  
  /**
   * service
   * @param guiService1 the guiService to set
   */
  public void setGuiService(GuiService guiService1) {
    this.guiService = guiService1;
  }

  /**
   * gui group
   */
  private GuiGroup guiGroup;

  /**
   * gui group
   * @return gui group
   */
  public GuiGroup getGuiGroup() {
    return this.guiGroup;
  }

  /**
   * gui group
   * @param guiGroup1
   */
  public void setGuiGroup(GuiGroup guiGroup1) {
    this.guiGroup = guiGroup1;
  }
  
  /**
   * gui privilege
   */
  private GuiPrivilege guiPrivilege;
  
  /**
   * gui privilege
   * @return priv
   */
  public GuiPrivilege getGuiPrivilege() {
    return this.guiPrivilege;
  }

  /**
   * gui privilege
   * @param guiPrivilege1
   */
  public void setGuiPrivilege(GuiPrivilege guiPrivilege1) {
    this.guiPrivilege = guiPrivilege1;
  }

  /**
   * gui member
   */
  private GuiMember guiMember;

  /**
   * gui member
   * @return gui member
   */
  public GuiMember getGuiMember() {
    return this.guiMember;
  }

  /**
   * gui member
   * @param guiMember1
   */
  public void setGuiMember(GuiMember guiMember1) {
    this.guiMember = guiMember1;
  }
  
  /**
   * gui subject
   */
  private GuiSubject guiSubject;

  /**
   * if the logged in user can read rules, lazy loaded
   */
  private Boolean canReadRules;

  /**
   * if the logged in user can update rules, lazy loaded
   */
  private Boolean canUpdateRules;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(CommonRequestContainer.class);

  /**
   * gui subject
   * @return gui subject
   */
  public GuiSubject getGuiSubject() {
    return this.guiSubject;
  }

  /**
   * gui subject
   * @param guiSubject1
   */
  public void setGuiSubject(GuiSubject guiSubject1) {
    this.guiSubject = guiSubject1;
  }

  /**
   * if the logged in user can read rules, lazy loaded
   * @return if can read rules
   */
  public boolean isCanReadRules() {
    if (this.canReadRules == null) {
      try {
        final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        
        this.canReadRules = (Boolean)GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
              
              @Override
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                AttributeDef attributeDef = RuleUtils.ruleTypeAttributeDef();
                return attributeDef.getPrivilegeDelegate().canAttrRead(loggedInSubject);
              }
            });
        

      } catch (Exception e) {
        //ignore
        if (LOG.isDebugEnabled()) {
          LOG.debug("problem checking rule", e);
        }
      }
    }
    
    return this.canReadRules;
  
  }

  /**
   * if the logged in user can update rules, lazy loaded
   * @return if can update rules
   */
  public boolean isCanUpdateRules() {
    if (this.canUpdateRules == null) {
      try {
        final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        
        this.canUpdateRules = (Boolean)GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
              
              @Override
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                AttributeDef attributeDef = RuleUtils.ruleTypeAttributeDef();
                return attributeDef.getPrivilegeDelegate().canAttrUpdate(loggedInSubject);
              }
            });
        

      } catch (Exception e) {
        //ignore
        if (LOG.isDebugEnabled()) {
          LOG.debug("problem checking rule", e);
        }
      }
    }
    
    return this.canUpdateRules;
  
  }

  
}
