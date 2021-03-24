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
    ldapSession.assignDebug(this.isDebug());
    List<LdapEntry> result = ldapSession.list(ldapPoolName, baseDn, ldapSearchScope, filter, attributeNames.toArray(new String[] {}), null);
    if (this.isDebug()) {
      this.debugLog.append(ldapSession.getDebugLog());
    }
    return result;    
  }
  
  @Override
  public List<LdapEntry> search(String ldapPoolName, String baseDn, String filter, LdapSearchScope ldapSearchScope, List<String> attributeNames, Long sizeLimit) {
    LdapSession ldapSession = LdapSessionUtils.ldapSession();
    ldapSession.assignDebug(this.isDebug());
    List<LdapEntry> result = ldapSession.list(ldapPoolName, baseDn, ldapSearchScope, filter, attributeNames.toArray(new String[] {}), sizeLimit);
    if (this.isDebug()) {
      this.debugLog.append(ldapSession.getDebugLog());
    }
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

  /**
   * debug log where lines are separated by newlines
   */
  private StringBuilder debugLog = null;

  
  /**
   * if we are debugging
   */
  private boolean debug = false;
  
  /**
   * if we are debugging
   * @return
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * if we should capture debug info
   * @param inDiagnostics
   */
  public void assignDebug(boolean inDiagnostics) {
    this.debug = inDiagnostics;
    if (inDiagnostics) {
      this.debugLog = new StringBuilder();
    } else {
      this.debugLog = null;
    }
  }
  
  /**
   * debug log where lines are separated by newlines
   * @return
   */
  public StringBuilder getDebugLog() {
    return debugLog;
  }

}
