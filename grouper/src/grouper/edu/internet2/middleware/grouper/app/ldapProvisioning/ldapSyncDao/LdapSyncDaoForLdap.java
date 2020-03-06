package edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao;

import java.util.List;

import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;

public class LdapSyncDaoForLdap extends LdapSyncDao {

  @Override
  public List<LdapEntry> search(String ldapPoolName, String baseDn, String filter, LdapSearchScope ldapSearchScope, List<String> attributeNames) {
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list(ldapPoolName, baseDn, ldapSearchScope, filter, attributeNames.toArray(new String[] {}), null);
    
    return ldapEntries;
  }

  @Override
  public List<LdapEntry> read(String ldapPoolName, List<String> dnList, List<String> attributeNames) {

    // If there's a config that specifies the dn attribute, then option 1: "(|(entryDn=dn1)(entryDn=dn2)(entryDn=dn3)(entryDn=dn4))"  (or distinguishedName for AD)
    // option 2, loop through each: "(objectclass=*)" basedn = dn1  (warn, print once)
    return null;
  }

  @Override
  public boolean delete(String ldapPoolName, String dn) {
    
    // true if deleted, false if didn't exist
    // note if there's an error during deletion, if the exception says it didn't exist, then return false.  otherwise query and see if it exists or not.  if doesn't, return false, if does throw exception
    // TODO Auto-generated method stub
    
    return false;
  }

  @Override
  public boolean create(String ldapPoolName, LdapEntry ldapEntry) {
    
    // if create failed because object is there, then do an update with the attributes that were given
    // some attributes given may have no values and therefore clear those attributes?
    // true if created, false if updated
    // TODO Auto-generated method stub
    
    return false;
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
