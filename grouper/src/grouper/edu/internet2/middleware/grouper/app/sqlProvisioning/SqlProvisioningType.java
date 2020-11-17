package edu.internet2.middleware.grouper.app.sqlProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * types of sql provisioning
 * @author mchyzer-local
 *
 */
public enum SqlProvisioningType {

  /**
   * group table and memberships tables
   */
  groupsAndMembershipsTables {

    @Override
    public Class<? extends GrouperProvisionerTargetDaoBase> sqlTargetDaoClass() {
      throw new UnsupportedOperationException();
    }
  },
  
  /**
   * only a memberships table
   */
  membershipsTable {

    @Override
    public Class<? extends GrouperProvisionerTargetDaoBase> sqlTargetDaoClass() {
      throw new UnsupportedOperationException();
    }
  },
  
  /**
   * table for groups, entities, and memberships
   */
  groupsAndEntitiesAndMembershipsTables {

    @Override
    public Class<? extends GrouperProvisionerTargetDaoBase> sqlTargetDaoClass() {
      throw new UnsupportedOperationException();
    }
  },
  
  /**
   * group table and attributes table like ldap
   * might have user link table with attributes too
   */
  sqlLikeLdapGroupMemberships {

    @Override
    public Class<? extends GrouperProvisionerTargetDaoBase> sqlTargetDaoClass() {
      return SqlProvisioningDaoGroupsWithAttributesAsMembersLikeLdap.class;
    }
  },

  /**
   * entity table and attributes table like ldap
   * might have group table with attributes too
   */
  sqlLikeLdapUserAttributes {

    @Override
    public Class<? extends GrouperProvisionerTargetDaoBase> sqlTargetDaoClass() {
      throw new UnsupportedOperationException();
    }
  };
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static SqlProvisioningType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(SqlProvisioningType.class, string, exceptionOnNull);
  }

  public abstract Class<? extends GrouperProvisionerTargetDaoBase> sqlTargetDaoClass();
  
}
