package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import org.ldaptive.dn.DefaultRDnNormalizer;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.MinimalAttributeValueEscaper;
import org.ldaptive.dn.RDn;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningCompare;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class LdapSyncCompare extends GrouperProvisioningCompare {

  private static final Log LOG = GrouperUtil.getLog(LdapSyncCompare.class);
  
  @Override
  public boolean attributeValueEquals(String attributeName, Object grouperValue,
      Object targetValue, ProvisioningUpdatable grouperTargetUpdatable) {
    boolean originalCheck = super.attributeValueEquals(attributeName, grouperValue, targetValue,
        grouperTargetUpdatable);
    
    // if it's equal, return
    if (originalCheck) {
      return originalCheck;
    }
    
    // if this isn't the dn, return
    if (!LdapProvisioningTargetDao.ldap_dn.equals(attributeName)) {
      return originalCheck;
    }
    
    // if either value is null, return
    if (grouperValue == null || targetValue == null) {
      return originalCheck;
    }
    
    String dn1;
    String dn2;
    
    String baseDnString = null;
    if ("group".equals(grouperTargetUpdatable.objectTypeName())) {
      baseDnString = ((LdapSyncConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).getGroupSearchBaseDn();
    } else if ("entity".equals(grouperTargetUpdatable.objectTypeName())) {
      baseDnString = ((LdapSyncConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).getUserSearchBaseDn();
    }
    
    try {
      dn1 = getMinimallyEncodedDNStringWithLowercaseAttributeNamesIgnoreBase((String)grouperValue, baseDnString);
    } catch (Exception e) {
      LOG.warn("Failed to parse DN: " + grouperValue, e);
      return originalCheck;
    }
    
    try {
      dn2 = getMinimallyEncodedDNStringWithLowercaseAttributeNamesIgnoreBase((String)targetValue, baseDnString);
    } catch (Exception e) {
      LOG.warn("Failed to parse DN: " + targetValue, e);
      return originalCheck;
    }
    
    return GrouperUtil.equals(dn1, dn2);
  }
  
  private static String getMinimallyEncodedDNStringWithLowercaseAttributeNamesIgnoreBase(String dnString, String baseDnString) {
    Dn dn = new Dn(dnString);
    
    List<RDn> rdns;
    
    if (StringUtils.isEmpty(baseDnString)) {
      rdns = dn.getRDns();
    } else {
      rdns = new ArrayList<>();
      List<RDn> remainingRdns = dn.getRDns();
      Dn baseDn = new Dn(baseDnString);
      
      while (remainingRdns.size() > 0) {
        if (new Dn(remainingRdns).isSame(baseDn)) {
          // the rest is the base
          break;
        }
        rdns.add(remainingRdns.remove(0));
      }
    }
    
    return new Dn(rdns).format(
      new DefaultRDnNormalizer(new MinimalAttributeValueEscaper(), DefaultRDnNormalizer.LOWERCASE, s -> s));
  }
}
