package edu.internet2.middleware.grouper.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

public class RuleConfig {
  
  private String pattern;
  
  private String checkType;
  
  private String checkOwner;
  
  private String checkOwnerUuidOrName;
  
  private String ifConditionOption;

  private String ifConditionEl;
  
  private String ifConditionOwner;
  
  private String ifConditionOwnerUuidOrName; 
  
  private String thenEl;
  
  private String thenOption;
  
  private String checkOwnerStemScope;
  
  private String ifConditionOwnerStemScope;
  
  private String checkArg0;

  private String checkArg1;
  
  private String ifConditionArg0;

  private String ifConditionArg1;
  
  private String thenArg0;

  private String thenArg1;

  private String thenArg2;
  
  private Boolean runDaemon;
  
  private GrouperObject grouperObject;
  
  private Map<String, String> patternPropertiesValues = new HashMap<>();

  private RuleDefinition ruleDefinition;
  
  public RuleConfig(Subject subject, GrouperObject grouperObject) {
    this.subject = subject;
    this.grouperObject = grouperObject;
  }
  
  private Subject subject;
  
  
  /**
   * if the logged in user can set daemon
   * @return 
   */
  public boolean isCanSetDaemon() {
    
    if ( StringUtils.isNotBlank(this.pattern) && !StringUtils.equals(this.pattern, "custom")) {
       RulePattern rulePattern = RulePattern.valueOf(this.pattern);
       if (rulePattern.isDaemonApplicable()) {
         if (PrivilegeHelper.isWheelOrRoot(subject)) {
           return true;
         } else if (rulePattern.isDaemonAssignableByNonAdmin()) {
           return true;
         }
       }
    }
    
    return false;
  }
  
  
  public Map<String, String> getPatternPropertiesValues() {
    return patternPropertiesValues;
  }



  
  public void setPatternPropertiesValues(Map<String, String> patternPropertiesValues) {
    this.patternPropertiesValues = patternPropertiesValues;
  }



  public GrouperObject getGrouperObject() {
    return grouperObject;
  }


  public String getPattern() {
    return pattern;
  }

  
  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
  
  
  public RulePattern getRulePattern() {
    if ( StringUtils.isNotBlank(this.pattern) && !StringUtils.equals(this.pattern, "custom")) {
      return RulePattern.valueOf(this.pattern);
    }
    
    return null;
  }
  
  private boolean canSubjectViewStem(String stemIdOrName, StemPrivilegeStrategy stemPrivilegeStrategy) {
    
    if (PrivilegeHelper.isWheelOrRootOrViewonlyRoot(this.subject)) {
      return true;
    }
    
    Boolean canView = (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Stem stem = StemFinder.findByName(stemIdOrName, false);
        if (stem == null) {
          stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemIdOrName, false);
        }
        
        if (stemPrivilegeStrategy != null) {
          return stemPrivilegeStrategy.canSubjectViewStem(RuleConfig.this.subject, stem);
        }
        return false;
      }
     });
    return canView;
  }
  
  
  private boolean canSubjectViewGroup(String groupIdOrName, GroupPrivilegeStrategy groupPrivilegeStrategy) {
    
    if (PrivilegeHelper.isWheelOrRootOrViewonlyRoot(this.subject)) {
      return true;
    }
    
    Boolean canView = (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Group group = GroupFinder.findByName(groupIdOrName, false);
        if (group == null) {
          group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupIdOrName, false);
        }
        
        if (groupPrivilegeStrategy != null) {
          return groupPrivilegeStrategy.canSubjectViewGroup(RuleConfig.this.subject, group);
        }
        return false;
      }
     });
    return canView;
  }
  
  public List<GrouperConfigurationModuleAttribute> getElementsToShow() {
    if ( StringUtils.isNotBlank(this.pattern) && !StringUtils.equals(this.pattern, "custom")) {
      
//       boolean canViewCheckGrouperObject = true;
//       if (!StringUtils.equals(this.getCheckOwner(), "thisStem") && !StringUtils.equals(this.getCheckOwner(), "thisGroup") 
//           && this.getCheckOwnerType() != null) {
//         
//         RuleCheckType ruleCheckType = RuleCheckType.valueOfIgnoreCase(RuleConfig.this.checkType, true);
//         
//         if (this.getCheckOwnerType() == RuleOwnerType.FOLDER) {
//           String stemIdOrName = this.getCheckOwnerUuidOrName();
//           StemPrivilegeStrategy stemPrivilegeStrategy = ruleCheckType.getStemPrivilegeStrategy();
//           canViewCheckGrouperObject = canSubjectViewStem(stemIdOrName, stemPrivilegeStrategy);
//         } else if (this.getCheckOwnerType() == RuleOwnerType.GROUP) {
//           String groupIdOrName = this.getCheckOwnerUuidOrName();
//           GroupPrivilegeStrategy groupPrivilegeStrategy = ruleCheckType.getGroupPrivilegeStrategy();
//           canViewCheckGrouperObject = canSubjectViewGroup(groupIdOrName, groupPrivilegeStrategy);
//         }
//       }
//       
//       boolean canViewConditionGrouperObject = true;
//       if (!StringUtils.equals(this.getIfConditionOwner(), "thisStem") && !StringUtils.equals(this.getIfConditionOwner(), "thisGroup") && 
//           this.getIfConditionOwnerType() != null) {
//         
//         RuleIfConditionEnum ruleIfConditionEnum = RuleIfConditionEnum.valueOfIgnoreCase(RuleConfig.this.getIfConditionOption(), true);
//         
//         if (this.getIfConditionOwnerType() == RuleOwnerType.FOLDER) {
//           String stemIdOrName = this.getIfConditionOwnerUuidOrName();
//           StemPrivilegeStrategy stemPrivilegeStrategy = ruleIfConditionEnum.getStemPrivilegeStrategy();
//           canViewConditionGrouperObject = canSubjectViewStem(stemIdOrName, stemPrivilegeStrategy);
//         } else if (this.getIfConditionOwnerType() == RuleOwnerType.GROUP) {
//           String groupIdOrName = this.getIfConditionOwnerUuidOrName();
//           GroupPrivilegeStrategy groupPrivilegeStrategy = ruleIfConditionEnum.getGroupPrivilegeStrategy();
//           canViewConditionGrouperObject = canSubjectViewGroup(groupIdOrName, groupPrivilegeStrategy);
//         }
//       }
       
       List<GrouperConfigurationModuleAttribute> elementsToShow = RulePattern.valueOf(this.pattern).getElementsToShow(this.grouperObject, this.ruleDefinition);
       
       return elementsToShow;
    }
    return new ArrayList<>();
  }

  
  public String getCheckType() {
    return checkType;
  }
  
  public void setCheckType(String checkType) {
    this.checkType = checkType;
  }

  public RuleOwnerType getCheckOwnerType() {
    
    RuleCheckType ruleCheckType = RuleCheckType.valueOfIgnoreCase(this.getCheckType(), false);
    
    if (ruleCheckType != null) {
      return ruleCheckType.getOwnerType();
    }
    
    return null;
  }

  
  public String getCheckOwnerUuidOrName() {
    return checkOwnerUuidOrName;
  }

  
  public void setCheckOwnerUuidOrName(String checkOwnerUuidOrName) {
    this.checkOwnerUuidOrName = checkOwnerUuidOrName;
  }

  
  public String getIfConditionOption() {
    return ifConditionOption;
  }

  
  public void setIfConditionOption(String ifConditionOption) {
    this.ifConditionOption = ifConditionOption;
  }

  public String getIfConditionEl() {
    return ifConditionEl;
  }

  
  public void setIfConditionEl(String ifConditionEl) {
    this.ifConditionEl = ifConditionEl;
  }

  
  public String getIfConditionOwnerUuidOrName() {
    return ifConditionOwnerUuidOrName;
  }

  
  public void setIfConditionOwnerUuidOrName(String ifConditionOwnerUuidOrName) {
    this.ifConditionOwnerUuidOrName = ifConditionOwnerUuidOrName;
  }

  
  public String getThenEl() {
    return thenEl;
  }

  
  public void setThenEl(String thenEl) {
    this.thenEl = thenEl;
  }

  
  public String getCheckOwnerStemScope() {
    return checkOwnerStemScope;
  }

  
  public void setCheckOwnerStemScope(String checkOwnerStemScope) {
    this.checkOwnerStemScope = checkOwnerStemScope;
  }

  
  public String getIfConditionOwnerStemScope() {
    return ifConditionOwnerStemScope;
  }

  
  public void setIfConditionOwnerStemScope(String ifConditionOwnerStemScope) {
    this.ifConditionOwnerStemScope = ifConditionOwnerStemScope;
  }

  
  public RuleOwnerType getIfConditionOwnerType() {
    
    if (StringUtils.equals(this.getIfConditionOption(), "EL")) {
      return null;
    }
    
    RuleIfConditionEnum ifConditionEnum = RuleIfConditionEnum.valueOfIgnoreCase(this.getIfConditionOption(), false);
    
    if (ifConditionEnum != null) {
      return ifConditionEnum.getOwnerType();
    }
    
    return null;
  }
  
  public boolean isCheckUsesArg0() {
    
    if (StringUtils.equals(this.getCheckType(), "EL")) {
      return false;
    }
    
    RuleCheckType ruleCheckType = RuleCheckType.valueOfIgnoreCase(this.getCheckType(), false);
    
    if (ruleCheckType != null) {
      return ruleCheckType.usesArg0();
    }
    
    return false;
  }
  
  public boolean isCheckUsesArg1() {
    
    if (StringUtils.equals(this.getCheckType(), "EL")) {
      return false;
    }
    
    RuleCheckType ruleCheckType = RuleCheckType.valueOfIgnoreCase(this.getCheckType(), false);
    
    if (ruleCheckType != null) {
      return ruleCheckType.usesArg1();
    }
    
    return false;
  }
  
  public boolean isIfUsesArg0() {
    
    if (StringUtils.equals(this.getIfConditionOption(), "EL")) {
      return false;
    }
    
    RuleIfConditionEnum ruleIfConditionEnum = RuleIfConditionEnum.valueOfIgnoreCase(this.getIfConditionOption(), false);
    
    if (ruleIfConditionEnum != null) {
      return ruleIfConditionEnum.usesArg0();
    }
    
    return false;
  }
  
  public boolean isIfUsesArg1() {
    
    if (StringUtils.equals(this.getIfConditionOption(), "EL")) {
      return false;
    }
    
    RuleIfConditionEnum ruleIfConditionEnum = RuleIfConditionEnum.valueOfIgnoreCase(this.getIfConditionOption(), false);
    
    if (ruleIfConditionEnum != null) {
      return ruleIfConditionEnum.usesArg1();
    }
    
    return false;
  }

  
  public String getIfConditionOwner() {
    return ifConditionOwner;
  }

  
  public void setIfConditionOwner(String ifConditionOwner) {
    this.ifConditionOwner = ifConditionOwner;
  }

  
  public String getCheckArg0() {
    return checkArg0;
  }

  
  public void setCheckArg0(String checkArg0) {
    this.checkArg0 = checkArg0;
  }

  
  public String getCheckArg1() {
    return checkArg1;
  }

  
  public void setCheckArg1(String checkArg1) {
    this.checkArg1 = checkArg1;
  }

  
  public String getIfConditionArg0() {
    return ifConditionArg0;
  }

  
  public void setIfConditionArg0(String ifConditionArg0) {
    this.ifConditionArg0 = ifConditionArg0;
  }

  
  public String getIfConditionArg1() {
    return ifConditionArg1;
  }

  
  public void setIfConditionArg1(String ifConditionArg1) {
    this.ifConditionArg1 = ifConditionArg1;
  }

  
  public String getThenArg0() {
    return thenArg0;
  }

  
  public void setThenArg0(String thenArg0) {
    this.thenArg0 = thenArg0;
  }

  
  public String getThenArg1() {
    return thenArg1;
  }

  
  public void setThenArg1(String thenArg1) {
    this.thenArg1 = thenArg1;
  }

  
  public String getThenArg2() {
    return thenArg2;
  }

  
  public void setThenArg2(String thenArg2) {
    this.thenArg2 = thenArg2;
  }

  
  public String getThenOption() {
    return thenOption;
  }

  
  public void setThenOption(String thenOption) {
    this.thenOption = thenOption;
  }
  
  public boolean isThenUsesArg0() {
    
    if (StringUtils.equals(this.getThenOption(), "EL")) {
      return false;
    }
    
    RuleThenEnum ruleThenEnum = RuleThenEnum.valueOfIgnoreCase(this.getThenOption(), false);
    
    if (ruleThenEnum != null) {
      return ruleThenEnum.usesArg0();
    }
    
    return false;
  }
  
  public boolean isThenUsesArg1() {
    
    if (StringUtils.equals(this.getThenOption(), "EL")) {
      return false;
    }
    
    RuleThenEnum ruleThenEnum = RuleThenEnum.valueOfIgnoreCase(this.getThenOption(), false);
    
    if (ruleThenEnum != null) {
      return ruleThenEnum.usesArg1();
    }
    
    return false;
  }
  
  public boolean isThenUsesArg2() {
    
    if (StringUtils.equals(this.getThenOption(), "EL")) {
      return false;
    }
    
    RuleThenEnum ruleThenEnum = RuleThenEnum.valueOfIgnoreCase(this.getThenOption(), false);
    
    if (ruleThenEnum != null) {
      return ruleThenEnum.usesArg2();
    }
    
    return false;
  }

  
  
  public Boolean getRunDaemon() {
    return runDaemon;
  }

  public void setRunDaemon(Boolean runDaemon) {
    this.runDaemon = runDaemon;
  }

  
  public String getCheckOwner() {
    return checkOwner;
  }

  
  public void setCheckOwner(String checkOwner) {
    this.checkOwner = checkOwner;
  }
  
  public void populateCheckOwnerValuesForStem(Stem stem) {
    
    String checkOwnerName = null;
    String checkOwnerStemScope = null;
    
    //rule is being assigned on a folder 
    String checkOwner = this.getCheckOwner();
    if (StringUtils.isNotBlank(checkOwner)) {
      
      checkOwnerStemScope = this.getCheckOwnerStemScope();
      
      //value must be thisStem, anotherStem
      if (StringUtils.equals(checkOwner, "thisStem")) {
        checkOwnerName = stem.getName();
      } else if (StringUtils.equals(checkOwner, "anotherStem")) {
        String stemIdOrName = this.getCheckOwnerUuidOrName();
        Stem checkOwnerStem = StemFinder.findByName(stemIdOrName, false);
        if (checkOwnerStem == null) {
          checkOwnerStem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemIdOrName, false);
        }
        
        if (checkOwnerStem != null) {
          checkOwnerName = checkOwnerStem.getName();
        } else {
          //Add error and return
        }
        
      }
    } else {
      //must be group if not blank
      String groupIdOrName = this.getCheckOwnerUuidOrName();
      if (StringUtils.isNotBlank(groupIdOrName)) {
        Group group = GroupFinder.findByName(groupIdOrName, false);
        if (group == null) {
          group = GroupFinder.findByUuid(groupIdOrName, false);
        }
        
        if (group != null) {
          checkOwnerName = group.getName();
        } else {
          //Add error and return
        }
      }
      
    }
  }




  public void setRuleDefinition(RuleDefinition ruleDef) {
    this.ruleDefinition = ruleDef;
  }

  
  public RuleDefinition getRuleDefinition() {
    return ruleDefinition;
  }
  
  

}
