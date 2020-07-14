package edu.internet2.middleware.grouper.app.serviceLifecycle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperRecentMemberships {

  public static void upgradeFromV2_5_29_to_V2_5_30() {
    AttributeDefName grouperRecentMembershipsMarker = AttributeDefNameFinder.findByName(
        GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
    
    AttributeDefName grouperRecentMembershipsDays = AttributeDefNameFinder.findByName(
        GrouperRecentMemberships.recentMembershipsStemName() + ":grouperRecentMembershipsDays", false);

    AttributeDefName grouperRecentMembershipsGroupName = AttributeDefNameFinder.findByName(
        GrouperRecentMemberships.recentMembershipsStemName() + ":grouperRecentMembershipsGroupName", false);

    AttributeDefName grouperRecentMembershipsGroupUuidFrom = AttributeDefNameFinder.findByName(
        GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, true);

    AttributeDefName grouperRecentMembershipsMicros = AttributeDefNameFinder.findByName(
        GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS, true);

    AttributeDefName grouperRecentMembershipsIncludeCurrent = AttributeDefNameFinder.findByName(
        GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT, true);

    if (grouperRecentMembershipsDays != null || grouperRecentMembershipsGroupName != null) {
      
      if (grouperRecentMembershipsDays != null && grouperRecentMembershipsGroupName != null) {
      
        List<MultiKey> oldConfigs = new ArrayList<MultiKey>();
        
        for (AttributeAssign attributeAssign : GrouperUtil.nonNull(new AttributeAssignFinder()
            .assignAttributeAssignType(AttributeAssignType.group)
            .addAttributeDefNameId(grouperRecentMembershipsMarker.getId()).findAttributeAssignFinderResults().getIdToAttributeAssignMap().values())) {
          
          String groupUuidFrom = attributeAssign.getOwnerGroupId();
          
          if (StringUtils.isBlank(groupUuidFrom)) {
            continue;
          }
          
          String groupNameTo = attributeAssign.getAttributeValueDelegate().retrieveValueString(grouperRecentMembershipsGroupName.getName());

          if (StringUtils.isBlank(groupNameTo)) {
            continue;
          }

          Group groupTo = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupNameTo, false);
          
          if (groupTo == null) {
            continue;
          }
          
          String daysString = attributeAssign.getAttributeValueDelegate().retrieveValueString(grouperRecentMembershipsDays.getName());
          
          if (StringUtils.isBlank(daysString)) {
            continue;
          }
          int days = -1;
          try {
            days = GrouperUtil.intValue(daysString);
          } catch (Exception e) {
            continue;
          }

          if (days < 0) {
            continue;
          }

          long micros = days * 24L * 60 * 60 * 1000000L;
          
          String includeCurrentString = attributeAssign.getAttributeValueDelegate().retrieveValueString(grouperRecentMembershipsIncludeCurrent.getName());

          boolean includeCurrent = false;

          try {
            includeCurrent = GrouperUtil.booleanValue(includeCurrentString);
          } catch (Exception e) {
            continue;
          }

          MultiKey multiKey = new MultiKey(groupTo, groupUuidFrom, micros, includeCurrent ? "T" : "F");
          
          oldConfigs.add(multiKey);
          LOG.error("Note: migrating v2.5.29 recent membership groupTo: " + groupTo.getName() + ", groupUuidFrom: " 
              + groupUuidFrom + ", days: " + days + ", includeCurrent: " + includeCurrent);
          
          attributeAssign.delete();
        }
        
        // remove all first since it might conflict with what is being assigned
        
        for (MultiKey multiKey : oldConfigs) {
          
          Group groupTo = null;
          String groupUuidFrom = null;
          long micros = -1;
          String includeCurrent = null;
          
          try {
            groupTo = (Group)multiKey.getKey(0);
            groupUuidFrom = (String)multiKey.getKey(1);
            micros = (Long)multiKey.getKey(2);
            includeCurrent = (String)multiKey.getKey(3);

            AttributeAssign attributeAssign = groupTo.getAttributeDelegate().assignAttribute(grouperRecentMembershipsMarker).getAttributeAssign();

            attributeAssign.getAttributeValueDelegate().assignValueInteger(grouperRecentMembershipsMicros.getName(), micros);
            attributeAssign.getAttributeValueDelegate().assignValue(grouperRecentMembershipsGroupUuidFrom.getName(), groupUuidFrom);
            attributeAssign.getAttributeValueDelegate().assignValue(grouperRecentMembershipsIncludeCurrent.getName(), includeCurrent);
          } catch (Exception e) {
            LOG.error("Error migrating membership groupTo: " + groupTo.getName() + ", groupUuidFrom: " 
              + groupUuidFrom + ", micros: " + micros + ", includeCurrent: " + includeCurrent, e);
          }
        }
          
      }
      if (grouperRecentMembershipsDays != null) {
        grouperRecentMembershipsDays.delete();
      }
      if (grouperRecentMembershipsGroupName != null) {
        grouperRecentMembershipsGroupName.delete();
      }

    }

  }
  
  /**
   * 
   * @return the stem name
   */
  public static String recentMembershipsStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:recentMemberships";
  }

  public static final String GROUPER_RECENT_MEMBERSHIPS_MARKER_DEF = "grouperRecentMembershipsMarkerDef";
  
  public static final String GROUPER_RECENT_MEMBERSHIPS_MARKER = "grouperRecentMembershipsMarker";

  public static final String GROUPER_RECENT_MEMBERSHIPS_VALUE_DEF = "grouperRecentMembershipsValueDef";

  public static final String GROUPER_RECENT_MEMBERSHIPS_INT_VALUE_DEF = "grouperRecentMembershipsIntValueDef";

  public static final String GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS = "grouperRecentMembershipsMicros";
  
  public static final String GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM = "grouperRecentMembershipsGroupUuidFrom";

  public static final String GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT = "grouperRecentMembershipsIncludeCurrent";

  public static final String GROUPER_RECENT_MEMBERSHIPS_LOADER_GROUP_NAME = "grouperRecentMembershipsLoader";

// old attributes
//  public static final String GROUPER_RECENT_MEMBERSHIPS_ATTR_DAYS = "grouperRecentMembershipsDays";
//  
//  public static final String GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_NAME = "grouperRecentMembershipsGroupName";

  static String groupQuery = "select group_uuid_to group_id, group_name_to group_name from grouper_recent_mships_conf_v";
  
  static String query = "select group_name, subject_id, subject_source_id from grouper_recent_mships_load_v";
  
  public static void setupRecentMembershipsLoaderJob(Group group) {
    
    boolean recentMembershipsEnabled = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.recentMemberships.loaderJob.enable", true);

    GroupType grouperLoaderType = GroupTypeFinder.find("grouperLoader", true);
    boolean hasChange = false;
    if (recentMembershipsEnabled) {
      
      if (!group.hasType(grouperLoaderType)) {
        group.addType(grouperLoaderType);
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_TYPE), "SQL_GROUP_LIST")) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_TYPE, "SQL_GROUP_LIST");
        hasChange = true;
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_DB_NAME), "grouper")) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_DB_NAME, "grouper");
        hasChange = true;
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE), "CRON")) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, "CRON");
        hasChange = true;
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON), "0 41 3 * * ?")) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, "0 41 3 * * ?");
        hasChange = true;
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_QUERY), query)) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, query);
        hasChange = true;
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_GROUP_QUERY), groupQuery)) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_QUERY, groupQuery);
        hasChange = true;
      }
      
    } else {
      
      if (group.hasType(grouperLoaderType)) {
        group.deleteType(grouperLoaderType);
        hasChange = true;
      }
    }
    
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperRecentMemberships.class);
  
}
