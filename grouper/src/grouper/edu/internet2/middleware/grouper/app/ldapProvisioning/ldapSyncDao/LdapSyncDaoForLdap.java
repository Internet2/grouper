package edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao;

import java.util.List;

import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;

public class LdapSyncDaoForLdap extends LdapSyncDao {

  @Override
  public List<LdapEntry> search(String ldapPoolName, String baseDn, String filter, LdapSearchScope ldapSearchScope, List<String> attributeNames) {
    return LdapSessionUtils.ldapSession().list(ldapPoolName, baseDn, ldapSearchScope, filter, attributeNames.toArray(new String[] {}), null);    
  }

  @Override
  public List<LdapEntry> read(String ldapPoolName, String baseDn, List<String> dnList, List<String> attributeNames) {
    return LdapSessionUtils.ldapSession().read(ldapPoolName, baseDn, dnList, attributeNames.toArray(new String[] {}));
  }

  @Override
  public void delete(String ldapPoolName, String dn) {
    LdapSessionUtils.ldapSession().delete(ldapPoolName, dn);
  }

  @Override
  public boolean create(String ldapPoolName, LdapEntry ldapEntry) {
    return LdapSessionUtils.ldapSession().create(ldapPoolName, ldapEntry);
  }

  @Override
  public boolean move(String ldapPoolName, String oldDn, String newDn) {
    // what happens if newdn exists and old doesn't?  return false?
    // return true if moved
    
    // what if OU is changing?  do some ldaps not allow that or allow with a config?  should the provisioner or pool have a config on whether to allow a move that changes OUs or to do a delete/recreate?
    
    // TODO Auto-generated method stub
    
    return false;
  }

  @Override
  public void internal_modifyHelper(String ldapPoolName, String dn, List<LdapModificationItem> ldapModificationItems) {
    
    // TODO Auto-generated method stub
    
  }
}
