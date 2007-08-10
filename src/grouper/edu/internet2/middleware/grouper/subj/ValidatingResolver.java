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
 * @version $Id: ValidatingResolver.java,v 1.2 2007-08-10 13:19:14 blair Exp $
 * @since   @HEAD@
 */
public class ValidatingResolver extends SubjectResolverDecorator {


  /**
   * @see     SubjectResolverDecorator(SubjectResolver)
   * @since   @HEAD@
   */
  public ValidatingResolver(SubjectResolver resolver) {
    super(resolver);
  }



  /**
   * @see     SubjectResolver#find(String)
   * @since   @HEAD
   */
  public Subject find(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    // TODO 20070806 DRY w/ SourcesXmlResolver#find(String, String)
    if (id == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Subject Id");
    }
    return super.getDecoratedResolver().find(id);
  }            

  /**
   * @see     SubjectResolver#find(String, String)
   * @since   @HEAD
   */
  public Subject find(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    // TODO 20070806 DRY w/ SourcesXmlResolver#find(String)
    if (id == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Subject Id");
    }
    if (type == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Subject Type");
    }
    return super.getDecoratedResolver().find(id, type);
  }

  /**
   * @see     SubjectResolver#find(String, String, String)
   * @since   @HEAD
   */
  public Subject find(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if (id == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Subject Id");
    }
    if (type == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Subject Type");
    }
    if (source == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Source Id");
    }
    return super.getDecoratedResolver().find(id, type, source);
  }

  /**
   * @see     SubjectResolver#findAll(String)
   * @since   @HEAD
   */
  public Set<Subject> findAll(String query)
    throws  IllegalArgumentException
  {
    if (query == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null query string");
    }
    return super.getDecoratedResolver().findAll(query);
  }

  /**
   * @see     SubjectResolver#findAll(String, String)
   * @since   @HEAD
   */
  public Set<Subject> findAll(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    if (query == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null query string");
    }
    if (source == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Source Id");
    }
    return super.getDecoratedResolver().findAll(query);
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String)
   * @since   @HEAD
   */
  public Subject findByIdentifier(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    // TODO 20070806 DRY w/ SourcesXmlResolver#findByIdentifier(String, String)
    if (id == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Subject Id");
    }
    return super.getDecoratedResolver().findByIdentifier(id);
  }            

  /**
   * @see     SubjectResolver#findByIdentifier(String, String)
   * @since   @HEAD
   */
  public Subject findByIdentifier(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    // TODO 20070806 DRY w/ SourcesXmlResolver#findByIdentifier(String)
    if (id == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Subject Id");
    }
    if (type == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Subject Type");
    }
    return super.getDecoratedResolver().findByIdentifier(id, type);
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String, String, String)
   * @since   @HEAD
   */
  public Subject findByIdentifier(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if (id == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Subject Id");
    }
    if (type == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Subject Type");
    }
    if (source == null) { // TODO 20070806 ParameterHelper
      throw new IllegalArgumentException("null Source Id");
    }
    return super.getDecoratedResolver().findByIdentifier(id, type, source);
  }

  /**
   * @see     SubjectResolver#getSource(String)
   * @since   @HEAD@
   */
  public Source getSource(String id) 
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    if (id == null) { // TODO 20070803 ParameterHelper
      throw new IllegalArgumentException("null source id");
    }
    return super.getDecoratedResolver().getSource(id);
  }
 
  /**
   * @see     SubjectResolver#getSources()
   * @since   @HEAD@
   */
  public Set<Source> getSources() {
    return super.getDecoratedResolver().getSources();
  }

  /**
   * @see     SubjectResolver#getSources(String)
   * @since   @HEAD@
   */
  public Set<Source> getSources(String subjectType) 
    throws  IllegalArgumentException
  {
    if (subjectType == null) { // TODO 20070803 ParameterHelper
      throw new IllegalArgumentException("null SubjectType");
    }
    return super.getDecoratedResolver().getSources(subjectType);
  }

}

