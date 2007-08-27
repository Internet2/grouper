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
 * Subject resolution interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SubjectResolver.java,v 1.2 2007-08-27 15:53:53 blair Exp $
 * @since   1.2.1
 */
public interface SubjectResolver {


  /**
   * @return  Subject matching search parameters.
   * @param   id    Subject id to search on.
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  SubjectNotFoundException if no matching subject is found.
   * @throws  SubjectNotUniqueException if more than one matching subject is found.
   * @since   @HEAD
   */
  Subject find(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
            ;

  /**
   * @return  Subject matching search parameters.
   * @param   id    Subject id to search on.
   * @param   type  Subject type to search on.
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  SubjectNotFoundException if no matching subject is found.
   * @throws  SubjectNotUniqueException if more than one matching subject is found.
   * @since   @HEAD
   */
  Subject find(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
            ;

  /**
   * @return  Subject matching search parameters.
   * @param   id      Subject id to search on.
   * @param   type    Subject type to search on.
   * @param   source  Source adapter to search within.
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  SourceUnavailableException if source is unavailable.
   * @throws  SubjectNotFoundException if no matching subject is found.
   * @throws  SubjectNotUniqueException if more than one matching subject is found.
   * @since   @HEAD
   */
  Subject find(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
            ;

  /**
   * @param   query   A source-appropraite query string.
   * @return  All subjects matching <i>query</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   @HEAD
   */
  Set findAll(String query)
    throws  IllegalArgumentException;

  /**
   * @param   query   A source-appropriate query string.
   * @param   source  Restrict query to within this source.
   * @return  All subjects matching <i>query</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  SourceUnavailableException if source is unavailable.
   * @since   @HEAD
   */
  Set findAll(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
            ;

  /**
   * @return  Subject matching search parameters.
   * @param   id    Subject identifier to search on.
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  SubjectNotFoundException if no matching subject is found.
   * @throws  SubjectNotUniqueException if more than one matching subject is found.
   * @since   @HEAD
   */
  Subject findByIdentifier(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
            ;

  /**
   * @return  Subject matching search parameters.
   * @param   id    Subject identifier to search on.
   * @param   type  Subject type to search on.
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  SubjectNotFoundException if no matching subject is found.
   * @throws  SubjectNotUniqueException if more than one matching subject is found.
   * @since   @HEAD
   */
  Subject findByIdentifier(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
            ;

  /**
   * @return  Subject matching search parameters.
   * @param   id      Subject identifier to search on.
   * @param   type    Subject type to search on.
   * @param   source  Source adapter to search within.
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  SourceUnavailableException if source is unavailable.
   * @throws  SubjectNotFoundException if no matching subject is found.
   * @throws  SubjectNotUniqueException if more than one matching subject is found.
   * @since   @HEAD
   */
  Subject findByIdentifier(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
            ;

  /**
   * @return  Subject source identified by <i>id</i>.
   * @throws  IllegalArgumentException if <i>id</i> is null.
   * @throws  SourceUnavailableException if source cannot be returned.
   * @since   1.2.1
   */
  Source getSource(String id) 
    throws  IllegalArgumentException,
            SourceUnavailableException
            ;
  
  /**
   * @return  All Subject sources.
   * @since   1.2.1
   */
  Set<Source> getSources();

  /**
   * @param   subjectType   Only return sources that provide this type.
   * @return  All Subject sources that provide <i>subjectType</i>.
   * @throws  IllegalArgumentException if <i>subjectType</i> is null or invalid.
   * @since   1.2.1
   */
  Set<Source> getSources(String subjectType) throws IllegalArgumentException;
  
}

