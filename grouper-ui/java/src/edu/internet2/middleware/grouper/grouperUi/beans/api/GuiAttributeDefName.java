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
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Result of one attribute def name being retrieved
 * 
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GuiAttributeDefName extends GuiObjectBase implements Serializable {

  /**
   * 
   * @param attributeDefNames
   * @param configMax
   * @param max
   * @return
   */
  public static Set<GuiAttributeDefName> convertFromAttributeDefNames(Set<AttributeDefName> attributeDefNames) {
    return convertFromAttributeDefNames(attributeDefNames, null, -1);
  }

  /**
   * 
   * @param attributeDefNames
   * @param configMax
   * @param max
   * @return
   */
  public static Set<GuiAttributeDefName> convertFromAttributeDefNames(Set<AttributeDefName> attributeDefNames, String configMax, int defaultMax) {
    Set<GuiAttributeDefName> tempAttributeDefNames = new LinkedHashSet<GuiAttributeDefName>();
    
    Integer max = null;
    
    if (!StringUtils.isBlank(configMax)) {
      max = GrouperUiConfig.retrieveConfig().propertyValueInt(configMax, defaultMax);
    }
    
    int count = 0;
    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
      tempAttributeDefNames.add(new GuiAttributeDefName(attributeDefName));
      if (max != null && ++count >= max) {
        break;
      }
    }
    
    return tempAttributeDefNames;
    
  }
  
  /**
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getShortLink() {
    
    return shortLinkHelper(false, false);
  }
  
  /**
   * display short link with image next to it in li
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getShortLinkWithIcon() {
    
    return shortLinkHelper(true, false);
  }

  /**
   * display short link with image next to it in li and the path info below it
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getShortLinkWithIconAndPath() {
    
    return shortLinkHelper(true, true);
  }

  /**
   * 
   * @param showIcon
   * @return the link
   */
  private String shortLinkHelper(boolean showIcon, boolean showPath) {
    
    if (this.attributeDefName == null) {
      //TODO put icon here?
      return TextContainer.retrieveFromRequest().getText().get("guiObjectUnknown");
    }
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiAttributeDefName(this);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(showIcon);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowPath(showPath);
    
    try {
      
      String result = TextContainer.retrieveFromRequest().getText().get("guiAttributeDefNameShortLink");
      return result;
      
    } finally {

      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiAttributeDefName(null);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(false);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowPath(false);

    }

  }

  /** folder */
  private AttributeDefName attributeDefName;
  

  /**
   * return the attribute def name
   * @return the attribute def name
   */
  public AttributeDefName getAttributeDefName() {
    return this.attributeDefName;
  }

  /**
   * 
   */
  public GuiAttributeDefName() {
    
  }
  
  /**
   * 
   * @param theAttributeDefName
   */
  public GuiAttributeDefName(AttributeDefName theAttributeDefName) {
    this.attributeDefName = theAttributeDefName;
  }
  
  /**
   * @see GuiObjectBase#getObject()
   */
  @Override
  public GrouperObject getGrouperObject() {
    return this.attributeDefName;
  }
  
}
