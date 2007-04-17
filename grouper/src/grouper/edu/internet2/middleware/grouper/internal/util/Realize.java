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

package edu.internet2.middleware.grouper.internal.util;
import  edu.internet2.middleware.grouper.GrouperRuntimeException;
import  java.lang.reflect.*;

/**
 * Reflectively instantiate classes.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Realize.java,v 1.2 2007-04-17 17:35:00 blair Exp $
 * @since   1.2.0
 */
public class Realize {

  // PUBLIC CLASS METHODS //

  /**
   * @since   1.2.0
   */ 
  public static Object instantiate(String name)
    throws  GrouperRuntimeException
  {
    try {
      Class       classType   = Class.forName(name);
      Class[]     paramsClass = new Class[] { };
      Constructor con         = classType.getDeclaredConstructor(paramsClass);
      Object[]    params      = new Object[] { };
      return con.newInstance(params);
    }
    catch (Exception e) {
      throw new GrouperRuntimeException( "cannot instantiate (" + name + "): " + e.getMessage(), e );
    }
  } 

} 

