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
