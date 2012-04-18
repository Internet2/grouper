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
 * $Id: GrouperSessionWrapper.java,v 1.2 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.j2ee;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.ui.SessionInitialiser;


/**
 * wrap session so we can customize
 */
public class GrouperSessionWrapper implements HttpSession {

  /**
   * wrapped session
   */
  private HttpSession httpSession = null;
  
  /**
   * @param httpSession1 session
   */
  public GrouperSessionWrapper(HttpSession httpSession1) {
    this.httpSession = httpSession1;
  }

  /**
   * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
   */
  public Object getAttribute(String attributeName) {
    
    MultiKey resourceBundleKey = (MultiKey)this.httpSession.getAttribute(SessionInitialiser.RESOURCE_BUNDLE_KEY);
    
    //these cant be in session, so keep them global
    if ("nav".equals(attributeName)) {
      return SessionInitialiser.retrieveLocalizationContext(resourceBundleKey, true);
    }
    if ("navMap".equals(attributeName)) {
      return SessionInitialiser.retrieveMapBundleWrapper(resourceBundleKey, true, false);
    }
    if ("navNullMap".equals(attributeName)) {
      return SessionInitialiser.retrieveMapBundleWrapper(resourceBundleKey, true, true);
    }

    if ("media".equals(attributeName)) {
      return SessionInitialiser.retrieveLocalizationContext(resourceBundleKey, false);
    }
    if ("mediaMap".equals(attributeName)) {
      return SessionInitialiser.retrieveMapBundleWrapper(resourceBundleKey, false, false);
    }
    if ("mediaNullMap".equals(attributeName)) {
      return SessionInitialiser.retrieveMapBundleWrapper(resourceBundleKey, false, true);
    }

    Object object = this.httpSession.getAttribute(attributeName);
    if (object != null) {
//      System.out.println(attributeName + ": " + GrouperUtil.className(object));
    }
    return object;
  }

  /**
   * @see javax.servlet.http.HttpSession#getAttributeNames()
   */
  public Enumeration getAttributeNames() {
    //note: dont include "nav", "media", etc here
    return this.httpSession.getAttributeNames();
  }

  /**
   * @see javax.servlet.http.HttpSession#getCreationTime()
   */
  public long getCreationTime() {
    return this.httpSession.getCreationTime();
  }

  /**
   * @see javax.servlet.http.HttpSession#getId()
   */
  public String getId() {
    return this.httpSession.getId();
  }

  /**
   * @see javax.servlet.http.HttpSession#getLastAccessedTime()
   */
  public long getLastAccessedTime() {
    return this.httpSession.getLastAccessedTime();
  }

  /**
   * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
   */
  public int getMaxInactiveInterval() {
    return this.httpSession.getMaxInactiveInterval();
  }

  /**
   * @see javax.servlet.http.HttpSession#getServletContext()
   */
  public ServletContext getServletContext() {
    return this.httpSession.getServletContext();
  }

  /**
   * @return session context
   * @see javax.servlet.http.HttpSession
   */
  @SuppressWarnings("deprecation")
  public javax.servlet.http.HttpSessionContext getSessionContext() {
    return this.httpSession.getSessionContext();
  }

  /**
   * @param arg0 
   * @return the value
   * @see javax.servlet.http.HttpSession
   */
  @SuppressWarnings("deprecation")
  public Object getValue(String arg0) {
    return this.httpSession.getValue(arg0);
  }

  /**
   * @return value names
   * @see javax.servlet.http.HttpSession
   */
  @SuppressWarnings("deprecation")
  public String[] getValueNames() {
    return this.httpSession.getValueNames();
  }

  /**
   * @see javax.servlet.http.HttpSession#invalidate()
   */
  public void invalidate() {
    this.httpSession.invalidate();
  }

  /**
   * @see javax.servlet.http.HttpSession#isNew()
   */
  public boolean isNew() {
    return this.httpSession.isNew();
  }

  /**
   * @param arg0 
   * @param arg1 
   * @see javax.servlet.http.HttpSession
   */
  @SuppressWarnings("deprecation")
  public void putValue(String arg0, Object arg1) {
    this.httpSession.putValue(arg0, arg1);
  }

  /**
   * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
   */
  public void removeAttribute(String attributeName) {
    this.httpSession.removeAttribute(attributeName);
  }

  /**
   * @param arg0 
   * @see javax.servlet.http.HttpSession
   */
  @SuppressWarnings("deprecation")
  public void removeValue(String arg0) {
    this.httpSession.removeValue(arg0);
  }

  /**
   * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
   */
  public void setAttribute(String attributeName, Object value) {
    this.httpSession.setAttribute(attributeName, value);
  }

  /**
   * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
   */
  public void setMaxInactiveInterval(int arg0) {
    this.httpSession.setMaxInactiveInterval(arg0);
  }

  /**
   * 
   * @return the session
   */
  public HttpSession getHttpSession() {
    return this.httpSession;
  }

}
