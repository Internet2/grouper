package edu.internet2.middleware.grouper.app.dataProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryFieldConfig;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;

public class GrouperLdapDataProviderQueryTargetDao extends GrouperDataProviderQueryTargetDao {

  @Override
  public List<Object[]> selectData(Map<String, Integer> lowerColumnNameToZeroIndex) {
    List<Object[]> rows = new ArrayList<Object[]>();
    GrouperLdapDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperLdapDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();

    List<String> ldapAttributes = new ArrayList<String>();
    for (GrouperDataProviderQueryFieldConfig grouperDataProviderQueryFieldConfig : grouperDataProviderQueryConfig.getGrouperDataProviderQueryFieldConfigs()) {
      ldapAttributes.add(grouperDataProviderQueryFieldConfig.getProviderDataFieldAttribute());
    }
    
    if (ldapAttributes.size() == 0) {
      //??
      return rows;
    }
    
    if (!ldapAttributes.contains(grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute())) {
      ldapAttributes.add(grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute());
    }
    
    for (int i = 0; i < ldapAttributes.size(); i++) {
      lowerColumnNameToZeroIndex.put(ldapAttributes.get(i).toLowerCase(), i);
    }
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list(
        grouperDataProviderQueryConfig.getProviderQueryLdapConfigId(), 
        grouperDataProviderQueryConfig.getProviderQueryLdapBaseDn(), 
        LdapSearchScope.valueOfIgnoreCase(grouperDataProviderQueryConfig.getProviderQueryLdapSearchScope(), true), 
        grouperDataProviderQueryConfig.getProviderQueryLdapFilter(), 
        ldapAttributes.toArray(new String[0]), null);
    for (LdapEntry ldapEntry : ldapEntries) {
      Object[] row = new Object[ldapAttributes.size()];
      
      for (int i = 0; i < ldapAttributes.size(); i++) {
        Object value = null;

        String ldapAttributeString = ldapAttributes.get(i);
        LdapAttribute ldapAttribute = ldapEntry.getAttribute(ldapAttributeString);
        if (ldapAttribute != null && ldapAttribute.getStringValues().size() > 0) {
          if (ldapAttribute.getStringValues().size() == 1) {
            value = ldapAttribute.getStringValues().iterator().next();
          } else {
            value = new HashSet<String>(ldapAttribute.getStringValues()); 
          }
        }
        
        row[i] = value;
      }
      
      rows.add(row);
    }
    
    return rows;
  }
}
