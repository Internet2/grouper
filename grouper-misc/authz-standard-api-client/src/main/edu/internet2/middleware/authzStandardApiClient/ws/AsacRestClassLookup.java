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
 * $Id: AsacRestClassLookup.java,v 1.9 2009-12-30 04:23:02 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiClient.ws;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacDefaultResource;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacDefaultResourceContainer;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacDefaultVersionResource;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacDefaultVersionResourceContainer;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacFolder;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacGroup;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacGroupSearchContainer;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacMeta;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacResponseMeta;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacResultProblem;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacServiceMeta;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacVersionResource;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacVersionResourceContainer;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientUtils;


/**
 *
 */
public class AsacRestClassLookup {

  /** map of aliases to classes */
  static Map<String, Class<?>> aliasClassMap = Collections
      .synchronizedMap(new HashMap<String, Class<?>>());

  /** add a bunch of xstream aliases */
  static {
    
    addAliasClass(AsacDefaultResource.class);
    addAliasClass(AsacDefaultResourceContainer.class);
    addAliasClass(AsacDefaultVersionResource.class);
    addAliasClass(AsacDefaultVersionResourceContainer.class);
    addAliasClass(AsacFolder.class);
    addAliasClass(AsacGroup.class);
    addAliasClass(AsacGroupSearchContainer.class);
    addAliasClass(AsacMeta.class);
    addAliasClass(AsacResponseMeta.class);
    addAliasClass(AsacResultProblem.class);
    addAliasClass(AsacServiceMeta.class);
    addAliasClass(AsacVersionResource.class);
    addAliasClass(AsacVersionResourceContainer.class);
  }
  
  /**
   * add an alias by class simple name
   * @param theClass
   */
  public static void addAliasClass(Class<?> theClass) {
    synchronized (aliasClassMap) {
      String registerByName = theClass.getSimpleName();
      if (registerByName.toLowerCase().startsWith("asac")) {
        registerByName = registerByName.substring(4);
      }
      registerByName = StandardApiClientUtils.lowerFirstLetter(registerByName);
      aliasClassMap.put(registerByName, theClass);
    }
  }

  /**
   * find a class object based on simple name
   * @param simpleClassName
   * @return the class object or null if blank
   */
  public static Class<?> retrieveClassBySimpleName(String simpleClassName) {
    //blank is ok
    if (StandardApiClientUtils.isBlank(simpleClassName)) {
      return null;
    }
    Class<?> theClass = aliasClassMap.get(simpleClassName);
    if (theClass != null) {
      return theClass;
    }
    //make a good exception.
    StringBuilder error = new StringBuilder("Cant find class from simple name: '").append(simpleClassName);
    error.append("', expecting one of: ");
    for (String simpleName : aliasClassMap.keySet()) {
      error.append(simpleName).append(", ");
    }
    throw new RuntimeException(error.toString());
  }

  /**
   * map of aliases to classes
   * @return the alias to class map
   */
  public static Map<String, Class<?>> getAliasClassMap() {
    return aliasClassMap;
  }

}
