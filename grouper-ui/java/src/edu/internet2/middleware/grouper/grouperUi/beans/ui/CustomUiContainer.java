/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.customUi.CustomUiEngine;
import edu.internet2.middleware.grouper.ui.customUi.CustomUiTextType;
import edu.internet2.middleware.grouper.ui.customUi.CustomUiUserQueryDisplayBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class CustomUiContainer {

  /**
   * 
   * @return the value for combobox
   */
  public String getUserComboboxValue() {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    final Member loggedInMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), loggedInSubject, true);

    if (!StringUtils.equals(loggedInMember.getId(), this.member.getId())) {
      
      return this.member.getSubjectSourceId() + "||" + this.member.getSubjectId();
      
    }
    return "";
  }
  
  /**
   * member to operate on
   */
  private Member member;
  

  
  /**
   * @return the member
   */
  public Member getMember() {
    return this.member;
  }

  
  /**
   * @param member the member to set
   */
  public void setMember(Member member) {
    this.member = member;
  }

  private boolean calculatedDisplayBeans = false;
  
  /**
   * 
   * @return the set of display beans
   */
  public Set<CustomUiUserQueryDisplayBean> getCustomUiUserQueryDisplayBeans() {
    if (!this.calculatedDisplayBeans) {
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
      substituteMap.put("grouperRequestContainer", GrouperRequestContainer.retrieveFromRequestOrCreate());
      substituteMap.put("request", GrouperUiFilter.retrieveHttpServletRequest());
      substituteMap.put("textContainer", GrouperTextContainer.retrieveFromRequest());

      this.customUiEngine.generateUserQueryDisplayBeans(substituteMap);
    }
    return this.customUiEngine.getCustomUiUserQueryDisplayBeans();
  }
  
  public String getLogCustomUiEngine() {
    if (LOG.isDebugEnabled()) {
      LOG.debug(GrouperUtil.mapToString(this.customUiEngine.getDebugMap()));
    }
    return "";
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(CustomUiContainer.class);

  /**
   * map from text type to text
   */
  private Map<String, Object> textTypeToText = new HashMap<String, Object>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {

      CustomUiTextType customUiTextType = CustomUiTextType.valueOfIgnoreCase((String)key, true);
      
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
      substituteMap.put("grouperRequestContainer", GrouperRequestContainer.retrieveFromRequestOrCreate());
      substituteMap.put("request", GrouperUiFilter.retrieveHttpServletRequest());
      substituteMap.put("textContainer", GrouperTextContainer.retrieveFromRequest());
      
      return CustomUiContainer.this.customUiEngine.findBestText(customUiTextType, substituteMap);
    }
    
  };
  
  /**
   * @return the textTypeToText
   */
  public Map<String, Object> getTextTypeToText() {
    return this.textTypeToText;
  }

  /**
   * if can change variables, null if not calculated
   */
  private Boolean canChangeVariables;
  
  /**
   * 
   * @return if can change variables
   */
  public boolean isCanChangeVariables() {
    if (this.canChangeVariables == null) {
      
      Boolean overrideOff = (Boolean)this.customUiEngine.userQueryVariables().get("cu_grouperTurnOffManager");
      if (overrideOff != null && overrideOff) {
        this.canChangeVariables = false;
      }
    }      
    if (this.canChangeVariables == null) {
      
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
      substituteMap.put("grouperRequestContainer", GrouperRequestContainer.retrieveFromRequestOrCreate());
      substituteMap.put("request", GrouperUiFilter.retrieveHttpServletRequest());
      substituteMap.put("textContainer", GrouperTextContainer.retrieveFromRequest());

      Object result = this.customUiEngine.findBestText(CustomUiTextType.canAssignVariables, substituteMap);
      if (result != null) {
        this.canChangeVariables = GrouperUtil.booleanObjectValue(result);
      }
    }
    return this.canChangeVariables;
  }
  
  /**
   * custom ui engine
   */
  private CustomUiEngine customUiEngine = null;
  
  /**
   * custom ui engine
   * @return the customUiEngine
   */
  public CustomUiEngine getCustomUiEngine() {
    return this.customUiEngine;
  }

  /**
   * custom ui engine
   * @param customUiEngine1 the customUiEngine to set
   */
  public void setCustomUiEngine(CustomUiEngine customUiEngine1) {
    this.customUiEngine = customUiEngine1;
  }

  /**
   * attribute names for screen
   * @return attributes
   */
  public Set<String> getAttributeNames() {
    Map<String, Object> userQueryVariables = this.customUiEngine.userQueryVariables();
    if (userQueryVariables == null) {
      return null;
    }
    return userQueryVariables.keySet();
  }
  
  
  /**
   * 
   */
  public CustomUiContainer() {
  }

  /**
   * if should show enroll button
   * @return true if should show
   */
  public boolean isEnrollButtonShow() {
    String show = (String)this.getTextTypeToText().get(CustomUiTextType.enrollButtonShow.name());
    if (!StringUtils.isBlank(show)) {
      return GrouperUtil.booleanValue(show);
    }
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    // else if can optin
    final Group group = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    
    boolean isMember = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return group.hasMember(loggedInSubject);
      }
    });  
    
    return !isMember && group.canHavePrivilege(loggedInSubject, "optins", false);
  }
  
  /**
   * 
   * @return the text
   */
  public String getEnrollButtonText() {
    String buttonText = (String)this.getTextTypeToText().get(CustomUiTextType.enrollButtonText.name());
    if (!StringUtils.isBlank(buttonText)) {
      return buttonText;
    }
    return TextContainer.retrieveFromRequest().getText().get("guiCustomUiGroupDefaultEnrollButtonText");
  }
  
  /**
   * cache if manager
   */
  private Boolean manager = null;

  /**
   * cache of privs for custom ui
   */
  private static ExpirableCache<MultiKey, Boolean> subjectSourceSubjectIdGroupNameFieldNameCache = new ExpirableCache<MultiKey, Boolean>(2);
  
  /**
   * if manager
   * @return true if should show
   */
  public boolean isManager() {
    
    if (this.manager == null && this.customUiEngine != null) {
      
      final Map<String, Object> userQueryVariables = this.customUiEngine.userQueryVariables();
      if (userQueryVariables != null) {
        Boolean overrideOff = (Boolean)userQueryVariables.get("cu_grouperTurnOffManager");
        if (overrideOff != null && overrideOff) {
          this.manager = false;
        }
      }
    }      

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    // else if can optin
    final Group group = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();

    MultiKey readerKey = new MultiKey(loggedInSubject.getSourceId(), loggedInSubject.getId(), group.getName(), "readers");
    MultiKey updaterKey = new MultiKey(loggedInSubject.getSourceId(), loggedInSubject.getId(), group.getName(), "updaters");
    Boolean readerResult = null;
    Boolean updaterResult = null;
    
    if (this.manager == null) {
      readerResult = subjectSourceSubjectIdGroupNameFieldNameCache.get(readerKey);
      updaterResult = subjectSourceSubjectIdGroupNameFieldNameCache.get(updaterKey);
      if (readerResult != null && updaterResult != null) {
        this.manager = readerResult && updaterResult;
      }
    }

    if (this.manager == null) {
      readerResult = group.canHavePrivilege(loggedInSubject, "readers", false);
      updaterResult = group.canHavePrivilege(loggedInSubject, "updaters", false);
  
      this.manager = readerResult && updaterResult;
      
      subjectSourceSubjectIdGroupNameFieldNameCache.put(readerKey, readerResult);
      subjectSourceSubjectIdGroupNameFieldNameCache.put(updaterKey, updaterResult);
    }
    return this.manager;
  }


  /**
   * if should show unenroll button
   * @return true if should show
   */
  public boolean isUnenrollButtonShow() {
    String show = (String)this.getTextTypeToText().get(CustomUiTextType.unenrollButtonShow.name());
    if (!StringUtils.isBlank(show)) {
      return GrouperUtil.booleanValue(show);
    }
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    // else if can optin
    final Group group = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    
    
    boolean isMember = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return group.hasMember(loggedInSubject);
      }
    });  

    return isMember && group.canHavePrivilege(loggedInSubject, "optouts", false);
  }
  
  /**
   * 
   * @return the text
   */
  public String getUnenrollButtonText() {
    String buttonText = (String)this.getTextTypeToText().get(CustomUiTextType.unenrollButtonText.name());
    if (!StringUtils.isBlank(buttonText)) {
      return buttonText;
    }
    return TextContainer.retrieveFromRequest().getText().get("guiCustomUiGroupDefaultUnenrollButtonText");
  }

  /**
   * @param b
   */
  public void resetCache() {
    this.canChangeVariables = null;
    this.manager = null;
  }

  
}
