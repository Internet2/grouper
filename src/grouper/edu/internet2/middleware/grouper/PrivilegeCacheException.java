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
 * {@link PrivilegeCache} runtime exception.
 * <p/>
 * @author  blair christensen.
 * @version $Id: PrivilegeCacheException.java,v 1.1 2006-08-21 18:46:10 blair Exp $
 * @since   1.1.0
 */
public class PrivilegeCacheException extends GrouperRuntimeException {
  /**
   * @since 1.1.0
   */
  public PrivilegeCacheException() { 
    super(); 
  }
  /**
   * @since 1.1.0
   */
  public PrivilegeCacheException(String msg) { 
    super(msg); 
  }
  /**
   * @since 1.1.0
   */
  public PrivilegeCacheException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  /**
   * @since 1.1.0
   */
  public PrivilegeCacheException(Throwable cause) { 
    super(cause); 
  }

} // public class PrivilegeCacheException extends GrouperRuntimeException()


