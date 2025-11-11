package edu.internet2.middleware.grouper.userLifecycle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UserLifecycleService {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UserLifecycleService.class);
  
  /**
   * 
   * @return number of lifecycle event configs
   */
  public static int retrieveUserLifecycleEventNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperLifecycleEventConfig.lifecycleEventConfigIds));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of lifecycle action configs
   */
  public static int retrieveUserLifecycleActionNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(UserLifecycleActionConfiguration.lifecycleActionConfigIds));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of lifecycle policy configs
   */
  public static int retrieveUserLifecyclePolicyNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(UserLifecyclePolicyConfiguration.lifecyclePolicyConfigIds));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of lifecycle policy part configs
   */
  public static int retrieveUserLifecyclePolicyPartNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(UserLifecyclePolicyPartConfiguration.lifecyclePolicyPartConfigIds));
    return configIdsInConfig.size();
  }
  
  /**
   * Retrieve user lifecycle policies for the given subject. For sysadmin and public policies, no permission check is done 
   * @param subject
   * @return
   */
  public static List<UserLifecyclePolicyConfiguration> retrieveUserLifecyclePolicies(Subject subject) {
    
    final List<UserLifecyclePolicyConfiguration> result = new ArrayList<>();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        
        List<UserLifecyclePolicyConfiguration> allUserLifecyclePolicyConfigurations = UserLifecyclePolicyConfiguration.retrieveAllUserLifecyclePolicyConfigurations();
        
        if (PrivilegeHelper.isWheelOrRoot(subject)) {
          result.addAll(allUserLifecyclePolicyConfigurations);
          return result;
        }
        
        for (UserLifecyclePolicyConfiguration policyConfiguration: allUserLifecyclePolicyConfigurations) {
          String attributeValueFromConfig = policyConfiguration.retrieveAttributeValueFromConfig("isPublic", false);
          if (GrouperUtil.booleanValue(attributeValueFromConfig, false)) {
            result.add(policyConfiguration);
            continue;
          } 
          
          String groupIdOrName = policyConfiguration.retrieveAttributeValueFromConfig("groupIdOrName", false);
          
          Group group = GroupFinder.findByUuid(groupIdOrName, false);
          if (group == null) {
            group = GroupFinder.findByName(groupIdOrName, false);
          }
          
          if (group == null) {
            LOG.error(String.format("On user lifecycle policy config '%s' group '%s' not found", policyConfiguration.getConfigId(), groupIdOrName));
            continue;
          }
          
          if (group.hasMember(subject)) {
            result.add(policyConfiguration);
          }
          
        }
        
        return result;
        
      };
    });
    return result;
  }
  
  public static void savePolicyConfigOnGroup(Group group, String policyConfigId, Subject subject) {
    
    List<UserLifecyclePolicyConfiguration> userLifecyclePolicies = retrieveUserLifecyclePolicies(subject);
    Set<String> totalPolicyConfigIdsSubjectCanAccess = new HashSet<String>();
    for (UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration: userLifecyclePolicies) {
      totalPolicyConfigIdsSubjectCanAccess.add(userLifecyclePolicyConfiguration.getConfigId());
    }
    
//    String existingPolicyConfigId = retrieveExistingPolicyConfigId(group);
    boolean subjectAllowedToConfigurePolicy = false;
    
    if (StringUtils.isBlank(policyConfigId) || totalPolicyConfigIdsSubjectCanAccess.contains(policyConfigId)) {
      subjectAllowedToConfigurePolicy = true;
    }
    
    // if user is adding a policy for the first time on the group, then check if the new policy can be accessed by the user
//    if (StringUtils.isBlank(existingPolicyConfigId)) {
//      
//      if (totalPolicyConfigIdsSubjectCanAccess.contains(policyConfigId)) {
//        subjectAllowedToConfigurePolicy = true;
//      }
//    } else {
//      // if user is modifying an existing policy, then check both the existing policy and new policy can be accessed by the user
//      if (totalPolicyConfigIdsSubjectCanAccess.contains(existingPolicyConfigId) && (StringUtils.isBlank(policyConfigId)) 
//          || totalPolicyConfigIdsSubjectCanAccess.contains(existingPolicyConfigId)) {
//        subjectAllowedToConfigurePolicy = true;
//      }
//    }
    
    if (!subjectAllowedToConfigurePolicy) {
      throw new RuntimeException("Subject "+subject.getId()+ " not allowed to save user lifecycle policy!!");
    }
    
    //TODO: add audits, type: attribute assign. ideally a new one - assign policy, remove policy
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      @Override
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        
//        Set<AttributeAssign> attributeAssigns = group.getAttributeDelegate().retrieveAssignments(UserLifecycleAttributeNames.retrieveAttributeDefNameMarker());
        
        AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", UserLifecycleAttributeNames.retrieveAttributeDefNameMarker(), false, false);
        
//        AttributeAssign attributeAssign = UserLifecycleAttributeNames.getAttributeAssignMatchingPolicyConfigId(attributeAssigns, policyConfigId);
        if (attributeAssign == null && StringUtils.isNotBlank(policyConfigId)) {
          // we only want to add the assignment if the policy we're going to add is not blank. blank actually means remove it if it already exists
          attributeAssign = group.getAttributeDelegate().assignAttribute(UserLifecycleAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
        }
        
        if (StringUtils.isBlank(policyConfigId) && attributeAssign != null) {
          attributeAssign.delete();
        } else {
          AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_POLICY_GROUP_VALUE_CONFIG_ID, true);
          attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), policyConfigId);
          attributeAssign.saveOrUpdate();
        }
        return null;
        
      };
    });
    
  }
  
  public static String retrieveExistingPolicyConfigId(Group group) {
    
    String result = (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      @Override
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        
        AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", UserLifecycleAttributeNames.retrieveAttributeDefNameMarker(), false, false);
        
        if (attributeAssign == null) {
          return "";
        }
        
        AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_POLICY_GROUP_VALUE_CONFIG_ID);
        if (attributeAssignValue == null || StringUtils.isBlank(attributeAssignValue.getValueString())) {
          return "";
        } 
        
        String policyConfigIdFromDb = attributeAssignValue.getValueString();
        
        return policyConfigIdFromDb == null ? "" : policyConfigIdFromDb;
      };
    });
    
    return result;
    
  }

}
