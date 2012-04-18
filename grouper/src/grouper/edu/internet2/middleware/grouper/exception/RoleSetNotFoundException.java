/*******************************************************************************
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
 ******************************************************************************/
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
 * Exception thrown when a role set is not found within the Groups
 * Registry.
 * <p/>
 * @author  mchyzer.
 * @version $Id: RoleSetNotFoundException.java,v 1.1 2009-09-17 04:19:15 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class RoleSetNotFoundException extends RuntimeException {
  
  /**
   * 
   */
  public RoleSetNotFoundException() { 
    super(); 
  }
  
  /**
   * 
   * @param msg
   */
  public RoleSetNotFoundException(String msg) { 
    super(msg); 
  }
  
  /**
   * 
   * @param msg
   * @param cause
   */
  public RoleSetNotFoundException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  
  /**
   * 
   * @param cause
   */
  public RoleSetNotFoundException(Throwable cause) { 
    super(cause); 
  }
}

