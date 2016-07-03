/**
 * Copyright 2014 Internet2
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
 * Exception thrown when an attribute def scope is not found within the Groups
 * Registry.
 * 
 * @author  mchyzer.
 * @version $Id: AttributeDefScopeNotFoundException.java,v 1.1 2009-06-29 15:58:24 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class AttributeDefScopeNotFoundException extends RuntimeException {
  
  /**
   * 
   */
  public AttributeDefScopeNotFoundException() { 
    super(); 
  }
  
  /**
   * 
   * @param msg
   */
  public AttributeDefScopeNotFoundException(String msg) { 
    super(msg); 
  }
  
  /**
   * 
   * @param msg
   * @param cause
   */
  public AttributeDefScopeNotFoundException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  
  /**
   * 
   * @param cause
   */
  public AttributeDefScopeNotFoundException(Throwable cause) { 
    super(cause); 
  }
}

