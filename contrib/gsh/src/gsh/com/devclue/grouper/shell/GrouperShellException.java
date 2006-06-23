/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;

/**
 * Generic {@link GrouperShell} exception.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperShellException.java,v 1.1 2006-06-23 17:30:09 blair Exp $
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

