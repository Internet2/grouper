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
 * Validation methods that apply to multiple Grouper classes.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Validator.java,v 1.19 2007-01-04 17:17:45 blair Exp $
 */
class Validator {

  // PROTECTED CLASS METHODS //
  
  // Throw IAE if argument is null
  // @since 1.0
  protected static void argNotNull(Object o, String msg)
    throws  IllegalArgumentException
  {
    if (o == null) {
      throw new IllegalArgumentException(msg);
    }
  } // protected static void argNotNull(o, msg)

  // @since   1.1.0
  protected static boolean isNotNullOrBlank(Object s) {
    return !isNullOrBlank(s);
  } // protected static boolean isNotNullOrBlank(s)

  // @since   1.1.0
  protected static boolean isNullOrBlank(Object s) {
    if (
      (s == null)
      ||
      ( !(s instanceof String) )
      ||
      ( s.equals(GrouperConfig.EMPTY_STRING) )
    )
    {
      return true;
    }
    return false;
  } // protected static boolean isNullOrBlank(s)

  // @since 1.0
  protected static void notNullPerModel(Object o, String msg) 
    throws  ModelException
  {
    if (o == null) {
      throw new ModelException(msg);
    }
  } // protected static void notNullPerModel(o, msg)

  // Throw NPE if value is null
  // @since 1.0
  protected static void valueNotNull(Object o, String msg) 
    throws  NullPointerException
  {
    if (o == null) {
      throw new NullPointerException(msg);
    }   
  } // protected static void valueNotNull(o, msg)

} // class Validator

