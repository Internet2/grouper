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
 * Exception thrown when there is a problem with a Grouper Session.
 * <p />
 * @author  blair christensen.
 * @version $Id: SessionException.java,v 1.3 2006-02-03 19:38:53 blair Exp $
 */
public class SessionException extends Exception {
  public SessionException() { 
    super(); 
  }
  public SessionException(String msg) { 
    super(msg); 
  }
  public SessionException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  public SessionException(Throwable cause) { 
    super(cause); 
  }
}

