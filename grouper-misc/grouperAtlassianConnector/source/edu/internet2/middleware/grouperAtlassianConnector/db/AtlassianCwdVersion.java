/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperAtlassianConnector.db.v0.AtlassianCwdGroupV0;
import edu.internet2.middleware.grouperAtlassianConnector.db.v0.AtlassianCwdMembershipV0;
import edu.internet2.middleware.grouperAtlassianConnector.db.v0.AtlassianCwdUserV0;
import edu.internet2.middleware.grouperAtlassianConnector.db.v1.AtlassianCwdGroupV1;
import edu.internet2.middleware.grouperAtlassianConnector.db.v1.AtlassianCwdMembershipV1;
import edu.internet2.middleware.grouperAtlassianConnector.db.v1.AtlassianCwdUserV1;
import edu.internet2.middleware.grouperAtlassianConnector.db.v2.AtlassianCwdGroupV2;
import edu.internet2.middleware.grouperAtlassianConnector.db.v2.AtlassianCwdMembershipV2;
import edu.internet2.middleware.grouperAtlassianConnector.db.v2.AtlassianCwdUserV2;
import edu.internet2.middleware.grouperAtlassianConnector.db.v2.AtlassianUserMappingV2;
import edu.internet2.middleware.grouperAtlassianConnector.db.v3.AtlassianCwdGroupV3;
import edu.internet2.middleware.grouperAtlassianConnector.db.v3.AtlassianCwdMembershipV3;
import edu.internet2.middleware.grouperAtlassianConnector.db.v3.AtlassianCwdUserV3;
import edu.internet2.middleware.grouperAtlassianConnector.db.v3.AtlassianUserMappingV3;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/** 
 * which version of the database
 */
public enum AtlassianCwdVersion {

  
  /** v0 is the older version from confluence (e.g. 3.4.5) */
  V0 {

    @Override
    public Map<String, AtlassianCwdUser> retrieveUsers() {
      return AtlassianCwdUserV0.retrieveUsers();
    }

    @Override
    public AtlassianCwdMembership newMembership() {
      return new AtlassianCwdMembershipV0();
    }

    @Override
    public AtlassianCwdGroup newGroup() {
      return new AtlassianCwdGroupV0();
    }

    @Override
    public AtlassianCwdUser newUser() {
      return new AtlassianCwdUserV0();
    }

    @Override
    public Map<String, AtlassianCwdGroup> retrieveGroups() {
      return AtlassianCwdGroupV0.retrieveGroups();
    }
    @Override
    public List<AtlassianCwdMembership> retrieveMemberships() {
      return AtlassianCwdMembershipV0.retrieveMemberships();
    }

    @Override
    public Map<String, AtlassianUserMapping> retrieveUserMappings() {
      return null;
    }

    @Override
    public boolean doUserMappings() {
      return false;
    }

    @Override
    public AtlassianUserMapping newUserMapping() {
      return null;
    }
},
  
  
  /** v1 is the older version from jira */
  V1 {

    @Override
    public Map<String, AtlassianCwdUser> retrieveUsers() {
      return AtlassianCwdUserV1.retrieveUsers();
    }

    @Override
    public AtlassianCwdMembership newMembership() {
      return new AtlassianCwdMembershipV1();
    }

    @Override
    public AtlassianCwdGroup newGroup() {
      return new AtlassianCwdGroupV1();
    }

    @Override
    public AtlassianCwdUser newUser() {
      return new AtlassianCwdUserV1();
    }

    @Override
    public Map<String, AtlassianCwdGroup> retrieveGroups() {
      return AtlassianCwdGroupV1.retrieveGroups();
    }
    @Override
    public List<AtlassianCwdMembership> retrieveMemberships() {
      return AtlassianCwdMembershipV1.retrieveMemberships();
    }

    @Override
    public Map<String, AtlassianUserMapping> retrieveUserMappings() {
      return null;
    }

    @Override
    public boolean doUserMappings() {
      return false;
    }

    @Override
    public AtlassianUserMapping newUserMapping() {
      return null;
    }
},
  
  /** v2 is the newer version from confluence */
  V2 {

    @Override
    public Map<String, AtlassianCwdUser> retrieveUsers() {
      return AtlassianCwdUserV2.retrieveUsers();
    }

    @Override
    public AtlassianCwdMembership newMembership() {
      return new AtlassianCwdMembershipV2();
    }

    @Override
    public AtlassianCwdGroup newGroup() {
      return new AtlassianCwdGroupV2();
    }

    @Override
    public AtlassianCwdUser newUser() {
      return new AtlassianCwdUserV2();
    }

    @Override
    public Map<String, AtlassianCwdGroup> retrieveGroups() {
      return AtlassianCwdGroupV2.retrieveGroups();
    }

    @Override
    public List<AtlassianCwdMembership> retrieveMemberships() {
      return AtlassianCwdMembershipV2.retrieveMemberships();
    }

    @Override
    public boolean doUserMappings() {
      return true;
    }

    @Override
    public Map<String, AtlassianUserMapping> retrieveUserMappings() {
      return AtlassianUserMappingV2.retrieveUserMappings();
    }

    @Override
    public AtlassianUserMapping newUserMapping() {
      return new AtlassianUserMappingV2();
    }
  },
  /** v3 is the newer version from jira (e.g. 6.3.1) */
  V3 {

    @Override
    public Map<String, AtlassianCwdUser> retrieveUsers() {
      return AtlassianCwdUserV3.retrieveUsers();
    }

    @Override
    public AtlassianCwdMembership newMembership() {
      return new AtlassianCwdMembershipV3();
    }

    @Override
    public AtlassianCwdGroup newGroup() {
      return new AtlassianCwdGroupV3();
    }

    @Override
    public AtlassianCwdUser newUser() {
      return new AtlassianCwdUserV3();
    }

    @Override
    public Map<String, AtlassianCwdGroup> retrieveGroups() {
      return AtlassianCwdGroupV3.retrieveGroups();
    }

    @Override
    public List<AtlassianCwdMembership> retrieveMemberships() {
      return AtlassianCwdMembershipV3.retrieveMemberships();
    }

    @Override
    public boolean doUserMappings() {
      return true;
    }

    @Override
    public Map<String, AtlassianUserMapping> retrieveUserMappings() {
      return AtlassianUserMappingV3.retrieveUserMappings();
    }

    @Override
    public AtlassianUserMapping newUserMapping() {
      return new AtlassianUserMappingV3();
    }
  }
  
  ;

  /**
   * if we should worry about user mappings
   * @return true if yes, false if no
   */
  public abstract boolean doUserMappings();
  
  /**
   * retrieve users
   * @return the map of users
   */
  public abstract Map<String, AtlassianCwdUser> retrieveUsers();
  
  /**
   * retrieve user mappings
   * @return the map of usermappings
   */
  public abstract Map<String, AtlassianUserMapping> retrieveUserMappings();
  
  /**
   * retrieve memberships
   * @return the map of memberships
   */
  public abstract List<AtlassianCwdMembership> retrieveMemberships();
  
  /**
   * retrieve groups
   * @return the map of groups
   */
  public abstract Map<String, AtlassianCwdGroup> retrieveGroups();
  
  /**
   * new membership
   * @return new membership
   */
  public abstract AtlassianCwdMembership newMembership();
  
  /**
   * new group
   * @return new group
   */
  public abstract AtlassianCwdGroup newGroup();
  
  /**
   * new user mapping
   * @return new user mapping
   */
  public abstract AtlassianUserMapping newUserMapping();
  
  /**
   * new user
   * @return new user
   */
  public abstract AtlassianCwdUser newUser();
  
  /**
   * 
   * @param string
   * @param exceptionIfNotFound
   * @return the enum
   */
  public static AtlassianCwdVersion valueOfIgnoreCase(String string, boolean exceptionIfNotFound) {
    return GrouperClientUtils.enumValueOfIgnoreCase(AtlassianCwdVersion.class, string, exceptionIfNotFound);
  }

  /**
   * get the current configured version
   * @return the version
   */
  public static AtlassianCwdVersion currentVersion() {
    String versionFromConfig = GrouperClientConfig.retrieveConfig().propertyValueString("atlassian.cwd.version");
    return valueOfIgnoreCase(versionFromConfig, true);
  }
  
}
