/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
 * Grouper Runtime Exception.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperRuntimeException.java,v 1.1 2006-06-19 15:17:40 blair Exp $
 * @since   1.0
 */
public class GrouperRuntimeException extends RuntimeException {
  /**
   * @since 1.0
   */
  public GrouperRuntimeException() { 
    super(); 
  }
  /**
   * @since 1.0
   */
  public GrouperRuntimeException(String msg) { 
    super(msg); 
  }
  /**
   * @since 1.0
   */
  public GrouperRuntimeException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  /**
   * @since 1.0
   */
  public GrouperRuntimeException(Throwable cause) { 
    super(cause); 
  }
}

