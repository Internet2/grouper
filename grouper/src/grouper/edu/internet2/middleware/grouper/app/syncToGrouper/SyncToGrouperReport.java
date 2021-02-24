package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * sync to grouper report
 * @author mchyzer
 *
 */
public class SyncToGrouperReport {

  public SyncToGrouperReport() {
  }

  public SyncToGrouperReport(SyncToGrouper syncToGrouper) {
    super();
    this.syncToGrouper = syncToGrouper;
  }

  private SyncToGrouper syncToGrouper = null;

  
  public SyncToGrouper getSyncToGrouper() {
    return syncToGrouper;
  }

  
  public void setSyncToGrouper(SyncToGrouper syncToGrouper) {
    this.syncToGrouper = syncToGrouper;
  }

  /**
   * how many changes overall
   */
  private int changeCountOverall;

  /**
   * how many changes overall
   * @return change count overall
   */
  public int getDifferenceCountOverall() {
    return this.getStemInserts() + this.getStemUpdates() + this.getStemDeletes()
      + this.getGroupInserts() + this.getGroupUpdates() + this.getGroupDeletes()
      + this.getCompositeInserts() + this.getCompositeUpdates() + this.getCompositeDeletes()
      + this.getMembershipInserts() + this.getMembershipUpdates() + this.getMembershipDeletes()
      + this.getPrivilegeGroupInserts() + this.getPrivilegeGroupDeletes()
      + this.getPrivilegeStemInserts() + this.getPrivilegeStemDeletes()
      ;
  }

  /**
   * how many changes overall
   * @return change count overall
   */
  public int getChangeCountOverall() {
    return this.changeCountOverall;
  }

  /**
   * 
   * @return
   */
  public int getStemInserts() {
    return GrouperUtil.length(this.syncToGrouper.getSyncStemToGrouperLogic().getStemInserts());
  }

  /**
   * this is dynamically built for a report
   * @return the set of stem names which are inserts
   */
  public Set<String> getStemInsertsNames() {
    Set<String> stemInsertsNames = new TreeSet<String>();
    for (SyncStemToGrouperBean syncStemToGrouperBean : (GrouperUtil.nonNull(this.syncToGrouper.getSyncStemToGrouperLogic().getStemInserts()))) {
      stemInsertsNames.add(syncStemToGrouperBean.getName());
      if (stemInsertsNames.size() >= 50) {
        break;
      }

    }
    return stemInsertsNames;
  }

  /**
   * this is dynamically built for a report
   * @return the set of stem names which are updates
   */
  public Set<String> getStemUpdatesNames() {
    Set<String> stemUpdatesNames = new TreeSet<String>();
    for (SyncStemToGrouperBean syncStemToGrouperBean : (GrouperUtil.nonNull(this.syncToGrouper.getSyncStemToGrouperLogic().getStemUpdates()))) {
      stemUpdatesNames.add(syncStemToGrouperBean.getName());
      if (stemUpdatesNames.size() >= 50) {
        break;
      }

    }
    return stemUpdatesNames;
  }

  /**
   * output lines
   */
  private List<String> outputLines = new ArrayList<String>();

  /**
   * error lines
   */
  private List<String> errorLines = new ArrayList<String>();

  /**
   * error lines
   * @return
   */
  public List<String> getErrorLines() {
    return errorLines;
  }

  /**
   * output lines
   * @return output lines
   */
  public List<String> getOutputLines() {
    return this.outputLines;
  }

  /**
   * add output line
   * @param outputLines1
   */
  public void addOutputLine(String outputLine) {
    if (this.outputLines.size() < 20000) {
      this.outputLines.add(outputLine);
      if (this.outputLines.size() == 19999) {
        this.outputLines.add("Too many outputLines, not adding more!");
      }
    }
  }

  /**
   * add error line
   * @param errorLine
   */
  public void addErrorLine(String errorLine) {
    if (this.errorLines.size() < 20000) {
      this.errorLines.add(errorLine);
      if (this.errorLines.size() == 19999) {
        this.errorLines.add("Too many errorLines, not adding more!");
      }
    }
  }

  /**
   * add change count
   */
  public void addChangeOverall() {
    this.changeCountOverall++;
  }

  public int getStemUpdates() {
    return GrouperUtil.length(this.syncToGrouper.getSyncStemToGrouperLogic().getStemUpdates());
  }

  /**
   * 
   * @return
   */
  public int getStemDeletes() {
    return GrouperUtil.length(this.syncToGrouper.getSyncStemToGrouperLogic().getStemDeletes());
  }

  /**
   * this is dynamically built for a report
   * @return the set of stem names which are deletes
   */
  public Set<String> getStemDeletesNames() {
    Set<String> stemDeletesNames = new TreeSet<String>();
    for (Stem stem : (GrouperUtil.nonNull(this.syncToGrouper.getSyncStemToGrouperLogic().getStemDeletes()))) {
      stemDeletesNames.add(stem.getName());
      if (stemDeletesNames.size() >= 50) {
        break;
      }

    }
    return stemDeletesNames;
  }

  /**
   * 
   * @return
   */
  public int getGroupDeletes() {
    return GrouperUtil.length(this.syncToGrouper.getSyncGroupToGrouperLogic().getGroupDeletes());
  }

  /**
   * this is dynamically built for a report
   * @return the set of Group names which are deletes
   */
  public Set<String> getGroupDeletesNames() {
    Set<String> groupDeletesNames = new TreeSet<String>();
    for (Group stem : (GrouperUtil.nonNull(this.syncToGrouper.getSyncGroupToGrouperLogic().getGroupDeletes()))) {
      groupDeletesNames.add(stem.getName());
      if (groupDeletesNames.size() >= 50) {
        break;
      }

    }
    return groupDeletesNames;
  }

  /**
   * 
   * @return
   */
  public int getGroupInserts() {
    return GrouperUtil.length(this.syncToGrouper.getSyncGroupToGrouperLogic().getGroupInserts());
  }

  /**
   * this is dynamically built for a report
   * @return the set of stem names which are inserts
   */
  public Set<String> getGroupInsertsNames() {
    Set<String> stemInsertsNames = new TreeSet<String>();
    for (SyncGroupToGrouperBean syncGroupToGrouperBean : (GrouperUtil.nonNull(this.syncToGrouper.getSyncGroupToGrouperLogic().getGroupInserts()))) {
      stemInsertsNames.add(syncGroupToGrouperBean.getName());
      if (stemInsertsNames.size() >= 50) {
        break;
      }

    }
    return stemInsertsNames;
  }

  public int getGroupUpdates() {
    return GrouperUtil.length(this.syncToGrouper.getSyncGroupToGrouperLogic().getGroupUpdates());
  }

  /**
   * this is dynamically built for a report
   * @return the set of stem names which are updates
   */
  public Set<String> getGroupUpdatesNames() {
    Set<String> groupUpdatesNames = new TreeSet<String>();
    for (SyncGroupToGrouperBean syncGroupToGrouperBean : (GrouperUtil.nonNull(this.syncToGrouper.getSyncGroupToGrouperLogic().getGroupUpdates()))) {
      groupUpdatesNames.add(syncGroupToGrouperBean.getName());
      if (groupUpdatesNames.size() >= 50) {
        break;
      }

    }
    return groupUpdatesNames;
  }

  /**
   * 
   * @return
   */
  public int getCompositeDeletes() {
    return GrouperUtil.length(this.syncToGrouper.getSyncCompositeToGrouperLogic().getCompositeDeletes());
  }

  /**
   * this is dynamically built for a report
   * @return the set of Composite names which are deletes
   */
  public Set<String> getCompositeDeletesNames() {
    Set<String> compositeDeletesNames = new TreeSet<String>();
    for (SyncCompositeToGrouperBean composite : (GrouperUtil.nonNull(this.syncToGrouper.getSyncCompositeToGrouperLogic().getCompositeDeletes()))) {
      compositeDeletesNames.add(composite.getOwnerName());
      if (compositeDeletesNames.size() >= 50) {
        break;
      }

    }
    return compositeDeletesNames;
  }

  /**
   * 
   * @return
   */
  public int getCompositeInserts() {
    return GrouperUtil.length(this.syncToGrouper.getSyncCompositeToGrouperLogic().getCompositeInserts());
  }

  /**
   * this is dynamically built for a report
   * @return the set of composites names which are inserts
   */
  public Set<String> getCompositeInsertsNames() {
    Set<String> compositeInsertsNames = new TreeSet<String>();
    for (SyncCompositeToGrouperBean composite : (GrouperUtil.nonNull(this.syncToGrouper.getSyncCompositeToGrouperLogic().getCompositeInserts()))) {
      compositeInsertsNames.add(composite.getOwnerName());
      if (compositeInsertsNames.size() >= 50) {
        break;
      }

    }
    return compositeInsertsNames;
  }

  public int getCompositeUpdates() {
    return GrouperUtil.length(this.syncToGrouper.getSyncCompositeToGrouperLogic().getCompositeUpdates());
  }

  /**
   * this is dynamically built for a report
   * @return the set of composite names which are updates
   */
  public Set<String> getCompositeUpdatesNames() {
    Set<String> compositeUpdatesNames = new TreeSet<String>();
    for (SyncCompositeToGrouperBean composite : (GrouperUtil.nonNull(this.syncToGrouper.getSyncCompositeToGrouperLogic().getCompositeUpdates()))) {
      compositeUpdatesNames.add(composite.getOwnerName());
      if (compositeUpdatesNames.size() >= 50) {
        break;
      }

    }
    return compositeUpdatesNames;
  }

  /**
   * 
   * @return
   */
  public int getMembershipDeletes() {
    return GrouperUtil.length(this.syncToGrouper.getSyncMembershipToGrouperLogic().getMembershipDeletes());
  }

  /**
   * this is dynamically built for a report, of size ten
   * @return the set of Membership names which are deletes
   */
  public Set<String> getMembershipDeleteNames() {
    Set<String> membershipDeletesNames = new TreeSet<String>();
    for (SyncMembershipToGrouperBean membership : (GrouperUtil.nonNull(this.syncToGrouper.getSyncMembershipToGrouperLogic().getMembershipDeletes()))) {
      membershipDeletesNames.add(membership.getGroupName() + " - " + (StringUtils.equals("g:gsa", membership.getSubjectSourceId()) ? membership.getSubjectIdentifier() : membership.getSubjectId()));
      if (membershipDeletesNames.size() >= 50) {
        break;
      }
    }
    return membershipDeletesNames;
  }

  /**
   * 
   * @return
   */
  public int getMembershipInserts() {
    return GrouperUtil.length(this.syncToGrouper.getSyncMembershipToGrouperLogic().getMembershipInserts());
  }

  /**
   * this is dynamically built for a report
   * @return the set of composites names which are inserts
   */
  public Set<String> getMembershipInsertsNames() {
    Set<String> membershipInsertNames = new TreeSet<String>();
    for (SyncMembershipToGrouperBean membership : (GrouperUtil.nonNull(this.syncToGrouper.getSyncMembershipToGrouperLogic().getMembershipInserts()))) {
      membershipInsertNames.add(membership.getGroupName() + " - " + (StringUtils.equals("g:gsa", membership.getSubjectSourceId()) ? membership.getSubjectIdentifier() : membership.getSubjectId()));
      if (membershipInsertNames.size() >= 50) {
        break;
      }
    }
    return membershipInsertNames;
  }

  public int getMembershipUpdates() {
    return GrouperUtil.length(this.syncToGrouper.getSyncMembershipToGrouperLogic().getMembershipUpdates());
  }

  /**
   * this is dynamically built for a report
   * @return the set of composite names which are updates
   */
  public Set<String> getMembershipUpdatesNames() {
    Set<String> membershipUpdatesNames = new TreeSet<String>();
    for (SyncMembershipToGrouperBean membership : (GrouperUtil.nonNull(this.syncToGrouper.getSyncMembershipToGrouperLogic().getMembershipUpdates()))) {
      membershipUpdatesNames.add(membership.getGroupName() + " - " + (StringUtils.equals("g:gsa", membership.getSubjectSourceId()) ? membership.getSubjectIdentifier() : membership.getSubjectId()));
      if (membershipUpdatesNames.size() >= 50) {
        break;
      }
    }
    return membershipUpdatesNames;
  }

  /**
   * this is dynamically built for a report, of size ten
   * @return the set of Membership names which are deletes
   */
  public Set<String> getPrivilegeGroupDeleteNames() {
    Set<String> privilegeGroupDeletesNames = new TreeSet<String>();
    for (SyncPrivilegeGroupToGrouperBean privilegeGroup : (GrouperUtil.nonNull(this.syncToGrouper.getSyncPrivilegeGroupToGrouperLogic().getPrivilegeGroupDeletes()))) {
      privilegeGroupDeletesNames.add(privilegeGroup.getGroupName() + " - " + (StringUtils.equals("g:gsa", privilegeGroup.getSubjectSourceId()) ? privilegeGroup.getSubjectIdentifier() : privilegeGroup.getSubjectId()) + " - " + privilegeGroup.getFieldName());
      if (privilegeGroupDeletesNames.size() >= 50) {
        break;
      }
    }
    return privilegeGroupDeletesNames;
  }

  /**
   * 
   * @return
   */
  public int getPrivilegeGroupDeletes() {
    return GrouperUtil.length(this.syncToGrouper.getSyncPrivilegeGroupToGrouperLogic().getPrivilegeGroupDeletes());
  }

  /**
   * 
   * @return
   */
  public int getPrivilegeGroupInserts() {
    return GrouperUtil.length(this.syncToGrouper.getSyncPrivilegeGroupToGrouperLogic().getPrivilegeGroupInserts());
  }

  /**
   * this is dynamically built for a report
   * @return the set of composites names which are inserts
   */
  public Set<String> getPrivilegeGroupInsertsNames() {
    Set<String> privilegeGroupInsertNames = new TreeSet<String>();
    for (SyncPrivilegeGroupToGrouperBean privilegeGroup : (GrouperUtil.nonNull(this.syncToGrouper.getSyncPrivilegeGroupToGrouperLogic().getPrivilegeGroupInserts()))) {
      privilegeGroupInsertNames.add(privilegeGroup.getGroupName() + " - " + (StringUtils.equals("g:gsa", privilegeGroup.getSubjectSourceId()) ? privilegeGroup.getSubjectIdentifier() : privilegeGroup.getSubjectId()) + " - " + privilegeGroup.getFieldName());
      if (privilegeGroupInsertNames.size() >= 50) {
        break;
      }
    }
    return privilegeGroupInsertNames;
  }

  /**
   * this is dynamically built for a report, of size ten
   * @return the set of Membership names which are deletes
   */
  public Set<String> getPrivilegeStemDeleteNames() {
    Set<String> privilegeStemDeletesNames = new TreeSet<String>();
    for (SyncPrivilegeStemToGrouperBean privilegeStem : (GrouperUtil.nonNull(this.syncToGrouper.getSyncPrivilegeStemToGrouperLogic().getPrivilegeStemDeletes()))) {
      privilegeStemDeletesNames.add(privilegeStem.getStemName() + " - " + (StringUtils.equals("g:gsa", privilegeStem.getSubjectSourceId()) ? privilegeStem.getSubjectIdentifier() : privilegeStem.getSubjectId()) + " - " + privilegeStem.getFieldName());
      if (privilegeStemDeletesNames.size() >= 50) {
        break;
      }
    }
    return privilegeStemDeletesNames;
  }

  /**
   * 
   * @return
   */
  public int getPrivilegeStemDeletes() {
    return GrouperUtil.length(this.syncToGrouper.getSyncPrivilegeStemToGrouperLogic().getPrivilegeStemDeletes());
  }

  /**
   * 
   * @return
   */
  public int getPrivilegeStemInserts() {
    return GrouperUtil.length(this.syncToGrouper.getSyncPrivilegeStemToGrouperLogic().getPrivilegeStemInserts());
  }

  /**
   * this is dynamically built for a report
   * @return the set of composites names which are inserts
   */
  public Set<String> getPrivilegeStemInsertsNames() {
    Set<String> privilegeStemInsertNames = new TreeSet<String>();
    for (SyncPrivilegeStemToGrouperBean privilegeStem : (GrouperUtil.nonNull(this.syncToGrouper.getSyncPrivilegeStemToGrouperLogic().getPrivilegeStemInserts()))) {
      privilegeStemInsertNames.add(privilegeStem.getStemName() + " - " + (StringUtils.equals("g:gsa", privilegeStem.getSubjectSourceId()) ? privilegeStem.getSubjectIdentifier() : privilegeStem.getSubjectId()) + " - " + privilegeStem.getFieldName());
      if (privilegeStemInsertNames.size() >= 50) {
        break;
      }
    }
    return privilegeStemInsertNames;
  }

}
