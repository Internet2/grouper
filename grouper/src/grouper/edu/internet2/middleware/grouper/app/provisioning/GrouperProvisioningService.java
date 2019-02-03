package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_LAST_FULL_MILLIS_SINCE_1970;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_LAST_FULL_SUMMARY;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_LAST_INCREMENTAL_MILLIS_SINCE_1970;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_LAST_INCREMENTAL_SUMMARY;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;
import static edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;

public class GrouperProvisioningService {
  
  /**
   * retrieve type setting for a given grouper object (group/stem) and target name.
   * @param grouperObject
   * @param targetName
   * @return
   */
  public static GrouperProvisioningAttributeValue getProvisioningAttributeValue(GrouperObject grouperObject, String targetName) {
    
    AttributeAssign attributeAssign = getAttributeAssign(grouperObject, targetName);
    if (attributeAssign == null) {
      return null;
    }
    
    return buildGrouperProvisioningAttributeValue(attributeAssign);
  }

  /**
   * retrieve all the configured provisioning attributes for a given grouper object (group/stem)
   * @param grouperObject
   * @return
   */
  public static List<GrouperProvisioningAttributeValue> getProvisioningAttributeValues(final GrouperObject grouperObject) {
    
    final List<GrouperProvisioningAttributeValue> result = new ArrayList<GrouperProvisioningAttributeValue>();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Map<String, GrouperProvisioningTarget> targetNames = GrouperProvisioningSettings.getTargets();
        
        for (String targetName: targetNames.keySet()) {
          GrouperProvisioningAttributeValue value = getProvisioningAttributeValue(grouperObject, targetName);
          if (value != null) {
            result.add(value);
          }
        }
        
        return null;
      }
      
    });
    
    return result;
  }
  
  /**
   * get provisioning attributes for a given grouper object and target name
   * @param grouperObject
   * @param targetName
   * @return
   */
  private static AttributeAssign getAttributeAssign(GrouperObject grouperObject, String targetName) {
    
    Set<AttributeAssign> attributeAssigns = null;
    
    if (grouperObject instanceof Group) {
      Group group = (Group)grouperObject;
      attributeAssigns = group.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    } else {
      Stem stem = (Stem)grouperObject;
      attributeAssigns = stem.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    }
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_TARGET);
      if (attributeAssignValue == null || StringUtils.isBlank(attributeAssignValue.getValueString())) {
        return null;
      }
      
      String targetNameFromDB = attributeAssignValue.getValueString();
      if (targetName.equals(targetNameFromDB)) {
       return attributeAssign;
      }
    }
    return null;
    
  }
  
  /**
   * build provisioning attribute object from underlying info
   * @param attributeAssign
   * @return
   */
  private static GrouperProvisioningAttributeValue buildGrouperProvisioningAttributeValue(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    GrouperProvisioningAttributeValue result = new GrouperProvisioningAttributeValue();
    result.setTargetName(attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_TARGET).getValueString());
    
    AttributeAssignValue directAssignmentAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT);
    String directAssignmentStr = directAssignmentAssignValue != null ? directAssignmentAssignValue.getValueString(): null;
    boolean directAssignment = BooleanUtils.toBoolean(directAssignmentStr);
    result.setDirectAssignment(directAssignment);
    
    AttributeAssignValue stemScopeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_STEM_SCOPE);
    result.setStemScopeString(stemScopeAssignValue != null ? stemScopeAssignValue.getValueString(): null);
    
    AttributeAssignValue doProvisionAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_DO_PROVISION);
    String doProvisionStr = doProvisionAssignValue != null ? doProvisionAssignValue.getValueString(): null;
    boolean doProvision = BooleanUtils.toBoolean(doProvisionStr);
    result.setDoProvision(doProvision);
    
    AttributeAssignValue lastProvisionedFullMillisAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_LAST_FULL_MILLIS_SINCE_1970);
    result.setLastFullMillisSince1970String(lastProvisionedFullMillisAssignValue != null ? lastProvisionedFullMillisAssignValue.getValueString(): null);
    
    AttributeAssignValue lastProvisionedIncrementalMillisAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_LAST_INCREMENTAL_MILLIS_SINCE_1970);
    result.setLastIncrementalMillisSince1970String(lastProvisionedIncrementalMillisAssignValue != null ? lastProvisionedIncrementalMillisAssignValue.getValueString(): null);
    
    AttributeAssignValue lastProvisionedFullSummaryAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_LAST_FULL_SUMMARY);
    result.setLastFullSummary(lastProvisionedFullSummaryAssignValue != null ? lastProvisionedFullSummaryAssignValue.getValueString(): null);
    
    AttributeAssignValue lastProvisionedIncrementalSummaryAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_LAST_INCREMENTAL_SUMMARY);
    result.setLastIncrementalSummary(lastProvisionedIncrementalSummaryAssignValue != null ? lastProvisionedIncrementalSummaryAssignValue.getValueString(): null);
    
    AttributeAssignValue ownerStemIdAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(provisioningConfigStemName()+":"+PROVISIONING_OWNER_STEM_ID);
    result.setOwnerStemId(ownerStemIdAssignValue != null ? ownerStemIdAssignValue.getValueString(): null);
    
    return result;
  }
  
  /**
   * save or update provisioning config for a given grouper object (group/stem)
   * @param grouperProvisioningAttributeValue
   * @param grouperObject
   */
  public static void saveOrUpdateProvisioningAttributes(GrouperProvisioningAttributeValue grouperProvisioningAttributeValue, GrouperObject grouperObject) {
    
    AttributeAssign attributeAssign = getAttributeAssign(grouperObject, grouperProvisioningAttributeValue.getTargetName());
   
    if (attributeAssign == null) {
      if (grouperObject instanceof Group) {
        attributeAssign = ((Group)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      } else {
        attributeAssign = ((Stem)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      }
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(grouperProvisioningAttributeValue.isDirectAssignment()));
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_TARGET, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.getTargetName());
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DO_PROVISION, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(grouperProvisioningAttributeValue.isDoProvision()));
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_OWNER_STEM_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.isDirectAssignment() ? null: grouperProvisioningAttributeValue.getOwnerStemId());
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_STEM_SCOPE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperProvisioningAttributeValue.getStemScopeString());
    
    attributeAssign.saveOrUpdate();
    
    if (grouperObject instanceof Stem && grouperProvisioningAttributeValue.isDirectAssignment()) {
      // delete all the existing attributes on children where the current stem is the owner stem
      deleteAttributesOnAllChildrenWithIndirectConfig((Stem)grouperObject, grouperProvisioningAttributeValue.getTargetName());
      
      GrouperProvisioningAttributeValue valueToSave = GrouperProvisioningAttributeValue.copy(grouperProvisioningAttributeValue);
      valueToSave.setOwnerStemId(((Stem)grouperObject).getId());
      valueToSave.setDirectAssignment(false);
      saveOrUpdateProvisioningAttributesOnChildren((Stem)grouperObject, valueToSave, grouperProvisioningAttributeValue.getStemScope());
    }
    
  }
  
  /**
   * find provisioning config in the parent hierarchy for a given grouper object for all targets (ldap, box, etc) and assign that config to this grouper object.
   * @param grouperObject
   */
  public static void copyConfigFromParent(final GrouperObject grouperObject) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Map<String, GrouperProvisioningTarget> targetNames = GrouperProvisioningSettings.getTargets();
        
        for (String targetName: targetNames.keySet()) {
          copyConfigFromParent(grouperObject, targetName);
        }
        
        return null;
        
      }
      
    });
    
  }
  
  /**
   * find provisioning config in the parent hierarchy for a given grouper object and target. Assign that config to the given grouper object
   * @param grouperObject
   * @param targetName
   */
  public static void copyConfigFromParent(GrouperObject grouperObject, String targetName) {
    
    //don't do this now
    if (GrouperCheckConfig.isInCheckConfig() || !GrouperProvisioningSettings.provisioningInUiEnabled()) {
      return;
    }
    
    if (grouperObject instanceof Stem && ((Stem) grouperObject).isRootStem()) {
      return;
    }
    
    deleteAttributeAssign(grouperObject, targetName);
    
    // if we changed from direct to indirect, we need to go through all the children
    // and delete metadata on them that were inheriting from this stem.
    if (grouperObject instanceof Stem) {
      deleteAttributesOnAllChildrenWithIndirectConfig((Stem)grouperObject, targetName);
    }
    
    Stem parent = grouperObject.getParentStem();
    
    if(parent.isRootStem()) {
      return;
    }
    
    GrouperProvisioningAttributeValue savedValue = null;
    
    int distanceFromParent = 1;
    
    while (parent != null) {
      
      GrouperProvisioningAttributeValue attributeValue = getProvisioningAttributeValue(parent, targetName);
      
      if (attributeValue != null && attributeValue.isDirectAssignment()) {
        
        if (attributeValue.getStemScope() == Stem.Scope.SUB || (attributeValue.getStemScope() == Stem.Scope.ONE && distanceFromParent < 2 )) {
          savedValue = new GrouperProvisioningAttributeValue();
          savedValue.setDirectAssignment(false);
          savedValue.setDoProvision(attributeValue.isDoProvision());
          savedValue.setOwnerStemId(parent.getId());
          savedValue.setStemScopeString(attributeValue.getStemScopeString());
          savedValue.setTargetName(attributeValue.getTargetName());
          saveOrUpdateProvisioningAttributes(savedValue, grouperObject);
          break;
        }
        
      }
      
      parent = parent.getParentStem();
      distanceFromParent++;
      
      if (parent.isRootStem()) {
        break;
      }
      
    }
    
    // if it's a stem where we changed from direct to indirect, we need to go through all the children of that stem and update the attributes with some parent's metadata if there
    if (grouperObject instanceof Stem && savedValue != null) {
      saveOrUpdateProvisioningAttributesOnChildren((Stem)grouperObject, savedValue, savedValue.getStemScope());
    }
    
  }

  /**
   * save provisioning attributes on children of a given stem
   * @param parentStem
   * @param valueToSave
   * @param scope
   */
  private static void saveOrUpdateProvisioningAttributesOnChildren(Stem parentStem, GrouperProvisioningAttributeValue valueToSave, Scope scope) {
    
    Set<String> childrenStemIds = new HashSet<String>();
    
    for (Stem stem: parentStem.getChildStems(scope)) {
      childrenStemIds.add(stem.getId());
    }
    
    Set<GrouperObject> children = new HashSet<GrouperObject>(parentStem.getChildGroups(scope));
    children.addAll(parentStem.getChildStems(scope));
    
    for (GrouperObject childGrouperObject: children) {
      boolean shouldSaveForThisChild = true;
      
      GrouperProvisioningAttributeValue mayBeGroupTypeAttributeValue = getProvisioningAttributeValue(childGrouperObject, valueToSave.getTargetName());
      if (mayBeGroupTypeAttributeValue != null) {
        
        if (mayBeGroupTypeAttributeValue.isDirectAssignment()) {
          shouldSaveForThisChild = false;
          continue;
        }
        
        String ownerStemId = mayBeGroupTypeAttributeValue.getOwnerStemId();

        // some child of parentStem's settings are already configured on this group/stem, we don't need to update because we will increase the distance otherwise
        if (childrenStemIds.contains(ownerStemId)) {
          shouldSaveForThisChild = false;
        }
        
      }
      
      if (shouldSaveForThisChild) {
        saveOrUpdateProvisioningAttributes(valueToSave, childGrouperObject);
      }
      
    }
  }
  
  /**
   * is given target editable for given subject and grouper object
   * @param target
   * @param subject
   * @param grouperObject
   * @return
   */
  public static boolean isTargetEditable(GrouperProvisioningTarget target, Subject subject, GrouperObject grouperObject) {
    
    boolean readOnly = target.isReadOnly();
    
    if(readOnly) {
      return false;
    }
    
    boolean isEditable = true;
    
    if (grouperObject instanceof Stem) {
      isEditable = isTargetEditableForStem(target, (Stem)grouperObject);
    } else {
      isEditable = !target.isAllowAssignmentsOnlyOnOneStem();
    }
    
    return isEditable && isTargetEditableBySubject(target, subject);
  }
  
  private static boolean isTargetEditableForStem(GrouperProvisioningTarget target, Stem stem) {
    
    boolean allowAssignOnOneStem = target.isAllowAssignmentsOnlyOnOneStem();
    
    if (allowAssignOnOneStem) { //can not edit if this target is already assigned to another stem
      
      List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
          .assignNameOfAttributeDefName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("true")
          .assignNameOfAttributeDefName2(provisioningConfigStemName()+":"+PROVISIONING_TARGET).addAttributeValuesOnAssignment2(target.getName())
          .findStems());
      
      if (stems.size() > 1) {
        return false;
      }
      
      if (stems.size() == 1 && !stems.get(0).getId().equals(stem.getId())) {
        return false; // stem we are working with is not the same it's already assigned to therefore not editable
      }
      
    }
    
    return true;
  }
  
  /**
   * can the given subject edit the given target
   * @param target
   * @param subject
   * @return
   */
  private static boolean isTargetEditableBySubject(GrouperProvisioningTarget target, Subject subject) {
    
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    
    String groupAllowedToAssign = target.getGroupAllowedToAssign();
    if (StringUtils.isBlank(groupAllowedToAssign)) {
      return PrivilegeHelper.isWheelOrRoot(subject); // only grouper system admin is allowed when no specific group is allowed to assign the given target
    }
    
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupAllowedToAssign, false);
    if (group == null) {
      try { // try looking up group by id
        Long groupId = Long.valueOf(groupAllowedToAssign);
        group = GroupFinder.findByIdIndexSecure(groupId, false, new QueryOptions());
        if (group == null) {
          throw new RuntimeException(groupAllowedToAssign+" is not a valid group id or group name");
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(groupAllowedToAssign+" is not a valid group id or group name");
      }
     
    }
    
    for (Member member: group.getMembers()) {
      Subject groupSubject = member.getSubject();
      if (subject.getId().equals(groupSubject.getId())) {
        return true;
      }
    }
    
    return false;

  }
  
  /**
   * delete provisioning attributes on all the children of a given stem and target
   * @param stem
   * @param targetName
   */
  private static void deleteAttributesOnAllChildrenWithIndirectConfig(Stem stem, String targetName) {
      
      Set<GrouperObject> children = new HashSet<GrouperObject>(stem.getChildGroups(Scope.SUB));
      children.addAll(stem.getChildStems(Scope.SUB));
      
      for (GrouperObject childGrouperObject: children) {
        GrouperProvisioningAttributeValue mayBeGroupTypeAttributeValue = getProvisioningAttributeValue(childGrouperObject, targetName);
        if (mayBeGroupTypeAttributeValue != null) {
          
          if (mayBeGroupTypeAttributeValue.isDirectAssignment()) {
            continue;
          }
          
          String ownerStemId = mayBeGroupTypeAttributeValue.getOwnerStemId();
          if (stem.getId().equals(ownerStemId)) {
            deleteAttributeAssign(childGrouperObject, targetName);
          }
        }
        
      }
      
    }

  /**
   * delete provisioning attributes from a given grouper object and target
   * @param grouperObject
   * @param targetName
   */
  private static void deleteAttributeAssign(GrouperObject grouperObject, String targetName) {
    AttributeAssign currentAttributeAssign = getAttributeAssign(grouperObject, targetName);
    if (currentAttributeAssign != null) {
      currentAttributeAssign.delete();
    }
  }
}
