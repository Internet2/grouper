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
 * Exception thrown when an attribute def name cannot be added to the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AttributeDefNameAddException.java,v 1.1 2009-06-24 06:22:24 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class AttributeDefNameAddException extends RuntimeException {
  
  /**
   * 
   */
  public AttributeDefNameAddException() { 
    super(); 
  }
  
  /**
   * 
   * @param msg
   */
  public AttributeDefNameAddException(String msg) { 
    super(msg); 
  }
  
  /**
   * 
   * @param msg
   * @param cause
   */
  public AttributeDefNameAddException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  
  /**
   * 
   * @param cause
   */
  public AttributeDefNameAddException(Throwable cause) { 
    super(cause); 
  }
}

