/**
 * @author mchyzer
 * $Id: ControllerDone.java,v 1.2 2009-08-07 07:36:02 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.exceptions;


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
