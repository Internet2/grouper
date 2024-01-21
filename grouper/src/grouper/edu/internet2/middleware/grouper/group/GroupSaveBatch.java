package edu.internet2.middleware.grouper.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GroupSaveBatch {

  
  private boolean makeChangesIfExist = true;

  public GroupSaveBatch assignMakeChangesIfExist(boolean theMakeChangesIfExist) {
    this.makeChangesIfExist = theMakeChangesIfExist;
    return this;
  }
  
  private List<GroupSave> groupSaves = new ArrayList<GroupSave>();
  
  public GroupSaveBatch addGroupSaves(Collection<GroupSave> theGroupSaves) {
    if (theGroupSaves != null) {
      this.groupSaves.addAll(theGroupSaves);
    }
    return this;
  }

  public Map<String, Group> save() {
    
    Set<String> groupNames = new HashSet<String>();

    for (GroupSave groupSave : groupSaves) {
      if (!StringUtils.isBlank(groupSave.getName())) {
        groupNames.add(groupSave.getName());
      }
      if (!StringUtils.isBlank(groupSave.getGroupNameToEdit())) {
        groupNames.add(groupSave.getGroupNameToEdit());
      }
    }
    
    Set<Group> groups = new GroupFinder().assignGroupNames(groupNames).findGroups();
    
    Map<String, Group> groupNameToGroup = new HashMap<String, Group>();
    
    for (Group group : GrouperUtil.nonNull(groups)) {
      groupNameToGroup.put(group.getName(), group);
    }
    
    for (GroupSave groupSave : groupSaves) {
      if (!StringUtils.isBlank(groupSave.getName())) {
        Group group = groupNameToGroup.get(groupSave.getName());
        if (group != null) {
          if (this.makeChangesIfExist) {
            group = groupSave.save();
          }
        } else {
          group = groupSave.save();
        }
        if (group != null) {
          groupNameToGroup.put(group.getName(), group);
        }
      }
    }
    return groupNameToGroup;
  }
}
