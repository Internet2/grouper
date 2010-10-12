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
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute;

/** 
 * Basic <code>ExternalSubjectAttribute</code> DAO interface.
 */
public interface ExternalSubjectAttributeDAO extends GrouperDAO {

  /**
   * delete an external subject and all its attributes
   * @param externalSubjectAttribute 
   */
  void delete(ExternalSubjectAttribute externalSubjectAttribute);

} 

