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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.service.ServiceUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Result of one service being retrieved
 * 
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GuiService extends GuiObjectBase implements Serializable {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GuiService.class);

  /**
   * 
   * @param attributeDefNames
   * @return services
   */
  public static Set<GuiService> convertFromAttributeDefNames(Set<AttributeDefName> attributeDefNames) {
    return convertFromAttributeDefNames(attributeDefNames, null, -1);
  }

  /**
   * 
   * @param attributeDefNames
   * @param configMax
   * @param defaultMax
   * @return attribute def names
   */
  public static Set<GuiService> convertFromAttributeDefNames(Set<AttributeDefName> attributeDefNames, String configMax, int defaultMax) {
    Set<GuiService> tempServices = new LinkedHashSet<GuiService>();
    
    Integer max = null;
    
    if (!StringUtils.isBlank(configMax)) {
      max = GrouperUiConfig.retrieveConfig().propertyValueInt(configMax, defaultMax);
    }
    
    int count = 0;
    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
      
      //lets see where this is assigned
      QueryOptions queryOptions = QueryOptions.create(null, null, 1, 2);
      Set<Stem> stems = ServiceUtils.retrieveStemsForService(attributeDefName.getId(), queryOptions);
      
      GuiAttributeDefName theGuiAttributeDefName = new GuiAttributeDefName(attributeDefName);
      
      //if one then go right to the stem
      if (GrouperUtil.length(stems) == 1) {
        GuiStem theGuiStem = new GuiStem(stems.iterator().next());
        tempServices.add(new GuiService(theGuiAttributeDefName, 
            theGuiStem));
        
      } else if (GrouperUtil.length(stems) > 1) {
        tempServices.add(new GuiService(theGuiAttributeDefName, null));
      
      } else {
        LOG.error("Why are there no stems returned??? " + attributeDefName);
      }
      
      if (max != null && ++count >= max) {
        break;
      }
    }
    
    return tempServices;
    
  }

  /**
   * gui attribute def name for this service
   */
  private GuiAttributeDefName guiAttributeDefName;

  /**
   * gui stem for this service if there is only one stem
   */
  private GuiStem guiStem;

  /**
   * gui stem for this service if there is only one stem
   * @return the guiStem
   */
  public GuiStem getGuiStem() {
    return this.guiStem;
  }
  
  /**
   * gui stem for this service if there is only one stem
   * @param guiStem1 the guiStem to set
   */
  public void setGuiStem(GuiStem guiStem1) {
    this.guiStem = guiStem1;
  }

  /**
   * gui attribute def name for this service
   * @return the guiAttributeDefName
   */
  public GuiAttributeDefName getGuiAttributeDefName() {
    return this.guiAttributeDefName;
  }

  
  /**
   * gui attribute def name for this service
   * @param guiAttributeDefName1 the guiAttributeDefName to set
   */
  public void setGuiAttributeDefName(GuiAttributeDefName guiAttributeDefName1) {
    this.guiAttributeDefName = guiAttributeDefName1;
  }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GuiService)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.guiAttributeDefName, ( (GuiService) other ).guiAttributeDefName )
      .isEquals();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.guiAttributeDefName )
      .toHashCode();
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
   * @param showPath 
   * @return the link
   */
  private String shortLinkHelper(boolean showIcon, boolean showPath) {

    if (this.guiAttributeDefName == null) {
      return TextContainer.retrieveFromRequest().getText().get("guiObjectUnknown");
    }
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiService(this);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiStem(this.guiStem);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiAttributeDefName(this.guiAttributeDefName);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(showIcon);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowPath(showPath);
    
    try {
      
      String result = TextContainer.retrieveFromRequest().getText().get(
          this.guiStem == null ? "guiServiceShortLink" : "guiServiceFolderShortLink");
      return result;
      
    } finally {

      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiService(null);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiStem(null);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiAttributeDefName(null);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(false);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowPath(false);

    }

  }

  /**
   * 
   */
  public GuiService() {
    
  }
  
  /**
   * @param theGuiStem
   * @param theGuiAttributeDefName
   */
  public GuiService(GuiAttributeDefName theGuiAttributeDefName, GuiStem theGuiStem) {
    this.guiAttributeDefName = theGuiAttributeDefName;
    this.guiStem = theGuiStem;
  }
  
  /**
   * @see GuiObjectBase#getGrouperObject()
   */
  @Override
  public GrouperObject getGrouperObject() {
    return this.guiAttributeDefName.getGrouperObject();
  }

  /**
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getLink() {
    
    return linkHelper(false);
  }

  /**
   * display short link with image next to it in li
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getLinkWithIcon() {
    
    return linkHelper(true);
  }

  /**
   * 
   * @param showIcon
   * @return the link
   */
  private String linkHelper(boolean showIcon) {
    
    if (this.guiAttributeDefName == null) {
      return TextContainer.retrieveFromRequest().getText().get("guiObjectUnknown");
    }
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiService(this);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiStem(this.guiStem);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiAttributeDefName(this.guiAttributeDefName);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(showIcon);
    
    try {
      
      String result = TextContainer.retrieveFromRequest().getText().get(this.guiStem == null ? "guiServiceLink" : "guiServiceFolderLink");
      return result;

    } finally {
  
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiService(null);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiStem(null);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiAttributeDefName(null);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(false);
  
    }
  
  }
  
}
