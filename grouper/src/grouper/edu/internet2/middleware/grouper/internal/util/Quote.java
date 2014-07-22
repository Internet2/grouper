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

package edu.internet2.middleware.grouper.internal.util;

/**
 * Utility class for enclosing data in enclosing "quotes" of various types.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Quote.java,v 1.2 2007-04-17 17:35:00 blair Exp $
 * @since   1.2.0
 */
public class Quote {

  // PRIVATE CLASS CONSTANTS //
  private static final String PARENS_CLOSE  = ") ";
  private static final String PARENS_OPEN   = "(";
  private static final String SINGLE_CLOSE  = "'";
  private static final String SINGLE_OPEN   = "'";


  // PUBLIC CLASS METHODS //

  /**
   * Enclose <code>input</code> within parentheses.
   * <p/>
   * @since   1.2.0
   */
  public static String parens(String input) {
    return PARENS_OPEN + input + PARENS_CLOSE;
  }

  /**
   * Enclose <code>input</code> in single quotes.
   * <p/>
   * @since   1.2.0
   */
  public static String single(boolean input) {
    return single( Boolean.toString(input) );
  }

  /**
   * Enclose <code>input</code> in single quotes.
   * <p/>
   * @since   1.2.0
   */
  public static String single(String input) {
    return SINGLE_OPEN + input + SINGLE_CLOSE;
  }
 
} 

