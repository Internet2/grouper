/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyInput;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh.GrouperGroovyResult;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
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
      Map<String, Object> substituteMap = overrideMap();

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
  private Map<String, String> textTypeToText = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {

      CustomUiTextType customUiTextType = key instanceof CustomUiTextType ? (CustomUiTextType)key : CustomUiTextType.valueOfIgnoreCase((String)key, true);
      
      Map<String, Object> substituteMap = overrideMap();
      
      return CustomUiContainer.this.customUiEngine.findBestText(customUiTextType, substituteMap);
    }
    
  };
  
  /**
   * has computed logic
   */
  private boolean hasComputedLogic = false;

  
  /**
   * has computed logic
   * @return the hasComputedLogic
   */
  public boolean isHasComputedLogic() {
    return this.hasComputedLogic;
  }


  
  /**
   * has computed logic
   * @param hasComputedLogic1 the hasComputedLogic to set
   */
  public void setHasComputedLogic(boolean hasComputedLogic1) {
    this.hasComputedLogic = hasComputedLogic1;
  }


  /**
   * @return the textTypeToText
   */
  public Map<String, String> getTextTypeToText() {
    return this.textTypeToText;
  }

  /**
   * if can change variables, null if not calculated
   */
  private Boolean canAssignVariables;
  
  /**
   * 
   * @return if can change variables
   */
  public boolean isCanAssignVariables() {
    if (this.canAssignVariables == null) {

      Boolean result = GrouperUtil.booleanObjectValue(this.getTextTypeToText().get(CustomUiTextType.canAssignVariables));
      if (result == null) {
        final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        result = PrivilegeHelper.isWheelOrRootOrReadonlyRoot(loggedInSubject);
      }
      this.canAssignVariables = result;
    }
    return this.canAssignVariables;
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
   * 
   * @return true if should add/remove member
   */
  public boolean isManageMembership() {
    
    String manageMembership = (String)this.getTextTypeToText().get(CustomUiTextType.manageMembership.name());
    return GrouperUtil.booleanValue(manageMembership, true);

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
   * if show user environment
   */
  private Boolean canSeeUserEnvironment;
  
  
  /**
   * @return the showUserEnvironment
   */
  public boolean isCanSeeUserEnvironment() {
    if (this.canSeeUserEnvironment == null) {
      Boolean result = GrouperUtil.booleanObjectValue(this.getTextTypeToText().get(CustomUiTextType.canSeeUserEnvironment));
      if (result == null) {
        result = this.isManager();
      }
      this.canSeeUserEnvironment = result;
    }
    return this.canSeeUserEnvironment;
  }
  
  /**
   * are we on an enroll or unenroll
   */
  private boolean enroll;
  
  /**
   * @return the enroll
   */
  public boolean isEnroll() {
    return this.enroll;
  }

  /**
   * @param enroll1 the enroll to set
   */
  public void setEnroll(boolean enroll1) {
    this.enroll = enroll1;
  }


  /**
   * @return the showScreenState
   */
  public boolean isCanSeeScreenState() {
    if (this.canSeeScreenState == null) {
      
      Boolean result = GrouperUtil.booleanObjectValue(this.getTextTypeToText().get(CustomUiTextType.canSeeScreenState));
      if (result == null || !result) {
        final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        result = PrivilegeHelper.isWheelOrRootOrReadonlyRoot(loggedInSubject);
      }
      this.canSeeScreenState = result;
    }
    return this.canSeeScreenState;
  }

  /**
   * if show screen state
   */
  private Boolean canSeeScreenState;
  
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
   */
  public void resetCache() {
    this.canAssignVariables = null;
    this.canSeeScreenState = null;
    this.canSeeUserEnvironment = null;
    this.manager = null;
  }

  private boolean leaveGroupButtonPressed = false;

  
  
  
  public boolean isLeaveGroupButtonPressed() {
    return leaveGroupButtonPressed;
  }


  
  public void setLeaveGroupButtonPressed(boolean leaveGroupButtonPressed) {
    this.leaveGroupButtonPressed = leaveGroupButtonPressed;
  }

  private boolean joinGroupButtonPressed = false;
  

  
  public boolean isJoinGroupButtonPressed() {
    return joinGroupButtonPressed;
  }


  
  public void setJoinGroupButtonPressed(boolean joinGroupButtonPressed) {
    this.joinGroupButtonPressed = joinGroupButtonPressed;
  }

  public String gshRunScript(Group group, Subject subject, Subject subjectLoggedIn, String scriptPart) {
    final StringBuilder script = new StringBuilder();
    
    Map<String, Object> variables = new TreeMap<String, Object>();
    
    variables.putAll(GrouperUtil.nonNull(this.overrideMap()));
    variables.putAll(GrouperUtil.nonNull(this.customUiEngine.userQueryVariables()));

    GrouperGroovyInput grouperGroovyInput = new GrouperGroovyInput();

    for (String key: new TreeSet<String>(variables.keySet())) {
      
      Object value = variables.get(key);
      grouperGroovyInput.assignInputValueObject(key, value);
      if (value instanceof Number) {
        script.append("Number " + key + " = (Number)grouperGroovyRuntime.retrieveInputValueObject(\"" + key + "\");\n");
      } else if (value instanceof Boolean) {
        script.append("boolean " + key + " = grouperGroovyRuntime.retrieveInputValueBoolean(\"" + key + "\");\n");
      } else if (value instanceof String) {
        script.append("String " + key + " = grouperGroovyRuntime.retrieveInputValueString(\"" + key + "\");\n");
      } else if (value == null) {
        script.append("Object " + key + " = null;\n");
      }
      
    }
   return (String) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        grouperGroovyInput.assignInputValueObject("subject", subject);
        script.append("Subject subject = (Subject)grouperGroovyRuntime.retrieveInputValueObject(\"subject\");\n");
        grouperGroovyInput.assignInputValueObject("subjectLoggedIn", subjectLoggedIn);
        script.append("Subject subjectLoggedIn = (Subject)grouperGroovyRuntime.retrieveInputValueObject(\"subjectLoggedIn\");\n");
        grouperGroovyInput.assignInputValueObject("group", group);
        script.append("Group group = (Group)grouperGroovyRuntime.retrieveInputValueObject(\"group\");\n");
        script.append("GrouperSession grouperSession = grouperGroovyRuntime.getGrouperSession();\n");
        script.append(scriptPart);
        grouperGroovyInput.assignScript(script.toString());
        GrouperGroovyResult grouperGroovyResult = new GrouperGroovyResult();
        GrouperGroovysh.runScript(grouperGroovyInput, grouperGroovyResult);
        return grouperGroovyResult.fullOutput();
      }
    });
  }
  
  /**
   * @return map
   */
  public Map<String, Object> overrideMap() {
    
    Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
    substituteMap.put("grouperRequestContainer", GrouperRequestContainer.retrieveFromRequestOrCreate());
    substituteMap.put("request", GrouperUiFilter.retrieveHttpServletRequest());
    substituteMap.put("textContainer", GrouperTextContainer.retrieveFromRequest());
    substituteMap.put("cu_joinGroupButtonPressed", false);
    substituteMap.put("cu_leaveGroupButtonPressed", false);
    if (this.joinGroupButtonPressed) {
      substituteMap.put("cu_joinGroupButtonPressed", true);
    } else if (this.leaveGroupButtonPressed) {
      substituteMap.put("cu_leaveGroupButtonPressed", true);
    }
    return substituteMap;
  }

  
}
