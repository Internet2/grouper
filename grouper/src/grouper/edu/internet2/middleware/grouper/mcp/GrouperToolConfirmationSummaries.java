/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * what a user is shown before a write tool runs.  built by code from the arguments, never by the
 * model, so what the user approves is what is about to happen.
 *
 * <p>each summary is a plain headline for the arguments that decide what the call does, read the
 * same way the tool reads them, so that for example a privilege call without "allowed" reads as a
 * revoke, because that is what it does.  every other argument the call was given is listed after
 * the headline, so nothing that affects the call is left out, including arguments a tool gains
 * after this was written.</p>
 */
public class GrouperToolConfirmationSummaries {

  /**
   * @param arguments group_add_member arguments
   * @return the summary
   */
  public static String groupAddMember(JsonNode arguments) {

    String target = membershipTarget(arguments);
    String subjects = subjects(arguments);

    String headline = null;
    if (argumentBoolean(arguments, "replaceAllExisting")) {
      headline = "Replace ALL existing members of " + target + " with " + subjects;
    } else {
      headline = "Add " + subjects + " to " + target;
    }

    return withOtherArguments(headline, arguments, "groupName", "fieldName", "subjects", "replaceAllExisting");
  }

  /**
   * @param arguments group_remove_member arguments
   * @return the summary
   */
  public static String groupRemoveMember(JsonNode arguments) {
    String headline = "Remove " + subjects(arguments) + " from " + membershipTarget(arguments);
    return withOtherArguments(headline, arguments, "groupName", "fieldName", "subjects");
  }

  /**
   * @param arguments group_delete arguments
   * @return the summary
   */
  public static String groupDelete(JsonNode arguments) {
    String headline = "Delete group " + quote(argumentText(arguments, "groupName"));
    return withOtherArguments(headline, arguments, "groupName");
  }

  /**
   * @param arguments folder_delete arguments
   * @return the summary
   */
  public static String folderDelete(JsonNode arguments) {
    String headline = "Delete folder " + quote(argumentText(arguments, "stemName"));
    return withOtherArguments(headline, arguments, "stemName");
  }

  /**
   * @param arguments folder_save arguments
   * @return the summary
   */
  public static String folderSave(JsonNode arguments) {

    String action = argumentText(arguments, "action");
    String folder = quote(argumentText(arguments, "stemName"));

    String headline = null;
    if (StringUtils.equals("createFolder", action)) {
      headline = "Create folder " + folder;
    } else if (StringUtils.equals("createOrUpdateFolder", action)) {
      headline = "Create or update folder " + folder;
    } else if (StringUtils.equals("updateFolderPart", action)) {
      headline = "Update folder " + folder;
    } else {
      headline = "Change folder " + folder + " with action " + quote(action);
    }

    return withOtherArguments(headline, arguments, "action", "stemName");
  }

  /**
   * @param arguments privilege_assign arguments
   * @return the summary
   */
  public static String privilegeAssign(JsonNode arguments) {

    String groupName = argumentText(arguments, "groupName");
    String stemName = argumentText(arguments, "stemName");

    String on = null;
    if (!StringUtils.isBlank(groupName) && !StringUtils.isBlank(stemName)) {
      on = "group " + quote(groupName) + " and folder " + quote(stemName);
    } else if (!StringUtils.isBlank(stemName)) {
      on = "folder " + quote(stemName);
    } else {
      on = "group " + quote(groupName);
    }

    String subject = subject(argumentText(arguments, "subjectIdOrIdentifier"),
        argumentText(arguments, "sourceId"), argumentText(arguments, "subjectIdType"));

    // access for a group privilege, naming for a folder one.  part of the headline rather than an
    // extra, since it is an ordinary argument of the tool.  the two real values are shown as words;
    // anything else the model wrote, even blank, is quoted, so it is neither hidden nor able to read
    // as part of the sentence
    String privilegeType = argumentText(arguments, "privilegeType");
    String privilegeTypeText = "";
    if (StringUtils.equals("access", privilegeType) || StringUtils.equals("naming", privilegeType)) {
      privilegeTypeText = " " + privilegeType;
    } else if (privilegeType != null) {
      privilegeTypeText = " " + quote(privilegeType);
    }
    String privilege = quote(argumentText(arguments, "privilegeName")) + privilegeTypeText + " privilege";

    // the tool revokes unless allowed is true, so a missing allowed is a revoke
    String headline = null;
    if (argumentBoolean(arguments, "allowed")) {
      headline = "Grant the " + privilege + " on " + on + " to " + subject;
    } else {
      headline = "Revoke the " + privilege + " on " + on + " from " + subject;
    }

    return withOtherArguments(headline, arguments, "groupName", "stemName", "subjectIdOrIdentifier",
        "sourceId", "subjectIdType", "privilegeName", "privilegeType", "allowed");
  }

  /**
   * @param arguments group_save arguments
   * @return the summary
   */
  public static String groupSave(JsonNode arguments) {

    String action = argumentText(arguments, "action");
    String group = quote(argumentText(arguments, "groupName"));

    String headline = null;
    if (StringUtils.equals("createGroup", action)) {
      headline = "Create group " + group;
    } else if (StringUtils.equals("createOrUpdateGroup", action)) {
      headline = "Create or update group " + group;
    } else if (StringUtils.equals("updateGroupPart", action)) {
      headline = "Update group " + group;
    } else if (StringUtils.equals("renameGroup", action)) {
      headline = "Rename group " + group;
    } else if (StringUtils.equals("addGroupType", action)) {
      headline = "Add a group type to group " + group;
    } else if (StringUtils.equals("removeGroupType", action)) {
      headline = "Remove a group type from group " + group;
    } else if (StringUtils.equals("addComposite", action)) {
      headline = "Make group " + group + " a composite";
    } else if (StringUtils.equals("updateComposite", action)) {
      headline = "Change the composite of group " + group;
    } else if (StringUtils.equals("removeComposite", action)) {
      headline = "Remove the composite from group " + group;
    } else if (StringUtils.equals("addEligibilityRequirement", action)) {
      headline = "Add an eligibility requirement to group " + group;
    } else if (StringUtils.equals("removeEligibilityRequirement", action)) {
      headline = "Remove an eligibility requirement from group " + group;
    } else if (StringUtils.equals("addProvisioner", action)) {
      headline = "Add a provisioner to group " + group;
    } else if (StringUtils.equals("removeProvisioner", action)) {
      headline = "Remove a provisioner from group " + group;
    } else {
      headline = "Change group " + group + " with action " + quote(action);
    }

    return withOtherArguments(headline, arguments, "action", "groupName");
  }

  /**
   * @param arguments attribute_assignment_save arguments
   * @return the summary
   */
  public static String attributeAssignmentSave(JsonNode arguments) {
    // which owner applies depends on attributeAssignType, so the owners are listed as given rather
    // than one being picked here
    String headline = "Change attribute assignments: " + quote(argumentText(arguments, "attributeAssignOperation"))
        + " attribute " + quote(argumentText(arguments, "attributeDefNameName"));
    return withOtherArguments(headline, arguments, "attributeAssignOperation", "attributeDefNameName");
  }

  /**
   * @param arguments recipe arguments with action update
   * @return the summary
   */
  public static String recipeUpdate(JsonNode arguments) {
    String headline = "Update recipe " + quote(argumentText(arguments, "name"))
        + ", which changes guidance for everybody who uses it";
    return withOtherArguments(headline, arguments, "action", "name");
  }

  /**
   * @param arguments institutional_tools arguments running a template which is not readonly
   * @return the summary
   */
  public static String institutionalToolExecute(JsonNode arguments) {
    // the owner and the inputs are what the script acts on, and are listed after the headline
    String headline = "Run institutional tool " + quote(argumentText(arguments, "configId"))
        + ", a script which can make changes";
    return withOtherArguments(headline, arguments, "action", "configId");
  }

  /**
   * @param arguments admin_daemon_job_run arguments
   * @return the summary
   */
  public static String adminDaemonJobRun(JsonNode arguments) {
    String headline = "Run daemon job " + quote(argumentText(arguments, "jobName")) + " now";
    return withOtherArguments(headline, arguments, "jobName");
  }

  /**
   * @param arguments membership tool arguments
   * @return which list of which group, e.g. group 'a:b', or the 'updaters' list of group 'a:b'
   */
  private static String membershipTarget(JsonNode arguments) {
    String group = "group " + quote(argumentText(arguments, "groupName"));
    String fieldName = argumentText(arguments, "fieldName");
    if (StringUtils.isBlank(fieldName) || StringUtils.equals("members", fieldName)) {
      return group;
    }
    // a field other than members is a privilege list, so this grants or removes a privilege
    return "the " + quote(fieldName) + " list of " + group;
  }

  /**
   * @param arguments tool arguments with a subjects array
   * @return the subjects, e.g. 2 subjects: 'jsmith', 'jdoe' (source 'ldap')
   */
  private static String subjects(JsonNode arguments) {

    JsonNode subjects = arguments == null ? null : arguments.get("subjects");

    if (subjects == null || !subjects.isArray()) {
      return "subjects " + (subjects == null ? "(not given)" : escape(subjects.toString(), false));
    }

    StringBuilder result = new StringBuilder();
    result.append(subjects.size()).append(subjects.size() == 1 ? " subject: " : " subjects: ");
    for (int i = 0; i < subjects.size(); i++) {
      if (i > 0) {
        result.append(", ");
      }
      JsonNode subjectNode = subjects.get(i);
      if (!subjectNode.isObject()) {
        result.append(escape(subjectNode.toString(), false));
        continue;
      }
      result.append(subject(argumentText(subjectNode, "subjectIdOrIdentifier"),
          argumentText(subjectNode, "sourceId"), argumentText(subjectNode, "subjectIdType")));

      // anything else on a subject is shown too
      String otherFields = otherArguments(subjectNode, toSet("subjectIdOrIdentifier", "sourceId", "subjectIdType"));
      if (otherFields != null) {
        result.append(" (also given: ").append(otherFields).append(")");
      }
    }
    return result.toString();
  }

  /**
   * @param subjectIdOrIdentifier id or identifier
   * @param sourceId source, may be null
   * @param subjectIdType how to read the id, may be null
   * @return e.g. 'jsmith' (source 'ldap')
   */
  private static String subject(String subjectIdOrIdentifier, String sourceId, String subjectIdType) {
    StringBuilder result = new StringBuilder(quote(subjectIdOrIdentifier));
    if (!StringUtils.isBlank(sourceId) || !StringUtils.isBlank(subjectIdType)) {
      result.append(" (");
      if (!StringUtils.isBlank(sourceId)) {
        result.append("source ").append(quote(sourceId));
      }
      if (!StringUtils.isBlank(subjectIdType)) {
        if (!StringUtils.isBlank(sourceId)) {
          result.append(", ");
        }
        result.append("as ").append(quote(subjectIdType));
      }
      result.append(")");
    }
    return result.toString();
  }

  /**
   * @param headline what the call does
   * @param arguments all the arguments
   * @param coveredNames arguments the headline already shows
   * @return the headline, then every other argument, labelled so they read as arguments and not
   * as part of the headline
   */
  static String withOtherArguments(String headline, JsonNode arguments, String... coveredNames) {
    String otherArguments = otherArguments(arguments, toSet(coveredNames));
    if (otherArguments == null) {
      return headline;
    }
    return headline + " (also given: " + otherArguments + ")";
  }

  /**
   * @param arguments an object
   * @param coveredNames fields to leave out
   * @return the other fields as name: value, or null if there are none
   */
  private static String otherArguments(JsonNode arguments, Set<String> coveredNames) {

    if (arguments == null || !arguments.isObject()) {
      return null;
    }

    StringBuilder result = new StringBuilder();
    Iterator<String> fieldNames = arguments.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      if (coveredNames.contains(fieldName)) {
        continue;
      }
      if (result.length() > 0) {
        result.append(", ");
      }
      JsonNode value = arguments.get(fieldName);
      result.append(argumentName(fieldName)).append(": ")
        .append(value.isTextual() ? quote(value.asText()) : escape(value.toString(), false));
    }

    return result.length() == 0 ? null : result.toString();
  }

  /**
   * read a text argument the way the tools do.
   *
   * <p>this deliberately does not skip an explicit json null, even though
   * GrouperUtil.jsonJacksonGetString does.  the tools read their arguments as
   * arguments.has(x) ? arguments.get(x).asText() : null, and asText() on a json null is the string
   * "null", so a null stemName makes the tool act on a folder named "null".  the summary has to say
   * folder 'null' because that is what will run.  having the summary alone report "not given" would
   * let a user approve "on no folder" and get the privilege granted on a folder named "null".
   * the fix for the whole class of problem is to make the tools treat an explicit null as not given,
   * which is a change across every tool, not something to patch here.</p>
   *
   * @param arguments the arguments
   * @param name the argument
   * @return the text, or null if not given
   */
  private static String argumentText(JsonNode arguments, String name) {
    if (arguments == null || !arguments.has(name)) {
      return null;
    }
    return arguments.get(name).asText();
  }

  /**
   * read a boolean argument the way the tools do: true only if given and true
   * @param arguments the arguments
   * @param name the argument
   * @return the value
   */
  private static boolean argumentBoolean(JsonNode arguments, String name) {
    return arguments != null && arguments.has(name) && arguments.get(name).asBoolean(false);
  }

  /**
   * @param value a value from the arguments
   * @return it in quotes, escaped, or (not given)
   */
  private static String quote(String value) {
    if (value == null) {
      return "(not given)";
    }
    return "'" + escape(value, true) + "'";
  }

  /**
   * @param name an argument name
   * @return the name as is if it is a plain identifier, otherwise quoted and escaped
   */
  private static String argumentName(String name) {
    if (StringUtils.isEmpty(name)) {
      return quote(name);
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      boolean plain = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
      if (!plain) {
        return quote(name);
      }
    }
    return name;
  }

  /**
   * make text from the model safe to show inside a summary.  the model writes the arguments, and
   * one steered by something it read could otherwise use them to make the summary say something
   * other than what the call does: close a quote early and carry on in what looks like the
   * summary's own words, start what looks like a new line, or visually reorder the text with
   * invisible direction marks.  so quotes and backslashes are escaped, and control characters,
   * line and paragraph separators and invisible formatting characters are shown as \\uXXXX.
   * characters are checked whole, not per UTF-16 unit, so an invisible character above U+FFFF,
   * such as the Unicode tag characters used to hide text, is caught too and shown as its two
   * surrogate halves
   * @param text the text
   * @param escapeQuotes true for text shown inside single quotes, false for JSON, which already
   * escapes its own quotes
   * @return the escaped text
   */
  public static String escape(String text, boolean escapeQuotes) {
    StringBuilder result = new StringBuilder(text.length());
    int i = 0;
    while (i < text.length()) {
      int codePoint = text.codePointAt(i);
      int charCount = Character.charCount(codePoint);
      if (escapeQuotes && (codePoint == '\'' || codePoint == '\\')) {
        result.append('\\').append((char)codePoint);
      } else if (escapeQuotes && codePoint == '\n') {
        result.append("\\n");
      } else if (isHiddenOrControl(codePoint)) {
        for (int j = i; j < i + charCount; j++) {
          result.append(String.format("\\u%04x", (int)text.charAt(j)));
        }
      } else {
        result.append(text, i, i + charCount);
      }
      i += charCount;
    }
    return result.toString();
  }

  /**
   * @param codePoint a character
   * @return true if it is a control character, a line or paragraph separator, or an invisible
   * formatting character
   */
  private static boolean isHiddenOrControl(int codePoint) {
    int type = Character.getType(codePoint);
    // a surrogate here is one without its other half, which is not a character at all
    return Character.isISOControl(codePoint) || type == Character.FORMAT
        || type == Character.LINE_SEPARATOR || type == Character.PARAGRAPH_SEPARATOR
        || type == Character.SURROGATE;
  }

  /**
   * @param values names
   * @return them as a set
   */
  private static Set<String> toSet(String... values) {
    Set<String> result = new HashSet<String>();
    for (String value : values) {
      result.add(value);
    }
    return result;
  }

}
