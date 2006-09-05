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
 * @author  blair christensen.
 * @version $Id: ModelException.java,v 1.3 2006-09-05 18:25:15 blair Exp $
 */
class ModelException extends Exception {
  // FIXME  I'm really skeptical of this whole tangent.  I wonder if I can - or
  //        should - just get rid of it.
  public ModelException() { 
    super(); 
  }
  public ModelException(String msg) { 
    super(msg); 
  }
  public ModelException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  public ModelException(Throwable cause) { 
    super(cause); 
  }
}

