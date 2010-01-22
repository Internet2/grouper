/**
 * @author mchyzer
 * $Id: ControllerDone.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.exceptions;


/**
 * when the servlet is done with no error
 */
public class ControllerDone extends RuntimeException {

  /**
   * if this should be printed
   */
  private boolean printGuiReponseJs = false;
  
  /**
   * 
   */
  public ControllerDone() {
  }

  /**
   * @param printGuiReponseJs1
   */
  public ControllerDone(boolean printGuiReponseJs1) {
    super();
    this.printGuiReponseJs = printGuiReponseJs1;
  }

  
  /**
   * @return the printGuiReponseJs
   */
  public boolean isPrintGuiReponseJs() {
    return this.printGuiReponseJs;
  }

  
}
