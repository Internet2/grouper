package edu.internet2.middleware.grouper.app.grouperTypes;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DATA_OWNER;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_MEMBERS_DESCRIPTION;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_OWNER_STEM_ID;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_SERVICE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;
import static edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;

public class GrouperObjectTypesConfiguration {
  
  /**
   * retrieve type setting for a given grouper object (group/stem) and object type name.
   * @param grouperObject
   * @param objectTypeName
   * @return
   */
  public static GrouperObjectTypesAttributeValue getGrouperObjectTypesAttributeValue(GrouperObject grouperObject, String objectTypeName) {
    
    AttributeAssign attributeAssign = getAttributeAssign(grouperObject, objectTypeName);
    if (attributeAssign == null) {
      return null;
    }
    
    return buildGrouperObjectTypeAttributeValue(attributeAssign);
  }

  /**
   * retrieve all the configured type settings for a given grouper object (group/stem)
   * @param grouperObject
   * @return
   */
  public static List<GrouperObjectTypesAttributeValue> getGrouperObjectTypesAttributeValues(final GrouperObject grouperObject) {
    
    final List<GrouperObjectTypesAttributeValue> result = new ArrayList<GrouperObjectTypesAttributeValue>();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        for (String objectType: GrouperObjectTypesSettings.getObjectTypeNames()) {
          GrouperObjectTypesAttributeValue value = getGrouperObjectTypesAttributeValue(grouperObject, objectType);
          if (value != null) {
            result.add(value);
          }
        }
        
        return null;
      }
      
    });
    
    return result;
  }
  
  private static boolean grouperObjectTypesAttributeValuesDifferent(GrouperObjectTypesAttributeValue one, 
      GrouperObjectTypesAttributeValue two) {
    
    if (one == null && two == null) return false;
    if (one == null || two == null) return true;
    
    if (!StringUtils.equals(one.getObjectTypeName(), two.getObjectTypeName())) {
      return true;
    }
    
    if (one.isDirectAssignment() != two.isDirectAssignment()) {
      return true;
    }
    
    if (!StringUtils.equals(one.getObjectTypeDataOwner(), two.getObjectTypeDataOwner())) {
      return true;
    }
    
    if (!StringUtils.equals(one.getObjectTypeMemberDescription(), two.getObjectTypeMemberDescription())) {
      return true;
    }
    
    if (!StringUtils.equals(one.getObjectTypeOwnerStemId(), two.getObjectTypeOwnerStemId())) {
      return true;
    }
    
    if (!StringUtils.equals(one.getObjectTypeServiceName(), two.getObjectTypeServiceName())) {
      return true;
    }
    
    return false;
  }
  
  /**
   * save or update type config for a given grouper object (group/stem)
   * @param grouperObjectTypesAttributeValue
   * @param grouperObject
   */
  public static void saveOrUpdateTypeAttributes(GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue, GrouperObject grouperObject) {
    
    AttributeAssign attributeAssign = getAttributeAssign(grouperObject, grouperObjectTypesAttributeValue.getObjectTypeName());
   
    if (attributeAssign == null) {
      if (grouperObject instanceof Group) {
        attributeAssign = ((Group)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      } else if (grouperObject instanceof Stem) {
        attributeAssign = ((Stem)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      } else {
        throw new RuntimeException("Only Groups and Folders can have types");
      }
    } else {
      GrouperObjectTypesAttributeValue existingGrouperObjectTypesAttributeValue = buildGrouperObjectTypeAttributeValue(attributeAssign);
      boolean newValueDifferentFromOldValue = grouperObjectTypesAttributeValuesDifferent(grouperObjectTypesAttributeValue, existingGrouperObjectTypesAttributeValue);
      if (!newValueDifferentFromOldValue) return;
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(grouperObjectTypesAttributeValue.isDirectAssignment()));
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperObjectTypesAttributeValue.getObjectTypeName());
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DATA_OWNER, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_MEMBERS_DESCRIPTION, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_SERVICE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperObjectTypesAttributeValue.getObjectTypeServiceName());
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_OWNER_STEM_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperObjectTypesAttributeValue.isDirectAssignment() ? null: grouperObjectTypesAttributeValue.getObjectTypeOwnerStemId());
    
    attributeAssign.saveOrUpdate();
    
    if (grouperObject instanceof Stem && grouperObjectTypesAttributeValue.isDirectAssignment()) {
      GrouperObjectTypesAttributeValue valueToSave = GrouperObjectTypesAttributeValue.copy(grouperObjectTypesAttributeValue);
      valueToSave.setObjectTypeOwnerStemId(((Stem)grouperObject).getId());
      valueToSave.setDirectAssignment(false);
      saveOrUpdateTypeAttributesOnChildren((Stem)grouperObject, valueToSave);
    }
    
  }
  
  /**
   * find type config in the parent hierarchy for a given grouper object for all object types (ref, basis, etc) and assign that config to this grouper object.
   * @param grouperObject
   */
  public static void copyConfigFromParent(final GrouperObject grouperObject) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        for (String objectType: GrouperObjectTypesSettings.getObjectTypeNames()) {
          copyConfigFromParent(grouperObject, objectType);
        }
        
        return null;
        
      }
      
    });
    
  }
  
  public static void fixGrouperObjectTypesAttributeValuesForChildrenOfDirectStem(final Stem stem) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        List<String> objectTypeNames = GrouperObjectTypesSettings.getObjectTypeNames();
        
        for (String objectTypeName: objectTypeNames) {
          fixGrouperObjectTypesAttributeValuesForChildrenOfDirectStem(stem, objectTypeName);
        }
        
        return null;
        
      }
      
    });
    
  }
  
  public static void fixGrouperObjectTypesAttributeValuesForChildrenOfDirectStem(Stem stem, String objectTypeName) {
    
    if (stem.isRootStem()) {
      return;
    }
    
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = getGrouperObjectTypesAttributeValue(stem, objectTypeName);
    
    if (grouperObjectTypesAttributeValue == null) return;
    
    {
      
      Set<GrouperObject> children = new HashSet<GrouperObject>();
      
      Set<Group> childGroups = stem.getChildGroups(Stem.Scope.SUB);
      Set<Stem> childStems = stem.getChildStems(Stem.Scope.SUB);
      
      children.addAll(childGroups);
      children.addAll(childStems);
      
      for (GrouperObject child: children) {
        
        GrouperObjectTypesAttributeValue childObbjectTypeAttributeValue = getGrouperObjectTypesAttributeValue(child, objectTypeName);
        
        if (childObbjectTypeAttributeValue != null && childObbjectTypeAttributeValue.isDirectAssignment()) {
          continue;
        }
        
        fixGrouperObjectTypeAttributeValueForIndirectGrouperObject(child, objectTypeName);
        
      }
      
    }
    
    
  }
  
  public static void fixGrouperObjectTypeAttributeValueForIndirectGrouperObject(final GrouperObject grouperObject, String objectTypeName) {
    
    if (grouperObject instanceof Stem && ((Stem) grouperObject).isRootStem()) {
      return;
    }
    
    Stem parent = grouperObject.getParentStem();
    
    GrouperObjectTypesAttributeValue parentAttributeValue = null;
    
    while (parent != null) {
      
      parentAttributeValue = getGrouperObjectTypesAttributeValue(parent, objectTypeName);
      
      if (parentAttributeValue != null && parentAttributeValue.isDirectAssignment()) {
        break;
      } else {
        parentAttributeValue = null;
      }
      
      if (parent.isRootStem()) {
        break;
      }
      
      parent = parent.getParentStem();
      
    }
    
    if (parentAttributeValue == null) {
      deleteAttributeAssign(grouperObject, objectTypeName); // orphan attribute value. delete it.
    } else {
      
      GrouperObjectTypesAttributeValue attributeValue = getGrouperObjectTypesAttributeValue(grouperObject, objectTypeName);
      
      if (attributeValue != null && StringUtils.isNotBlank(attributeValue.getObjectTypeOwnerStemId()) &&
          StringUtils.equals(attributeValue.getObjectTypeOwnerStemId(), parent.getId())) {
        return; // correct owner; no need to make any changes
      }
      
      if (attributeValue == null) {
        GrouperObjectTypesAttributeValue childValueToSave = new GrouperObjectTypesAttributeValue();
        childValueToSave.setDirectAssignment(false);
        childValueToSave.setObjectTypeOwnerStemId(parent.getId());
        childValueToSave.setObjectTypeDataOwner(parentAttributeValue.getObjectTypeDataOwner());
        childValueToSave.setObjectTypeMemberDescription(parentAttributeValue.getObjectTypeMemberDescription());
        childValueToSave.setObjectTypeName(objectTypeName);
        childValueToSave.setObjectTypeServiceName(parentAttributeValue.getObjectTypeServiceName());
        saveOrUpdateTypeAttributes(childValueToSave, grouperObject);
      } else {
        AttributeAssign attributeAssign = getAttributeAssign(grouperObject, objectTypeName);
        AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_OWNER_STEM_ID, true);
        attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), parent.getId());
        
        attributeAssign.saveOrUpdate();
      }
      
      
    }
    
  }
  
  public static void fixGrouperObjectTypesAttributeValueForIndirectGrouperObject(final GrouperObject grouperObject) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        List<String> objectTypeNames = GrouperObjectTypesSettings.getObjectTypeNames();
        
        for (String objectTypeName: objectTypeNames) {
          fixGrouperObjectTypeAttributeValueForIndirectGrouperObject(grouperObject, objectTypeName);
        }
        return null;
        
      }
      
    });
    
  }
  
  /**
   * find type config in the parent hierarchy for a given grouper object and type. Assign that config to the given grouper object
   * @param grouperObject
   * @param objectType
   */
  public static void copyConfigFromParent(GrouperObject grouperObject, String objectType) {
    
    //don't do this now
    if (GrouperCheckConfig.isInCheckConfig() || !GrouperObjectTypesSettings.objectTypesEnabled()) {
      return;
    }
    
    if (grouperObject instanceof Stem && ((Stem) grouperObject).isRootStem()) {
      return;
    }
    
    deleteAttributeAssign(grouperObject, objectType);
    
    // if we changed from direct to indirect, we need to go through all the children
    // and delete metadata on them that were inheriting from this stem.
    if (grouperObject instanceof Stem) {
      deleteAttributesOnAllChildrenWithIndirectConfig((Stem)grouperObject, objectType);
    }
    
    Stem parent = grouperObject.getParentStem();
    
    if(parent.isRootStem()) {
      return;
    }
    
    GrouperObjectTypesAttributeValue savedValue = null;
    
    while (parent != null) {
      
      GrouperObjectTypesAttributeValue attributeValue = getGrouperObjectTypesAttributeValue(parent, objectType);
      
      if (attributeValue != null && attributeValue.isDirectAssignment()) {
        savedValue = new GrouperObjectTypesAttributeValue();
        savedValue.setDirectAssignment(false);
        savedValue.setObjectTypeDataOwner(attributeValue.getObjectTypeDataOwner());
        savedValue.setObjectTypeMemberDescription(attributeValue.getObjectTypeMemberDescription());
        savedValue.setObjectTypeName(attributeValue.getObjectTypeName());
        savedValue.setObjectTypeOwnerStemId(parent.getId());
        savedValue.setObjectTypeServiceName(attributeValue.getObjectTypeServiceName());
        saveOrUpdateTypeAttributes(savedValue, grouperObject);
        break;
      }
      
      parent = parent.getParentStem();
      
      if (parent.isRootStem()) {
        break;
      }
      
    }
    
    // if it's a stem where we changed from direct to indirect, we need to go through all the children of that stem and update the attributes
    //with parent's metadata
    if (grouperObject instanceof Stem && savedValue != null) {
      saveOrUpdateTypeAttributesOnChildren((Stem)grouperObject, savedValue);
    }
    
  }
  
  /**
   * find all stems where given subject is admin of service
   * @return
   */
  public static List<Stem> findStemsWhereCurrentUserIsAdminOfService(Subject subject) {
    
    List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignSubject(subject)
        .assignNameOfAttributeDefName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("true")
        .assignNameOfAttributeDefName2(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME).addAttributeValuesOnAssignment2("service")
        .addPrivilege(NamingPrivilege.STEM_ADMIN).findStems());
    
    return stems;
  }
  
  private static Set<Stem> stemsForAutoAssignSuggestion(Stem parentStem, Subject subject) {
    
    String attributeDefName = GrouperObjectTypesSettings.objectTypesStemName()
        + ":"+ GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;

    Map<String, String> scopesToObjectType = new HashMap<String, String>();
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.BASIS+"%", GrouperObjectTypesSettings.BASIS);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.REF+"%", GrouperObjectTypesSettings.REF);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.POLICY+"%", GrouperObjectTypesSettings.POLICY);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.ETC+"%", GrouperObjectTypesSettings.ETC);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.GROUPER_SECURITY+"%", GrouperObjectTypesSettings.GROUPER_SECURITY);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.ORG+"%", GrouperObjectTypesSettings.ORG);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.APP+"%", GrouperObjectTypesSettings.APP);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.SERVICE+"%", GrouperObjectTypesSettings.SERVICE);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.READ_ONLY+"%", GrouperObjectTypesSettings.READ_ONLY);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.TEST+"%", GrouperObjectTypesSettings.TEST);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.MANUAL+"%", GrouperObjectTypesSettings.MANUAL);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.INTERMEDIATE+"%", GrouperObjectTypesSettings.INTERMEDIATE);
    scopesToObjectType.put("%"+GrouperObjectTypesSettings.BUNDLE+"%", GrouperObjectTypesSettings.REF);
    
    Map<String, Pattern> scopesToPattern = new HashMap<String, Pattern>();
    scopesToPattern.put("%"+GrouperObjectTypesSettings.BASIS+"%", Pattern.compile("^basis$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.REF+"%", Pattern.compile("^ref$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.POLICY+"%", Pattern.compile("^policy$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.ETC+"%", Pattern.compile("^etc$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.GROUPER_SECURITY+"%", Pattern.compile("^grouperSecurity$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.ORG+"%", Pattern.compile("^org$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.APP+"%", Pattern.compile("^app$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.SERVICE+"%", Pattern.compile("^service$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.READ_ONLY+"%", Pattern.compile("^readOnly$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.TEST+"%", Pattern.compile("^test$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.MANUAL+"%", Pattern.compile("^manual$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.INTERMEDIATE+"%", Pattern.compile("^intermediate$"));
    scopesToPattern.put("%"+GrouperObjectTypesSettings.BUNDLE+"%", Pattern.compile("^bundle$"));
    
    Set<Stem> stems = new HashSet<Stem>();
    for (String key: scopesToObjectType.keySet()) {
      
      Set<Stem> localStems = new StemFinder().assignStemScope(Scope.SUB)
          .assignSubject(subject)
          .assignParentStemId(parentStem.getId())
          .addPrivilege(AccessPrivilege.ADMIN)
          .assignAttributeCheckReadOnAttributeDef(false)
          .assignNameOfAttributeDefName(attributeDefName)
          .assignAttributeNotAssigned(true)
          .addAttributeValuesOnAssignment(scopesToObjectType.get(key))
          .assignScope(key)
          .findStems();
      
      Pattern pattern = scopesToPattern.get(key);
      
      for (Stem stem: localStems) {
        if (pattern.matcher(stem.getExtension()).matches()) {
          stems.add(stem);
        }
      }
      
    }
    
    return stems;
  }
  
  private static Set<Group> groupsForAutoAssignSuggestion(Stem parentStem, Subject subject) {
    
    String attributeDefName = GrouperObjectTypesSettings.objectTypesStemName()
        + ":"+ GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
    
    Map<String, String> scopesToObjectType = new HashMap<String, String>();
    scopesToObjectType.put("%manual%", "manual");
    scopesToObjectType.put("%allow%", "intermediate");
    scopesToObjectType.put("%deny%", "intermediate");
    scopesToObjectType.put("%preRequire%", "intermediate");
    scopesToObjectType.put("%excludes%", "intermediate");
    scopesToObjectType.put("%includes%", "intermediate");
    scopesToObjectType.put("%systemOfRecordAndIncludes%", "intermediate");
    scopesToObjectType.put("%systemOfRecord%", "intermediate");
    
    Map<String, Pattern> scopesToPattern = new HashMap<String, Pattern>();
    scopesToPattern.put("%manual%", Pattern.compile("^.*_manual$"));
    scopesToPattern.put("%allow%", Pattern.compile("^.*_allow$"));
    scopesToPattern.put("%deny%", Pattern.compile("^.*_deny$"));
    scopesToPattern.put("%preRequire%", Pattern.compile("^.*_preRequire_.*$"));
    scopesToPattern.put("%excludes%", Pattern.compile("^.*_excludes$"));
    scopesToPattern.put("%includes%", Pattern.compile("^.*_includes$"));
    scopesToPattern.put("%systemOfRecordAndIncludes%", Pattern.compile("^.*_systemOfRecordAndIncludes$"));
    scopesToPattern.put("%systemOfRecord%", Pattern.compile("^.*_systemOfRecord$"));
    
    Set<Group> groups = new HashSet<Group>();
    for (String key: scopesToObjectType.keySet()) {
      
      Set<Group> localGroups = new GroupFinder().assignStemScope(Scope.SUB)
          .assignSubject(subject)
          .assignParentStemId(parentStem.getId())
          .addPrivilege(AccessPrivilege.ADMIN)
          .assignAttributeCheckReadOnAttributeDef(false)
          .assignNameOfAttributeDefName(attributeDefName)
          .assignAttributeNotAssigned(true)
          .addAttributeValuesOnAssignment(scopesToObjectType.get(key))
          .assignScope(key)
          .findGroups();
      
      Pattern pattern = scopesToPattern.get(key);
      
      for (Group group: localGroups) {
        if (pattern.matcher(group.getExtension()).matches()) {
          groups.add(group);
        }
      }
      
    }
    
    return groups;
  }
  
  /**
   * get list of objects that qualify for auto assign type suggestions 
   * @param stem
   * @param subject
   * @return
   */
  public static List<StemOrGroupObjectType> getAutoAssignTypeCandidates(Stem stem, Subject subject) {
    
    Set<Stem> stems = stemsForAutoAssignSuggestion(stem, subject);
    Set<Group> groups = groupsForAutoAssignSuggestion(stem, subject);

    Set<GrouperObject> grouperObjects = new TreeSet<GrouperObject>();
    grouperObjects.addAll(groups);
    grouperObjects.addAll(stems);
    
    List<StemOrGroupObjectType> result = new ArrayList<StemOrGroupObjectType>();
    
    for (GrouperObject grouperObject: grouperObjects) {
      String objectType = null;
      if (grouperObject instanceof Stem) {
        Stem stm = (Stem)grouperObject;
        String folderExtension = stm.getExtension().toLowerCase();
        objectType = GrouperObjectTypesSettings.getFolderExtensionToTypeSuggestion().get(folderExtension);
      } else {
        objectType = getObjectTypeForGroupName(grouperObject.getName());
      }

      if (StringUtils.isNotBlank(objectType)) {
        
        result.add(new StemOrGroupObjectType(grouperObject, objectType));
        
      }
      
    }
    return result;
  }
  
  
  private static String getObjectTypeForGroupName(String groupName) {
     /**
         if system name of group ends in _manual, then suggest type as manual
         if system name ends in _allow or _deny, then suggest type as intermediate
         if system name contains _preRequire_ , then suggest intermediate
         if system name ends in "_excludes" is intermediate
         if system name ends in "_includes" is intermediate
         if system name ends in "_systemOfRecordAndIncludes" is intermediate
         if system name ends in "_systemOfRecord" is intermediate
       */
    
    if (groupName.endsWith("_manual")) return GrouperObjectTypesSettings.MANUAL;
    
    if (StringUtils.endsWithAny(groupName, "_allow", "_deny", "_excludes", "_includes",
        "_systemOfRecordAndIncludes", "_systemOfRecord")) return GrouperObjectTypesSettings.INTERMEDIATE;
    
    if (StringUtils.contains(groupName, "_preRequire_")) return GrouperObjectTypesSettings.INTERMEDIATE;
    
    return null;
    
  }
  
  private static void deleteAttributesOnAllChildrenWithIndirectConfig(Stem stem, String objectType) {
    
    Set<GrouperObject> children = new HashSet<GrouperObject>(stem.getChildGroups(Scope.SUB));
    children.addAll(stem.getChildStems(Scope.SUB));
    
    for (GrouperObject childGrouperObject: children) {
      GrouperObjectTypesAttributeValue mayBeGroupTypeAttributeValue = getGrouperObjectTypesAttributeValue(childGrouperObject, objectType);
      if (mayBeGroupTypeAttributeValue != null) {
        
        if (mayBeGroupTypeAttributeValue.isDirectAssignment()) {
          continue;
        }
        
        String ownerStemId = mayBeGroupTypeAttributeValue.getObjectTypeOwnerStemId();
        if (stem.getId().equals(ownerStemId)) {
          deleteAttributeAssign(childGrouperObject, objectType);
        }
      }
      
    }
    
  }

  private static void deleteAttributeAssign(GrouperObject grouperObject, String objectType) {
    AttributeAssign currentAttributeAssign = getAttributeAssign(grouperObject, objectType);
    if (currentAttributeAssign != null) {
      currentAttributeAssign.delete();
    }
  }
  
  private static void saveOrUpdateTypeAttributesOnChildren(Stem parentStem, GrouperObjectTypesAttributeValue valueToSave) {
    
    Set<String> childrenStemIds = new HashSet<String>();
    
    for (Stem stem: parentStem.getChildStems(Scope.SUB)) {
      childrenStemIds.add(stem.getId());
    }
    
    Set<GrouperObject> children = new HashSet<GrouperObject>(parentStem.getChildGroups(Scope.SUB));
    children.addAll(parentStem.getChildStems(Scope.SUB));
    
    for (GrouperObject childGrouperObject: children) {
      boolean shouldSaveForThisChild = true;
      
      GrouperObjectTypesAttributeValue mayBeGroupTypeAttributeValue = getGrouperObjectTypesAttributeValue(childGrouperObject, valueToSave.getObjectTypeName());
      if (mayBeGroupTypeAttributeValue != null) {
        
        if (mayBeGroupTypeAttributeValue.isDirectAssignment()) {
          shouldSaveForThisChild = false;
          continue;
        }
        
        String ownerStemId = mayBeGroupTypeAttributeValue.getObjectTypeOwnerStemId();

        // some child of parentStem's settings are already configured on this group/stem, we don't need to update because we will increase the distance otherwise
        if (childrenStemIds.contains(ownerStemId)) {
          shouldSaveForThisChild = false;
        }
        
      }
      
      if (shouldSaveForThisChild) {
        saveOrUpdateTypeAttributes(valueToSave, childGrouperObject);
      }
      
    }
    
  }
  
  
  private static AttributeAssign getAttributeAssign(GrouperObject grouperObject, String objectType) {
    
    Set<AttributeAssign> attributeAssigns = new HashSet<AttributeAssign>();
    
    if (grouperObject instanceof Group) {
      Group group = (Group)grouperObject;
      attributeAssigns = group.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    } else if (grouperObject instanceof Stem) {
      Stem stem = (Stem)grouperObject;
      attributeAssigns = stem.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    }
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME);
      if (attributeAssignValue == null || StringUtils.isBlank(attributeAssignValue.getValueString())) {
        return null;
      }
      
      String objectTypeNameFromDB = attributeAssignValue.getValueString();
      if (objectType.equals(objectTypeNameFromDB)) {
       return attributeAssign;
      }
    }
    return null;
    
  } 
  
  private static GrouperObjectTypesAttributeValue buildGrouperObjectTypeAttributeValue(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    GrouperObjectTypesAttributeValue result = new GrouperObjectTypesAttributeValue();
    result.setObjectTypeName(attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME).getValueString());
    
    AttributeAssignValue dataOwnerAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DATA_OWNER);
    result.setObjectTypeDataOwner(dataOwnerAssignValue != null ? dataOwnerAssignValue.getValueString(): null);
    
    AttributeAssignValue memberDescriptionAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_MEMBERS_DESCRIPTION);
    result.setObjectTypeMemberDescription(memberDescriptionAssignValue != null ? memberDescriptionAssignValue.getValueString(): null);

    AttributeAssignValue serviceNameAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_SERVICE_NAME);
    result.setObjectTypeServiceName(serviceNameAssignValue != null ? serviceNameAssignValue.getValueString(): null);
    
    AttributeAssignValue ownerStemIdAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_OWNER_STEM_ID);
    result.setObjectTypeOwnerStemId(ownerStemIdAssignValue != null ? ownerStemIdAssignValue.getValueString(): null);
    
    AttributeAssignValue directAssignmentAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT);
    String directAssignmentStr = directAssignmentAssignValue != null ? directAssignmentAssignValue.getValueString(): null;
    boolean directAssignment = BooleanUtils.toBoolean(directAssignmentStr);
    result.setDirectAssignment(directAssignment);
    return result;
  }

}
