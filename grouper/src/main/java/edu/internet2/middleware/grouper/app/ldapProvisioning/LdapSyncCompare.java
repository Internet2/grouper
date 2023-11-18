package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.RDN;

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
    } catch (LDAPException e) {
      LOG.warn("Failed to parse DN: " + grouperValue, e);
      return originalCheck;
    }
    
    try {
      dn2 = getMinimallyEncodedDNStringWithLowercaseAttributeNamesIgnoreBase((String)targetValue, baseDnString);
    } catch (LDAPException e) {
      LOG.warn("Failed to parse DN: " + targetValue, e);
      return originalCheck;
    }
    
    return GrouperUtil.equals(dn1, dn2);
  }
  
  private static String getMinimallyEncodedDNStringWithLowercaseAttributeNamesIgnoreBase(String dnString, String baseDnString) throws LDAPException {
    DN dn = new DN(dnString);
    
    List<RDN> rdns;
    
    if (StringUtils.isEmpty(baseDnString)) {
      rdns = Arrays.asList(dn.getRDNs());
    } else {
      rdns = new ArrayList<RDN>();
      List<RDN> remainingRdns = new ArrayList<>(Arrays.asList(dn.getRDNs()));
      String baseDnNormalizedString = new DN(baseDnString).toNormalizedString();
      
      while (remainingRdns.size() > 0) {
        if (new DN(remainingRdns.toArray(new RDN[0])).toNormalizedString().equals(baseDnNormalizedString)) {
          // the rest is the base
          break;
        }
        rdns.add(remainingRdns.remove(0));
      }
    }
    
    for (RDN rdn : rdns) {
      String[] oldAttributeNames = rdn.getAttributeNames();
      String[] newAttributeNames = new String[oldAttributeNames.length];
      for (int i = 0; i < oldAttributeNames.length; i++) {
        newAttributeNames[i] = oldAttributeNames[i].toLowerCase();
      }
      
      GrouperUtil.assignField(rdn, "attributeNames", newAttributeNames);      
    }
    
    return new DN(rdns).toMinimallyEncodedString();
  }
}
