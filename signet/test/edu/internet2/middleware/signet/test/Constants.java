/*--
$Id: Constants.java,v 1.11 2006-01-18 17:11:59 acohen Exp $
$Date: 2006-01-18 17:11:59 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.signet.Status;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Constants
{
  public static final String SUBSYSTEM_ID
		= "testSubsystemId";
  public static final String SUBSYSTEM_NAME
		= "testSubsystemName";
  public static final String SUBSYSTEM_HELPTEXT
		= "testSubsystemHelptext";
  

  public static final String TREE_ID		= "testTreeId";
  public static final String TREE_NAME 	= "testTreeName";
  
  public static final String CHANGED_SUFFIX = "_CHANGED";
  
  public static final int MAX_CHOICE_SETS = 3;
  public static final int MAX_LIMITS			= 3;
  public static final int MAX_PERMISSIONS = 3;
  public static final int MAX_FUNCTIONS		= 3;
  public static final int MAX_CATEGORIES	= 3;
  public static final int MAX_SUBJECTS    = 3;
  public static final int MAX_TREE_DEPTH 	= 3;
  public static final int MAX_TREE_WIDTH 	= 3;
  
  public static final String DELIMITER = "_";
  
  public static final Date TODAY
    = Common.getDate(0);
  public static final Date YESTERDAY
    = Common.getDate(-1);
  public static final Date TOMORROW
    = Common.getDate(1);
  public static final Date DAY_BEFORE_YESTERDAY
    = Common.getDate(-2);
  public static final Date DAY_AFTER_TOMORROW
    = Common.getDate(2);
  public static final Date NEXT_WEEK
  = Common.getDate(7);

  public static final boolean ASSIGNMENT_CANUSE = true;
  public static final boolean ASSIGNMENT_CANGRANT  = true;
  
  public static final boolean PROXY_CANUSE  = true;
  public static final boolean PROXY_CANEXTEND = true;
  
  public static final Set STATUS_ACTIVE_OR_PENDING;
  
  static
  {
    STATUS_ACTIVE_OR_PENDING = new HashSet();
    STATUS_ACTIVE_OR_PENDING.add(Status.ACTIVE);
    STATUS_ACTIVE_OR_PENDING.add(Status.PENDING);
  }
}
