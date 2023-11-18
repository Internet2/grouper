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
package edu.internet2.middleware.subject;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.internet2.middleware.subject.provider.SubjectStatusConfig;



/**
 * Source to find and get Subjects and their attributes.
 * You should probably extend BaseSourceAdapter instead of implement this interface
 */
public interface Source {

  /**
   * start logging the source low level actions
   */
  public void loggingStart();

  /**
   * stop logging and get the output
   */
  public String loggingStop();
  
  /**
   * get all subject ids
   * @return all subjectIds
   * @throws UnsupportedOperationException if not implemented
   */
  public Set<String> retrieveAllSubjectIds();
  
  /**
   * get the config bean for this source
   * @return the config bean for this source
   */
  public SubjectStatusConfig getSubjectStatusConfig();
  
  /**
   * Returns the ID of this source.
   * @return id
   */
  public String getId();

  /**
   * Sets the ID of this source.
   * @param id 
   */
  public void setId(String id);
  
  /**
   * Returns the name of this source.
   * @return name
   */
  public String getName();

  /**
   * Sets the name of this source.
   * @param name 
   */
  public void setName(String name);

  /**
   * Gets the SubjectTypes supported by this source.
   * @return set
   */
  public Set<SubjectType> getSubjectTypes();
  
  /**
   * Gets a Subject by its ID.
   * @param id 
   * @return  subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   * @deprecated use the overload instead
   */
  @Deprecated
  public Subject getSubject(String id)
    throws SubjectNotFoundException,SubjectNotUniqueException;

  /**
   * Gets a Subject by other well-known identifiers, aside
   * from the subject ID, for example, login ID.
   * @param id 
   * @return subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   * @deprecated use the overload instead
   */
  @Deprecated
  public Subject getSubjectByIdentifier(String id)
    throws SubjectNotFoundException,SubjectNotUniqueException;
  
  /**
   * Gets a Subject by its ID.
   * @param id 
   * @param exceptionIfNull if SubjectNotFoundException should be 
   * throws if the subject is null, or if null should be returned
   * @return subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   */
  public Subject getSubject(String id, boolean exceptionIfNull)
    throws SubjectNotFoundException,SubjectNotUniqueException;

  /**
   * Get subjects by ids.  Note, if the subjects arent found or arent unique, 
   * they wont be returned.  Duplicates are ok on the input, but will return one output.
   * @param ids
   * @return a map of results never null indexed by the id
   */
  public Map<String, Subject> getSubjectsByIds(Collection<String> ids);

  /**
   * Get subjects by identifiers.  Note, if the subjects arent found or arent unique, 
   * they wont be returned.  Identifiers are unique ways to refer to a subject that
   * isnt its id (e.g. netid).  Duplicates are ok on the input, but will return one output.
   * @param identifiers
   * @return a map of results never null indexed by the identifiers
   */
  public Map<String, Subject> getSubjectsByIdentifiers(Collection<String> identifiers);

  /**
   * Get subjects by ids or identifiers.  Note, if the subjects arent found or arent unique, 
   * they wont be returned.  Identifiers are unique ways to refer to a subject that
   * isnt its id (e.g. netid).  Duplicates are ok on the input, but will return one output.
   * 
   * @param idsOrIdentifiers each string could be a subject id or identifier
   * @return a map of results never null indexed by the id or identifier that was passed in.
   * Note, the same subject could be returned twice if looked up by id and identifier (two inputs)
   */
  public Map<String, Subject> getSubjectsByIdsOrIdentifiers(Collection<String> idsOrIdentifiers);

  /**
   * find by id or identifier.  pass in either an id or an identifier
   * @param idOrIdentifier
   * @param exceptionIfNull if SubjectNotFoundException or null
   * @return the subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   */
  public Subject getSubjectByIdOrIdentifier(String idOrIdentifier, boolean exceptionIfNull) 
      throws SubjectNotFoundException, SubjectNotUniqueException;

  
  /**
   * Gets a Subject by other well-known identifiers, aside
   * from the subject ID, for example, login ID.
   * @param id 
   * @param exceptionIfNull 
   * @return subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   */
  public Subject getSubjectByIdentifier(String id, boolean exceptionIfNull)
    throws SubjectNotFoundException,SubjectNotUniqueException;
  
  /**
   * Unstructured search for Subjects. Each implementation
   * utilizes its own search algorithm tailored to
   * the Subject repository and schema.  Note if config param:
   * throwErrorOnFindAllFailure is false, then swallow and log exceptions
   * @param searchValue 
   * @return set
   */
  public Set<Subject> search(String searchValue);
  
  /**
   * Called by SourceManager when it loads this source.
   * @throws SourceUnavailableException 
   */
  public void init()
    throws SourceUnavailableException;

  /**
   * make sure the config is ok, and log descriptive errors if not
   */
  public void checkConfig();

  /**
   * in the startup on this i2mi app, print helpful and brief info about this source
   * @return the info
   */
  public String printConfig();

  /**
   * get the init param e.g. from the subject.properties
   * @param name1
   * @return param
   */
  public String getInitParam(String name1);

  /**
   * get the init params e.g. from the subject.properties
   * @return params
   */
  public Properties initParams();
  
  /**
   * Get a set of attributes that are marked as being internal attributes.
   * 
   * Note, these will be in lower case
   * 
   * @return set to lower case
   */
  public Set<String> getInternalAttributes();
  
  /**
   * Get the names of attributes that are subject identifiers.  This only returns the first
   * @return subject identifiers
   */
  public Map<Integer, String> getSubjectIdentifierAttributes();
  
  /**
   * Get all the names of attributes that are subject identifiers.
   * @return subject identifiers
   */
  public Map<Integer, String> getSubjectIdentifierAttributesAll();
  
  /**
   * Get the names of attributes used for sorting.
   * @return sort attributes in lower case
   */
  public Map<Integer, String> getSortAttributes();
  
  /**
   * Get the names of attributes used for searching.
   * @return search attributes in lower case
   */
  public Map<Integer, String> getSearchAttributes();
  
  /**
   * Unstructured search for Subjects. Each implementation
   * utilizes its own search algorithm tailored to
   * the Subject repository and schema.  Note if config param:
   * throwErrorOnFindAllFailure is false, then swallow and log exceptions
   * if maxPageSize is set in subject.properties, then only return max that many, and
   * if there are more, set the tooManyResults flag
   * @param searchValue 
   * @return results and if there are too many, never return null!!!
   */
  public SearchPageResult searchPage(String searchValue);
  
  /**
   * Unstructured search for Subjects. Each implementation
   * utilizes its own search algorithm tailored to
   * the Subject repository and schema.  Note if config param:
   * throwErrorOnFindAllFailure is false, then swallow and log exceptions
   * if maxPageSize is set in subject.properties, then only return max that many, and
   * if there are more, set the tooManyResults flag
   * @param realm string value that sets the realm for the search.  The source can
   * implement various realms to account for permissions of the calling user
   * @param searchValue 
   * @return results and if there are too many, never return null!!!
   */
  public SearchPageResult searchPage(String searchValue, String realm);

  /**
   * Gets a Subject by its ID.
   * @param id 
   * @param realm string value that sets the realm for the search.  The source can
   * implement various realms to account for permissions of the calling user
   * @param exceptionIfNull if SubjectNotFoundException should be 
   * throws if the subject is null, or if null should be returned
   * @return subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   */
  public Subject getSubject(String id, boolean exceptionIfNull, String realm)
    throws SubjectNotFoundException,SubjectNotUniqueException;

  /**
   * Gets a Subject by other well-known identifiers, aside
   * from the subject ID, for example, login ID.
   * @param id 
   * @param exceptionIfNull 
   * @param realm string value that sets the realm for the search.  The source can
   * implement various realms to account for permissions of the calling user
   * @return subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   */
  public Subject getSubjectByIdentifier(String id, boolean exceptionIfNull, String realm)
    throws SubjectNotFoundException,SubjectNotUniqueException;

  /**
   * find by id or identifier.  pass in either an id or an identifier
   * @param idOrIdentifier
   * @param exceptionIfNull if SubjectNotFoundException or null
   * @param realm string value that sets the realm for the search.  The source can
   * implement various realms to account for permissions of the calling user
   * @return the subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   */
  public Subject getSubjectByIdOrIdentifier(String idOrIdentifier, boolean exceptionIfNull, String realm) 
      throws SubjectNotFoundException, SubjectNotUniqueException;

  /**
   * Get subjects by identifiers.  Note, if the subjects arent found or arent unique, 
   * they wont be returned.  Identifiers are unique ways to refer to a subject that
   * isnt its id (e.g. netid).  Duplicates are ok on the input, but will return one output.
   * @param identifiers
   * @param realm string value that sets the realm for the search.  The source can
   * implement various realms to account for permissions of the calling user
   * @return a map of results never null indexed by the identifiers
   */
  public Map<String, Subject> getSubjectsByIdentifiers(Collection<String> identifiers, String realm);

  /**
   * Get subjects by ids.  Note, if the subjects arent found or arent unique, 
   * they wont be returned.  Duplicates are ok on the input, but will return one output.
   * @param ids
   * @param realm string value that sets the realm for the search.  The source can
   * implement various realms to account for permissions of the calling user
   * @return a map of results never null indexed by the id
   */
  public Map<String, Subject> getSubjectsByIds(Collection<String> ids, String realm);

  /**
   * Get subjects by ids or identifiers.  Note, if the subjects arent found or arent unique, 
   * they wont be returned.  Identifiers are unique ways to refer to a subject that
   * isnt its id (e.g. netid).  Duplicates are ok on the input, but will return one output.
   * 
   * @param idsOrIdentifiers each string could be a subject id or identifier
   * @param realm string value that sets the realm for the search.  The source can
   * implement various realms to account for permissions of the calling user
   * @return a map of results never null indexed by the id or identifier that was passed in.
   * Note, the same subject could be returned twice if looked up by id and identifier (two inputs)
   */
  public Map<String, Subject> getSubjectsByIdsOrIdentifiers(Collection<String> idsOrIdentifiers, String realm);

  /**
   * Unstructured search for Subjects. Each implementation
   * utilizes its own search algorithm tailored to
   * the Subject repository and schema.  Note if config param:
   * throwErrorOnFindAllFailure is false, then swallow and log exceptions
   * @param searchValue 
   * @param realm string value that sets the realm for the search.  The source can
   * implement various realms to account for permissions of the calling user
   * @return set
   */
  public Set<Subject> search(String searchValue, String realm);
  
  /**
   * @return true if the source is editable otherwise false
   */
  public boolean isEditable();
  
  
  /**
   * @return true if the source is active otherwise false
   */
  public boolean isEnabled();
  
  /**
   * @return configId for this source 
   */
  public String getConfigId();
  
  /**
   * set config id for this source
   */
  public void setConfigId(String configId);
  
  public String convertSubjectAttributeToSourceAttribute(String nameOfSubjectAttribute);
  
  public String convertSourceAttributeToSubjectAttribute(String nameOfSourceAttribute);
  
}
