/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;


/**
 * data about stem delete screen
 */
public class StemDeleteContainer {

  /**
   * 
   */
  public StemDeleteContainer() {
  }

  /**
   * is grouper admin
   * @return if grouper admin
   */
  public boolean isGrouperAdmin() {
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    return PrivilegeHelper.isWheelOrRoot(loggedInSubject);
  }

  /**
   * if can obliterate
   */
  private boolean canObliterate;
  
  /**
   * if can obliterate
   * @return the canObliterate
   */
  public boolean isCanObliterate() {
    return this.canObliterate;
  }
  
  /**
   * if can obliterate
   * @param canObliterate1 the canObliterate to set
   */
  public void setCanObliterate(boolean canObliterate1) {
    this.canObliterate = canObliterate1;
  }

  /**
   * if should obliterate empty stems
   */
  private boolean obliterateEmptyStems;
  
  /**
   * if should obliterate empty stems
   * @return the obliterateEmptyStems
   */
  public boolean isObliterateEmptyStems() {
    return this.obliterateEmptyStems;
  }
  
  /**
   * if should obliterate empty stems
   * @param obliterateEmptyStems1 the obliterateEmptyStems to set
   */
  public void setObliterateEmptyStems(boolean obliterateEmptyStems1) {
    this.obliterateEmptyStems = obliterateEmptyStems1;
  }

  /**
   * if should obliterate group memberships
   */
  private boolean obliterateGroupMemberships;
  
  /**
   * if should obliterate group memberships
   * @return the obliterateGroupMemberships
   */
  public boolean isObliterateGroupMemberships() {
    return this.obliterateGroupMemberships;
  }
  
  /**
   * if should obliterate group memberships
   * @param obliterateGroupMemberships1 the obliterateGroupMemberships to set
   */
  public void setObliterateGroupMemberships(boolean obliterateGroupMemberships1) {
    this.obliterateGroupMemberships = obliterateGroupMemberships1;
  }

  /**
   * if should obliterate groups
   */
  private boolean obliterateGroups;
  
  
  /**
   * if should obliterate groups
   * @return the obliterateGroups
   */
  public boolean isObliterateGroups() {
    return this.obliterateGroups;
  }

  
  /**
   * if should obliterate groups
   * @param obliterateGroups1 the obliterateGroups to set
   */
  public void setObliterateGroups(boolean obliterateGroups1) {
    this.obliterateGroups = obliterateGroups1;
  }

  /**
   * obliterate only one level 
   */
  private boolean obliterateStemScopeOne;
  
  /**
   * @return the obliterateStemScopeOne
   */
  public boolean isObliterateStemScopeOne() {
    return this.obliterateStemScopeOne;
  }
  
  /**
   * @param obliterateStemScopeOne1 the obliterateStemScopeOne to set
   */
  public void setObliterateStemScopeOne(boolean obliterateStemScopeOne1) {
    this.obliterateStemScopeOne = obliterateStemScopeOne1;
  }

  /**
   * attributeDefName count
   */
  private int attributeDefNameCount;

  /**
   * @return the attributeDefNameCount
   */
  public int getAttributeDefNameCount() {
    return this.attributeDefNameCount;
  }
  
  /**
   * @param attributeDefNameCount1 the attributeDefNameCount to set
   */
  public void setAttributeDefNameCount(int attributeDefNameCount1) {
    this.attributeDefNameCount = attributeDefNameCount1;
  }

  /**
   * attributeDef count
   */
  private int attributeDefCount;
  
  /**
   * @return the attributeDefCount
   */
  public int getAttributeDefCount() {
    return this.attributeDefCount;
  }
  
  /**
   * @param attributeDefCount1 the attributeDefCount to set
   */
  public void setAttributeDefCount(int attributeDefCount1) {
    this.attributeDefCount = attributeDefCount1;
  }

  /**
   * group count
   */
  private int groupCount;

  
  /**
   * group count
   * @return the groupCount
   */
  public int getGroupCount() {
    return this.groupCount;
  }

  
  /**
   * group count
   * @param groupCount1 the groupCount to set
   */
  public void setGroupCount(int groupCount1) {
    this.groupCount = groupCount1;
  }

  /**
   * stem count
   */
  private int stemCount;
  
  /**
   * stem count
   * @return the stemCount
   */
  public int getStemCount() {
    return this.stemCount;
  }

  /**
   * stem count
   * @param stemCount1 the stemCount to set
   */
  public void setStemCount(int stemCount1) {
    this.stemCount = stemCount1;
  }

  /**
   * if this stem is empty
   */
  private boolean emptyStem;
  
  /**
   * if this stem is empty
   * @return the emptyStem
   */
  public boolean isEmptyStem() {
    return this.emptyStem;
  }

  /**
   * if this stem is empty
   * @param emptyStem1 the emptyStem to set
   */
  public void setEmptyStem(boolean emptyStem1) {
    this.emptyStem = emptyStem1;
  }

  /**
   * obliterateAll, obliterateSome, deleteStem
   */
  private String obliterateType;
  
  /**
   * obliterateAll, obliterateSome, deleteStem
   * @return the obliterateAll
   */
  public String getObliterateType() {
    return this.obliterateType;
  }
  
  /**
   * obliterateAll, obliterateSome, deleteStem
   * @param obliterateType1 the obliterateAll to set
   */
  public void setObliterateType(String obliterateType1) {
    this.obliterateType = obliterateType1;
  }
  
  /**
   * if obliterate point in time
   */
  private Boolean obliteratePointInTime;
  
  /**
   * if obliterate point in time
   * @return the obliteratePointInTime
   */
  public Boolean getObliteratePointInTime() {
    return this.obliteratePointInTime;
  }
  
  /**
   * if obliterate point in time
   * @param obliteratePointInTime1 the obliteratePointInTime to set
   */
  public void setObliteratePointInTime(Boolean obliteratePointInTime1) {
    this.obliteratePointInTime = obliteratePointInTime1;
  }
  
  /**
   * confirm the user wants to do that
   */
  private Boolean areYouSure;

  /**
   * confirm the user wants to do that
   * @return the areYouSure
   */
  public Boolean getAreYouSure() {
    return this.areYouSure;
  }
  
  /**
   * confirm the user wants to do that
   * @param areYouSure1 the areYouSure to set
   */
  public void setAreYouSure(Boolean areYouSure1) {
    this.areYouSure = areYouSure1;
  }
  
  /**
   * if should obliterate attributeDefs
   */
  private boolean obliterateAttributeDefs;
  
  /**
   * if should obliterate attributeDefs
   * @return the obliterateAttributeDefs
   */
  public boolean isObliterateAttributeDefs() {
    return this.obliterateAttributeDefs;
  }
  
  /**
   * if should obliterate attributeDefs
   * @param obliterateAttributeDefs1 the obliterateAttributeDefs to set
   */
  public void setObliterateAttributeDefs(boolean obliterateAttributeDefs1) {
    this.obliterateAttributeDefs = obliterateAttributeDefs1;
  }
  
  /**
   * if should obliterate attributeDefNames
   */
  private boolean obliterateAttributeDefNames;
  
  /**
   * attribute def count total (even ones arent allowed to delete)
   */
  private int attributeDefCountTotal;
  
  /**
   * attribute def name count total objects (even ones you arent allowed to delete)
   */
  private int attributeDefNameCountTotal;
  
  /**
   * group count total (even ones not allowed to delete)
   */
  private int groupCountTotal;
  
  /**
   * stem count total (even ones not allowed to delete)
   */
  private int stemCountTotal;

  /**
   * 
   * @return the obliterate count
   */
  public int getObliterateCount() {
    return this.attributeDefCountTotal + this.attributeDefNameCountTotal + this.stemCountTotal + this.groupCountTotal;
  }
  
  /**
   * if should obliterate attributeDefNames
   * @return the obliterateAttributeDefNames
   */
  public boolean isObliterateAttributeDefNames() {
    return this.obliterateAttributeDefNames;
  }

  
  /**
   * if should obliterate attributeDefNames
   * @param obliterateAttributeDefNames1 the obliterateAttributeDefNames to set
   */
  public void setObliterateAttributeDefNames(boolean obliterateAttributeDefNames1) {
    this.obliterateAttributeDefNames = obliterateAttributeDefNames1;
  }

  /**
   * attribute def count total (even ones arent allowed to delete)
   * @return the attributeDefCountTotal
   */
  public int getAttributeDefCountTotal() {
    return this.attributeDefCountTotal;
  }

  /**
   * attribute def name count total objects (even ones you arent allowed to delete)
   * @return the attributeDefNameCountTotal
   */
  public int getAttributeDefNameCountTotal() {
    return this.attributeDefNameCountTotal;
  }

  /**
   * group count total (even ones not allowed to delete)
   * @return the groupCountTotal
   */
  public int getGroupCountTotal() {
    return this.groupCountTotal;
  }

  /**
   * @return the stemCountTotal
   */
  public int getStemCountTotal() {
    return this.stemCountTotal;
  }

  /**
   * attribute def count total (even ones arent allowed to delete)
   * @param attributeDefCountTotal the attributeDefCountTotal to set
   */
  public void setAttributeDefCountTotal(int attributeDefCountTotal) {
    this.attributeDefCountTotal = attributeDefCountTotal;
  }

  /**
   * attribute def name count total objects (even ones you arent allowed to delete)
   * @param attributeDefNameCountTotal1 the attributeDefNameCountTotal to set
   */
  public void setAttributeDefNameCountTotal(int attributeDefNameCountTotal1) {
    this.attributeDefNameCountTotal = attributeDefNameCountTotal1;
  }

  /**
   * group count total (even ones not allowed to delete)
   * @param groupCountTotal1 the groupCountTotal to set
   */
  public void setGroupCountTotal(int groupCountTotal1) {
    this.groupCountTotal = groupCountTotal1;
  }

  /**
   * @param stemCountTotal1 the stemCountTotal to set
   */
  public void setStemCountTotal(int stemCountTotal1) {
    this.stemCountTotal = stemCountTotal1;
  }

  
}
