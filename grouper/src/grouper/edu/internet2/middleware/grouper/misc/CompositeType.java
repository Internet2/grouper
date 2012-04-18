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

package edu.internet2.middleware.grouper.misc;
import java.io.Serializable;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Composite Type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CompositeType.java,v 1.4 2009-03-02 07:33:25 mchyzer Exp $    
 * @since   1.0
 */
public enum CompositeType implements Serializable {

  /** the members in the left, which are not in the right (e.g. the right is an excludes list) */
  COMPLEMENT("complement"),

  /** the members in the left, or in the right (right is an includes list) */
  UNION("union"),

  /** the members who are in the left, who are also in the right (right is a required list) */
  INTERSECTION("intersection");

  /**
   * find the value of a string and ignore case
   * @param theName
   */
  public static CompositeType valueOfIgnoreCase(String theName) {
    return GrouperUtil.enumValueOfIgnoreCase(CompositeType.class,theName, false );
  }
  
  /**
   * construct with name
   */
  private CompositeType(String theName) {
    this.name = theName;
  }
  
  /** friendly name of composite */
  private String name;
  
  /**
   * get name of composite type, e.g. complement, union, intersection
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.name;
  }
}

