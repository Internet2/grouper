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
 * $HeadURL:https://svn.apache.org/repos/asf/jakarta/commons/proper/httpclient/trunk/src/java/org/apache/commons/httpclient/cookie/CookieAttributeHandler.java $
 * $Revision: 1.1 $
 * $Date: 2008-11-30 10:57:19 $
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.cookie;

import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.Cookie;

/**
 * Ths interface represents a cookie attribute handler responsible
 * for parsing, validating, and matching a specific cookie attribute, 
 * such as path, domain, port, etc.
 *
 * Different cookie specifications can provide a specific
 * implementation for this class based on their cookie handling
 * rules.
 *
 * @author jain.samit@gmail.com (Samit Jain)
 * 
 * @since 3.1
 */
public interface CookieAttributeHandler {

  /**
   * Parse the given cookie attribute value and update the corresponding
   * {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.Cookie} property.
   *
   * @param cookie {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.Cookie} to be updated
   * @param value cookie attribute value from the cookie response header
   */
  void parse(Cookie cookie, String value)
          throws MalformedCookieException;

  /**
   * Peforms cookie validation for the given attribute value.
   *
   * @param cookie {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.Cookie} to validate
   * @param origin the cookie source to validate against
   * @throws MalformedCookieException if cookie validation fails for this attribute
   */
  void validate(Cookie cookie, CookieOrigin origin)
          throws MalformedCookieException;

  /**
   * Matches the given value (property of the destination host where request is being
   * submitted) with the corresponding cookie attribute.
   *
   * @param cookie {@link edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.Cookie} to match
   * @param origin the cookie source to match against
   * @return <tt>true</tt> if the match is successful; <tt>false</tt> otherwise
   */
  boolean match(Cookie cookie, CookieOrigin origin);

}
