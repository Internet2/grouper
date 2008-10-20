/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.exception;

/**
 * Exception thrown when an operation cannot be performed due to
 * insufficient privileges.
 * <p/>
 * @author  blair christensen.
 * @version $Id: InsufficientPrivilegeRuntimeException.java,v 1.1 2008-10-20 14:41:20 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class InsufficientPrivilegeRuntimeException extends RuntimeException {

  /**
   * 
   */
  public InsufficientPrivilegeRuntimeException() { 
    super(); 
  }
  /**
   * 
   * @param msg
   */
  public InsufficientPrivilegeRuntimeException(String msg) { 
    super(msg); 
  }
  /**
   * 
   * @param msg
   * @param cause
   */
  public InsufficientPrivilegeRuntimeException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  /**
   * 
   * @param cause
   */
  public InsufficientPrivilegeRuntimeException(Throwable cause) { 
    super(cause); 
  }
}

