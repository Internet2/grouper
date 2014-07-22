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
   * Note, the keys are case-insensitive
	 * @param attributeName 
	 * @return value or null if not found
	 */
	public String getAttributeValue(String attributeName);
	
	/**
	 * Returns the values of a multi-valued attribute, or a set of size one for a single valued attribute.
	 * Note the returned set should not be changed.
	 * This does not return values for internal attributes.
   * Note, the keys are case-insensitive
	 * @param attributeName 
	 * @return set or empty set or null if not there
	 */
	public java.util.Set<String> getAttributeValues(String attributeName);

  /**
   * Returns the attribute value if single-valued, or
   * if multi-valued, throws an exception.  Implementors
   * can use the static helper in SubjectImpl.
   * This does not return values for internal attributes.
   * Note, the keys are case-insensitive
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
   * Note, the keys are case-insensitive
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
	 * Note, the keys are case-insensitive
	 * 
	 * @return map or empty map or null if not there
	 */
	public java.util.Map<String, Set<String>> getAttributes();
	
	 /**
   * Returns the value of a single-valued attribute.
   * If multivalued, this returns the first value.
   * Note, the keys are case-insensitive
   * @param attributeName 
	 * @param excludeInternalAttributes if true, values for internal attributes are not returned
   * @return value or null if not found
   */
  public String getAttributeValue(String attributeName, boolean excludeInternalAttributes);
  
  /**
   * Returns the values of a multi-valued attribute, or a set of size one for a single valued attribute.
   * Note the returned set should not be changed.
   * Note, the keys are case-insensitive
   * @param attributeName 
   * @param excludeInternalAttributes if true, values for internal attributes are not returned
   * @return set or empty set or null if not there
   */
  public java.util.Set<String> getAttributeValues(String attributeName, boolean excludeInternalAttributes);

  /**
   * Returns the attribute value if single-valued, or
   * if multi-valued, throws an exception.  Implementors
   * can use the static helper in SubjectImpl.
   * Note, the keys are case-insensitive
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
   * Note, the keys are case-insensitive
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
   * Note, the keys are case-insensitive
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
