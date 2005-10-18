/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;

/**
 * Exception thrown when an operation cannot be performed due to
 * insufficient privileges.
 * <p />
 * @author  blair christensen.
 * @version $Id: InsufficientPrivilegeException.java,v 1.1.2.1 2005-10-18 20:06:36 blair Exp $
 */
public class InsufficientPrivilegeException extends Exception {
  public InsufficientPrivilegeException() { 
    super(); 
  }
  public InsufficientPrivilegeException(String msg) { 
    super(msg); 
  }
  public InsufficientPrivilegeException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  public InsufficientPrivilegeException(Throwable cause) { 
    super(cause); 
  }
}

