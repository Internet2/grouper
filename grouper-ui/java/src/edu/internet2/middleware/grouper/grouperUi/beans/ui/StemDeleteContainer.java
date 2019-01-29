/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;


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
  
  
  
}
