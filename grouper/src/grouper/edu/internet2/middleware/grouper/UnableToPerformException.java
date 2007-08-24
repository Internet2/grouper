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


package edu.internet2.middleware.grouper;


/**
 * Exception thrown when an operation cannot be performed.
 * <p/>
 * @author  blair christensen.
 * @version $Id: UnableToPerformException.java,v 1.1 2007-08-24 14:18:15 blair Exp $
 * @since   @HEAD@
 */
public class UnableToPerformException extends GrouperException {


  public UnableToPerformException() { 
    super(); 
  }

  public UnableToPerformException(String msg) { 
    super(msg); 
  }

  public UnableToPerformException(String msg, Throwable cause) { 
    super(msg, cause); 
  }

  public UnableToPerformException(Throwable cause) { 
    super(cause); 
  }

}

