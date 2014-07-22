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
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.internal.dao;
import java.util.Set;

import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;

/** 
 * Basic <code>ExternalSubject</code> DAO interface.
 */
public interface ExternalSubjectDAO extends GrouperDAO {

  /**
   * find all external subjects which have a disabled date which are not disabled
   * @return the set of subjects
   */
  public Set<ExternalSubject> findAllDisabledMismatch();

  /**
   * find all external subjects
   * @return the set of subjects
   */
  public Set<ExternalSubject> findAll();

  /**
   * find an external subject by identifier
   * @param identifier
   * @param exceptionIfNotFound
   * @param queryOptions 
   * @return the external subject or null or exception
   */
  ExternalSubject findByIdentifier(String identifier, boolean exceptionIfNotFound, QueryOptions queryOptions);
  
  /**
   * delete an external subject and all its attributes
   * @param externalSubject 
   */
  void delete(ExternalSubject externalSubject);

  /**
   * insert or update an external subject to the DB
   * @param externalSubject
   */
  void saveOrUpdate( ExternalSubject externalSubject );
} 

