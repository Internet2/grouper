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
/*
 * @author mchyzer
 * $Id: XstreamPoc.java,v 1.5 2009-11-21 21:50:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.poc;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
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
    //xmlXstream();
    //gson();
    jsonOrg();
  }

  /**
   * 
   */
  public static void xmlXstream() {
    XstreamPocGroup group = new XstreamPocGroup("myGroup",
        new XstreamPocMember[]{
         new XstreamPocMember("John", "John Smith - Employee"),
         new XstreamPocMember("Mary", "Mary Johnson - Student")});
    XStream xStream = new XStream(new XppDriver());
    
    //do javabean properties, not fields
    xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()) {

      /**
       * @see com.thoughtworks.xstream.converters.javabean.JavaBeanConverter#canConvert(java.lang.Class)
       */
      @SuppressWarnings("unchecked")
      @Override
      public boolean canConvert(Class type) {
        //see if one of our beans
        return type.getName().startsWith("edu.internet2");
      }
      
    }); 
    
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

  /**
   * 
   */
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

  /**
   * 
   */
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

  /**
   * 
   */
  public static void gson() {
  
    XstreamPocGroup group = new XstreamPocGroup("myGroup",
        new XstreamPocMember[]{
         new XstreamPocMember("John", "John Smith - Employee"),
         new XstreamPocMember("Mary", "Mary Johnson - Student")});
  
    String json = GrouperUtil.jsonConvertTo(group);
    Map<String, Class<?>> conversionMap = new HashMap<String, Class<?>>();
    conversionMap.put("XstreamPocGroup", XstreamPocGroup.class);
    
//    XStream xStream = new XStream(new JettisonMappedXmlDriver());
//    xStream.alias("XstreamPocGroup", XstreamPocGroup.class);
//    xStream.alias("XstreamPocMember", XstreamPocMember.class);
//    String json = xStream.toXML(group);
    System.out.println(GrouperUtil.indent(json, true));
//    group = (XstreamPocGroup)xStream.fromXML(json);
    group = (XstreamPocGroup)GrouperUtil.jsonConvertFrom(conversionMap, json);

    System.out.println(group.getName() + ", number of members: " 
        + group.getMembers().length);
  
  }

  /**
     * 
     */
    public static void jsonOrg() {
    
      XstreamPocGroup group = new XstreamPocGroup("myGroup",
          new XstreamPocMember[]{
           new XstreamPocMember("John", "John Smith - Employee"),
           new XstreamPocMember("Mary", "Mary Johnson - Student")});

      group.setName(null);
      
//      JSONObject jsonObject = net.sf.json.JSONObject.fromObject( group );  
//      String json = jsonObject.toString();

      JsonConfig jsonConfig = new JsonConfig();  
      jsonConfig.setJsonPropertyFilter( new PropertyFilter(){  
         public boolean apply( Object source, String name, Object value ) {  
            return value == null; 
         }  
      });  
      JSONObject jsonObject = JSONObject.fromObject( group, jsonConfig );  
      String json = jsonObject.toString();
      
      Map<String, Class<?>> conversionMap = new HashMap<String, Class<?>>();
      conversionMap.put("XstreamPocGroup", XstreamPocGroup.class);

  //    XStream xStream = new XStream(new JettisonMappedXmlDriver());
  //    xStream.alias("XstreamPocGroup", XstreamPocGroup.class);
  //    xStream.alias("XstreamPocMember", XstreamPocMember.class);
  //    String json = xStream.toXML(group);
      System.out.println(GrouperUtil.indent(json, true));

      jsonObject = JSONObject.fromObject( json );  
      group = (XstreamPocGroup) JSONObject.toBean( jsonObject, XstreamPocGroup.class );  

  //    group = (XstreamPocGroup)xStream.fromXML(json);
//      group = (XstreamPocGroup)GrouperUtil.jsonConvertFrom(conversionMap, json);

      System.out.println(group.getName() + ", number of members: " 
          + group.getMembers().length + ", " + group.getMembers()[1].getName());



    }
  
}
