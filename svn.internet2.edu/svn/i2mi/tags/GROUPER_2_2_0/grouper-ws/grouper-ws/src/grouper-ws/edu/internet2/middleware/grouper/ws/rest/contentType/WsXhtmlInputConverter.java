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
 * @author mchyzer $Id: WsXhtmlInputConverter.java,v 1.3 2009-11-15 18:54:00 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.rest.WsRestClassLookup;

/**
 * convert an xhtml string to objects.  Only use once and throw away.
 * this parser assumes that beans do not have default values on fields, or
 * if so, if blank or null is passed in, then the default value will be used.
 */
public class WsXhtmlInputConverter {

  /**
   * map of unqualified (or qualified) classname to a class 
   */
  private Map<String, Class<?>> classLookup = Collections
      .synchronizedMap(new HashMap<String, Class<?>>());

  /**
   * add an alias for demarshaling
   * @param key
   * @param theClass
   */
  public void addAlias(String key, Class<?> theClass) {
    this.classLookup.put(key, theClass);
  }
  
  /** if extra elements are there, give warnings */
  private StringBuilder warnings = new StringBuilder();

  /** namespace of html element */
  private Namespace htmlNamespace = null;

  /** xml pattern */
  private static Pattern xmlHeader = Pattern.compile("^<\\?xml[^>]+>\\s*(.*)$", Pattern.DOTALL | Pattern.MULTILINE);

  /** doctype pattern */
  private static Pattern doctypeHeader = Pattern.compile("<!DOCTYPE [^>]+>\\s*(.*)$", Pattern.DOTALL | Pattern.MULTILINE);

  /**
   * parse a string to object 
   * @param string
   * @param requireHtmlHeader means that the html and body tags are required
   * @return object
   */
  public Object parseXhtmlString(String string) {
    try {
      // process xml
      //getting this error, cant get rid of it
      //Server returned HTTP response code: 503 for URL: http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd
      //strip headers
      //<?xml version='1.0' encoding='iso-8859-1'?>
      Matcher matcher = xmlHeader.matcher(string);
      if (matcher.matches()) {
        string = matcher.group(1);
      }
      //<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
      matcher = doctypeHeader.matcher(string);
      if (matcher.matches()) {
        string = matcher.group(1);
      }
      
      //lets load this into jdom, since it is xml
      StringReader xmlReader = new StringReader(string);

      SAXBuilder saxBuilder = new SAXBuilder();

      Document document = saxBuilder.build(xmlReader);
      return parseDocument(document);
    } catch (Exception e) {
      throw new RuntimeException("Problems parsing string: " + string, e);
    }
  }

  /**
   * parse a document
   * @param document
   * @return
   */
  private Object parseDocument(Document document) {
    Element rootElement = document.getRootElement();

    this.htmlNamespace = rootElement.getNamespace();

    Element bodyElement = null;

    Element mainDiv = null;

    //html, body are optional...
    if (StringUtils.equals(rootElement.getName(), "body")) {
      bodyElement = rootElement;
    } else {

      if (StringUtils.equals(rootElement.getName(), "div")) {
        mainDiv = rootElement;
      } else {
        //must be html
        //this should be "html"
        if (!StringUtils.equals("html", rootElement.getName())) {
          throw new RuntimeException("Expecting html root element, but was: '"
              + rootElement.getName() + "'");
        }
        //get html, body (might be head in there too)
        bodyElement = retrieveExactlyOneChild(rootElement, "body", true);
      }

    }
    //see if found div yet
    if (mainDiv == null) {
      //main div element
      mainDiv = retrieveExactlyOneChild(bodyElement, "div", false);
    }
    //at this point we need a div...
    Object object = parseObject(null, mainDiv, true, "div", true);

    return object;

  }

  /**
   * attributes for divs
   */
  private static Set<String> divAttributes = GrouperUtil.toSet("class", "title");

  /**
   * attributes for divs
   */
  private static Set<String> rootDivAttributes = GrouperUtil.toSet("title");

  /**
   * attributes for lis
   */
  private static Set<String> liAttributes = GrouperUtil.toSet("title");

  /**
   * attributes for ps
   */
  private static Set<String> pAttributes = GrouperUtil.toSet("class");

  /**
   * attributes for ul
   */
  private static Set<String> ulAttributes = GrouperUtil.toSet("class");

  /**
   * attributes for ps
   */
  private static Set<String> liScalarAttributes = new HashSet<String>();

  /**
   * attributes for ul
   */
  private static Set<String> ulSubElements = GrouperUtil.toSet("li");

  /**
   * attributes for ps
   */
  private static Set<String> scalarSubElements = new HashSet<String>();

  /**
   * elements for lis or divs
   */
  private static Set<String> objectSubElements = GrouperUtil.toSet("p", "ul", "div");

  /**
   * parse an object from a div or li, and assign to parent if there
   * @param parent is the paren
   * @param element is the element to parse
   * @param errorIfProblem is if exception should be thrown if problem with type or parent setter
   * @param expectedElementName is the element name we are expecting (li or div)
   * @param rootDiv true if root div since that has different attributes
   * @return the object
   */
  @SuppressWarnings( { "cast", "unchecked" })
  private Object parseObject(Object parent, Element element, boolean errorIfProblem,
      String expectedElementName, Boolean rootDiv) {

    //validate attributes
    if (StringUtils.equals(element.getName(), "div")
        && StringUtils.equals("div", expectedElementName)) {
      if (rootDiv) {
        if (!validateAttributes(element, rootDivAttributes, errorIfProblem)) {
          return null;
        }
      } else {
        if (!validateAttributes(element, divAttributes, errorIfProblem)) {
          return null;
        }
      }
    } else if (StringUtils.equals(element.getName(), "li")
        && StringUtils.equals("li", expectedElementName)) {
      if (!validateAttributes(element, liAttributes, errorIfProblem)) {
        return null;
      }
    } else {
      if (errorIfProblem) {
        throw new RuntimeException("Expecting '" + expectedElementName + "', but was "
            + element.getName());
      }
      return null;
    }
    //get the type of the object, is in title
    String className = element.getAttributeValue("title");

    Class<?> type = null;

    if (!StringUtils.isBlank(className)) {
      type = retrieveClass(className, errorIfProblem);
    }

    boolean hasChildren = validateElements(element, objectSubElements);

    //if no children, nothing to do
    if (!hasChildren) {
      return null;
    }

    //instantiate
    Object object = GrouperUtil.newInstance(type);

    //now we need to parse all the fields
    List<Element> childElements = (List<Element>) element.getChildren();

    for (Element childElement : childElements) {
      String childElementName = childElement.getName();
      if (StringUtils.equals("div", childElementName)) {
        //this is an object, so assign
        this.parseObject(object, childElement, false, "div", false);
      } else if (StringUtils.equals("p", childElementName)) {
        this.parseScalar(object, childElement, true);
      } else if (StringUtils.equals("ul", childElementName)) {
        this.parseList(object, childElement);
      }
      //note, if none of these, then already warned
    }

    if (parent != null) {
      //get the field name
      String fieldName = element.getAttributeValue("class");

      //get setter.  if this fails, that is an error
      GrouperUtil.assignSetter(parent, fieldName, object, true);
    }

    return object;
  }

  /**
   * parse a child list (ul tag).  If no class attribute, just ignore.
   * If there are child elements, then it is ignored
   * @param object to assign to
   * @param element
   * @param assignField is true for p tags, and false for li tags
   */
  @SuppressWarnings("unchecked")
  private void parseList(Object object, Element element) {
    //if doesnt have "class", then that is a problem
    if (!this.validateAttributes(element, ulAttributes, false)) {
      return;
    }
    //validate subelements, shouldnt be any
    //validate so there are warnings

    if (!this.validateElements(element, ulSubElements)) {
      return;
    }
    //get the field name
    String fieldName = element.getAttributeValue("class");

    //get the children
    List<Element> liChildren = element.getChildren("li", this.htmlNamespace);

    Method setter = GrouperUtil.setter(object.getClass(), fieldName, true, true);
    Class setterParamType = setter.getParameterTypes()[0];
    Class arrayType = setterParamType.getComponentType();
    //init array based on how many li's
    Object array = Array.newInstance(arrayType, liChildren.size());

    int index = 0;
    for (Element liElement : liChildren) {
      Object currentValue = null;
      //see if scalar or not (has a title attribute?)
      if (arrayType.getName().startsWith("java") || !arrayType.getName().contains(".")) {
        currentValue = parseScalar(null, liElement, false);
      } else {
        currentValue = parseObject(null, liElement, false, "li", null);
      }
      if (currentValue != null) {
        //type cast
        currentValue = GrouperUtil.typeCast(currentValue, arrayType);
        //assign to array
        Array.set(array, index++, currentValue);
      }
    }

    //get setter.  if this fails, that is an error
    GrouperUtil.assignSetter(object, fieldName, array, true);

  }

  /**
   * parse a child element (p or li tag).  If no class attribute, just ignore.
   * however, if the type of the property is not int or string, then 
   * there is a problem.  If there are child elements, then it is ignored
   * @param object to assign to
   * @param element
   * @param assignField is true for p tags, and false for li tags
   * @return the value or null if there is a problem
   */
  private String parseScalar(Object object, Element element, boolean assignField) {
    //if doesnt have "class", then that is a problem
    if (assignField && !this.validateAttributes(element, pAttributes, false)) {
      return null;
    }
    if (!assignField && !this.validateAttributes(element, liScalarAttributes, false)) {
      //this should never happen, no required attributes
      return null;
    }
    //validate subelements, shouldnt be any
    if (GrouperUtil.length(element.getChildren()) != 0) {
      //validate so there are warnings
      this.validateElements(element, scalarSubElements);
      return null;
    }
    //get the field name
    String fieldName = null;

    if (assignField) {
      fieldName = element.getAttributeValue("class");
    }

    //get the value, but trim to null?
    String value = StringUtils.trimToNull(element.getTextTrim());

    if (assignField) {
      //get setter.  if this fails, that is an error
      GrouperUtil.assignSetter(object, fieldName, value, true);
    }

    return value;

  }

  /**
   * make sure that an element has required attributes, and if others, then log
   * @param element
   * @param requiredAttributes (dont pass null, pass empty set if none
   * @param errorIfMissingRequired 
   * @return if has no problem (true) or if has problem (false)
   */
  @SuppressWarnings("unchecked")
  private boolean validateAttributes(Element element, Set<String> requiredAttributes,
      boolean errorIfMissingRequired) {
    for (String requiredAttribute : requiredAttributes) {
      if (element.getAttribute(requiredAttribute) == null) {
        String error = "Element '" + element.getName()
            + "' is missing required attribute: '" + requiredAttribute + "'";
        if (errorIfMissingRequired) {
          throw new RuntimeException(error);
        }
        this.warnings.append(error).append("\n");
        return false;
      }
    }
    //now see if any are there which shouldnt and warn
    for (Attribute attribute : GrouperUtil.nonNull((List<Attribute>) element
        .getAttributes())) {
      if (!requiredAttributes.contains(attribute.getName())) {
        this.warnings.append("Element '").append(element.getName()).append(
            "' is not expecting attribute: '" + attribute.getName() + "'").append("\n");
      }
    }
    return true;
  }

  /**
   * make sure that an element has only allowed children, if not, log
   * @param element
   * @param allowedChildren (dont pass null, pass empty set if none)
   * @return if this element has any allowed children, or empty
   */
  @SuppressWarnings( { "unchecked", "cast" })
  private boolean validateElements(Element element, Set<String> allowedChildren) {

    List<Element> childElements = (List<Element>) element.getChildren();

    //see if empty
    if (GrouperUtil.length(childElements) == 0) {
      return false;
    }
    int allowedChildrenCount = 0;
    for (Element childElement : childElements) {
      if (!allowedChildren.contains(childElement.getName())) {
        this.warnings.append("Element '").append(element.getName()).append(
            "' is not expecting child element: '").append(childElement.getName()).append(
            "'\n");
      } else {
        allowedChildrenCount++;
      }
    }
    return allowedChildrenCount > 0;
  }

  /**
   * make sure this element has one and only one child, and it has a certain name.  If more elements,
   * log warning, but it must have one with this name
   * @param element
   * @param childName
   * @param allowOthers true if others are allowed, not just the one we are looking for
   * @return the child element
   */
  @SuppressWarnings("unchecked")
  private Element retrieveExactlyOneChild(Element element, String childName,
      boolean allowOthers) {
    List<Element> children = element.getChildren();

    int length = GrouperUtil.length(children);

    if (length > 1 && !allowOthers) {
      this.warnings.append("Element '").append(element.getName()).append(
          "' should have one child '").append(childName).append("', but instead has ")
          .append(length).append(" children.  \n");

      Set<String> childSet = new LinkedHashSet<String>();
      for (Element child : children) {
        if (!StringUtils.equals(childName, child.getName())) {
          childSet.add(child.getName());
        }
      }
      this.warnings.append("Element '").append(element.getName()).append(
          "' should only have children named '").append(childName).append(
          "' but instead has ");
      for (String childInvalidName : childSet) {
        this.warnings.append(childInvalidName).append(", ");
      }
      this.warnings.append("\n");
      //get the first one
      return element.getChild(childName, this.htmlNamespace);
    }
    //if one element, and right name
    children = element.getChildren(childName, this.htmlNamespace);

    if (children.size() == 1) {
      return children.get(0);
    } else if (children.size() > 1) {
      throw new RuntimeException("Looking in element: '" + element.getName()
          + "' for child" + " with name: '" + childName + "', but found multiple ("
          + children.size() + ".  ");
    }

    throw new RuntimeException("Looking in element: '" + element.getName()
        + "' for child" + " with name: '" + childName + "', but cant find it.  ");
  }

  /**
   * based on className which can be fully qualified or not, 
   * return the class object (and cache this)
   * @param className
   * @param errorIfProblem 
   * @return the class
   */
  public Class<?> retrieveClass(String className, boolean errorIfProblem) {
    
    if (this.classLookup.containsKey(className)) {
      return this.classLookup.get(className);
    }
    
    if (errorIfProblem) {
      return WsRestClassLookup.retrieveClassBySimpleName(className);
    }
    return WsRestClassLookup.retrieveClassBySimpleName(className, this.warnings);
  }
  
  /**
   * get warnings, will return the empty string if none (never null)
   * @return the warnings
   */
  public String getWarnings() {
    return this.warnings.toString();
  }

}
