package edu.internet2.middleware.subject;
import java.io.Serializable;
import java.util.Set;



/**
 * Source to find and get Subjects and their attributes.
 * You should probably extend BaseSourceAdapter instead of implement this interface
 */
public interface Source extends Serializable {

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
	 * the Subject repository and schema.
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
}
