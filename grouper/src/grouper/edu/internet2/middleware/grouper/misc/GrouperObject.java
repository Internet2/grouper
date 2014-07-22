/**
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.misc;

import java.util.Set;


/**
 * grouper objects extend this, e.g. groups, stems, attribute def names
 * @author mchyzer
 *
 */
public interface GrouperObject extends GrouperId {

  /**
   * see if this object matches the filter strings
   * @param filterStrings
   * @return true if matches
   */
  public boolean matchesLowerSearchStrings(Set<String> filterStrings);
  
  /**
   * name of object, e.g. a:b:c
   * @return the name of this object
   */
  public String getName();
  
  /**
   * description of object
   * @return description
   */
  public String getDescription();
  
  /**
   * display name of object
   * @return display name
   */
  public String getDisplayName();
}
