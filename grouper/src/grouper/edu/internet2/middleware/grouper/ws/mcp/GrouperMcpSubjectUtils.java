/*******************************************************************************
 * Copyright 2024 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.mcp;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;

/**
 * Shared utility methods for MCP tools that deal with subject parameters.
 * Provides a consolidated approach using subjectIdOrIdentifier + subjectIdType
 * instead of separate subjectId/subjectIdentifier fields.
 *
 * <p>The subjectIdType controls how the value is resolved:
 * <ul>
 *   <li>"subjectIdOrIdentifier" (default) - tries both id and identifier lookup</li>
 *   <li>"subjectId" - treats the value as a subject ID only</li>
 *   <li>"subjectIdentifier" - treats the value as a subject identifier only</li>
 * </ul>
 *
 * @author mchyzer
 */
public class GrouperMcpSubjectUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * valid values for the subjectIdType parameter
   */
  public static final String SUBJECT_ID_TYPE_ID_OR_IDENTIFIER = "subjectIdOrIdentifier";

  /**
   * valid values for the subjectIdType parameter
   */
  public static final String SUBJECT_ID_TYPE_ID = "subjectId";

  /**
   * valid values for the subjectIdType parameter
   */
  public static final String SUBJECT_ID_TYPE_IDENTIFIER = "subjectIdentifier";

  /**
   * the grouper group subject source id
   */
  private static final String GROUP_SOURCE_ID = "g:gsa";

  /**
   * create a WsSubjectLookup from a subjectIdOrIdentifier value and optional subjectIdType.
   * If subjectIdType is null or "subjectIdOrIdentifier", sets both subjectId and
   * subjectIdentifier to the same value so WsSubjectLookup does an id-or-identifier lookup.
   * If subjectIdType is "subjectId", only sets the subjectId field.
   * If subjectIdType is "subjectIdentifier", only sets the subjectIdentifier field.
   *
   * <p>For the g:gsa (group) source, applies smart detection:
   * if subjectIdType is "subjectId" but the value contains colons, it is treated as
   * a subjectIdentifier (group name). If subjectIdType is "subjectIdentifier" but the
   * value does not contain colons, it is treated as a subjectId (group UUID).
   *
   * @param subjectIdOrIdentifier the subject id or identifier value
   * @param subjectIdType optional type hint: "subjectIdOrIdentifier" (default), "subjectId", or "subjectIdentifier"
   * @param sourceId optional source ID to restrict the lookup
   * @return the WsSubjectLookup configured for the appropriate lookup type
   */
  public static WsSubjectLookup createSubjectLookup(String subjectIdOrIdentifier,
      String subjectIdType, String sourceId) {

    // smart detection for g:gsa source: colons indicate a group name (identifier),
    // no colons indicates a group UUID (id)
    if (GROUP_SOURCE_ID.equals(sourceId) && StringUtils.isNotBlank(subjectIdOrIdentifier)) {
      boolean hasColons = subjectIdOrIdentifier.contains(":");
      if (SUBJECT_ID_TYPE_ID.equals(subjectIdType) && hasColons) {
        // caller said subjectId but the value looks like a group name, treat as identifier
        subjectIdType = SUBJECT_ID_TYPE_IDENTIFIER;
      } else if (SUBJECT_ID_TYPE_IDENTIFIER.equals(subjectIdType) && !hasColons) {
        // caller said subjectIdentifier but the value looks like a UUID, treat as id
        subjectIdType = SUBJECT_ID_TYPE_ID;
      }
    }

    if (StringUtils.isBlank(subjectIdType)
        || SUBJECT_ID_TYPE_ID_OR_IDENTIFIER.equals(subjectIdType)) {
      // set both to same value so WsSubjectLookup treats it as id-or-identifier
      return new WsSubjectLookup(subjectIdOrIdentifier, sourceId, subjectIdOrIdentifier);
    } else if (SUBJECT_ID_TYPE_ID.equals(subjectIdType)) {
      return new WsSubjectLookup(subjectIdOrIdentifier, sourceId, null);
    } else if (SUBJECT_ID_TYPE_IDENTIFIER.equals(subjectIdType)) {
      return new WsSubjectLookup(null, sourceId, subjectIdOrIdentifier);
    } else {
      throw new IllegalArgumentException(
          "Invalid subjectIdType: '" + subjectIdType + "'. "
          + "Must be one of: 'subjectIdOrIdentifier' (default), 'subjectId', 'subjectIdentifier'.");
    }
  }

  /**
   * validate a subjectIdType value. Returns null if valid, or an error message if invalid.
   * @param subjectIdType the value to validate (null/blank is valid, defaults to subjectIdOrIdentifier)
   * @return null if valid, error message if invalid
   */
  public static String validateSubjectIdType(String subjectIdType) {
    if (StringUtils.isBlank(subjectIdType)
        || SUBJECT_ID_TYPE_ID_OR_IDENTIFIER.equals(subjectIdType)
        || SUBJECT_ID_TYPE_ID.equals(subjectIdType)
        || SUBJECT_ID_TYPE_IDENTIFIER.equals(subjectIdType)) {
      return null;
    }
    return "Invalid subjectIdType: '" + subjectIdType + "'. "
        + "Must be one of: 'subjectIdOrIdentifier' (default), 'subjectId', 'subjectIdentifier'.  "
        + "Subject ID and source ID uniquely identify a subject.  Subject ID is recommended to be "
        + "unique and unchanging like a numeric emlpoyee ID (though it is a configuration in the "
        + "Grouper deployment).  Subject identifiers can change and are usually netIDs, EPPNs, etc."
        + "  subjectIdOrIdentifier can be either one.";
  }

  /**
   * add the subjectIdOrIdentifier property to a JSON properties node for tool definitions.
   * @param properties the properties node to add to
   * @param description the description for the field, or null for the default
   */
  public static void addSubjectIdOrIdentifierProperty(ObjectNode properties, String description) {
    ObjectNode prop = objectMapper.createObjectNode();
    prop.put("type", "string");
    prop.put("description",
        StringUtils.defaultIfBlank(description,
            "The subject ID or identifier (e.g., login ID, pennkey, eppn). "
            + "By default this is resolved as either an ID or identifier."));
    properties.set("subjectIdOrIdentifier", prop);
  }

  /**
   * add the subjectIdType property to a JSON properties node for tool definitions.
   * @param properties the properties node to add to
   */
  public static void addSubjectIdTypeProperty(ObjectNode properties) {
    ObjectNode prop = objectMapper.createObjectNode();
    prop.put("type", "string");
    prop.put("description",
        "How to interpret the subject value. Defaults to 'subjectIdOrIdentifier' which "
        + "tries both ID and identifier. Use 'subjectId' to look up by ID only, "
        + "or 'subjectIdentifier' to look up by identifier only.");
    ArrayNode enumValues = objectMapper.createArrayNode();
    enumValues.add(SUBJECT_ID_TYPE_ID_OR_IDENTIFIER);
    enumValues.add(SUBJECT_ID_TYPE_ID);
    enumValues.add(SUBJECT_ID_TYPE_IDENTIFIER);
    prop.set("enum", enumValues);
    prop.put("default", SUBJECT_ID_TYPE_ID_OR_IDENTIFIER);
    properties.set("subjectIdType", prop);
  }

  /**
   * add the sourceId property to a JSON properties node for tool definitions.
   * @param properties the properties node to add to
   * @param description the description for the field, or null for the default
   */
  public static void addSourceIdProperty(ObjectNode properties, String description) {
    ObjectNode prop = objectMapper.createObjectNode();
    prop.put("type", "string");
    prop.put("description",
        StringUtils.defaultIfBlank(description,
            "Optional source ID to restrict the subject lookup to a specific source."));
    properties.set("sourceId", prop);
  }

  /**
   * add subject properties (subjectIdOrIdentifier, subjectIdType, sourceId) to a
   * JSON properties node inside an array items schema for tool definitions.
   * @param subjectProperties the properties node inside the array items to add to
   */
  public static void addSubjectArrayItemProperties(ObjectNode subjectProperties) {
    addSubjectIdOrIdentifierProperty(subjectProperties, null);
    addSubjectIdTypeProperty(subjectProperties);
    addSourceIdProperty(subjectProperties, null);
  }
}
