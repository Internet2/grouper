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
import  edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import  edu.internet2.middleware.subject.Source;
import  edu.internet2.middleware.subject.SourceUnavailableException;
import  edu.internet2.middleware.subject.Subject;
import  edu.internet2.middleware.subject.SubjectNotFoundException;
import  edu.internet2.middleware.subject.SubjectNotUniqueException;
import  java.util.Set;


/**
 * Decorator that provides parameter validation for {@link SubjectResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ValidatingResolver.java,v 1.7 2008-08-26 21:11:51 mchyzer Exp $
 * @since   1.2.1
 */
public class ValidatingResolver extends SubjectResolverDecorator {

  private ParameterHelper param;

  /**
   * flush the cache (e.g. for testing)
   */
  public void flushCache() {
    super.getDecoratedResolver().flushCache();
  }


  /**
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
  public Subject find(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    this.param.notNullString(id, "null Subject Id").notNullString(type, "null Subject Type");
    return super.getDecoratedResolver().find(id, type);
  }

  /**
   * @see     SubjectResolver#find(String, String, String)
   * @since   1.2.1
   */
  public Subject find(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    this.param.notNullString(id, "null Subject Id").notNullString(type, "null Subject Type").notNullString(source, "null Source Id");
    return super.getDecoratedResolver().find(id, type, source);
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
   * @see     SubjectResolver#findAll(String, String)
   * @since   1.2.1
   */
  public Set<Subject> findAll(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    this.param.notNullString(query, "null query string").notNullString(source, "null Source Id");
    return super.getDecoratedResolver().findAll(query);
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
  public Subject findByIdentifier(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    this.param.notNullString(id, "null Subject Id").notNullString(type, "null Subject Type");
    return super.getDecoratedResolver().findByIdentifier(id, type);
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String, String, String)
   * @since   1.2.1
   */
  public Subject findByIdentifier(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    this.param.notNullString(id, "null Subject Id").notNullString(type, "null Subject Type").notNullString(source, "null Source Id");
    return super.getDecoratedResolver().findByIdentifier(id, type, source);
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
   * @see     SubjectResolver#getSources(String)
   * @since   1.2.1
   */
  public Set<Source> getSources(String subjectType) 
    throws  IllegalArgumentException
  {
    this.param.notNullString(subjectType, "null Subject Type");
    return super.getDecoratedResolver().getSources(subjectType);
  }

}

