/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;

/**
 * Generic {@link GrouperShell} exception.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperShellException.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
 * @since   0.0.1
 */
public class GrouperShellException extends Exception {
  public GrouperShellException() { 
    super(); 
  }
  public GrouperShellException(String msg) { 
    super(msg); 
  }
  public GrouperShellException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  public GrouperShellException(Throwable cause) { 
    super(cause); 
  }
}

