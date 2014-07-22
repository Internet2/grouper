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
 * Exception thrown when an attribute assign action set is not found within the Groups
 * Registry.
 * <p/>
 * @author  mchyzer.
 * @version $Id: AttributeAssignActionSetNotFoundException.java,v 1.1 2009-10-26 02:26:08 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class AttributeAssignActionSetNotFoundException extends RuntimeException {
  
  /**
   * 
   */
  public AttributeAssignActionSetNotFoundException() { 
    super(); 
  }
  
  /**
   * 
   * @param msg
   */
  public AttributeAssignActionSetNotFoundException(String msg) { 
    super(msg); 
  }
  
  /**
   * 
   * @param msg
   * @param cause
   */
  public AttributeAssignActionSetNotFoundException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  
  /**
   * 
   * @param cause
   */
  public AttributeAssignActionSetNotFoundException(Throwable cause) { 
    super(cause); 
  }
}

