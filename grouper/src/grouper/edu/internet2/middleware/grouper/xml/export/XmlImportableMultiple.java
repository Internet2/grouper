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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import java.util.Collection;


/**
 * Hibernated object which can be imported into.  There can be multiple in the DB
 * based on business key (e.g. milti-assign)
 * @param <T> is the type of the object
 */
public interface XmlImportableMultiple<T> extends XmlImportableBase<T> {

  /**
   * retrieve from db by id or key.  throws exception if duplicate
   * @param idsToIgnore these are ids already processed, do not pick these
   * @return the object or null if not found
   */
  public T xmlRetrieveByIdOrKey(Collection<String> idsToIgnore);
  
}
