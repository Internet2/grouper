
package edu.internet2.middleware.subject;

import java.io.Serializable;
import java.util.Set;

/**
 * <pre>
 * A Subject represents an entity, such as a person, group, or organization.
 * This class provides common characteristics and behaviors across these entities.
 * Note, implementations of this interface shouldnt hold onto sources, since they
 * arent serializable, they should lookup with AllSources
 * 
 * Implementors should probably subclass SubjectImpl instead of implement this directly.
 * 
 * Also, implementors should implement toString, equals and hashcode like Subject (based on
 * sourceId and subjectId).  There are static methods in subjectImpl which can be used as helper
 * methods
 * </pre>
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
	 * get the type name
	 * @return the type name
	 */
	public String getTypeName();
	
	/**
	 * get the source id of a subject
	 * @return the source id
	 */
	public String getSourceId();
	
	/**
	 * Gets this Subject's name.
	 * @return name or null if not there
	 */
	public String getName();

	/**
	 * Gets this Subject's description.
	 * @return description or null if not there
	 */
	public String getDescription();

	/**
	 * Returns the value of a single-valued attribute.
	 * If multivalued, this returns the first value.
	 * This does not return values for internal attributes.
	 * @param attributeName 
	 * @return value or null if not found
	 */
	public String getAttributeValue(String attributeName);
	
	/**
	 * Returns the values of a multi-valued attribute, or a set of size one for a single valued attribute.
	 * Note the returned set should not be changed.
	 * This does not return values for internal attributes.
	 * @param attributeName 
	 * @return set or empty set or null if not there
	 */
	public java.util.Set<String> getAttributeValues(String attributeName);

  /**
   * Returns the attribute value if single-valued, or
   * if multi-valued, throws an exception.  Implementors
   * can use the static helper in SubjectImpl.
   * This does not return values for internal attributes.
   * @param attributeName
   * @return value or null if not there
   */
  public String getAttributeValueSingleValued(String attributeName);

  /**
   * <pre>
   * Returns the attribute value if single-valued, or
   * if multi-valued, returns the values comma separated (with a space too).
   * So if the values are: a b c; this would return the string: "a, b, c"
   * Implementors can use the static helper in SubjectImpl.
   * This does not return values for internal attributes.
   * </pre>
   * @param attributeName
   * @return value or values or null if not there
   */
  public String getAttributeValueOrCommaSeparated(String attributeName);

	/**
	 * Gets a map attribute names and values. The map's key
	 * contains the attribute name and the map's value
	 * contains a Set of attribute value(s).
	 * This does not return internal attributes.
	 * @return map or empty map or null if not there
	 */
	public java.util.Map<String, Set<String>> getAttributes();
	
	 /**
   * Returns the value of a single-valued attribute.
   * If multivalued, this returns the first value.
   * @param attributeName 
	 * @param excludeInternalAttributes if true, values for internal attributes are not returned
   * @return value or null if not found
   */
  public String getAttributeValue(String attributeName, boolean excludeInternalAttributes);
  
  /**
   * Returns the values of a multi-valued attribute, or a set of size one for a single valued attribute.
   * Note the returned set should not be changed.
   * @param attributeName 
   * @param excludeInternalAttributes if true, values for internal attributes are not returned
   * @return set or empty set or null if not there
   */
  public java.util.Set<String> getAttributeValues(String attributeName, boolean excludeInternalAttributes);

  /**
   * Returns the attribute value if single-valued, or
   * if multi-valued, throws an exception.  Implementors
   * can use the static helper in SubjectImpl.
   * @param attributeName
   * @param excludeInternalAttributes if true, values for internal attributes are not returned
   * @return value or null if not there
   */
  public String getAttributeValueSingleValued(String attributeName, boolean excludeInternalAttributes);

  /**
   * <pre>
   * Returns the attribute value if single-valued, or
   * if multi-valued, returns the values comma separated (with a space too).
   * So if the values are: a b c; this would return the string: "a, b, c"
   * Implementors can use the static helper in SubjectImpl.
   * </pre>
   * @param attributeName
   * @param excludeInternalAttributes if true, values for internal attributes are not returned
   * @return value or values or null if not there
   */
  public String getAttributeValueOrCommaSeparated(String attributeName, boolean excludeInternalAttributes);

  /**
   * Gets a map attribute names and values. The map's key
   * contains the attribute name and the map's value
   * contains a Set of attribute value(s).  The returned Map can be augmented or changed.
   * @param excludeInternalAttributes if true, internal attributes are not returned
   * @return map or empty map or null if not there
   */
  public java.util.Map<String, Set<String>> getAttributes(boolean excludeInternalAttributes);

	/**
	 * Returns the Source of this Subject.
	 * @return source
	 */
	public Source getSource();

}
