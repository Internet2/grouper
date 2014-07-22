/**
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
 */
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

