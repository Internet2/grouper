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
 * $Id: AsasRestClassLookup.java,v 1.9 2009-12-30 04:23:02 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiServer.ws;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasDefaultResource;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasDefaultVersionResource;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasFolder;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasGroup;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasGroupSearch;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasMeta;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasPaging;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasResponseMeta;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasResultProblem;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasVersionResource;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;


/**
 *
 */
public class AsasRestClassLookup {

  /** map of aliases to classes */
  static Map<String, Class<?>> aliasClassMap = Collections
      .synchronizedMap(new HashMap<String, Class<?>>());

  /** add a bunch of xstream aliases */
  static {
    addAliasClass(AsasDefaultResource.class);
    addAliasClass(AsasDefaultVersionResource.class);
    addAliasClass(AsasFolder.class);
    addAliasClass(AsasGroup.class);
    addAliasClass(AsasGroupSearch.class);
    addAliasClass(AsasMeta.class);
    addAliasClass(AsasResponseMeta.class);
    addAliasClass(AsasResultProblem.class);
    addAliasClass(AsasVersionResource.class);
    addAliasClass(AsasPaging.class);
  }

  /**
   * add an alias by class simple name
   * @param theClass
   */
  public static void addAliasClass(Class<?> theClass) {
    synchronized (aliasClassMap) {
      String registerByName = StandardApiServerUtils.structureName(theClass);
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
    if (StandardApiServerUtils.isBlank(simpleClassName)) {
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
