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
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;

import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.privs.Privilege;


/**
 * Result of one privilege being retrieved.
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GuiPrivilege implements Serializable {

  /** privilege */
  private Privilege privilege;
  

  /**
   * return the privilege
   * @return the privilege
   */
  public Privilege getPrivilege() {
    return this.privilege;
  }

  /**
   * 
   */
  public GuiPrivilege() {
    
  }
  
  /**
   * 
   * @param theStem
   */
  public GuiPrivilege(Privilege thePrivilege) {
    this.privilege = thePrivilege;
  }
  
  /**
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getShortLink() {
    if (this.privilege == null) {
      return TextContainer.retrieveFromRequest().getText().get("guiObjectUnknown");
    }
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiPrivilege(this);
    
    try {
      
      String privTextKey = "priv." + this.privilege.getName();
      String result = TextContainer.retrieveFromRequest().getTextWithTooltip().get(privTextKey);
      return result;
      
    } finally {
  
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiPrivilege(null);
  
    }
  }

  
}
