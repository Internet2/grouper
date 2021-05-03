package edu.internet2.middleware.grouper.pspng;

import java.util.*;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple class to hold name and attribute information of a group.
 * This is useful because it provides common data whether the group is an
 * existing group or has been deleted and the information is coming from a
 * PITGroup
 * 
 * @author bert
 *
 */
public class GrouperGroupInfo {
  private static final Logger LOG = LoggerFactory.getLogger(GrouperGroupInfo.class);

  private final Group group;
  private final PITGroup pitGroup;

  public final String name;
  public Long idIndex;

  Map<String, Object> groupAttributes;
  Map<String, Object> stemAttributes;

  public GrouperGroupInfo(Group g) {
    if ( g != null )
      name = g.getName();
    else
      name = "null group";
    
    this.group = g;
    this.idIndex = g.getIdIndex();
    this.pitGroup = null;
  }

  public GrouperGroupInfo(PITGroup g) {
    if ( g != null )
      name = g.getName();
    else
      name = "null group";
    
    this.pitGroup = g;
    this.group = null;
  }

  // Create a barebones group info when neither Group nor PITGroup can be found
  // This is used when groups are deleted, but PIT information is not available.
  public GrouperGroupInfo(String name, Long idIndex) {
    this.pitGroup = null;
    this.group = null;

    this.name = name;
    this.idIndex = idIndex;
  }


  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    if (pitGroup != null)
      return String.format("%s/#%d(PIT)", getName(), idIndex);
    else if ( group != null )
      return String.format("%s/#%d(Existing)", getName(), idIndex);
    else
      return String.format("%s/#%d(Raw)", getName(), idIndex);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GrouperGroupInfo other = (GrouperGroupInfo) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    return true;
  }

  public Set<Member> getMembers() {
    if ( group != null ) {
      return group.getMembers();
    }
    else {
      return new HashSet<>();
    }
  }

  public Map<String, Object> getJexlMap() {
    Map<String, Object> result = new HashMap<String, Object>();

    if ( group != null ) {
      result.put("group", group);
      result.put("name", group.getName());
      result.put("displayName", group.getDisplayName());

      result.put("extension", group.getExtension());
      result.put("displayExtension", group.getDisplayExtension());

      result.put("description", group.getDescription());

      if ( group.getIdIndex() != null )
        result.put("idIndex", idIndex);

      result.put("groupId", group.getId());
      
    }
    else if ( pitGroup != null ) {
      result.put("pitGroup", pitGroup);
      result.put("name", pitGroup.getName());

      result.put("idIndex", idIndex);
      result.put("groupId", pitGroup.getSourceId());

      result.put("extension", GrouperUtil.extensionFromName(pitGroup.getName()));

      // Make display properties the same... since the group has been deleted
      result.put("displayName", result.get("name"));
      result.put("displayExtension", result.get("extension"));


      Map<String, Object> groupAttributes = PspUtils.getGroupAttributes(pitGroup);
      if ( groupAttributes != null )
        result.put("groupAttributes", groupAttributes);
      else
        result.put("groupAttributes", Collections.EMPTY_MAP);
      
      // Old stem attributes probably don't matter since group has been deleted
      result.put("stemAttributes", Collections.EMPTY_MAP);
      
    }
    else if ( name != null ) {
      // This is used when groups are deleted, but PIT information is not available.

      result.put("name", name);
      result.put("extension",GrouperUtil.extensionFromName(name) );

      // Make display properties the same... since that is all we have
      result.put("displayName", result.get("name"));
      result.put("displayExtension", result.get("extension"));


      // Maintain compatibility with expressions with some bogus values
      result.put("idIndex", -999999);
      result.put("groupId", "-9a9a9a9a9a9a");
      result.put("group", null);
      result.put("pitGroup", null);

      result.put("stemAttributes", Collections.EMPTY_MAP);
      result.put("groupAttributes", Collections.EMPTY_MAP);
    }
    return result;
  }

  public boolean hasGroupBeenDeleted() {
    return pitGroup != null;
  }

  public Group getGrouperGroup() {
    return group;
  }

  /**
   * This method rereads the Grouper objects from the database in order to
   * avoid L2 caching when database objects change.
   */
  public void hibernateRefresh() {
    final Object objectToHibernateRefresh;

    if ( group != null ) {
      objectToHibernateRefresh = group;
    } else {
      objectToHibernateRefresh = pitGroup;
    }

    if ( objectToHibernateRefresh != null ) {
      PspUtils.hibernateRefresh(objectToHibernateRefresh);
    }
  }

}
