package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDao;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogic;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class LdapSyncLogic extends GrouperProvisioningLogic {

  public LdapSyncLogic() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void retrieveAllTargetAndGrouperDataPost() {
    
    super.retrieveAllTargetAndGrouperDataPost();
    
    // first are we even doing this?
    if (((LdapSyncConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isAllowLdapGroupDnOverride()) {
      
      List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups();

      Set<String> grouperDnOverrides = new HashSet<String>();
      
      for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(grouperProvisioningGroups)) {
        String grouperDnOverride = provisioningGroup.retrieveAttributeValueString("md_grouper_ldapGroupDnOverride");
        if (!StringUtils.isBlank(grouperDnOverride)) {
          grouperDnOverrides.add(grouperDnOverride);
        }
      }

      if (grouperDnOverrides.size() > 0) {
        List<ProvisioningGroup> targetProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups();
        Set<String> targetDns = new HashSet<String>();
        for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetProvisioningGroups)) {
          String dn = provisioningGroup.retrieveAttributeValueString(LdapProvisioningTargetDao.ldap_dn);
          if (!StringUtils.isBlank(dn)) {
            targetDns.add(dn);
          }
        }
        
        Set<String> dnsToFind = new HashSet<String>(grouperDnOverrides);
        dnsToFind.removeAll(targetDns);
        
        for (String dn : dnsToFind) {
          
          LdapProvisioningTargetDao ldapProvisioningTargetDao = (LdapProvisioningTargetDao)this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao();
          ProvisioningGroup provisioningGroup = ldapProvisioningTargetDao.retrieveGroupByDn(dn, true, false);
          if (provisioningGroup != null) {
            targetProvisioningGroups.add(provisioningGroup);
          }
        }
        
      }
      
      
      

    }
    
    
    
  }

}
