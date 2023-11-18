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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Result of one attribute def name being retrieved
 * 
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GuiAttributeDef extends GuiObjectBase implements Serializable {

  public boolean isPermission() {
    return this.getAttributeDef().getAttributeDefType() == AttributeDefType.perm;
  }
  
  /**
   * @param attributeDef the attributeDef to set
   */
  public void setAttributeDef(AttributeDef attributeDef) {
    this.attributeDef = attributeDef;
  }

  /**
   * if the logged in user has admin
   * @return true
   */
  public boolean isHasAdmin() {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    return (Boolean)GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return GuiAttributeDef.this.attributeDef.getPrivilegeDelegate().hasAttrAdmin(loggedInSubject);
      }
    });


  }

  /**
   * if the attrDef has update granted to all
   * @return true
   */
  public boolean isGrantAllUpdate() {
    return this.attributeDef.getPrivilegeDelegate().hasAttrUpdate(SubjectFinder.findAllSubject());
  }

  /**
   * if the attrDef has admin granted to all
   * @return true
   */
  public boolean isGrantAllAdmin() {
    return this.attributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectFinder.findAllSubject());
  }

  /**
   * if the attrDef has read granted to all
   * @return true
   */
  public boolean isGrantAllRead() {
    return this.attributeDef.getPrivilegeDelegate().hasAttrRead(SubjectFinder.findAllSubject());
  }

  /**
   * if the attrDef has view granted to all
   * @return true
   */
  public boolean isGrantAllView() {
    return this.attributeDef.getPrivilegeDelegate().hasAttrView(SubjectFinder.findAllSubject());
  }

  /**
   * if the attrDef has optin granted to all
   * @return true
   */
  public boolean isGrantAllOptin() {
    return this.attributeDef.getPrivilegeDelegate().hasAttrOptin(SubjectFinder.findAllSubject());
  }


  /**
   * if the attrDef has optout granted to all
   * @return true
   */
  public boolean isGrantAllOptout() {
    return this.attributeDef.getPrivilegeDelegate().hasAttrOptout(SubjectFinder.findAllSubject());
  }


  /**
   * if the attrDef has attr read granted to all
   * @return true
   */
  public boolean isGrantAllAttrRead() {
    return this.attributeDef.getPrivilegeDelegate().hasAttrDefAttrRead(SubjectFinder.findAllSubject());
  }

  /**
   * if the attrDef has attr update granted to all
   * @return true
   */
  public boolean isGrantAllAttrUpdate() {
    return this.attributeDef.getPrivilegeDelegate().hasAttrDefAttrUpdate(SubjectFinder.findAllSubject());
  }

  /**
   * comma separated privilege labels allowed by grouper all
   * @return the labels
   */
  public String getPrivilegeLabelsAllowedByGrouperAll() {
    StringBuilder results = new StringBuilder();
    boolean foundOne = false;
    
    if (this.isGrantAllAdmin()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.attrAdminUpper"));
    }
    if (this.isGrantAllUpdate()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.attrUpdateUpper"));
    }
    if (this.isGrantAllRead()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.attrReadUpper"));
    }
    if (this.isGrantAllView()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.attrViewUpper"));
    }
    if (this.isGrantAllOptin()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.attrOptinUpper"));
    }
    if (this.isGrantAllOptout()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.attrOptoutUpper"));
    }
    if (this.isGrantAllAttrUpdate()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.attrDefAttrUpdateUpper"));
    }
    if (this.isGrantAllAttrRead()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.attrDefAttrReadUpper"));
    }
    return results.toString();
  }

  
  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GuiAttributeDef)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.attributeDef, ( (GuiAttributeDef) other ).attributeDef )
      .isEquals();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.attributeDef )
      .toHashCode();
  }


  /**
   * 
   * @param attributeDef
   * @param configMax
   * @param max
   * @return
   */
  public static Set<GuiAttributeDef> convertFromAttributeDefs(Set<AttributeDef> attributeDef) {
    return convertFromAttributeDefs(attributeDef, null, -1);
  }

  /**
   * 
   * @param attributeDefs
   * @param configMax
   * @param max
   * @return
   */
  public static Set<GuiAttributeDef> convertFromAttributeDefs(Set<AttributeDef> attributeDefs, String configMax, int defaultMax) {
    Set<GuiAttributeDef> tempAttributeDefs = new LinkedHashSet<GuiAttributeDef>();
    
    Integer max = null;
    
    if (!StringUtils.isBlank(configMax)) {
      max = GrouperUiConfig.retrieveConfig().propertyValueInt(configMax, defaultMax);
    }
    
    int count = 0;
    for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefs)) {
      tempAttributeDefs.add(new GuiAttributeDef(attributeDef));
      if (max != null && ++count >= max) {
        break;
      }
    }
    
    return tempAttributeDefs;
    
  }

  /**
   * colon space separated path e.g.
   * Full : Path : To : The : Entity
   * @return the colon space separated path
   */
  public String getPathColonSpaceSeparated() {

    String parentStemName = GrouperUtil.parentStemNameFromName(this.attributeDef.getName());
    
    if (StringUtils.isBlank(parentStemName) || StringUtils.equals(":", parentStemName)) {
      return ":";
    }

    return parentStemName.replace(":", " : ");
    
  }
  
  /**
   * title for browser:
   * &lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
   * note, this is not xml escaped, if used in a title tag, it needs to be xml escaped...
   * @return the title
   */
  public String getTitle() {
    
    StringBuilder result = new StringBuilder();
    
    result.append("<strong>").append(
        TextContainer.retrieveFromRequest().getText().get("guiTooltipFolderLabel"))
        .append("</string><br />").append(GrouperUtil.xmlEscape(this.getPathColonSpaceSeparated(), true));
    
    String resultString = result.toString();
    return resultString;
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
    
    if (this.attributeDef == null) {
      return TextContainer.retrieveFromRequest().getText().get("guiObjectUnknown");
    }
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiAttributeDef(this);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(showIcon);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowPath(showPath);
    
    try {
      
      String result = TextContainer.retrieveFromRequest().getText().get("guiAttributeDefShortLink");
      return result;
      
    } finally {

      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiAttributeDef(null);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(false);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowPath(false);

    }

  }

  /** folder */
  private AttributeDef attributeDef;
  

  /**
   * return the attribute def name
   * @return the attribute def name
   */
  public AttributeDef getAttributeDef() {
    return this.attributeDef;
  }

  /**
   * 
   */
  public GuiAttributeDef() {
    
  }
  
  /**
   * 
   * @param theAttributeDef
   */
  public GuiAttributeDef(AttributeDef theAttributeDef) {
    this.attributeDef = theAttributeDef;
  }

  @Override
  public GrouperObject getGrouperObject() {
    return this.attributeDef;
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
    
    if (this.attributeDef == null) {
      //TODO put icon here?
      return TextContainer.retrieveFromRequest().getText().get("guiObjectUnknown");
    }
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiAttributeDef(this);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(showIcon);
    
    try {
      
      String result = TextContainer.retrieveFromRequest().getText().get("guiAttributeDefLink");
      return result;
      
    } finally {
  
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiAttributeDef(null);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(false);
  
    }
  
  }
  
}
