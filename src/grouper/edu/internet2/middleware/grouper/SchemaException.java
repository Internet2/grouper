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
 * General Groups Registry schema exception.
 * <p />
 * @author  blair christensen.
 * @version $Id: SchemaException.java,v 1.3 2006-02-03 19:38:53 blair Exp $
 */
public class SchemaException extends Exception {
  public SchemaException() { 
    super(); 
  }
  public SchemaException(String msg) { 
    super(msg); 
  }
  public SchemaException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  public SchemaException(Throwable cause) { 
    super(cause); 
  }
}

