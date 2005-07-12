/*--
$Id: Constants.java,v 1.3 2005-07-12 23:13:26 acohen Exp $
$Date: 2005-07-12 23:13:26 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

public final class Constants {

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

}
