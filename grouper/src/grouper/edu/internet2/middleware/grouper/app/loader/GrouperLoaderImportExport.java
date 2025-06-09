package edu.internet2.middleware.grouper.app.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.abac.GrouperAbac;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMemberships;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperLoaderImportExport {
  
  
  
  public static String exportLoaderConfig(Group group) {
    
    String loaderType = retrieveLoaderType(group);
    
    ArrayNode attributesNode = GrouperUtil.jsonJacksonArrayNode();
    
    ObjectNode outerObjectNode = GrouperUtil.jsonJacksonNode();
    outerObjectNode.put("loaderType", loaderType);
    outerObjectNode.set("attributes", attributesNode);
    
    AttributeDefName attributeDefName = null;
    
    if (StringUtils.equals(loaderType, "JEXL_SCRIPT")) {
      
      attributeDefName = AttributeDefNameFinder.findByName(
          GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);
      
    } else if (StringUtils.equals(loaderType, "RECENT_MEMBERSHIPS")) {
      
      attributeDefName = AttributeDefNameFinder.findByName(
          GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
      
    } else if (StringUtils.equals(loaderType, "SQL")) {
      attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
          .findByNameSecure(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":legacy:attribute:legacyGroupType_grouperLoader", true);
      
    } else if (StringUtils.equals(loaderType, "LDAP")) {
      attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(LoaderLdapUtils.grouperLoaderLdapName(), true);
    }
    
    Set<AttributeAssign> attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    AttributeAssign theAttributeAssign = attributeAssigns.iterator().next();
    
    Set<AttributeAssign> attributeAssignVals = theAttributeAssign.getAttributeDelegate().retrieveAssignments();
    
    for (AttributeAssign attributeAssignVal: attributeAssignVals) {
      Set<AttributeAssignValue> attributeAssignValues = attributeAssignVal.getValueDelegate().getAttributeAssignValues();
      
      if (attributeAssignValues.size() > 0) {
        
        AttributeAssignValue attributeAssignValue = attributeAssignValues.iterator().next();
        
        ObjectNode innerAttributeNode = GrouperUtil.jsonJacksonNode();
        innerAttributeNode.put("attributeName", attributeAssignVal.getAttributeDefName().getName());
        innerAttributeNode.put("attributeValue", attributeAssignValue.getValueString());
        
        attributesNode.add(innerAttributeNode);
        
      }
      
    }
    
    String jsonStringToSend = GrouperUtil.jsonJacksonToString(outerObjectNode);
    return jsonStringToSend;
  }
  
  private static String retrieveLoaderType(Group group) {
    
    boolean isLoaderGroup = false;
    
    AttributeDefName grouperLoader = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":legacy:attribute:legacyGroupType_grouperLoader", false);
    
    //check if the attribute def name is assigned to this group
    if (grouperLoader != null) {
      isLoaderGroup = group.getAttributeDelegate().hasAttribute(grouperLoader);
    }
    
    if (isLoaderGroup) {
      return "SQL";
    }
    
    
    AttributeDefName grouperLoaderLdapName = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure(LoaderLdapUtils.grouperLoaderLdapName(), false);
    
    //check if the attribute def name is assigned to this group
    if (grouperLoaderLdapName != null) {
      isLoaderGroup = group.getAttributeDelegate().hasAttribute(grouperLoaderLdapName);
    }
    
    
    if (isLoaderGroup) {
      return "LDAP";
    }
    
    AttributeDefName recentMemberships = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
        GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
    
    //check if the attribute def name is assigned to this group
    if (recentMemberships != null) {
      isLoaderGroup = group.getAttributeDelegate().hasAttribute(recentMemberships);
    }
    
    if (isLoaderGroup) {
      return "RECENT_MEMBERSHIPS";
    }
    
    AttributeDefName jexlScript = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
        GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);
    
    //check if the attribute def name is assigned to this group
    if (jexlScript != null) {
      isLoaderGroup = group.getAttributeDelegate().hasAttribute(jexlScript);
    }
    
    if (isLoaderGroup) {
      return "JEXL_SCRIPT";
    }
    
    throw new RuntimeException("Group '"+group.getName()+"' is not loader group!!");
    
  }
  
  private static boolean isLoaderAlreadyAssignedOnGroup(Group group) {
    
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":legacy:attribute:legacyGroupType_grouperLoader", true);
    
    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);

    if (attributeAssign != null) {
      return true;
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(
        GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
    
    if (attributeAssign != null) {
      return true;
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(
        GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
    
    if (attributeAssign != null) {
      return true;
    }
    
    attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(LoaderLdapUtils.grouperLoaderLdapName(), true);
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
    
    if (attributeAssign != null) {
      return true;
    }
    
    return false;
  }

  public static List<String> importLoaderConfig(String json, Group group) {
    
    List<String> errors = new ArrayList<String>();
    
    if (StringUtils.isBlank(json)) {
      errors.add("No content found");
      return errors;
    }
    
    if (isLoaderAlreadyAssignedOnGroup(group)) {
      errors.add("Group already has the loader configured");
      return errors;
    }
    
    try {
      JsonNode jsonNode = GrouperUtil.jsonJacksonNode(json);
      String loaderType = GrouperUtil.jsonJacksonGetString(jsonNode, "loaderType");
      
      AttributeDefName attributeDefName  = null;
      
      if (StringUtils.equals(loaderType, "SQL")) {
        
        attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName()
            .findByNameSecure(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":legacy:attribute:legacyGroupType_grouperLoader", true);
        
      } else if ( StringUtils.equals(loaderType, "JEXL_SCRIPT")) {
        
        attributeDefName = AttributeDefNameFinder.findByName(
            GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);
        
      } else if ( StringUtils.equals(loaderType, "RECENT_MEMBERSHIPS")) {
        
        attributeDefName = AttributeDefNameFinder.findByName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
        
      } else if ( StringUtils.equals(loaderType, "LDAP")) {
        attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(LoaderLdapUtils.grouperLoaderLdapName(), true);
      } else {
        errors.add("Invalid json");
        return errors;
      }
      
      AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);

      if (attributeAssign != null) {
        errors.add("Loader is already configured on the group");
        return errors;
      }
      
      attributeAssign = group.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
      
      ArrayNode attributesArray = GrouperUtil.jsonJacksonGetArrayNode(jsonNode, "attributes");
      
      for (int index=0; index<attributesArray.size(); index++) {
        JsonNode singleAttributeNode = attributesArray.get(index);
        String attributeName = GrouperUtil.jsonJacksonGetString(singleAttributeNode, "attributeName");
        String attributeValue = GrouperUtil.jsonJacksonGetString(singleAttributeNode, "attributeValue");
        attributeAssign.getAttributeValueDelegate().assignValue(attributeName, attributeValue);
      }
      
    } catch (Exception e) {
      errors.add("Invalid json");
    }
    
    return errors;
  }

}
