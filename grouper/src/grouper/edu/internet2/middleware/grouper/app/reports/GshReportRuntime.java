package edu.internet2.middleware.grouper.app.reports;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.subject.Subject;

public class GshReportRuntime {

  /**
   * grouper report data
   */
  private GrouperReportData grouperReportData;

  /**
   * grouper report data
   * @return data
   */
  public GrouperReportData getGrouperReportData() {
    return grouperReportData;
  }

  /**
   * grouper report data
   * @param grouperReportData1
   */
  public void setGrouperReportData(GrouperReportData grouperReportData1) {
    this.grouperReportData = grouperReportData1;
  }

  private Subject currentSubject;
  
  private static ThreadLocal<GshReportRuntime> threadLocalGshReportRuntime = new InheritableThreadLocal<GshReportRuntime>();

  
  public Subject getCurrentSubject() {
    return currentSubject;
  }

  
  public void setCurrentSubject(Subject currentSubject) {
    this.currentSubject = currentSubject;
  }

  
  public static GshReportRuntime retrieveGshReportRuntime() {
    return threadLocalGshReportRuntime.get();
  }
  
  
  public static void assignThreadLocalGshReportRuntime(GshReportRuntime gshReportRuntime) {
    threadLocalGshReportRuntime.set(gshReportRuntime);
  }
  
  public static void removeThreadLocalGshReportRuntime() {
    threadLocalGshReportRuntime.remove();
  }

  /**
   * owner stem name where template was called
   */
  private String ownerStemName;

  /**
   * owner stem name where template was called
   * @param ownerStemName
   */
  public void setOwnerStemName(String ownerStemName) {
    this.ownerStemName = ownerStemName;
  }

  /**
   * owner group where template was called
   */
  private Group ownerGroup;
  
  /**
   * owner stem where template was called
   */
  private Stem ownerStem;
  
  /**
   * owner group where template was called
   * @return owner group
   */
  public Group getOwnerGroup() {
    return ownerGroup;
  }

  /**
   * owner group where template was called
   * @param ownerGroup
   */
  public void setOwnerGroup(Group ownerGroup) {
    this.ownerGroup = ownerGroup;
  }

  /**
   * owner stem where template was called
   * @return
   */
  public Stem getOwnerStem() {
    return ownerStem;
  }

  /**
   * owner stem where template was called
   * @param ownerStem
   */
  public void setOwnerStem(Stem ownerStem) {
    this.ownerStem = ownerStem;
  }

  /**
   * owner group name where template was called
   */
  private String ownerGroupName;
  
  /**
   * owner group name where template was called
   * @param ownerGroupName
   */
  public void setOwnerGroupName(String ownerGroupName) {
    this.ownerGroupName = ownerGroupName;
  }

  /**
   * owner stem name where template was called
   * @return
   */
  public String getOwnerStemName() {
    return ownerStemName;
  }

  /**
   * owner group name where template was called
   * @return
   */
  public String getOwnerGroupName() {
    return ownerGroupName;
  }
  
}
