package edu.internet2.middleware.subject;
import java.util.HashMap;



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
	public HashMap getSubjectTypes();
	
	/**
	 * Gets a Subject by its ID.
	 */
	public Subject getSubject(String id, String typeid)
		throws SubjectNotFoundException,SubjectNotUniqueException;

	/**
	 * Gets a Subject by other well-known identifiers, aside
	 * from the subject ID, for example, login ID.
	 */
	public Subject getSubjectByIdentifier(String id, String typeid)
		throws SubjectNotFoundException,SubjectNotUniqueException;
	
	/**
	 * Unstructured search for Subjects. Each implementation
	 * utilizes its own search algorithm tailored to
	 * the Subject repository and schema.
	 */
	public java.util.Set search(String searchValue, String typeid);
	
	/**
	 * Called by SourceManager when it loads this source.
	 */
	public void init()
		throws SourceUnavailableException;

}
