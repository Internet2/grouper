/**
 * Copyright 2017 Internet2
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
package edu.internet2.middleware.grouper.instrumentation;

/**
 * @author shilen
 */
public enum InstrumentationDataBuiltinTypes {

  /**
   * Hits to servlet
   */
  UI_REQUESTS,
  
  /**
   * folder add
   */
  API_STEM_ADD,
  
  /**
   * folder delete
   */
  API_STEM_DELETE,
  
  /**
   * group add
   */
  API_GROUP_ADD,
  
  /**
   * group delete
   */
  API_GROUP_DELETE,
  
  /**
   * membership (not privilege) add (immediate and composite)
   */
  API_MEMBERSHIP_ADD,
  
  /**
   * membership (not privilege) delete (immediate and composite)
   */
  API_MEMBERSHIP_DELETE;
}
