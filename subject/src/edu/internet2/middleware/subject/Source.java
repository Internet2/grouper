
package edu.internet2.middleware.subject;


/**
 * Source to find and get Subjects and their attributes.
 */
public interface Source {

	/**
	 * Returns the ID of this source.
	 */
	public String getId();

	/**
	 * Sets the ID of this source.
	 */
	public void setId(String id);
	
	/**
	 * Returns the name of this source.
	 */
	public String getName();

	/**
	 * Sets the name of this source.
	 */
	public void setName(String name);

	/**
	 * Gets the SubjectTypes supported by this source.
	 */
	public java.util.Set getSubjectTypes();
	
	/**
	 * Gets a Subject by its ID.
	 */
	public Subject getSubject(String id)
		throws SubjectNotFoundException;

	/**
	 * Gets a Subject by other well-known identifiers, aside
	 * from the subject ID, e.g. login ID.
	 */
	public Subject getSubjectByIdentifier(String id)
		throws SubjectNotFoundException;
	
	/**
	 * Unstructured search for Subjects. Each implementation
	 * utilizes its own search algorithm tailored to
	 * the Subject repository and schema.
	 */
	public java.util.Set search(String searchValue);
	
	/**
	 * Called by SourceManager when it loads this source.
	 */
	public void init()
		throws SourceUnavailableException;

}
