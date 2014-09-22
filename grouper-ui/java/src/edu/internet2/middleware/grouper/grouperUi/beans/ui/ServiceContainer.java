/*******************************************************************************
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiService;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;

/**
 * service container beans and objects
 * @author mchyzer
 *
 */
public class ServiceContainer {

  /**
   * gui service
   */
  private GuiService guiService;
  
  /**
   * @return the gui service
   */
  public GuiService getGuiService() {
    return this.guiService;
  }
  
  /**
   * @param guiService1 the guiService to set
   */
  public void setGuiService(GuiService guiService1) {
    this.guiService = guiService1;
  }

  /**
   * paging for service
   */
  private GuiPaging guiPaging = null;
  
  /**
   * gui stems in the service
   * @return gui services
   */
  public Set<GuiStem> getGuiStemsInService() {
    return this.guiStemsInService;
  }

  /**
   * gui stems in the service
   * @param guiStemsInService1
   */
  public void setGuiStemsInService(Set<GuiStem> guiStemsInService1) {
    this.guiStemsInService = guiStemsInService1;
  }

  /**
   * gui stems in the service
   */
  private Set<GuiStem> guiStemsInService;

  /**
   * paging
   * @return paging
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  /**
   * paging stems in service
   * @param guiPaging1
   */
  public void setGuiPaging(GuiPaging guiPaging1) {
    this.guiPaging = guiPaging1;
  }

}
