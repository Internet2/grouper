package edu.internet2.middleware.grouper.app.duo.role;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslatorBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class DuoRoleTranslator extends GrouperProvisioningTranslatorBase {

  @Override
  public List<ProvisioningEntity> translateGrouperToTargetEntities(
      List<ProvisioningEntity> grouperProvisioningEntities, boolean includeDelete,
      boolean forCreate) {
    List<ProvisioningEntity> translatedProvisioningEntities = super.translateGrouperToTargetEntities(grouperProvisioningEntities, includeDelete,
        forCreate);
    
    for (ProvisioningEntity provisioningEntity: GrouperUtil.nonNull(translatedProvisioningEntities)) {
      
      Set<String> roles = (Set<String>)provisioningEntity.retrieveAttributeValueSet("role");
      if (GrouperUtil.length(roles) > 1) {
        
        String highestPriorityRoleName = pickHighestPriorityRoleName(roles);
        roles.clear();
        roles.add(highestPriorityRoleName);
        // provisioningEntity.assignAttributeValue("role", roles);
      }
      
    }
    
    return translatedProvisioningEntities;
    
  }
  
  private String pickHighestPriorityRoleName(Set<String> roleNames) {
    
    if (roleNames.contains("Owner")) {
      return "Owner";
    } else if (roleNames.contains("Administrator")) {
      return "Administrator";
    } else if (roleNames.contains("Application Manager")) {
      return "Application Manager";
    } else if (roleNames.contains("User Manager")) {
      return "User Manager";
    } else if (roleNames.contains("Help Desk")) {
      return "Help Desk";
    } else if (roleNames.contains("Billing")) {
      return "Billing";
    } else if (roleNames.contains("Phishing Manager")) {
      return "Phishing Manager";
    } else if (roleNames.contains("Read-only")) {
      return "Read-only";
    }
    
    
    throw new RuntimeException("invalid role names");
  }

}
