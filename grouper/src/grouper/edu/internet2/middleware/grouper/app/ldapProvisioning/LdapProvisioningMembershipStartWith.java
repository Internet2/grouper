/**
 * 
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

/**
 */
public class LdapProvisioningMembershipStartWith extends ProvisionerStartWithBase {

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "ldapMemberships";
  }

  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(Map<String, String> startWithSuffixToValue,
      Map<String, Object> provisionerSuffixToValue) {
    // TODO Auto-generated method stub
    
  }
  
  

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    } 
    
    GrouperConfigurationModuleAttribute ldapPatternAttribute = this.retrieveAttributes().get("ldapPattern");
    
    String ldapPattern = ldapPatternAttribute.getValueOrExpressionEvaluation();
    
    if (StringUtils.equals(ldapPattern, "activeDirectoryGroups")) {
      GrouperConfigurationModuleAttribute ldapExternalSystemConfigId = this.retrieveAttributes().get("ldapExternalSystemConfigId");
      String ldapExternalSystemId = ldapExternalSystemConfigId.getValueOrExpressionEvaluation();
      
      boolean isActiveDirectory = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("ldap."+ldapExternalSystemId+".isActiveDirectory", false);
      
      if (!isActiveDirectory) {
        String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithLdapConfigurationValidationExternalSystemNotActiveDirectory");
        errorMessage = errorMessage.replace("$$externalSystemId$$", ldapExternalSystemId);
        validationErrorsToDisplay.put(ldapExternalSystemConfigId.getHtmlForElementIdHandle(), errorMessage);
      }
      
    }
    
    //TODO wait until all source attributes are exposed 
    // list those attributes and validate against subject source (textfield, comma separated attributes, required)
//    GrouperConfigurationModuleAttribute entityResolverAttributes = this.retrieveAttributes().get("subjectSourceEntityResolverAttributes");
//    if (entityResolverAttributes != null) {
//      String subjectAttributesCommaSeparated = entityResolverAttributes.getValueOrExpressionEvaluation();
//      Set<String> sujectAttributes = GrouperUtil.splitTrimToSet(subjectAttributesCommaSeparated, ",");
//      
//      for (Source source: SourceManager.getInstance().getSources()) {
//        Set<String> attributes = source.getInternalAttributes();
//        sujectAttributes.removeAll(attributes);
//      }
//      
//      if (sujectAttributes.size() > 0) {
//        String notFoundAttributes = GrouperUtil.join(sujectAttributes.iterator(), ',');
//        String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithLdapConfigurationValidationSubjectAttributesNotValid");
//        errorMessage = errorMessage.replace("$$subjectAttributes$$", notFoundAttributes);
//        validationErrorsToDisplay.put(entityResolverAttributes.getHtmlForElementIdHandle(), errorMessage);
//      }
//      
//    }
    
    
  }

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue, Set<String> suffixesUserJustChanged) {
    
    Map<String, String> result = new HashMap<>();
    
    for (String suffixUserJustChanged: suffixesUserJustChanged) {
      
      if (StringUtils.equals(suffixUserJustChanged, "ldapPattern")) {
        String valueUserEnteredOnScreen = suffixToValue.get(suffixUserJustChanged);
        if (StringUtils.equals(valueUserEnteredOnScreen, "activeDirectoryGroups")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("objectClassesForGroups", "top,group");
          result.put("membershipAttributeNameForGroups", "member");
          result.put("membershipValueDn", "true");
          result.put("groupOrganization", "bushy");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "bushyGroupsWithMembershipDNs")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("membershipValueDn", "true");
          result.put("groupOrganization", "bushy");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "bushyGroupsWithMembershipSubjectIds")) {
          result.put("userAttributesType", "core");
          result.put("membershipStructure", "groupAttributes");
          result.put("membershipValueDn", "false");
          result.put("groupOrganization", "bushy");
          result.put("membershipValueForGroups", "subjectId");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "flatGroupsWithMembershipDNs")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("membershipValueDn", "true");
          result.put("groupOrganization", "flat");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "flatGroupsWithMembershipSubjectIds")) {
          result.put("userAttributesType", "core");
          result.put("membershipStructure", "groupAttributes");
          result.put("membershipValueDn", "false");
          result.put("groupOrganization", "flat");
          result.put("membershipValueForGroups", "subjectId");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupOfNames")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("objectClassesForGroups", "top,groupOfNames");
          result.put("membershipAttributeNameForGroups", "member");
          result.put("membershipValueDn", "true");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "posixGroups")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("objectClassesForGroups", "top,posixGroup");
          result.put("membershipValueDn", "true");
          result.put("membershipAttributeNameForGroups", "member");
          result.put("idIndexAttribute", "gidNumber");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "usersWithEduPersonAffiliations")) {
          result.put("membershipStructure", "entityAttributes");
          result.put("membershipValueDn", "false");
          result.put("membershipAttributeNameForEntities", "eduPersonAffiliation");
          result.put("membershipValueForEntities", "extension");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "usersWithMembershipGroupExtensions")) {
          result.put("membershipStructure", "entityAttributes");
          result.put("membershipValueDn", "false");
          result.put("membershipValueForEntities", "extension");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "usersWithMembershipGroupNames")) {
          result.put("membershipStructure", "entityAttributes");
          result.put("membershipValueDn", "false");
          result.put("membershipValueForEntities", "name");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "other")) {
          result.clear();
        }
      }
      
    }
    
    return result;
  }
  
  
  
}
