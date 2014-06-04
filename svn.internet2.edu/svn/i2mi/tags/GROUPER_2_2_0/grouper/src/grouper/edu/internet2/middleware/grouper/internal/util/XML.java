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

package edu.internet2.middleware.grouper.internal.util;
import  org.apache.commons.lang.StringEscapeUtils;

/**
 * XML Utility class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: XML.java,v 1.1 2007-05-21 16:16:41 blair Exp $
 * @since   1.2.0
 */
public class XML {

  // PUBLIC CLASS METHODS //

  /**
   * Return string with escaped '&gt;', '&lt;', '&quot;', '&amp;' and '&apos;'.
   * <p/>
   * @since   1.2.0
   */
  public static String escape(String s) {
    return StringEscapeUtils.escapeXml(s);
  } 

} 

