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
 * $Id: DhtmlxMenuItem.java,v 1.1.2.1 2010/02/03 18:00:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.tags.menu;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * item XML in a menu.
 * see dhtmlx docs: http://docs.dhtmlx.com/doku.php?id=dhtmlxmenu:xml_format_template
 */
public class DhtmlxMenuItem {

  /** tooltip */
  private String tooltip;
  
  
  
  /**
   * tooltip
   * @return tooltip
   */
  public String getTooltip() {
    return this.tooltip;
  }

  /**
   * tooltip
   * @param tooltip1
   */
  public void setTooltip(String tooltip1) {
    this.tooltip = tooltip1;
  }

  /** id attribute */
  private String id;
  
  /** text attribute which is what shows up on screen */
  private String text;
  
  /** image for item */
  private String img;
  
  /** e.g. radio or checkbox */
  private String type;
  
  /** if enabled */
  private Boolean enabled;
  
  /** if this button is an href */
  private String href;
  
  /**
   * if this button is an href
   * @return href link
   */
  public String getHref() {
    return this.href;
  }

  /**
   * if this button is an href
   * @param href1
   */
  public void setHref(String href1) {
    this.href = href1;
  }

  /**
   * if enabled
   * @return the enabled
   */
  public Boolean getEnabled() {
    return this.enabled;
  }

  /**
   * if enabled
   * @param enabled1 the enabled to set
   */
  public void setEnabled(Boolean enabled1) {
    this.enabled = enabled1;
  }


  /**
   * e.g. radio or checkbox
   * @return the type
   */
  public String getType() {
    return this.type;
  }

  
  /**
   * e.g. radio or checkbox
   * @param type1 the type to set
   */
  public void setType(String type1) {
    this.type = type1;
  }

  /**
   * image for item
   * @return the img
   */
  public String getImg() {
    return this.img;
  }

  /** imgdis element image for disabled state */
  private String imgdis;

  /**
   * imgdis element image for disabled state
   * @return the imgdis
   */
  public String getImgdis() {
    return this.imgdis;
  }

  /** group of element e.g. radio */
  private String group;

  /**
   * group of element e.g. radio
   * @return the group
   */
  public String getGroup() {
    return this.group;
  }

  /**
   * group of element e.g. radio
   * @param group1 the group to set
   */
  public void setGroup(String group1) {
    this.group = group1;
  }

  /** if radio or checkbox is checked */
  private Boolean checked;

  
  
  
  /**
   * if radio or checkbox is checked
   * @param checked1 the checked to set
   */
  public void setChecked(Boolean checked1) {
    this.checked = checked1;
  }

  /**
   * imgdis element image for disabled state
   * @param imgdis1 the imgdis to set
   */
  public void setImgdis(String imgdis1) {
    this.imgdis = imgdis1;
  }


  /**
   * image for item
   * @param img1 the img to set
   */
  public void setImg(String img1) {
    this.img = img1;
  }


  /**
   * id attribute
   * @return the text
   */
  public String getText() {
    return this.text;
  }

  
  /**
   * id attribute
   * @param text1 the text to set
   */
  public void setText(String text1) {
    this.text = text1;
  }

  /** hotkey e.g. Ctrl+N */
  private String hotkey;



  /** menu items */
  private List<DhtmlxMenuItem> dhtmlxMenuItems;
  
  
  /**
   * hotkey e.g. Ctrl+N
   * @return the hotkey
   */
  public String getHotkey() {
    return this.hotkey;
  }

  
  /**
   * hotkey e.g. Ctrl+N
   * @param hotkey1 the hotkey to set
   */
  public void setHotkey(String hotkey1) {
    this.hotkey = hotkey1;
  }

  
  /**
   * @return the checked
   */
  public Boolean getChecked() {
    return this.checked;
  }

  /**
   * id attribute sent back on event
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id attribute sent back on event
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }



  /**
   * write this to xml
   * @param xmlStreamWriter 
   * @throws XMLStreamException
   */
  public void toXml(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
    
    boolean nonEmptyElement = GrouperUtil.length(this.dhtmlxMenuItems) > 0 || !StringUtils.isBlank(this.hotkey)
        || !StringUtils.isBlank(this.tooltip);
    if (nonEmptyElement) {
      xmlStreamWriter.writeStartElement("item");
    } else {
      xmlStreamWriter.writeEmptyElement("item");
    }
    if (!StringUtils.isBlank(this.id)) {
      xmlStreamWriter.writeAttribute("id", this.id);
    }
    if (!StringUtils.isBlank(this.text)) {
      xmlStreamWriter.writeAttribute("text", this.text);
    }
    if (!StringUtils.isBlank(this.group)) {
      xmlStreamWriter.writeAttribute("group", this.group);
    }
    if (!StringUtils.isBlank(this.type)) {
      xmlStreamWriter.writeAttribute("type", this.type);
    }
    if (!StringUtils.isBlank(this.img)) {
      xmlStreamWriter.writeAttribute("img", this.img);
    }
    if (!StringUtils.isBlank(this.imgdis)) {
      xmlStreamWriter.writeAttribute("imgdis", this.imgdis);
    }
    if (this.enabled != null) {
      xmlStreamWriter.writeAttribute("enabled", this.enabled ? "true" : "false");
    }
    if (this.checked != null) {
      xmlStreamWriter.writeAttribute("checked", this.checked ? "true" : "false");
    }

    
    if (!StringUtils.isBlank(this.hotkey)) {
      xmlStreamWriter.writeStartElement("hotkey");
      
      xmlStreamWriter.writeCharacters(this.hotkey);
      
      //end hotkey
      xmlStreamWriter.writeEndElement();
    }

    if (!StringUtils.isBlank(this.href)) {
      xmlStreamWriter.writeStartElement("href");
      
      xmlStreamWriter.writeCData(this.href);
      
      //end href
      xmlStreamWriter.writeEndElement();
      
    }
    

    if (!StringUtils.isBlank(this.tooltip)) {
      xmlStreamWriter.writeStartElement("tooltip");
      
      xmlStreamWriter.writeCharacters(this.tooltip);
      
      //end tooltip
      xmlStreamWriter.writeEndElement();
    }
    
    for (DhtmlxMenuItem dhtmlxMenuItem : GrouperUtil.nonNull(this.dhtmlxMenuItems)) {
      dhtmlxMenuItem.toXml(xmlStreamWriter);
    }
    
    //end item
    if (nonEmptyElement) {
      xmlStreamWriter.writeEndElement();
    }
  }

  /**
   * menu items
   * @param dhtmlxMenuItem the dhtmlxMenuItem to add
   */
  public void addDhtmlxItem(DhtmlxMenuItem dhtmlxMenuItem) {
    if (this.dhtmlxMenuItems == null) {
      this.dhtmlxMenuItems = new ArrayList<DhtmlxMenuItem>();
    }
    this.dhtmlxMenuItems.add(dhtmlxMenuItem);
  }

  /**
   * menu items 
   * @return the dhtmlxMenuItems
   */
  public List<DhtmlxMenuItem> getDhtmlxMenuItems() {
    return this.dhtmlxMenuItems;
  }

  /**
   * menu items
   * @param dhtmlxMenuItems1 the dhtmlxMenuItems to set
   */
  public void setDhtmlxMenuItems(List<DhtmlxMenuItem> dhtmlxMenuItems1) {
    this.dhtmlxMenuItems = dhtmlxMenuItems1;
  }

}
