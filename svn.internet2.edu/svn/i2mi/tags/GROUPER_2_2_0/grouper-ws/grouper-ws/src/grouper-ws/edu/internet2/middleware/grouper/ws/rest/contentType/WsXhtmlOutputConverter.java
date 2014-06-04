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
 * @author mchyzer $Id: WsXhtmlOutputConverter.java,v 1.1 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * this will write a bean based on javabean properties (read / getters).
 * supports only:
 * 1. String fields
 * 2. int fields
 * 3. String arrays
 * 4. int arrays
 * 5. Bean fields
 * 6. Bean arrays
 * 7. Will not work with circular references
 * Will throw exception if something is not right...
 * Does not support any other structures.  Inheritance is not supported
 * Use this object once and throw away
 * </pre>
 */
public class WsXhtmlOutputConverter {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsXhtmlOutputConverter.class);

  /** writer for the output */
  private XMLStreamWriter xmlWriter = null;

  /** is using the default constructor, this is the string */
  private Writer outWriter = null;

  /**
   * if the XHTML headers should be included
   */
  private boolean includeXhtmlHeaders = true;

  /**
   * write the title of the XHTML
   */
  private String title = null;

  /**
   * @param theIncludeXhtmlHeaders
   * @param theTitle
   * do not pass in a writer, generally this is for testing
   */
  public WsXhtmlOutputConverter(boolean theIncludeXhtmlHeaders, String theTitle) {
    this.includeXhtmlHeaders = theIncludeXhtmlHeaders;
    this.title = theTitle;
  }

  /**
   * based on object, get all getters, and write to stream.  This is
   * a top level object where the classname (not fully qualified) is 
   * written to stream
   * @param bean cannot be null, must be a javabean
   * @return the xhtml
   */
  public String writeBean(Object bean) {
    this.setOutWriter(new StringWriter());

    this.writeBeanHelper(bean);

    return ((StringWriter) this.outWriter).toString();

  }

  /**
   * based on object, get all getters, and write to stream.  This is
   * a top level object where the classname (not fully qualified) is 
   * written to stream
   * @param bean cannot be null, must be a javabean
   * @param writer is the writer to write to
   */
  public void writeBean(Object bean, Writer writer) {
    this.setOutWriter(writer);

    this.writeBeanHelper(bean);

  }

  /**
   * write bean irrespective of writer
   * @param bean
   */
  private void writeBeanHelper(Object bean) {
    //see if can be simplified
    Class<? extends Object> objectClass = bean.getClass();

    try {

      if (this.includeXhtmlHeaders) {

        //<?xml version="1.0" encoding="iso-8859-1"?>
        //<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
        //<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
        this.xmlWriter.writeStartDocument("utf-8", "1.0");
        this.xmlWriter
            .writeDTD("\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        this.xmlWriter.writeStartElement("html");
        this.xmlWriter.writeAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        this.xmlWriter.writeAttribute("xml:lang", "en");
        this.xmlWriter.writeAttribute("lang", "en");

        //  <head>
        //    <title>Get Members response</title>
        this.xmlWriter.writeStartElement("head");
        this.xmlWriter.writeStartElement("title");

        //just put the object class in the title if not found
        //maybe eventually we could have an interface with a more friendly name
        if (StringUtils.isBlank(this.title)) {
          this.title = objectClass.getSimpleName();
        }

        if (!StringUtils.isBlank(this.title)) {
          this.xmlWriter.writeCharacters(this.title);
        }
        this.xmlWriter.writeEndElement();
        //  </head>
        this.xmlWriter.writeEndElement();

        // <body>
        this.xmlWriter.writeStartElement("body");
      }

      //use the body tag for the top level object
      this.writeBean("div", objectClass, null, bean);

      this.xmlWriter.writeEndDocument();

    } catch (Throwable e) {
      throw new RuntimeException("Error writing class: " + objectClass + ", "
          + GrouperUtil.toStringForLog(bean), e);
    }
    //    <p class="resultCode">SUCCESS</p>
    //    <p class="resultMessage"></p>
    //    <p class="success">T</p>
    //    <p class="subjectIdentifierRequested">pennkey</p>
    //    <ul class="results">
    //      <li>
    //        <p class="subjectId">GrouperSystem</p>
    //        <p class="subjectName">GrouperSystem</p>
    //        <p class="subjectIdentifier">12345678</p>
    //       </li>
    //    </ul>

  }

  /**
   * based on object, get all getters, and write to stream
   * @param elementName to write (or none)
   * @param returnType type of object (since can be null)
   * @param propertyName 
   * @param object
   * @throws XMLStreamException 
   */
  private void writeBean(String elementName, Class<?> returnType, String propertyName,
      Object object) throws XMLStreamException {

    //objects are written as div's (but can be li)
    this.xmlWriter.writeStartElement(elementName);

    //maybe there is a property name, maybe not (if array)
    if (!StringUtils.isBlank(propertyName)) {
      this.xmlWriter.writeAttribute("class", propertyName);
    }

    //put the type in the title attribute
    this.xmlWriter.writeAttribute("title", returnType.getSimpleName());

    //lets get all the getters
    Set<Method> getters = GrouperUtil.getters(returnType, returnType, null, null);

    //it makes no sense that a bean would have no getters!
    if (GrouperUtil.length(getters) == 0) {
      throw new RuntimeException("Bean has no getters! " + returnType);
    }
    //if object is null, then dont call getters
    if (object != null) {
      for (Method getterMethod : getters) {

        //return type
        Class<?> subReturnType = getterMethod.getReturnType();

        //result of getter (getter better not take arguments)
        Object subReturnObject = GrouperUtil.invokeMethod(getterMethod, object, null);

        String subPropertyName = GrouperUtil.propertyName(getterMethod);

        //see if array
        if (subReturnType.isArray()) {

          this.writeArray(subReturnType, subPropertyName, subReturnObject);

        } else {

          this.writeScalarObject("p", "div", subReturnType, subPropertyName,
              subReturnObject);

        }

      }
    }
    //end the element
    this.xmlWriter.writeEndElement();

  }

  /**
   * write a non-array
   * @param elementNameNonBean is the HTML element to use for non beans (e.g. string, int)
   * @param elementNameBean is the HTML element to use for beans
   * @param returnType
   * @param returnObject
   * @param propertyName
   * @throws XMLStreamException
   */
  private void writeScalarObject(String elementNameNonBean, String elementNameBean,
      Class<?> returnType, String propertyName, Object returnObject)
      throws XMLStreamException {
    //see if String or int
    if (scalarType(returnType)) {
      this.writeString(elementNameNonBean, propertyName, GrouperUtil
          .toStringSafe(returnObject));
    } else {
      //must be bean
      this.writeBean(elementNameBean, returnType, propertyName, returnObject);
    }
  }

  /**
   * if this is a scalar type (String, int, etc)
   * @param theClass
   * @return true if scalar
   */
  private static boolean scalarType(Class<?> theClass) {
    return String.class.equals(theClass) || int.class.equals(theClass)
        || Integer.class.equals(theClass);
  }

  /**
   * write a string e.g. &lt;p class="resultCode"&gt;SUCCESS&lt;/p&gt;
   * @param propertyName
   * @param value
   * @param elementName is the xhtml element to write
   * @throws XMLStreamException if problem
   */
  private void writeString(String elementName, String propertyName, String value)
      throws XMLStreamException {

    this.xmlWriter.writeStartElement(elementName);

    //maybe we dont have a property name
    if (!StringUtils.isBlank(propertyName)) {
      this.xmlWriter.writeAttribute("class", propertyName);
    }
    //if null or blank just close the element
    //this is because reading the string cant tell the different, so write null as not there
    if (!StringUtils.isBlank(value)) {
      this.xmlWriter.writeCharacters(value);
    }
    this.xmlWriter.writeEndElement();
  }

  /**
   * write an array e.g. &lt;ul class="whatever"&gt; &lt;li class="field"&gt;someValue&lt;/li&gt; &lt;/ul&gt;
   * @param arrayType 
   * @param propertyName
   * @param array is array of beans, Strings, or ints
   * @param elementName is the xhtml element to write
   * @throws XMLStreamException if problem
   */
  private void writeArray(Class<?> arrayType, String propertyName, Object array)
      throws XMLStreamException {

    //if array is null, forget it
    if (array != null) {
      int length = GrouperUtil.length(array);
      if (length > 0) {
        this.xmlWriter.writeStartElement("ul");
        this.xmlWriter.writeAttribute("class", propertyName);

        Class<?> arrayClass = arrayType.getComponentType();
        for (int i = 0; i < length; i++) {
          Object value = Array.get(array, i);
          //note, cant have an array of arrays
          this.writeScalarObject("li", "li", arrayClass, null, value);
        }

        //end the ul
        this.xmlWriter.writeEndElement();
      }
    }
  }

  /**
   * set the out writer
   * @param outWriter1
   */
  private void setOutWriter(Writer outWriter1) {
    this.outWriter = outWriter1;

    XMLOutputFactory factory = XMLOutputFactory.newInstance();
    try {
      this.xmlWriter = factory.createXMLStreamWriter(this.outWriter);
    } catch (XMLStreamException xse) {
      throw new RuntimeException(xse);
    }

  }
}
