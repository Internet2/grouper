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
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.spml.request;

import org.openspml.v2.msg.Marshallable;
import org.openspml.v2.msg.MarshallableElement;
import org.openspml.v2.msg.PrefixAndNamespaceTuple;
import org.openspml.v2.util.Spml2Exception;
import org.openspml.v2.util.xml.ObjectFactory;
import org.openspml.v2.util.xml.UnknownSpml2TypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdappcMarshallableCreator implements ObjectFactory.MarshallableCreator {

  private static final Logger LOG = LoggerFactory.getLogger(LdappcMarshallableCreator.class);

  public static final String URI = "http://grouper.internet2.edu/ldappc";

  public static final String PREFIX = "ldappc";

  public static final String pkg = LdappcMarshallableCreator.class.getPackage().getName();

  public LdappcMarshallableCreator() {
  }

  static PrefixAndNamespaceTuple[] staticGetNamespacesInfo() {
    return new PrefixAndNamespaceTuple[] { new PrefixAndNamespaceTuple(PREFIX, URI), };
  }

  public Marshallable createMarshallable(String nameAndPrefix, String uri) throws Spml2Exception {
    if (URI.equals(uri)) {
      Class cls = this.findClass(nameAndPrefix, uri);
      MarshallableElement e = ObjectFactory.getInstance().createMarshallableElement(cls);
      if (!(e instanceof Marshallable)) {
        throw new UnknownSpml2TypeException("Unknown object with nameAndPrefix '" + nameAndPrefix + "' and uri '" + uri
            + "'");
      }
      return (Marshallable) e;
    }
    return null;
  }

  private Class findClass(String nameAndPrefix, String uri) throws Spml2Exception {
    if (URI.equals(uri)) {
      // remove prefix from prefix:name
      String name = nameAndPrefix;
      int idx = name.indexOf(":");
      if (idx != -1) {
        name = name.substring(idx + 1);
      }
      String classname = pkg + "." + this.firstCharToUpper(name);
      try {
        return Class.forName(classname);
      } catch (ClassNotFoundException e) {
        LOG.error("Class Not Found for nameAndPrefix '" + nameAndPrefix + "' and uri '" + uri + "'", e);
        throw new Spml2Exception(e);
      }
    }
    return null;
  }

  private String firstCharToUpper(String name) {
    String fChar = name.substring(0, 1);
    fChar = fChar.toUpperCase();
    return fChar + name.substring(1);
  }

}
