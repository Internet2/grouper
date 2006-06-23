/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.session;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;

/**
 * Factory for returning Grouper sessions.
 * <p />
 * @author  blair christensen.
 * @version $Id: SessionFactory.java,v 1.1 2006-06-23 17:30:10 blair Exp $
 */
public class SessionFactory {

  /*
   * PRIVATE CLASS VARIABLES
   */

  // DESIGN I can't say that I'm fond of this
  private static GrouperSession s;


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Return a {@link GrouperSession}.
   * <p>
   * Currently a single, root Grouper session is created and then
   * shared between all applications callling this method.
   * </p>
   * <pre class="eg">
   * // Return a shared, root Grouper session.
   * GrouperSession = SessionFactory.getSession();
   * </pre>
   * @return  A Grouper session.
   */
  public static GrouperSession getSession() {
    if (s != null) {
      return s;
    }
    // Start session
    try {
      s = GrouperSession.start( SubjectFinder.findById("GrouperSystem") );  
      return s;
    } 
    catch (Exception e) {
      throw new RuntimeException(
        "Unable to start session: " + e.getMessage()
      );
    }
  } // public static GrouperSession getSession()

}

