package edu.internet2.middleware.authzStandardApiServer.ws;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasDefaultResource;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasDefaultResourceContainer;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasDefaultVersionResource;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasDefaultVersionResourceContainer;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasFolder;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasFolderDeleteResponse;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasFolderLookup;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasFolderSaveRequest;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasFolderSaveResponse;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasGroup;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasGroupSearchContainer;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasMeta;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasResponseMeta;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasResultProblem;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasServiceMeta;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasVersionResource;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasVersionResourceContainer;
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
    addAliasClass(AsasDefaultResourceContainer.class);
    addAliasClass(AsasDefaultVersionResource.class);
    addAliasClass(AsasDefaultVersionResourceContainer.class);
    addAliasClass(AsasFolder.class);
    addAliasClass(AsasFolderDeleteResponse.class);
    addAliasClass(AsasFolderLookup.class);
    addAliasClass(AsasFolderSaveRequest.class);
    addAliasClass(AsasFolderSaveResponse.class);
    addAliasClass(AsasGroup.class);
    addAliasClass(AsasGroupSearchContainer.class);
    addAliasClass(AsasMeta.class);
    addAliasClass(AsasResponseMeta.class);
    addAliasClass(AsasResultProblem.class);
    addAliasClass(AsasServiceMeta.class);
    addAliasClass(AsasVersionResource.class);
    addAliasClass(AsasVersionResourceContainer.class);

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
