
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
	 * Adds SubjectType supported by this source.
	 */
	public void addSubjectType(String type);
	
	/**
	 * Sets the SubjectTypes supported by this source.
	 */
	public void setSubjectTypes(java.util.Set types);
	
	/**
	 * Gets a Subject by its ID.
	 */
	public Subject getSubject(String id)
		throws SubjectNotFoundException;

	/**
	 * Unstructured search for Subjects. Each implementation
	 * utilizes its own search algorithm tailored to
	 * the Subject repository and schema.
	 */
	public java.util.Set search(String searchValue);

	/**
	 * Search by ID. Returns subjects independent of SubjectType.
	 */
	public java.util.Set searchByIdentifier(String id);

	/**
	 * Search by ID and SubjectType.
	 */
	public java.util.Set searchByIdentifier(String id, SubjectType type);
	
	/**
	 * Called by SourceManager when it loads this source.
	 */
	public void init()
		throws SourceUnavailableException;

	/**
	 * Called by SourceManager when it unloads this source.
	 */
	public void destroy();

}
