package edu.internet2.middleware.grouperClientExt.xmpp;

/**
 * container around esb event
 * @author mchyzer
 *
 */
public class EsbEvents {
  
  /** */
  private EsbEvent[] esbEvent;

  /**
   * 
   * @return event array
   */
  public EsbEvent[] getEsbEvent() {
    return esbEvent;
  }

  /**
   * 
   * @param esbEvent
   */
  public void setEsbEvent(EsbEvent[] esbEvent) {
    this.esbEvent = esbEvent;
  }

  /**
   * 
   * @param esbEvent
   */
  public void addEsbEvent(EsbEvent esbEvent) {
    if(this.esbEvent ==null) {
      this.esbEvent = new EsbEvent[] {esbEvent};
    } else {
      EsbEvent[] newArray = new EsbEvent[this.esbEvent.length + 1];
          System.arraycopy(this.esbEvent, 0, newArray, 0,
              this.esbEvent.length);
          newArray[this.esbEvent.length + 1] = esbEvent;
          this.esbEvent= newArray;
    }
  }
}
