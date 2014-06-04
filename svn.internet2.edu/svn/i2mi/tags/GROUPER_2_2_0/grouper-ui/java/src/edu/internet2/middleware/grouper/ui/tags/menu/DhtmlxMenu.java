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
 * $Id: DhtmlxMenu.java,v 1.1.2.1 2010/02/03 18:00:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.tags.menu;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * See dhtmlx docs: http://docs.dhtmlx.com/doku.php?id=dhtmlxmenu:xml_format_template
 */
public class DhtmlxMenu {

  /** menu items */
  private List<DhtmlxMenuItem> dhtmlxMenuItems;

  
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
   * 
   * @return the xml string
   */
  public String toXml() {
    StringWriter stringWriter = new StringWriter();
    XMLStreamWriter xmlStreamWriter = null;
    try {
      XMLOutputFactory xof =  XMLOutputFactory.newInstance();
      xmlStreamWriter = xof.createXMLStreamWriter(stringWriter);
      
      this.toXml(xmlStreamWriter);
      
    } catch (XMLStreamException xmlStreamException) {
      throw new RuntimeException(xmlStreamException);
    } finally {
      GrouperUtil.closeQuietly(xmlStreamWriter);
    }
    return stringWriter.toString();
  }
  
  /**
   * write this to xml
   * @param xmlStreamWriter 
   * @throws XMLStreamException
   */
  public void toXml(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
    
    xmlStreamWriter.writeStartElement("menu");
    
    for (DhtmlxMenuItem dhtmlxMenuItem : GrouperUtil.nonNull(this.dhtmlxMenuItems)) {
      dhtmlxMenuItem.toXml(xmlStreamWriter);
    }
    
    //end menu
    xmlStreamWriter.writeEndElement();
  }
  
}
