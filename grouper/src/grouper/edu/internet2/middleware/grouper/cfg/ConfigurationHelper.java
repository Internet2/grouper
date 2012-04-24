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

package edu.internet2.middleware.grouper.cfg;

/** 
 * Helper class for {@link Configuration} interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ConfigurationHelper.java,v 1.2 2007-08-27 15:53:52 blair Exp $
 * @since   1.2.1
 */
public class ConfigurationHelper {

  /**
   * Validate that <i>property</i> is not null.
   * @throws  IllegalArgumentException if null.
   * @since   1.2.1
   */
  public void validateParamsNotNull(String property) 
    throws  IllegalArgumentException
  {
    if (property == null) {
      throw new IllegalArgumentException("null property");
    }
  }

  /**
   * Validate that <i>property</i> and <i>value</i> are not null.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public void validateParamsNotNull(String property, String value) 
    throws  IllegalArgumentException
  {
    this.validateParamsNotNull(property);
    if (value == null) {
      throw new IllegalArgumentException("null value");
    }
  } 

}

