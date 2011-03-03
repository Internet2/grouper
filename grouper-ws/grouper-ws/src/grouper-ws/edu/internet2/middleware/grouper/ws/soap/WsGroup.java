/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Result of one group being retrieved since a user is a member of it.  The number of
 * groups will equal the number of groups the user is a member of (provided the filter matches)
 * 
 * @author mchyzer
 */
public class WsGroup implements Comparable<WsGroup> {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /** extension of group, the part to the right of last colon in name */
  private String extension;

  /** display extension, the part to the right of the last colon in display name */
  private String displayExtension;

  /**
   * convert a set of groups to results
   * @param groupSet
   * @param includeDetail true if detail of group should be sent
   * @return the groups (null if none or null)
   */
  public static WsGroup[] convertGroups(Set<Group> groupSet, boolean includeDetail) {
    if (groupSet == null || groupSet.size() == 0) {
      return null;
    }
    int groupSetSize = groupSet.size();
    WsGroup[] wsGroupResults = new WsGroup[groupSetSize];
    int index = 0;
    for (Group group : groupSet) {
      WsGroup wsGroup = new WsGroup(group, null, includeDetail);
      wsGroupResults[index] = wsGroup;
      index++;
    }
    return wsGroupResults;

  }
  
  /**
   * convert a set of pit groups to results
   * @param pitGroupSet
   * @return the groups (null if none or null)
   */
  public static WsGroup[] convertGroups(Set<PITGroup> pitGroupSet) {
    if (pitGroupSet == null || pitGroupSet.size() == 0) {
      return null;
    }
    int groupSetSize = pitGroupSet.size();
    WsGroup[] wsGroupResults = new WsGroup[groupSetSize];
    int index = 0;
    for (PITGroup pitGroup : pitGroupSet) {
      WsGroup wsGroup = new WsGroup(pitGroup);
      wsGroupResults[index] = wsGroup;
      index++;
    }
    return wsGroupResults;

  }

  /**
   * friendly description of this group
   */
  private String description;

  /**
   * friendly extensions of group and parent stems
   */
  private String displayName;

  /**
   * Full name of the group (all extensions of parent stems, separated by colons,  and the extention of this group
   */
  private String name;

  /**
   * universally unique identifier of this group
   */
  private String uuid;

  /**
   * if requested, return the detail properties of the group
   */
  private WsGroupDetail detail;

  /**
   * no arg constructor
   */
  public WsGroup() {
    //blank

  }

  /**
   * construct based on group, assign all fields
   * @param group 
   * @param wsGroupLookup is the lookup to set looked up values
   * @param includeDetail true to include detail about group
   */
  public WsGroup(Group group, WsGroupLookup wsGroupLookup, boolean includeDetail) {
    if (group != null) {
      this.setDescription(StringUtils.trimToNull(group.getDescription()));
      this.setDisplayName(group.getDisplayName());
      this.setName(group.getName());
      this.setUuid(group.getUuid());
      this.setExtension(group.getExtension());
      this.setDisplayExtension(group.getDisplayExtension());

      //see if detail info is needed
      if (includeDetail) {
        this.setDetail(new WsGroupDetail(group));
      }
    } else {
      if (wsGroupLookup != null) {
        //no group, set the look values so the caller can keep things in sync
        this.setName(wsGroupLookup.getGroupName());
        this.setUuid(wsGroupLookup.getUuid());
        this.setExtension(GrouperUtil.extensionFromName(wsGroupLookup.getGroupName()));

      }
    }
  }
  
  /**
   * construct based on pit group
   * @param pitGroup
   */
  public WsGroup(PITGroup pitGroup) {
    this.setName(pitGroup.getName());
    this.setUuid(pitGroup.getId());
    this.setExtension(GrouperUtil.extensionFromName(pitGroup.getName()));
  }

  /**
   * friendly description of this group
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * friendly extensions of group and parent stems
   * @return the displayName
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * Full name of the group (all extensions of parent stems, separated by colons, 
   * and the extention of this group
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * universally unique identifier of this group
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * friendly description of this group
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * friendly extensions of group and parent stems
   * @param displayName1 the displayName to set
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * Full name of the group (all extensions of parent stems, separated by colons, 
   * and the extention of this group
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * universally unique identifier of this group
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * if requested, these are the detail results of the group
   * @return the detail
   */
  public WsGroupDetail getDetail() {
    return this.detail;
  }

  /**
   * if requested, these are the detail results of the group
   * @param detail1 the detail to set
   */
  public void setDetail(WsGroupDetail detail1) {
    this.detail = detail1;
  }

  /**
   * extension of group, the part to the right of last colon in name
   * @return the extension
   */
  public String getExtension() {
    return this.extension;
  }

  /**
   * extension of group, the part to the right of last colon in name
   * @param extension1 the extension to set
   */
  public void setExtension(String extension1) {
    this.extension = extension1;
  }

  /**
   * display extension, the part to the right of the last colon in display name
   * @return the displayExtension
   */
  public String getDisplayExtension() {
    return this.displayExtension;
  }

  /**
   * display extension, the part to the right of the last colon in display name
   * @param displayExtension1 the displayExtension to set
   */
  public void setDisplayExtension(String displayExtension1) {
    this.displayExtension = displayExtension1;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(WsGroup o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (this == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }
    return GrouperUtil.compare(this.getName(), o2.getName());
  }
}
