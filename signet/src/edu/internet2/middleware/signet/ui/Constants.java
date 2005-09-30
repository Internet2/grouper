/*--
$Id: Constants.java,v 1.13 2005-09-30 17:52:46 acohen Exp $
$Date: 2005-09-30 17:52:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.signet.Category;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.tree.Tree;

public final class Constants
{

  /**
   * The package name for this application.
   */
  public static final String PACKAGE
    = "edu.internet2.middleware.signet.ui";


  /**
   * The token representing "failure" for this application.
   */
  public static final String FAILURE = "failure";


  /**
   * The token representing "success" for this application.
   */
  public static final String SUCCESS = "success";


  /**
   * The token representing the discovery of duplicate Assignments for this
   * application.
   */
  public static final String DUPLICATE_ASSIGNMENTS = "duplicateAssignments";


  /**
   * The token representing the discovery of data-entry errors for this
   * application.
   */
  public static final String DATA_ENTRY_ERRORS = "dataEntryErrors";


  /**
   * The application scope attribute under which our user database
   * is stored.
   */
  public static final String DATABASE_KEY = "database";


  /**
   * The session scope attribute under which the Subscription object
   * currently selected by our logged-in User is stored.
   */
  public static final String SUBSCRIPTION_KEY = "subscription";


  /**
   * The session scope attribute under which the User object
   * for the currently logged in user is stored.
   */
  public static final String USER_KEY = "user";


  /**
   * A static message in case database resource is not loaded.
   */
  public static final String ERROR_DATABASE_NOT_LOADED =
      "ERROR:  User database not loaded -- check servlet container logs for error messages.";


  /**
   * A static message in case message resource is not loaded.
   */
  public static final String ERROR_MESSAGES_NOT_LOADED =
      "ERROR:  Message resources not loaded -- check servlet container logs for error messages.";


  /**
   * The request attributes key under the WelcomeAction stores an ArrayList
   * of error messages, if required resources are missing.
   */
  public static final String ERROR_KEY = "ERROR";


  public static final String EFFECTIVE_DATE_PREFIX = "effectiveDate";
  public static final String EXPIRATION_DATE_PREFIX = "expirationDate";
  
  public static final String SUBSYSTEM_SELECTNAME = "subsystem";
  public static final String SUBSYSTEM_PROMPTVALUE = "__subsystem_prompt_value";
  public static final String SUBSYSTEM_ATTRNAME = "currentSubsystemAttr";
  
  public static final String LOGGEDINUSER_ATTRNAME = "loggedInPrivilegedSubject";
  
  public static final String GRANTEE_ATTRNAME = "currentGranteePrivilegedSubject";
  
  public static final String PROXY_ATTRNAME = "currentProxy";
  public static final String DUP_PROXIES_ATTRNAME = "duplicateProxies";
  
  public static final String PRIVDISPLAYTYPE_ATTRNAME = "privDisplayTypeAttr";
  public static final String PRIVDISPLAYTYPE_HTTPPARAMNAME="privDisplayType";
  
  public static final String SUBJECT_SELECTLIST_ID = "subjectSelectList";
  public static final String ACTING_FOR_SELECT_ID = "selectActingFor";
  
  public static final String COMPOSITE_ID_DELIMITER = ":";
  
  public static final String ACTAS_BUTTON_NAME = "actAsButton";
  public static final String ACTAS_BUTTON_ID = "actAsButton";
  
  public static final String HOMEPAGE_NAME = "My Overview";
  
  public static final Subsystem WILDCARD_SUBSYSTEM
    = new Subsystem()
      {
        public String getId() {return null;}
        public Set getCategories() {return null;}
        public Category getCategory(String categoryId){return null;}
        public Set getFunctions() {return null;}
        public void setFunctionsArray(Function[] categories) {}
        public Function getFunction(String functionId){return null;}
        public Tree getTree() {return null;}
        public void setTree(Tree tree) {}
        public void add(Category category) {}
        public void add(Limit limit) {}
        public Set getChoiceSets() {return null;}
        public ChoiceSet getChoiceSet(String id){return null;}
        public Map getLimits() {return null;}
        public Limit getLimit(String id){return null;}
        public Map getPermissions() {return null;}
        public Permission getPermission(String id){return null;}
        public void setHelpText(String helpText) {}
        public String getHelpText() {return null;}
        public void save() {}
        public void inactivate() {}
        public Status getStatus() {return null;}
        public Date getCreateDatetime() {return null;}
        public String getName() {return null;}
        public int compareTo(Object o) {return 0;}
  };
}
