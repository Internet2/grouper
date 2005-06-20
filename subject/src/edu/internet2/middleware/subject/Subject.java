
package edu.internet2.middleware.subject;

/**
 * A Subject represents an entity, such as a person, group, or organization.
 * This class provides common characteristics and behaviors across these entities.
 */
public interface Subject {

	/**
	 * Gets this Subject's ID.
	 */
	public String getId();

	/**
	 * Gets this Subject's type.
	 */
	public SubjectType getType();

	/**
	 * Gets this Subject's name.
	 */
	public String getName();

	/**
	 * Gets this Subject's description.
	 */
	public String getDescription();

	/**
	 * Returns the value of a single-valued attribute.
	 */
	public String getAttributeValue(String name);
	
	/**
	 * Returns the values of a multi-valued attribute.
	 */
	public java.util.Set getAttributeValues(String name);

	/**
	 * Gets a map attribute names and values. The map's key
	 * contains the attribute name and the map's value
	 * contains a Set of attribute value(s).
	 */
	public java.util.Map getAttributes();

	/**
	 * Returns the Source of this Subject.
	 */
	public Source getSource();

}
