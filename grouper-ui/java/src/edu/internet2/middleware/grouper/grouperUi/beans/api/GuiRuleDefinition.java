/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.RulesContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RulePattern;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;


/**
 * rule to be displayed on screen
 */
public class GuiRuleDefinition implements Serializable, Comparable {
  
  
  public static final ExpirableCache<MultiKey, Boolean> rulesEditors = new ExpirableCache<MultiKey, Boolean>(5);

  /**
   * rule definition this is based on
   */
  private RuleDefinition ruleDefinition;
  
  /**
   * 
   */
  private GuiStem ownerGuiStem;

  /**
   * gui subject for arg0 of then for rule
   */
  private GuiSubject thenArg0subject;

  /**
   * if this is a direct rule
   */
  private boolean direct;
  
  /**
   * if this is a direct rule
   * @return the direct
   */
  public boolean isDirect() {
    return this.direct;
  }
  
  /**
   * is check stem scope one
   * @return if one
   */
  public boolean isCheckStemScopeOne() {
    Stem.Scope scope = this.ruleDefinition.getCheck().stemScopeEnum();
    return scope == Scope.ONE;
  }
  
  /**
   * 
   * @return then arg 1 privileges
   */
  public String getThenArg1privileges() {
    String thenArg1privilegesString = this.ruleDefinition.getThen().getThenEnumArg1();
    if (StringUtils.isBlank(thenArg1privilegesString)) {
      return null;
    }
    
    StringBuilder result = new StringBuilder();
    
    Set<String> privilegesStringSet = GrouperUtil.splitTrimToSet(thenArg1privilegesString, ",");
    
    String separator = TextContainer.retrieveFromRequest().getText().get("rulesPrivilegesSeparator");
    
    for (String privilegeString: privilegesStringSet) {
      Privilege privilege = Privilege.getInstance(privilegeString);
      
      String privilegeStringForScreen = TextContainer.retrieveFromRequest().getText().get("priv." + privilege.getName());
      
      if (result.length() != 0) {
        result.append(separator).append(" ");
      }
      
      result.append(privilegeStringForScreen);
      
      //priv.optin=Optin
      //priv.optout=Optout
      //priv.view=View
      //priv.read=Read
      //priv.update=Update
      //priv.admin=Admin
      //priv.groupAttrRead=Attribute read
      //priv.groupAttrUpdate=Attribute update
      //
      //priv.create=Create
      //priv.stemAdmin=Admin
      //priv.stemAttrRead=Attribute read
      //priv.stemAttrUpdate=Attribute update
      //
      //priv.attrOptin=Optin
      //priv.attrOptout=Optout
      //priv.attrView=View
      //priv.attrRead=Read
      //priv.attrUpdate=Update
      //priv.attrAdmin=Admin
      //priv.attrDefAttrRead=Attribute read
      //priv.attrDefAttrUpdate=Attribute update

      
    }
    return result.toString();
  }
  
  /**
   * is check stem scope one
   * @return if one
   */
  public boolean isCheckStemScopeSub() {
    Stem.Scope scope = this.ruleDefinition.getCheck().stemScopeEnum();
    return scope == Scope.SUB;
  }
  
  /**
   * if this is a direct rule
   * @param direct1 the direct to set
   */
  public void setDirect(boolean direct1) {
    this.direct = direct1;
  }

  /**
   * 
   * @return then type
   */
  public String getThenTypeLabel() {
    
    switch(this.ruleDefinition.getThen().thenEnum()) {
      case assignGroupPrivilegeToGroupId:
        return TextContainer.retrieveFromRequest().getText().get("rulesThenTypeGroup");
        
      case assignStemPrivilegeToStemId:
        
        return TextContainer.retrieveFromRequest().getText().get("rulesThenTypeFolder");
        
      case assignAttributeDefPrivilegeToAttributeDefId:
        
        return TextContainer.retrieveFromRequest().getText().get("rulesThenTypeAttribute");
       default:
         LOG.debug("Not expecting then enum: " + this.ruleDefinition.getThen().thenEnum());
    }
    
    return "";
  }
  
  /**
   * gui subject for arg0 of then for rule
   * @return the thenArg0subject
   */
  public GuiSubject getThenArg0subject() {
    if (this.thenArg0subject == null) {
      if (this.assignedToStem) {
        String arg0 = this.ruleDefinition.getThen().getThenEnumArg0();
        if (!StringUtils.isBlank(arg0)) {
          Subject subject = SubjectFinder.findByPackedSubjectString(arg0, false);
          if (subject != null) {
            this.thenArg0subject = new GuiSubject(subject);
          }
        }
      }
    }
    
    return this.thenArg0subject;
  }
  
  /**
   * gui subject for arg0 of then for rule
   * @param thenArg0subject1 the thenArg0subject to set
   */
  public void setThenArg0subject(GuiSubject thenArg0subject1) {
    this.thenArg0subject = thenArg0subject1;
  }

  /**
   * if rule is assigned to stem
   */
  private boolean assignedToStem;

  /** logger */
  protected static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(GuiRuleDefinition.class);
  
  /**
   * @return the ruleDefinition
   */
  public RuleDefinition getRuleDefinition() {
    return this.ruleDefinition;
  }

  
  /**
   * @param ruleDefinition1 the ruleDefinition to set
   */
  public void setRuleDefinition(RuleDefinition ruleDefinition1) {
    this.ruleDefinition = ruleDefinition1;
  }

  
  /**
   * @return the ownerGuiStem
   */
  public GuiStem getOwnerGuiStem() {
    if (this.ownerGuiStem == null) {
      if (this.assignedToStem) {
        Stem stem = this.ruleDefinition.getAttributeAssignType().getOwnerStemFailsafe();
        if (stem != null) {
          this.ownerGuiStem = new GuiStem(stem);
        }
      }
    }
    return this.ownerGuiStem;
  }

  
  /**
   * @param ownerGuiStem1 the ownerGuiStem to set
   */
  public void setOwnerGuiStem(GuiStem ownerGuiStem1) {
    this.ownerGuiStem = ownerGuiStem1;
  }

  
  /**
   * @return the assignedToStem
   */
  public boolean isAssignedToStem() {
    return this.assignedToStem;
  }

  
  /**
   * @param assignedToStem1 the assignedToStem to set
   */
  public void setAssignedToStem(boolean assignedToStem1) {
    this.assignedToStem = assignedToStem1;
  }

  /**
   * 
   */
  public GuiRuleDefinition() {
  }

  /**
   * 
   * @param theRuleDefinition
   */
  public GuiRuleDefinition(RuleDefinition theRuleDefinition) {
    this.ruleDefinition = theRuleDefinition;

    switch (this.ruleDefinition.getAttributeAssignType().getAttributeAssignType()) {
      case stem:
        this.assignedToStem = true;
        break;
      default: 
        break;
    }
    
  }
  
  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    
    if (o == null) {
      return 1;
    }
    if (this == o) {
      return 0;
    }
    if (!(o instanceof GuiRuleDefinition)) {
      return -1;
    }
    GuiRuleDefinition other = (GuiRuleDefinition)o;
    if (this.assignedToStem != other.assignedToStem) {
      return this.assignedToStem ? 1 : -1;
    }
    int compare = -1;
    if (this.assignedToStem) {
      compare = StringUtils.defaultString(this.getOwnerGuiStem().getStem().getDisplayName())
          .compareTo(StringUtils.defaultString(other.getOwnerGuiStem().getStem().getDisplayName()));
      if (compare != 0) {
        return compare;
      }
    }
    compare = StringUtils.defaultString(this.ruleDefinition.getThen().getThenEnum()).compareTo(
        StringUtils.defaultString(other.getRuleDefinition().getThen().getThenEnum()));
    if (compare != 0) {
      return compare;
    }
    compare = StringUtils.defaultString(this.ruleDefinition.getThen().getThenEnumArg0()).compareTo(
        StringUtils.defaultString(other.getRuleDefinition().getThen().getThenEnumArg0()));
    if (compare != 0) {
      return compare;
    }
    compare = StringUtils.defaultString(this.ruleDefinition.getThen().getThenEnumArg1()).compareTo(
        StringUtils.defaultString(other.getRuleDefinition().getThen().getThenEnumArg1()));
    if (compare != 0) {
      return compare;
    }
    compare = StringUtils.defaultString(this.ruleDefinition.getCheck().getCheckStemScope()).compareTo(
        StringUtils.defaultString(other.getRuleDefinition().getCheck().getCheckStemScope()));
    if (compare != 0) {
      return compare;
    }
    //equal?  :)
    return 0;
  }
  
  
  private GuiObjectBase checkGuiObject;
  
  
  public GuiObjectBase getCheckGuiObject() {
    return checkGuiObject;
  }

  
  public void setCheckGuiObject(GuiObjectBase checkGuiObject) {
    this.checkGuiObject = checkGuiObject;
  }
  
  private GuiObjectBase ifGuiObject;
  
  
  public GuiObjectBase getIfGuiObject() {
    return ifGuiObject;
  }

  
  public void setIfGuiObject(GuiObjectBase ifGuiObject) {
    this.ifGuiObject = ifGuiObject;
  }
  
  private GuiObjectBase thenGuiObject;
  
  
  public GuiObjectBase getThenGuiObject() {
    return thenGuiObject;
  }

  
  public void setThenGuiObject(GuiObjectBase thenGuiObject) {
    this.thenGuiObject = thenGuiObject;
  }
  
  public String getOwner() {
    
    if (this.ruleDefinition.getAttributeAssignType().getOwnerGroup() != null) {
      Group ownerGroup = this.ruleDefinition.getAttributeAssignType().getOwnerGroup();
      return new GuiGroup(ownerGroup).getShortLinkWithIcon();
    }
    
    if (this.ruleDefinition.getAttributeAssignType().getOwnerStem() != null) {
      Stem ownerStem = this.ruleDefinition.getAttributeAssignType().getOwnerStem();
      return new GuiStem(ownerStem).getShortLinkWithIcon();
    }
    
    if (this.ruleDefinition.getAttributeAssignType().getOwnerAttributeDef() != null) {
      AttributeDef ownerAttributeDef = this.ruleDefinition.getAttributeAssignType().getOwnerAttributeDef();
      return new GuiAttributeDef(ownerAttributeDef).getShortLinkWithIcon();
    }
    
    return "";
  }

  public String getCheck() {
    
    if (this.ruleDefinition.getCheck() != null && this.ruleDefinition.getCheck().checkTypeEnum() != null) {
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      RulesContainer rulesContainer = grouperRequestContainer.getRulesContainer();
      
      //e.g. flattenedMembershipAdd
      String checkName = this.ruleDefinition.getCheck().checkTypeEnum().name();
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      if (this.ruleDefinition.getCheck().checkTypeEnum().isCheckOwnerTypeGroup(this.ruleDefinition)) {
        
        String groupId = this.ruleDefinition.getCheck().getCheckOwnerId();
        Group group = null;
        if (StringUtils.isNotBlank(groupId)) {
          group = GroupFinder.findByUuid(groupId, false);
          if (group == null) {
            return "Group not found: "+groupId; //TODO externalize "Group not found:"
          }
        }
        if (group == null) {
          String groupName = this.ruleDefinition.getCheck().getCheckOwnerName();
          if (StringUtils.isNotBlank(groupName)) {
            group = GroupFinder.findByName(groupName, false);
            if (group == null) {
              return "Group not found: "+groupName; //TODO externalize "Group not found:"
            }
          } 
        }
        if (group == null) {
          if (this.ruleDefinition.getAttributeAssignType() != null) {
            String ownerGroupId = this.ruleDefinition.getAttributeAssignType().getOwnerGroupId();
            if (StringUtils.isNotBlank(ownerGroupId)) {
              group = GroupFinder.findByUuid(groupId, false);
            }
          }
        } 
        
        if (group != null) {
          if (rulesContainer.isNeedsViewPrivilegeOnCheckConditionResult()) {
            boolean canView = group.canHavePrivilege(loggedInSubject, AccessPrivilege.VIEW.getName(), false);
            if (!canView) {
              group = null;
            }
          }
          GuiGroup guiGroup = new GuiGroup(group);
          this.setCheckGuiObject(guiGroup);
          return TextContainer.retrieveFromRequest().getText().get("rulesTableCheckHumanFriendlyValue_"+checkName);
        }
        
      } else if (this.ruleDefinition.getCheck().checkTypeEnum().isCheckOwnerTypeStem(this.ruleDefinition)) {
        
        String stemId = this.ruleDefinition.getCheck().getCheckOwnerId();
        Stem stem = null;
        if (StringUtils.isNotBlank(stemId)) {
          stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemId, false);
          if (stem == null) {
            return "Folder not found: "+stemId; //TODO externalize "Group not found:"
          }
        } 
        if (stem == null) {
          String stemName = this.ruleDefinition.getCheck().getCheckOwnerName();
          if (StringUtils.isNotBlank(stemName)) {
            stem = StemFinder.findByName(stemName, false);
            if (stem == null) {
              return "Folder not found: "+stemName; //TODO externalize "Group not found:"
            }
          }
        }
        
        if (stem == null) {
          if (this.ruleDefinition.getAttributeAssignType() != null) {
            String ownerStemId = this.ruleDefinition.getAttributeAssignType().getOwnerStemId();
            if (StringUtils.isNotBlank(ownerStemId)) {
              stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), ownerStemId, false);
            }
          }
        } 
        
        if (stem != null) {
          
          final Stem STEM = stem;
          if (rulesContainer.isNeedsViewPrivilegeOnCheckConditionResult()) {
            boolean canView = (boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
              @Override
              public Object callback(GrouperSession rootSession) throws GrouperSessionException {
                Stem stemToFind = new StemFinder().addStemId(STEM.getId()).assignSubject(loggedInSubject).findStem();
                return stemToFind != null;
              }
            });
            if (!canView) {
             stem = null;
            }
          }
          
          GuiStem guiStem = new GuiStem(stem);
          this.setCheckGuiObject(guiStem);
          return TextContainer.retrieveFromRequest().getText().get("rulesTableCheckHumanFriendlyValue_"+checkName);
        }
        
      }
      
    }
    return "";
  }
  
  
  public String getCondition() {
    
    if (this.ruleDefinition.getIfCondition() == null) {
      return TextContainer.retrieveFromRequest().getText().get("rulesTableConditionAlwaysHumanFriendlyValue");
    }
    
    if (StringUtils.isNotBlank(this.ruleDefinition.getIfCondition().getIfConditionEl())) {
      //EL: value of the attribute but abbreviate it and show first 50 characters
      return TextContainer.retrieveFromRequest().getText().get("rulesTableConditionElPrefix")
          + " " + GrouperUtil.abbreviate(this.ruleDefinition.getIfCondition().getIfConditionEl(), 53);
    }
     
    if (this.ruleDefinition.getIfCondition().ifConditionEnum() != null) {
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      RulesContainer rulesContainer = grouperRequestContainer.getRulesContainer();
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      //e.g. flattenedMembershipAdd
      String ifConditionEnumName = this.ruleDefinition.getIfCondition().ifConditionEnum().name();
      
      if (this.ruleDefinition.getIfCondition().ifConditionEnum().isIfOwnerTypeGroup(this.ruleDefinition)) {
        
        String groupId = this.ruleDefinition.getIfCondition().getIfOwnerId();
        Group group = null;
        if (StringUtils.isNotBlank(groupId)) {
          group = GroupFinder.findByUuid(groupId, false);
          if (group == null) {
            return "Group not found: "+groupId; //TODO externalize "Group not found:"
          }
        }
        if (group == null) {
          String groupName = this.ruleDefinition.getIfCondition().getIfOwnerName();
          if (StringUtils.isNotBlank(groupName)) {
            group = GroupFinder.findByName(groupName, false);
            if (group == null) {
              return "Group not found: "+groupName; //TODO externalize "Group not found:"
            }
          }
        }
        
        if (group == null) {
          if (this.ruleDefinition.getAttributeAssignType() != null) {
            String ownerGroupId = this.ruleDefinition.getAttributeAssignType().getOwnerGroupId();
            if (StringUtils.isNotBlank(ownerGroupId)) {
              group = GroupFinder.findByUuid(groupId, false);
            }
          }
        } 
        
        if (group != null) {
          
          if (rulesContainer.isNeedsViewPrivilegeOnCheckConditionResult()) {
            boolean canView = group.canHavePrivilege(loggedInSubject, AccessPrivilege.VIEW.getName(), false);
            if (!canView) {
              group = null;
            }
          }
          
          GuiGroup guiGroup = new GuiGroup(group);
          this.setIfGuiObject(guiGroup);
          return TextContainer.retrieveFromRequest().getText().get("rulesTableConditionHumanFriendlyValue_"+ifConditionEnumName);
        }
        
      } else if (this.ruleDefinition.getIfCondition().ifConditionEnum().isIfOwnerTypeStem(this.ruleDefinition)) {
        
        String stemId = this.ruleDefinition.getIfCondition().getIfOwnerId();
        Stem stem = null;
        if (StringUtils.isNotBlank(stemId)) {
          stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemId, false);
          if (stem == null) {
            return "Folder not found: "+stemId; //TODO externalize "Group not found:"
          }
        }
        if (stem == null) {
          String stemName = this.ruleDefinition.getIfCondition().getIfOwnerName();
          if (StringUtils.isNotBlank(stemName)) {
            stem = StemFinder.findByName(stemName, false);
            if (stem == null) {
              return "Folder not found: "+stemName; //TODO externalize "Group not found:"
            }
          }
        }
        
        if (stem == null) {
          if (this.ruleDefinition.getAttributeAssignType() != null) {
            String ownerStemId = this.ruleDefinition.getAttributeAssignType().getOwnerStemId();
            if (StringUtils.isNotBlank(ownerStemId)) {
              stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), ownerStemId, false);
            }
          }
        } 
        
        if (stem != null) {
          
          final Stem STEM = stem;
          if (rulesContainer.isNeedsViewPrivilegeOnCheckConditionResult()) {
            boolean canView = (boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
              @Override
              public Object callback(GrouperSession rootSession) throws GrouperSessionException {
                Stem stemToFind = new StemFinder().addStemId(STEM.getId()).assignSubject(loggedInSubject).findStem();
                return stemToFind != null;
              }
            });
            if (!canView) {
             stem = null;
            }
          }
          
          GuiStem guiStem = new GuiStem(stem);
          this.setIfGuiObject(guiStem);
          return TextContainer.retrieveFromRequest().getText().get("rulesTableConditionHumanFriendlyValue_"+ifConditionEnumName);
        }
        
      }
      
    }
    
    return TextContainer.retrieveFromRequest().getText().get("rulesTableConditionAlwaysHumanFriendlyValue");
  }
  
  public String convertPrivilegeToHtml(String privilegeName) {
    return GrouperTextContainer.textOrNull("priv."+privilegeName);
  }
  
  public String getResult() {
    
    if (this.ruleDefinition.getThen() == null) {
      return "";
    }
    
    if (StringUtils.isNotBlank(this.ruleDefinition.getThen().getThenEl())) {
      //EL: value of the attribute but abbreviate it and show first 50 characters
      return TextContainer.retrieveFromRequest().getText().get("rulesTableResultElPrefix")
          + " " + GrouperUtil.abbreviate(this.ruleDefinition.getThen().getThenEl(), 53);
    }
    
    
    if (this.ruleDefinition.getAttributeAssignType() != null) {
      
      final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      RulesContainer rulesContainer = grouperRequestContainer.getRulesContainer();
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      String ownerGroupId = this.ruleDefinition.getAttributeAssignType().getOwnerGroupId();
      String ownerStemId = this.ruleDefinition.getAttributeAssignType().getOwnerStemId();
      if (StringUtils.isNotBlank(ownerGroupId)) {
        Group group = GroupFinder.findByUuid(ownerGroupId, false);
        if (group != null) {
          if (rulesContainer.isNeedsViewPrivilegeOnCheckConditionResult()) {
            boolean canView = group.canHavePrivilege(loggedInSubject, AccessPrivilege.VIEW.getName(), false);
            if (!canView) {
              group = null;
            }
          }
        }
        
        GuiGroup guiGroup = new GuiGroup(group);
        this.setThenGuiObject(guiGroup);
      } else if (StringUtils.isNotBlank(ownerStemId)) {
        Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), ownerStemId, false);
        if (stem != null) {
          final Stem STEM = stem;
          if (rulesContainer.isNeedsViewPrivilegeOnCheckConditionResult()) {
            boolean canView = (boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
              @Override
              public Object callback(GrouperSession rootSession) throws GrouperSessionException {
                Stem stemToFind = new StemFinder().addStemId(STEM.getId()).assignSubject(loggedInSubject).findStem();
                return stemToFind != null;
              }
            });
            if (!canView) {
             stem = null;
            }
          }
        }
        GuiStem guiStem = new GuiStem(stem);
        this.setThenGuiObject(guiStem);
      }
    }
    
    if (this.ruleDefinition.getThen().thenEnum() != null) {
      String thenEnumName = this.ruleDefinition.getThen().thenEnum().name();
      return TextContainer.retrieveFromRequest().getText().get("rulesTableResultHumanFriendlyValue_"+thenEnumName);
    }
    
    return "";
  }

  public String getWillRunDaemon() {
    
    for (RulePattern rulePattern : RulePattern.values()) {
      if (rulePattern.isThisThePattern(this.ruleDefinition)) {
        if (rulePattern.isDaemonApplicable()) {
          if (GrouperUtil.booleanValue(this.ruleDefinition.getRunDaemon(), true)) {
            return GrouperTextContainer.textOrNull("provisioningConfigTableHeaderProvisionableYesLabel");
          }
          return GrouperTextContainer.textOrNull("provisioningConfigTableHeaderProvisionableNoLabel");
        }
      }
    }
    
    return GrouperTextContainer.textOrNull("provisioningConfigTableHeaderProvisionableNotApplicableLabel");

  }
  
  public boolean isFiresImmeditately() {
    if (this.ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipAdd || this.ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipAddInFolder
        || this.ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipRemove || this.ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipRemoveInFolder) {
      return false;
    }
    return true;
  }
  
  public boolean isCanViewRule() {
    
    AttributeAssign attributeAssignType = this.ruleDefinition.getAttributeAssignType();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    if (StringUtils.isNotBlank(attributeAssignType.getOwnerGroupId())) {
      Group group = attributeAssignType.getOwnerGroup();
      return group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false);
    } else if (StringUtils.isNotBlank(attributeAssignType.getOwnerStemId())) {
      
      Stem stem = attributeAssignType.getOwnerStem();
      return stem.canHavePrivilege(loggedInSubject, NamingPrivilege.CREATE.getName(), false);
    }
    
    //TODO implement for attribute def
    return false;
    
  }
  
  /**
   * @return
   */
  public boolean isCanEditRule() {
    
    AttributeAssign attributeAssignType = this.ruleDefinition.getAttributeAssignType();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    Boolean subjectInCache = rulesEditors.get(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()));
    if (subjectInCache != null) {
      if (subjectInCache == false) {
        return false;
      }
    } else {
      String restrictRulesGroupName = GrouperConfig.retrieveConfig().propertyValueString("rules.restrictRulesUiToMembersOfThisGroupName", "");
      if (StringUtils.isNotBlank(restrictRulesGroupName)) {
        Group restrictRulesGroup = GroupFinder.findByName(restrictRulesGroupName, false);
        if (restrictRulesGroup != null) {
          if (restrictRulesGroup.hasMember(loggedInSubject)) {
            rulesEditors.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), true);
          } else {
            rulesEditors.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), false);
            return false;
          }
          
        } else {
          LOG.warn("rules.restrictRulesUiToMembersOfThisGroupName is set to '"+restrictRulesGroupName+"' and it does not exist.");
        }
      }
    }
    
    //TODO call this method from UiV2Stem and UiV2Group on editRuleOnStem, addRuleOnStemSubmit, deleteRuleOnStem
    
    if (StringUtils.isNotBlank(attributeAssignType.getOwnerGroupId())) {
      Group group = attributeAssignType.getOwnerGroup();
      return group.canHavePrivilege(loggedInSubject, AccessPrivilege.ADMIN.getName(), false);
    } else if (StringUtils.isNotBlank(attributeAssignType.getOwnerStemId())) {
      
      Stem stem = attributeAssignType.getOwnerStem();
      return stem.canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ADMIN.getName(), false);
    }
    
    //TODO implement for attribute def
    return false;
    
  }
  
  
}
