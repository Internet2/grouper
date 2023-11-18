package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.provisioning.GrouperSyncLogWithOwner;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.subject.Subject;

public class GuiProvisionerLog {
  
  /**
   * gcGrouperSyncLog this instance is wrapping
   */
  private GrouperSyncLogWithOwner gcGrouperSyncLogWithOwner;
  
  public String getDescription() {
    return gcGrouperSyncLogWithOwner.getGcGrouperSyncLog().getDescriptionOrDescriptionClob();
  }

  public GrouperSyncLogWithOwner getGcGrouperSyncLogWithOwner() {
    return gcGrouperSyncLogWithOwner;
  }
  
  public void setGcGrouperSyncLogWithOwner(GrouperSyncLogWithOwner gcGrouperSyncLogWithOwner) {
    this.gcGrouperSyncLogWithOwner = gcGrouperSyncLogWithOwner;
  }
  
  public static List<GuiProvisionerLog> convertFromGcGrouperSyncWithOwner(List<GrouperSyncLogWithOwner> grouperSyncLogsWithOwner) {
    
    List<GuiProvisionerLog> result = new ArrayList<GuiProvisionerLog>();
    
    for (GrouperSyncLogWithOwner grouperSyncLogWithOwner: grouperSyncLogsWithOwner) {
      
      GuiProvisionerLog guiProvisionerLog = new GuiProvisionerLog();
      guiProvisionerLog.setGcGrouperSyncLogWithOwner(grouperSyncLogWithOwner);
      result.add(guiProvisionerLog);
    }
    
    return result;
  }
  
  public String getOwner() {
    
    if (gcGrouperSyncLogWithOwner.getGcGrouperSyncGroup() != null) {
      String groupId = gcGrouperSyncLogWithOwner.getGcGrouperSyncGroup().getGroupId();
      if (groupId == null) {
        return null;
      }
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, false);
      if (group == null) {
        return null;
      }
      return new GuiGroup(group).getShortLinkWithIcon();
    }
    
    if (gcGrouperSyncLogWithOwner.getGcGrouperSyncJob() != null) {
      String syncType = gcGrouperSyncLogWithOwner.getGcGrouperSyncJob().getSyncType();
      return syncType;
    }
    
    if (gcGrouperSyncLogWithOwner.getGcGrouperSyncMember() != null) {
      String subjectId = gcGrouperSyncLogWithOwner.getGcGrouperSyncMember().getSubjectId();
      if (subjectId == null) {
        return null;
      }
      Subject subject = SubjectFinder.findById(subjectId, false);
      if (subject == null) {
        return null;
      }
      return new GuiSubject(subject).getShortLinkWithIcon();
    }
    
    return null;
  }
  
}
