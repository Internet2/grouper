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

package edu.internet2.middleware.grouper.subj;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 * Decorator that provides parameter validation for {@link SubjectResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ValidatingResolver.java,v 1.8 2009-12-30 04:22:57 mchyzer Exp $
 * @since   1.2.1
 */
public class ValidatingResolver extends SubjectResolverDecorator {

  /**
   * param
   */
  private ParameterHelper param;

  /**
   * flush the cache (e.g. for testing)
   */
  public void flushCache() {
    super.getDecoratedResolver().flushCache();
  }


  /**
   * @param resolver 
   * @since   1.2.1
   */
  public ValidatingResolver(SubjectResolver resolver) {
    super(resolver);
    this.param = new ParameterHelper();
  }



  /**
   * @see     SubjectResolver#find(String)
   * @since   1.2.1
   */
  public Subject find(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    this.param.notNullString(id, "null Subject Id"); 
    return super.getDecoratedResolver().find(id);
  }            

  /**
   * @see     SubjectResolver#find(String, String)
   * @since   1.2.1
   */
  public Subject find(String id, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    this.param.notNullString(id, "null Subject Id").notNullString(source, "null Source Id");
    return super.getDecoratedResolver().find(id, source);
  }

  /**
   * @see     SubjectResolver#findAll(String)
   * @since   1.2.1
   */
  public Set<Subject> findAll(String query)
    throws  IllegalArgumentException
  {
    this.param.notNullString(query, "null query string");
    return super.getDecoratedResolver().findAll(query);
  }

  /**
   * @see SubjectResolver#findAll(String, Set)
   */
  public Set<Subject> findAll(String query, Set<Source> sources)
      throws IllegalArgumentException {
    this.param.notNullString(query, "null query string");
    return super.getDecoratedResolver().findAll(query, sources);
  }

  /**
   * @see SubjectResolver#findPage(String, Set)
   */
  public SearchPageResult findPage(String query, Set<Source> sources)
      throws SourceUnavailableException {
    this.param.notNullString(query, "null query string");
    return super.getDecoratedResolver().findPage(query, sources);
  }


  /**
   * @see     SubjectResolver#findAll(String, String)
   * @since   1.2.1
   */
  public Set<Subject> findAll(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    this.param.notNullString(query, "null query string").notNullString(source, "null Source Id");
    return super.getDecoratedResolver().findAll(query, source);
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String)
   * @since   1.2.1
   */
  public Subject findByIdentifier(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    this.param.notNullString(id, "null Subject Id");
    return super.getDecoratedResolver().findByIdentifier(id);
  }            

  /**
   * @see     SubjectResolver#findByIdentifier(String, String)
   * @since   1.2.1
   */
  public Subject findByIdentifier(String id,String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    this.param.notNullString(id, "null Subject Id").notNullString(source, "null Source Id");
    return super.getDecoratedResolver().findByIdentifier(id, source);
  }

  /**
   * @see     SubjectResolver#getSource(String)
   * @since   1.2.1
   */
  public Source getSource(String id) 
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    this.param.notNullString(id, "null Source Id");
    return super.getDecoratedResolver().getSource(id);
  }
 
  /**
   * @see     SubjectResolver#getSources()
   * @since   1.2.1
   */
  public Set<Source> getSources() {
    return super.getDecoratedResolver().getSources();
  }

  /**
   * @see SubjectResolver#findByIdOrIdentifier(String)
   */
  public Subject findByIdOrIdentifier(String id) throws IllegalArgumentException,
      SubjectNotFoundException, SubjectNotUniqueException {
    this.param.notNullString(id, "null Subject Id");
    return super.getDecoratedResolver().findByIdOrIdentifier(id);
  }

  /**
   * @see SubjectResolver#findByIdOrIdentifier(String, String)
   */
  public Subject findByIdOrIdentifier(String id, String source)
      throws IllegalArgumentException, SourceUnavailableException,
      SubjectNotFoundException, SubjectNotUniqueException {
    this.param.notNullString(id, "null Subject Id").notNullString(source, "null Source Id");
    return super.getDecoratedResolver().findByIdOrIdentifier(id, source);
  }

  /**
   * @see SubjectResolver#findAllInStem(String, String)
   */
  public Set<Subject> findAllInStem(String stemName, String query)
      throws IllegalArgumentException {
    this.param.notNullString(query, "null query string");
    this.param.notNullString(stemName, "null stem name");
    return super.getDecoratedResolver().findAllInStem(stemName, query);
  }


  /**
   * @see     SubjectResolver#findPage(String)
   * @since   2.0.2
   */
  public SearchPageResult findPage(String query)
    throws  IllegalArgumentException
  {
    this.param.notNullString(query, "null query string");
    return super.getDecoratedResolver().findPage(query);
  }


  /**
   * @see     SubjectResolver#findPage(String, String)
   * @since   2.0.2
   */
  public SearchPageResult findPage(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    this.param.notNullString(query, "null query string").notNullString(source, "null Source Id");
    return super.getDecoratedResolver().findPage(query, source);
  }


  /**
   * @see SubjectResolver#findPageInStem(String, String)
   */
  public SearchPageResult findPageInStem(String stemName, String query)
      throws IllegalArgumentException {
    this.param.notNullString(query, "null query string");
    this.param.notNullString(stemName, "null stem name");
    return super.getDecoratedResolver().findPageInStem(stemName, query);
  }

  /**
   * @see SubjectResolver#findPageInStem(String, String, Set)
   */
  public SearchPageResult findPageInStem(String stemName, String query, Set<Source> sources)
      throws IllegalArgumentException {
    if (GrouperUtil.length(sources) == 0) {
      throw new RuntimeException("Need to pass in at least one source");
    }
    this.param.notNullString(query, "null query string");
    this.param.notNullString(stemName, "null stem name");
    return super.getDecoratedResolver().findPageInStem(stemName, query, sources);
  }

  /**
   * @see SubjectResolver#findByIdentifiers(Collection)
   */
  public Map<String, Subject> findByIdentifiers(Collection<String> identifiers)
      throws IllegalArgumentException {
    
    this.param.notNullCollectionString(identifiers, "null Subject Identifiers");
    return super.getDecoratedResolver().findByIdentifiers(identifiers);

  }

  /**
   * @see SubjectResolver#findByIdentifiers(Collection, String)
   */
  public Map<String, Subject> findByIdentifiers(Collection<String> identifiers, String source)
      throws IllegalArgumentException, SourceUnavailableException {

    this.param.notNullCollectionString(identifiers, "null Subject Identifiers");
    return super.getDecoratedResolver().findByIdentifiers(identifiers, source);
  }

  /**
   * @see SubjectResolver#findByIds(Collection)
   */
  public Map<String, Subject> findByIds(Collection<String> ids)
      throws IllegalArgumentException {

    this.param.notNullCollectionString(ids, "null Subject Ids");
    return super.getDecoratedResolver().findByIds(ids);

  }
  
  /**
   * @see SubjectResolver#findByIds(Collection, String)
   */
  public Map<String, Subject> findByIds(Collection<String> ids, String source)
      throws IllegalArgumentException, SourceUnavailableException {

    this.param.notNullCollectionString(ids, "null Subject Ids");
    return super.getDecoratedResolver().findByIds(ids, source);

  }
  
  /**
   * @see SubjectResolver#findByIdsOrIdentifiers(Collection)
   */
  public Map<String, Subject> findByIdsOrIdentifiers(Collection<String> idsOrIdentifiers)
      throws IllegalArgumentException {

    this.param.notNullCollectionString(idsOrIdentifiers, "null Subject IdsOrIdentifiers");
    return super.getDecoratedResolver().findByIdsOrIdentifiers(idsOrIdentifiers);
  }
  
  /**
   * @see SubjectResolver#findByIdsOrIdentifiers(Collection, String)
   */
  public Map<String, Subject> findByIdsOrIdentifiers(Collection<String> idsOrIdentifiers, String source)
      throws IllegalArgumentException, SourceUnavailableException {

    this.param.notNullCollectionString(idsOrIdentifiers, "null Subject Ids");
    return super.getDecoratedResolver().findByIdsOrIdentifiers(idsOrIdentifiers, source);
  }

  /**
   * @see SubjectResolver#findByIdsOrIdentifiers(Collection, Set)
   */
  public Map<String, Subject> findByIdsOrIdentifiers(Collection<String> idsOrIdentifiers, Set<Source> sources)
      throws IllegalArgumentException, SourceUnavailableException {

    this.param.notNullCollectionString(idsOrIdentifiers, "null Subject Ids");
    return super.getDecoratedResolver().findByIdsOrIdentifiers(idsOrIdentifiers, sources);
  }


  
}

