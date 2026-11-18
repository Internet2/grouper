package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Allowlist parser and evaluator for scripted-group (JEXL_SCRIPT loader) scripts in the
 * releases that still evaluate those scripts (2.6.x and 4.x).
 *
 * In those releases the stored script used to be run as a JEXL script with static class
 * access, so whoever could edit a loader could run any Java as GrouperSystem (in the UI on
 * the edit form refresh and on save, and in the daemon on every full sync).  Later releases
 * (5.4.0 and up) parse the script and translate it to SQL instead, accepting only
 * entity.memberOf('...') combined with and / or / not and parentheses (plus newer
 * functions these releases do not have).
 *
 * This class gives these releases the same shape without JEXL: a script must be exactly
 *
 *   ${ expr }
 *   expr    := orExpr
 *   orExpr  := andExpr ( ( "||" | "or" ) andExpr )*
 *   andExpr := notExpr ( ( "&amp;&amp;" | "and" ) notExpr )*
 *   notExpr := ( "!" | "not" ) notExpr | primary
 *   primary := "(" expr ")" | entity.memberOf('group:name')
 *
 * The group name is a single-quoted literal with no quote, no backslash and no control
 * characters (the same names the full sync's group name regex extracts).  Anything else is
 * refused before evaluation.  An accepted script is evaluated here in plain Java against
 * the GrouperAbacEntity, so no class, method or variable other than entity.memberOf is
 * ever reachable.
 *
 * It also has the save-time rule from later releases (GRP-6031 / GRP-7542): a non-wheel
 * editor must be able to READ every group the script names, under that group's current
 * name.  A group that does not exist, that the editor cannot read, or that is named by an
 * old name all get the same message, so the check does not disclose which groups exist.
 * </pre>
 */
public class GrouperAbacScriptAllowlist {

  /**
   * no instances
   */
  private GrouperAbacScriptAllowlist() {
  }

  // ---------------------------------------------------------------------------------------
  // public API
  // ---------------------------------------------------------------------------------------

  /**
   * check the script against the allowlist
   * @param script stored or submitted script, e.g. ${entity.memberOf('a:b') &amp;&amp; !entity.memberOf('a:c')}
   * @return null if allowed, else a message saying why not
   */
  public static String allowlistErrorMessage(String script) {
    try {
      parse(script);
      return null;
    } catch (ScriptNotAllowedException snae) {
      return snae.getMessage();
    }
  }

  /**
   * group names the script uses, in order of first use
   * @param script
   * @return the names
   * @throws RuntimeException if the script is not allowed
   */
  public static Set<String> groupNames(String script) {
    Set<String> result = new LinkedHashSet<String>();
    parse(script).collectGroupNames(result);
    return result;
  }

  /**
   * evaluate an allowed script for an entity, without JEXL
   * @param script
   * @param grouperAbacEntity whose memberOf(...) answers the script
   * @return true if the entity should be in the scripted group
   * @throws RuntimeException if the script is not allowed
   */
  public static boolean evaluate(String script, GrouperAbacEntity grouperAbacEntity) {
    Node node = parse(script);
    if (grouperAbacEntity == null) {
      throw new ScriptNotAllowedException("Scripted group script needs an 'entity' to evaluate");
    }
    return node.evaluate(grouperAbacEntity);
  }

  /**
   * evaluate an allowed script with a JEXL-style variable map (the 'entity' variable is used,
   * nothing else is reachable)
   * @param script
   * @param variableMap
   * @return Boolean result
   */
  public static Object evaluate(String script, Map<String, Object> variableMap) {
    Object entity = variableMap == null ? null : variableMap.get("entity");
    if (entity != null && !(entity instanceof GrouperAbacEntity)) {
      throw new ScriptNotAllowedException("Scripted group script 'entity' must be a GrouperAbacEntity, not "
          + entity.getClass().getName());
    }
    return Boolean.valueOf(evaluate(script, (GrouperAbacEntity)entity));
  }

  /**
   * <pre>
   * check a script an editor submits in the loader edit form (the form refresh and the save),
   * before anything evaluates it: first the allowlist, then, for an editor who is not wheel or
   * root, READ on every group the script names, under its current (exact) name.  Then the
   * release's own GrouperAbac.validScript.
   * </pre>
   * @param script submitted script
   * @param editor the logged in subject (not the root session of a callback)
   * @return null if ok, else an error message (no stack trace)
   */
  public static String validScriptForEditor(String script, Subject editor) {
    String errorMessage = allowlistErrorMessage(script);
    if (errorMessage != null) {
      return errorMessage;
    }
    errorMessage = editorCannotReadMessage(script, editor);
    if (errorMessage != null) {
      return errorMessage;
    }
    return GrouperAbac.validScript(script);
  }

  /**
   * READ check for a script that passed the allowlist
   * @param script
   * @param editor
   * @return null if the editor is wheel/root or can READ every group, else the message
   */
  public static String editorCannotReadMessage(String script, final Subject editor) {

    final Set<String> groupNames = groupNames(script);

    if (editor == null) {
      return "Scripted group scripts can only be edited by a logged in user";
    }

    // wheel and root can read every group; the check would only cost queries
    if (PrivilegeHelper.isWheelOrRoot(editor)) {
      return null;
    }

    // look up and check as root, so the answer does not depend on what the editor can VIEW
    @SuppressWarnings("unchecked")
    List<String> notAllowed = (List<String>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        List<String> result = new ArrayList<String>();
        for (String groupName : groupNames) {
          Group group = GroupFinder.findByName(grouperSession, groupName, false);
          // exact current name only: an old (alternate) name is not used by the full sync,
          // and a later group with that name must be checked when it is used
          if (group == null || !groupName.equals(group.getName())
              || !group.canHavePrivilege(editor, AccessPrivilege.READ.getName(), false)) {
            result.add(groupName);
          }
        }
        return result;
      }
    });

    if (notAllowed.size() == 0) {
      return null;
    }
    return "A scripted group script can only use groups that exist under that name and that you can read. "
        + "Check these groups: " + join(notAllowed);
  }

  // ---------------------------------------------------------------------------------------
  // parser
  // ---------------------------------------------------------------------------------------

  /**
   * thrown when a script is not in the allowlist
   */
  public static class ScriptNotAllowedException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public ScriptNotAllowedException(String message) {
      super(message);
    }
  }

  /** what the script may call, exactly as the full sync's group name regex expects it */
  private static final String MEMBER_OF = "entity.memberOf";

  /**
   * parse a whole script, ${ ... }
   * @param script
   * @return the tree
   */
  static Node parse(String script) {
    if (script == null) {
      throw new ScriptNotAllowedException("Scripted group script is required");
    }
    String trimmed = script.trim();
    if (!trimmed.startsWith("${") || !trimmed.endsWith("}") || trimmed.length() < 3) {
      throw new ScriptNotAllowedException("Scripted group script must look like ${ entity.memberOf('folder:group') }");
    }
    Parser parser = new Parser(trimmed.substring(2, trimmed.length() - 1));
    Node node = parser.parseOr();
    parser.skipWhitespace();
    if (!parser.atEnd()) {
      throw parser.notAllowed();
    }
    return node;
  }

  /**
   * recursive descent parser over the text inside ${ }
   */
  private static class Parser {

    /** text inside ${ } */
    private final String text;

    /** current position */
    private int pos = 0;

    /** nesting depth, bounded so a huge script cannot overflow the stack */
    private int depth = 0;

    /**
     * @param text
     */
    Parser(String text) {
      this.text = text;
    }

    /**
     * @return error at the current position
     */
    ScriptNotAllowedException notAllowed() {
      return new ScriptNotAllowedException("Scripted group scripts can only use entity.memberOf('folder:group') "
          + "with &&, ||, !, and, or, not, and parentheses (not allowed at character " + (this.pos + 3) + ")");
    }

    /**
     * @return true at end of text
     */
    boolean atEnd() {
      return this.pos >= this.text.length();
    }

    /**
     * skip spaces, tabs and line breaks
     */
    void skipWhitespace() {
      while (!atEnd()) {
        char c = this.text.charAt(this.pos);
        if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
          this.pos++;
        } else {
          break;
        }
      }
    }

    /**
     * if the next token is this symbol, consume it
     * @param symbol
     * @return true if consumed
     */
    boolean acceptSymbol(String symbol) {
      skipWhitespace();
      if (this.text.startsWith(symbol, this.pos)) {
        this.pos += symbol.length();
        return true;
      }
      return false;
    }

    /**
     * if the next token is this word (and/or/not), consume it
     * @param word
     * @return true if consumed
     */
    boolean acceptWord(String word) {
      skipWhitespace();
      if (this.text.startsWith(word, this.pos)) {
        int end = this.pos + word.length();
        if (end >= this.text.length() || !isIdentifierChar(this.text.charAt(end))) {
          this.pos = end;
          return true;
        }
      }
      return false;
    }

    /**
     * @return or expression
     */
    Node parseOr() {
      enter();
      Node left = parseAnd();
      while (acceptSymbol("||") || acceptWord("or")) {
        left = new BinaryNode(false, left, parseAnd());
      }
      this.depth--;
      return left;
    }

    /**
     * @return and expression
     */
    Node parseAnd() {
      Node left = parseNot();
      while (acceptSymbol("&&") || acceptWord("and")) {
        left = new BinaryNode(true, left, parseNot());
      }
      return left;
    }

    /**
     * @return not expression
     */
    Node parseNot() {
      skipWhitespace();
      // "!=" is a comparison, not a not
      if (this.text.startsWith("!=", this.pos)) {
        throw notAllowed();
      }
      if (acceptSymbol("!") || acceptWord("not")) {
        enter();
        Node node = new NotNode(parseNot());
        this.depth--;
        return node;
      }
      return parsePrimary();
    }

    /**
     * @return parenthesized expression or memberOf call
     */
    Node parsePrimary() {
      skipWhitespace();
      if (acceptSymbol("(")) {
        Node node = parseOr();
        if (!acceptSymbol(")")) {
          throw notAllowed();
        }
        return node;
      }
      // entity.memberOf exactly, no spaces around the dot, then ( 'name' )
      if (this.text.startsWith(MEMBER_OF, this.pos)) {
        int end = this.pos + MEMBER_OF.length();
        if (end < this.text.length() && isIdentifierChar(this.text.charAt(end))) {
          throw notAllowed();
        }
        this.pos = end;
        if (!acceptSymbol("(")) {
          throw notAllowed();
        }
        skipWhitespace();
        if (atEnd() || this.text.charAt(this.pos) != '\'') {
          throw notAllowed();
        }
        this.pos++;
        int start = this.pos;
        while (!atEnd() && this.text.charAt(this.pos) != '\'') {
          char c = this.text.charAt(this.pos);
          if (c == '\\' || c < ' ' || c == 127) {
            throw notAllowed();
          }
          this.pos++;
        }
        if (atEnd() || this.pos == start) {
          throw notAllowed();
        }
        String groupName = this.text.substring(start, this.pos);
        this.pos++;
        if (!acceptSymbol(")")) {
          throw notAllowed();
        }
        return new MemberOfNode(groupName);
      }
      throw notAllowed();
    }

    /**
     * bound nesting
     */
    void enter() {
      this.depth++;
      if (this.depth > 100) {
        throw new ScriptNotAllowedException("Scripted group script is nested too deeply");
      }
    }

    /**
     * @param c
     * @return true for letters, digits, underscore and dollar
     */
    static boolean isIdentifierChar(char c) {
      return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }
  }

  // ---------------------------------------------------------------------------------------
  // tree (same node kinds 5.x+ accept from the JEXL AST: and, or, not, memberOf)
  // ---------------------------------------------------------------------------------------

  /**
   * a parsed node
   */
  abstract static class Node {

    /**
     * @param entity
     * @return value
     */
    abstract boolean evaluate(GrouperAbacEntity entity);

    /**
     * @param groupNames add names here
     */
    abstract void collectGroupNames(Collection<String> groupNames);
  }

  /**
   * and / or
   */
  private static class BinaryNode extends Node {

    /** true for and, false for or */
    private final boolean and;

    /** left */
    private final Node left;

    /** right */
    private final Node right;

    /**
     * @param and
     * @param left
     * @param right
     */
    BinaryNode(boolean and, Node left, Node right) {
      this.and = and;
      this.left = left;
      this.right = right;
    }

    @Override
    boolean evaluate(GrouperAbacEntity entity) {
      if (this.and) {
        return this.left.evaluate(entity) && this.right.evaluate(entity);
      }
      return this.left.evaluate(entity) || this.right.evaluate(entity);
    }

    @Override
    void collectGroupNames(Collection<String> groupNames) {
      this.left.collectGroupNames(groupNames);
      this.right.collectGroupNames(groupNames);
    }
  }

  /**
   * not
   */
  private static class NotNode extends Node {

    /** child */
    private final Node child;

    /**
     * @param child
     */
    NotNode(Node child) {
      this.child = child;
    }

    @Override
    boolean evaluate(GrouperAbacEntity entity) {
      return !this.child.evaluate(entity);
    }

    @Override
    void collectGroupNames(Collection<String> groupNames) {
      this.child.collectGroupNames(groupNames);
    }
  }

  /**
   * entity.memberOf('name')
   */
  private static class MemberOfNode extends Node {

    /** group name */
    private final String groupName;

    /**
     * @param groupName
     */
    MemberOfNode(String groupName) {
      this.groupName = groupName;
    }

    @Override
    boolean evaluate(GrouperAbacEntity entity) {
      return entity.memberOf(this.groupName);
    }

    @Override
    void collectGroupNames(Collection<String> groupNames) {
      groupNames.add(this.groupName);
    }
  }

  /**
   * @param strings
   * @return comma separated
   */
  private static String join(List<String> strings) {
    StringBuilder result = new StringBuilder();
    for (String string : strings) {
      if (result.length() > 0) {
        result.append(", ");
      }
      result.append(string);
    }
    return result.toString();
  }
}
