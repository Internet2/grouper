package edu.internet2.middleware.grouper.pspng;

import java.util.*;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.pit.PITGroup;

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
  private final Group group;
  private final PITGroup pitGroup;

  public final String name;
  Map<String, Object> groupAttributes;
  Map<String, Object> stemAttributes;

  public GrouperGroupInfo(Group g) {
    if ( g != null )
      name = g.getName();
    else
      name = "null group";
    
    this.group = g;
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

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    if (pitGroup != null)
      return String.format("%s(PIT)", getName());
    else
      return getName();
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
      
      if ( group.getIdIndex() != null )
        result.put("idIndex", group.getIdIndex());
      
      Map<String, Object> stemAttributes = PspUtils.getStemAttributes(group);
      result.put("stemAttributes", stemAttributes);

      Map<String, Object> groupAttributes = PspUtils.getGroupAttributes(group);
      result.put("groupAttributes", groupAttributes);
    }
    else if ( pitGroup != null ) {
      result.put("pitGroup", pitGroup);
      result.put("name", pitGroup.getName());
      
      // TODO: populate idIndex, but pitGroup does not have getIdIndex()
      //result.put("idIndex", pitGroup.getIdIndex());
      
      Map<String, Object> groupAttributes = PspUtils.getGroupAttributes(pitGroup);
      if ( groupAttributes != null )
        result.put("groupAttributes", groupAttributes);
      else
        result.put("groupAttributes", Collections.EMPTY_MAP);
      
      // Old stem attributes probably don't matter since group has been deleted
      result.put("stemAttributes", Collections.EMPTY_MAP);
      
    }
    return result;
  }

  public boolean hasGroupBeenDeleted() {
    return pitGroup != null;
  }

  public Group getGrouperGroup() {
    return group;
  }

}
