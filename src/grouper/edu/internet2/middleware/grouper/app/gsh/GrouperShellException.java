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
 * @version $Id: GrouperShellException.java,v 1.2 2009-03-15 06:37:23 mchyzer Exp $
 * @since   0.0.1
 */
public class GrouperShellException extends RuntimeException {
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

