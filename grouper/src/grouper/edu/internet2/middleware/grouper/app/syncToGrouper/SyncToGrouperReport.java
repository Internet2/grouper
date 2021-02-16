package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
      + this.getGroupInserts() + this.getGroupUpdates() + this.getGroupDeletes();
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
    this.outputLines.add(outputLine);
  }

  /**
   * add error line
   * @param errorLine
   */
  public void addErrorLine(String errorLine) {
    this.errorLines.add(errorLine);
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
    Set<String> stemDeletesNames = new TreeSet<String>();
    for (Group stem : (GrouperUtil.nonNull(this.syncToGrouper.getSyncGroupToGrouperLogic().getGroupDeletes()))) {
      stemDeletesNames.add(stem.getName());
    }
    return stemDeletesNames;
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
    }
    return groupUpdatesNames;
  }

}
