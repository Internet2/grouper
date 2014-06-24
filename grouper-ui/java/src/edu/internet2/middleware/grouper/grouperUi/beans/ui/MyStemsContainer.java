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

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;


public class MyStemsContainer {

  /**
   * for the index page, this is a short list of stems the user manages
   */
  private Set<GuiStem> guiStemsUserManages;
  /**
   * paging for my stems
   */
  private GuiPaging myStemsGuiPaging = null;

  /**
   * 
   * @return stems
   */
  public Set<GuiStem> getGuiStemsUserManages() {
    return this.guiStemsUserManages;
  }

  /**
   * 
   * @param guiStemsUserManages1
   */
  public void setGuiStemsUserManages(Set<GuiStem> guiStemsUserManages1) {
    this.guiStemsUserManages = guiStemsUserManages1;
  }

  /**
   * paging for my stems
   * @return paging
   */
  public GuiPaging getMyStemsGuiPaging() {
    if (this.myStemsGuiPaging == null) {
      this.myStemsGuiPaging = new GuiPaging();
    }
    return this.myStemsGuiPaging;
  }

  /**
   * paging for my stems
   * @param myStemsGuiPaging1
   */
  public void setMyStemsGuiPaging(GuiPaging myStemsGuiPaging1) {
    this.myStemsGuiPaging = myStemsGuiPaging1;
  }

}
