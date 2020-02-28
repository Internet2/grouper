package edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSyncAttributeMetadata;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSyncConfiguration;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSyncObject;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSyncObjectContainer;

/**
 * access LDAP or dry run or testing
 * @author mchyzer
 *
 */
public abstract class LdapSyncDao {

  /**
   * do a filter search
   * @param ldapSyncConfiguration
   * @param ldapPoolName
   * @param baseDn
   * @param filter
   * @param attributeNames are optional attribute names to get from the ldap object
   * @return the data
   */
  public abstract LdapSyncObjectContainer search(LdapSyncConfiguration ldapSyncConfiguration, String ldapPoolName, String baseDn, String filter, 
      List<String> attributeNames );
  
  /**
   * find objects by dn's
   * @param ldapSyncConfiguration
   * @param ldapPoolName
   * @param dnList
   * @param attributeNames are optional attribute names to get from the ldap object
   * @return the data
   */
  public abstract LdapSyncObjectContainer read(LdapSyncConfiguration ldapSyncConfiguration, String ldapPoolName, List<String> dnList, 
      List<String> attributeNames );
  
  /**
   * delete an object by dn
   * @param ldapSyncConfiguration
   * @param ldapPoolName
   * @param dn
   */
  public abstract void delete(LdapSyncConfiguration ldapSyncConfiguration, String ldapPoolName, String dn);

  /**
   * create an object by dn
   * @param ldapSyncConfiguration
   * @param ldapPoolName
   * @param dn
   * @param ldapSyncAttributeMetadatas metadata for attributes
   * @param ldapSyncObject contains dn and attribute values
   */
  public abstract void create(LdapSyncConfiguration ldapSyncConfiguration, 
      String ldapPoolName, List<LdapSyncAttributeMetadata> ldapSyncAttributeMetadatas, LdapSyncObject ldapSyncObject);

  /**
   * move an object to a new dn
   * @param ldapSyncConfiguration
   * @param ldapPoolName
   * @param oldDn
   * @param newDn
   */
  public abstract void move(LdapSyncConfiguration ldapSyncConfiguration, 
      String ldapPoolName, String oldDn, String newDn);

  /**
   * modify attributes for an object.  this should be done in bulk, and if there is an error, should be done ind
   * @param ldapSyncConfiguration
   * @param ldapPoolName
   * @param dn
   * @param ldapSyncDaoModifications
   * @return the result
   */
  public final LdapSyncDaoResult modify(LdapSyncConfiguration ldapSyncConfiguration, 
      String ldapPoolName, String dn, List<LdapSyncDaoModification> ldapSyncDaoModifications) {
    
    // do the bulk with internal_modifyHelper
    
    // if not exception return a success
    
    // if exception, get the object by dn with a read
    
    // note, pspng would do compare on each value as processing
    
    // compare attributes, try each individually with individ value: internal_modifyHelperSingle
    
    // return the result
    
    return null;
  }

  /**
   * modify attributes for an object.  this should be done in bulk, and if there is an error, throw it
   * @param ldapSyncConfiguration
   * @param ldapPoolName
   * @param dn
   * @param ldapSyncDaoModifications
   * @throws Exception if problem
   */
  public abstract void internal_modifyHelperMultiple(LdapSyncConfiguration ldapSyncConfiguration, 
      String ldapPoolName, String dn, List<LdapSyncDaoModification> ldapSyncDaoModifications);
  
  /**
   * modify attributes for an object for one attribute value, if error, return it, else null for success
   * @param ldapSyncConfiguration
   * @param ldapPoolName
   * @param dn
   * @param ldapSyncDaoModifications
   * @return null if ok, object if error
   */
  public abstract LdapSyncDaoAttributeError internal_modifyHelperSingle(LdapSyncConfiguration ldapSyncConfiguration, 
      String ldapPoolName, String dn, LdapSyncDaoModification ldapSyncDaoModification);
  
}
