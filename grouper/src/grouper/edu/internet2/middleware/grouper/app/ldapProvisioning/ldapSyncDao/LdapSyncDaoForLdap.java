package edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao;

import java.util.List;

import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSession;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;

public class LdapSyncDaoForLdap extends LdapSyncDao {

  @Override
  public List<LdapEntry> search(String ldapPoolName, String baseDn, String filter, LdapSearchScope ldapSearchScope, List<String> attributeNames) {
    LdapSession ldapSession = LdapSessionUtils.ldapSession();
    List<LdapEntry> result = ldapSession.list(ldapPoolName, baseDn, ldapSearchScope, filter, attributeNames.toArray(new String[] {}), null);
    return result;    
  }
  
  @Override
  public List<LdapEntry> search(String ldapPoolName, String baseDn, String filter, LdapSearchScope ldapSearchScope, List<String> attributeNames, Long sizeLimit) {
    LdapSession ldapSession = LdapSessionUtils.ldapSession();
    List<LdapEntry> result = ldapSession.list(ldapPoolName, baseDn, ldapSearchScope, filter, attributeNames.toArray(new String[] {}), sizeLimit);
    return result;    
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
    return LdapSessionUtils.ldapSession().move(ldapPoolName, oldDn, newDn);
  }

  @Override
  public void internal_modifyHelper(String ldapPoolName, String dn, List<LdapModificationItem> ldapModificationItems) {
    LdapSessionUtils.ldapSession().internal_modifyHelper(ldapPoolName, dn, ldapModificationItems);
  }

}
