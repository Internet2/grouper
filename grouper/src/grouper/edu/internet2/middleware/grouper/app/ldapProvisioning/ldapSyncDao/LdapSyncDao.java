package edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao;

import java.util.List;

import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationResult;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;

/**
 * access LDAP or dry run or testing
 * @author mchyzer
 *
 */
public abstract class LdapSyncDao {

  /**
   * do a filter search
   * @param ldapPoolName
   * @param baseDn
   * @param filter
   * @param ldapSearchScope
   * @param attributeNames are optional attribute names to get from the ldap object
   * @return the data
   */
  public abstract List<LdapEntry> search(String ldapPoolName, String baseDn, String filter, LdapSearchScope ldapSearchScope, List<String> attributeNames );
  
  /**
   * find objects by dn's
   * @param ldapPoolName
   * @param dnList
   * @param attributeNames are optional attribute names to get from the ldap object
   * @return the data
   */
  public abstract List<LdapEntry> read(String ldapPoolName, List<String> dnList, List<String> attributeNames);
  
  /**
   * delete an object by dn
   * @param ldapPoolName
   * @param dn
   * @return true if deleted, false if didn't exist
   */
  public abstract boolean delete(String ldapPoolName, String dn);

  /**
   * create an object
   * @param ldapPoolName
   * @param ldapEntry
   * @return true if created, false if existed and updated
   */
  public abstract boolean create(String ldapPoolName, LdapEntry ldapEntry);

  /**
   * move an object to a new dn
   * @param ldapPoolName
   * @param oldDn
   * @param newDn
   * @return true if moved, false if newDn exists and oldDn doesn't exist so no update
   */
  public abstract boolean move(String ldapPoolName, String oldDn, String newDn);

  /**
   * modify attributes for an object.  this should be done in bulk, and if there is an error, should be done individually
   * @param ldapPoolName
   * @param dn
   * @param ldapModificationItems
   * @return the result
   */
  public final LdapModificationResult modify(String ldapPoolName, String dn, List<LdapModificationItem> ldapModificationItems) {
    
    // do the bulk with internal_modifyHelper (in batches based on ldap setting)
    
    // if not exception return a success
    
    // if exception, get the object by dn with a read
    
    // note, pspng would do compare on each value as processing
    
    // compare attributes, try each individually with individ value: internal_modifyHelperSingle
    
    // return the result
        
    return null;
  }

  /**
   * modify attributes for an object.  this should be done in bulk, and if there is an error, throw it
   * @param ldapPoolName
   * @param dn
   * @param ldapModificationItems
   * @throws Exception if problem
   */
  public abstract void internal_modifyHelper(String ldapPoolName, String dn, List<LdapModificationItem> ldapModificationItems);
}
