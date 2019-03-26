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

import edu.internet2.middleware.grouper.RegistrySubjectAttribute;

/** 
 * Basic <code>RegistrySubjectAttribute</code> DAO interface.
 * @since   2.4.0.patch
 */
public interface RegistrySubjectAttributeDAO extends GrouperDAO {

  /**
   * @since   2.4.0.patch
   */
  void create(RegistrySubjectAttribute _subjAttribute);

  /**
   * @since   2.4.0.patch
   */
  void delete(RegistrySubjectAttribute _subjAttribute);

  /**
   * @since   2.4.0.patch
   */
  void update(RegistrySubjectAttribute _subjAttribute);

  /**
   * @since 2.4.0.patch
   */
  public RegistrySubjectAttribute find(String subjectId, String attributeName, boolean exceptionIfNotFound);

  /**
   * @since 2.4.0.patch
   */
  public Set<RegistrySubjectAttribute> findByRegistrySubjectId(String subjectId);

} 

