package edu.internet2.middleware.grouper.app.ldapProvisioning;

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
  public boolean attributeValueEquals(Object first, Object second) {
    return super.attributeValueEquals(first, second);
  }

  @Override
  public boolean compareFieldValueEquals(String fieldName, Object grouperValue,
      Object targetValue, ProvisioningUpdatable grouperTargetUpdatable) {
    boolean originalCheck = super.compareFieldValueEquals(fieldName, grouperValue, targetValue,
        grouperTargetUpdatable);
    
    // if it's equal, return
    if (originalCheck) {
      return originalCheck;
    }
    
    // if this isn't the dn, return
    if (!"name".equals(fieldName)) {
      return originalCheck;
    }
    
    // if either value is null, return
    if (grouperValue == null || targetValue == null) {
      return originalCheck;
    }
    
    String dn1;
    String dn2;
    
    try {
      dn1 = getMinimallyEncodedDNStringWithLowercaseAttributeNames((String)grouperValue);
    } catch (LDAPException e) {
      LOG.warn("Failed to parse DN: " + grouperValue, e);
      return originalCheck;
    }
    
    try {
      dn2 = getMinimallyEncodedDNStringWithLowercaseAttributeNames((String)targetValue);
    } catch (LDAPException e) {
      LOG.warn("Failed to parse DN: " + targetValue, e);
      return originalCheck;
    }
    
    return GrouperUtil.equals(dn1, dn2);
  }
  
  private String getMinimallyEncodedDNStringWithLowercaseAttributeNames(String dnString) throws LDAPException {
    DN dn = new DN(dnString);
    
    for (RDN rdn : dn.getRDNs()) {
      String[] oldAttributeNames = rdn.getAttributeNames();
      String[] newAttributeNames = new String[oldAttributeNames.length];
      for (int i = 0; i < oldAttributeNames.length; i++) {
        newAttributeNames[i] = oldAttributeNames[i].toLowerCase();
      }
      
      GrouperUtil.assignField(rdn, "attributeNames", newAttributeNames);
    }
    
    return dn.toMinimallyEncodedString();
  }
}
