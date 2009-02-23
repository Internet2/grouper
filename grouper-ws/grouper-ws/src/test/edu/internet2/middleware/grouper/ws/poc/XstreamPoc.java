/*
 * @author mchyzer
 * $Id: XstreamPoc.java,v 1.1.2.1 2009-02-23 18:39:59 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.poc;

import java.io.StringWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.rest.contentType.WsXhtmlInputConverter;
import edu.internet2.middleware.grouper.ws.rest.contentType.WsXhtmlOutputConverter;


/**
 *
 */
public class XstreamPoc {

  /**
   * @param args
   */
  public static void main(String[] args) {
    //xhtmlConversion();
    //jsonXstream();
    xmlXstream();
  }

  public static void xmlXstream() {
    XstreamPocGroup group = new XstreamPocGroup("myGroup",
        new XstreamPocMember[]{
         new XstreamPocMember("John", "John Smith - Employee"),
         new XstreamPocMember("Mary", "Mary Johnson - Student")});
    XStream xStream = new XStream(new XppDriver());
    xStream.alias("XstreamPocGroup", XstreamPocGroup.class);
    xStream.alias("XstreamPocMember", XstreamPocMember.class);
    StringWriter stringWriter = new StringWriter();
    xStream.marshal(group, new CompactWriter(stringWriter));
    String xml = stringWriter.toString();
    System.out.println(GrouperUtil.indent(xml, true));
    group = (XstreamPocGroup)xStream.fromXML(xml);
    System.out.println(group.getName() + ", number of members: " 
        + group.getMembers().length);

  }
  
  public static void jsonXstream() {

    XstreamPocGroup group = new XstreamPocGroup("myGroup",
        new XstreamPocMember[]{
         new XstreamPocMember("John", "John Smith - Employee"),
         new XstreamPocMember("Mary", "Mary Johnson - Student")});

    XStream xStream = new XStream(new JettisonMappedXmlDriver());
    xStream.alias("XstreamPocGroup", XstreamPocGroup.class);
    xStream.alias("XstreamPocMember", XstreamPocMember.class);
    String json = xStream.toXML(group);
    System.out.println(GrouperUtil.indent(json, true));
    group = (XstreamPocGroup)xStream.fromXML(json);
    System.out.println(group.getName() + ", number of members: " 
        + group.getMembers().length);
  
  }

  public static void xhtmlConversion() {
    XstreamPocGroup group = new XstreamPocGroup("myGroup",
        new XstreamPocMember[]{
         new XstreamPocMember("John", "John Smith - Employee"),
         new XstreamPocMember("Mary", "Mary Johnson - Student")});
    WsXhtmlOutputConverter wsXhtmlOutputConverter = new WsXhtmlOutputConverter(true,
        null);
    StringWriter stringWriter = new StringWriter();
    wsXhtmlOutputConverter.writeBean(group, stringWriter);
    String xhtml = stringWriter.toString();
    System.out.println(GrouperUtil.indent(xhtml, true));
    WsXhtmlInputConverter wsXhtmlInputConverter = new WsXhtmlInputConverter();
    wsXhtmlInputConverter.addAlias("XstreamPocGroup", XstreamPocGroup.class);
    wsXhtmlInputConverter.addAlias("XstreamPocMember", XstreamPocMember.class);
    group = (XstreamPocGroup)wsXhtmlInputConverter.parseXhtmlString(xhtml);
    System.out.println(group.getName() + ", number of members: " 
        + group.getMembers().length);
  }
  
}
