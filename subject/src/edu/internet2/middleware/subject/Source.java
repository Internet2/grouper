package edu.internet2.middleware.subject;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;



/**
 * Source to find and get Subjects and their attributes.
 * You should probably extend BaseSourceAdapter instead of implement this interface
 */
public interface Source {

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
   * get the init param e.g. from the sources.xml
   * @param name1
   * @return param
   */
  public String getInitParam(String name1);

  /**
   * get the init params e.g. from the sources.xml
   * @return params
   */
  public Properties getInitParams();
  
  /**
   * Get a set of attributes that are marked as being internal attributes.
   * 
   * Note, these will be in lower case
   * 
   * @return set to lower case
   */
  public Set<String> getInternalAttributes();
  
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
}
