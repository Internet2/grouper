/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
