
package edu.internet2.middleware.subject;

import java.io.Serializable;
import java.util.Set;

/**
 * A Subject represents an entity, such as a person, group, or organization.
 * This class provides common characteristics and behaviors across these entities.
 * Note, implementations of this interface shouldnt hold onto sources, since they
 * arent serializable, they should lookup with AllSources
 */
public interface Subject extends Serializable {

	/**
	 * Gets this Subject's ID.
	 * @return string
	 */
	public String getId();

	/**
	 * Gets this Subject's type.
	 * @return subject type
	 */
	public SubjectType getType();

	/**
	 * Gets this Subject's name.
	 * @return name
	 */
	public String getName();

	/**
	 * Gets this Subject's description.
	 * @return description
	 */
	public String getDescription();

	/**
	 * Returns the value of a single-valued attribute.
	 * @param name 
	 * @return value
	 */
	public String getAttributeValue(String name);
	
	/**
	 * Returns the values of a multi-valued attribute.
	 * @param name 
	 * @return set
	 */
	public java.util.Set<String> getAttributeValues(String name);

	/**
	 * Gets a map attribute names and values. The map's key
	 * contains the attribute name and the map's value
	 * contains a Set of attribute value(s).
	 * @return map
	 */
	public java.util.Map<String, Set<String>> getAttributes();

	/**
	 * Returns the Source of this Subject.
	 * @return source
	 */
	public Source getSource();

//  /**
//   * Returns the Source ID of this Subject (hopefully without having to go to the source object).
//   * @return source
//   */
//  public Source getSourceId();

}
