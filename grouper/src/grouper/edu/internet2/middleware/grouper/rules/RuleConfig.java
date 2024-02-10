package edu.internet2.middleware.grouper.rules;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;

public class RuleConfig {
  
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
  
  private boolean runDaemon = true;
  
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
    
    RuleCheckType ruleCheckType = RuleCheckType.valueOfIgnoreCase(this.getCheckType(), false);
    
    if (ruleCheckType != null) {
      return ruleCheckType.usesArg0();
    }
    
    return false;
  }
  
  public boolean isCheckUsesArg1() {
    
    RuleCheckType ruleCheckType = RuleCheckType.valueOfIgnoreCase(this.getCheckType(), false);
    
    if (ruleCheckType != null) {
      return ruleCheckType.usesArg1();
    }
    
    return false;
  }
  
  public boolean isIfUsesArg0() {
    
    RuleIfConditionEnum ruleIfConditionEnum = RuleIfConditionEnum.valueOfIgnoreCase(this.getIfConditionOption(), false);
    
    if (ruleIfConditionEnum != null) {
      return ruleIfConditionEnum.usesArg0();
    }
    
    return false;
  }
  
  public boolean isIfUsesArg1() {
    
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
    
    RuleThenEnum ruleThenEnum = RuleThenEnum.valueOfIgnoreCase(this.getThenOption(), false);
    
    if (ruleThenEnum != null) {
      return ruleThenEnum.usesArg0();
    }
    
    return false;
  }
  
  public boolean isThenUsesArg1() {
    
    RuleThenEnum ruleThenEnum = RuleThenEnum.valueOfIgnoreCase(this.getThenOption(), false);
    
    if (ruleThenEnum != null) {
      return ruleThenEnum.usesArg1();
    }
    
    return false;
  }
  
  public boolean isThenUsesArg2() {
    
    RuleThenEnum ruleThenEnum = RuleThenEnum.valueOfIgnoreCase(this.getThenOption(), false);
    
    if (ruleThenEnum != null) {
      return ruleThenEnum.usesArg2();
    }
    
    return false;
  }

  
  public boolean isRunDaemon() {
    return runDaemon;
  }

  
  public void setRunDaemon(boolean runDaemon) {
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

}
