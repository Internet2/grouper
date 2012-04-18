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

package edu.internet2.middleware.grouper.entity;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/** 
 * Source adapter for Entities
 * <p/>
 * <p>
 * @author  chris hyzer.
 * @version $Id: GrouperSourceAdapter.java,v 1.31 2009-08-12 04:52:21 mchyzer Exp $
 */
public class EntitySourceAdapter extends GrouperSourceAdapter {

  /** instance */
  private static EntitySourceAdapter instance = null;

  /**
   * type of groups to search on
   * @return type of groups
   */
  @Override
  public Set<TypeOfGroup> typeOfGroups() {
    return TypeOfGroup.ENTITY_SET;
  }

  /** types */
  private Set _types  = new LinkedHashSet();
  

  /**
   * Gets the SubjectTypes supported by this source.
   * <pre class="eg">
   * SourceAdapter  sa    = new GrouperSourceAdapter();
   * Set            types = sa.getSubjectTypes();
   * </pre>
   * @return  Subject types supported by this source.
   */
  public Set getSubjectTypes() {
    if (_types.size() != 1) {
      _types.add( SubjectTypeEnum.valueOf("application") );
    }
    return _types;
  } // public Set getSubjectTypes()

  /**
   * instance
   * @return instance
   */
  public static EntitySourceAdapter instance() {
    if (instance == null) {
      synchronized (EntitySourceAdapter.class) {
        if (instance == null) {
          EntitySourceAdapter newInstance = new EntitySourceAdapter();
          newInstance.setId("grouperEntities");
          newInstance.setName("grouperEntities");
          newInstance.addSubjectType("application");
          
          newInstance.addInitParam("subjectVirtualAttribute_0_searchAttribute0", "${subject.getName()},${subject.getDescription()}");
          newInstance.addInitParam("sortAttribute0", "name");
          newInstance.addInitParam("searchAttribute0", "searchAttribute0");
          newInstance.addInternalAttribute("searchAttribute0");
          
          instance = newInstance;
          
        }
      }
    }
    return instance;
  }
}

