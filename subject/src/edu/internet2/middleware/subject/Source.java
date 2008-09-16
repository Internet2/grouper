package edu.internet2.middleware.subject;
import java.util.Set;



/**
 * Source to find and get Subjects and their attributes.
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
	 */
	public Subject getSubject(String id)
		throws SubjectNotFoundException,SubjectNotUniqueException;

	/**
	 * Gets a Subject by other well-known identifiers, aside
	 * from the subject ID, for example, login ID.
	 */
	public Subject getSubjectByIdentifier(String id)
		throws SubjectNotFoundException,SubjectNotUniqueException;
	
	/**
	 * Unstructured search for Subjects. Each implementation
	 * utilizes its own search algorithm tailored to
	 * the Subject repository and schema.
	 */
	public Set<Subject> search(String searchValue);
	
	/**
	 * Called by SourceManager when it loads this source.
	 */
	public void init()
		throws SourceUnavailableException;

}
