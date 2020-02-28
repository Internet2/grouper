package edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSyncAttributeMetadata;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSyncConfiguration;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSyncObject;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSyncObjectContainer;

public class LdapSyncDaoForLdap extends LdapSyncDao {

  @Override
  public LdapSyncObjectContainer search(LdapSyncConfiguration ldapSyncConfiguration,
      String ldapPoolName, String baseDn, String filter, List<String> attributeNames) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LdapSyncObjectContainer read(LdapSyncConfiguration ldapSyncConfiguration,
      String ldapPoolName, List<String> dnList, List<String> attributeNames) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void delete(LdapSyncConfiguration ldapSyncConfiguration, String ldapPoolName,
      String dn) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void create(LdapSyncConfiguration ldapSyncConfiguration, String ldapPoolName,
      List<LdapSyncAttributeMetadata> ldapSyncAttributeMetadatas,
      LdapSyncObject ldapSyncObject) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void move(LdapSyncConfiguration ldapSyncConfiguration, String ldapPoolName,
      String oldDn, String newDn) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void internal_modifyHelperMultiple(LdapSyncConfiguration ldapSyncConfiguration,
      String ldapPoolName, String dn,
      List<LdapSyncDaoModification> ldapSyncDaoModifications) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public LdapSyncDaoAttributeError internal_modifyHelperSingle(
      LdapSyncConfiguration ldapSyncConfiguration, String ldapPoolName, String dn,
      LdapSyncDaoModification ldapSyncDaoModification) {
    // TODO Auto-generated method stub
    return null;
  }

}
