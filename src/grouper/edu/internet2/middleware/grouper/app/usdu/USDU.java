package edu.internet2.middleware.grouper.app.usdu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;

/**
 * The Unresolvable Subject Deletion Utility finds and optionally deletes
 * members whose subjects can not be resolved by their source.
 * 
 * Documentation is available via the 'Unresolvable Subject Deletion Utility' on
 * the Grouper Product wiki
 * https://wiki.internet2.edu/confluence/display/GrouperWG/Grouper+Product
 */

public class USDU {

  // store the identifier for the GrouperSourceAdapter, probably "g:gsa"
  private static String grouperSourceAdapterId = null;

  // map list names to corresponding privileges, a better way probably exists
  private static Map<String, Privilege> list2priv = new HashMap<String, Privilege>();
  static {
    list2priv.put("admins", AccessPrivilege.ADMIN);
    list2priv.put("optins", AccessPrivilege.OPTIN);
    list2priv.put("optouts", AccessPrivilege.OPTOUT);
    list2priv.put("readers", AccessPrivilege.READ);
    list2priv.put("updaters", AccessPrivilege.UPDATE);
    list2priv.put("viewers", AccessPrivilege.VIEW);
    list2priv.put("creators", NamingPrivilege.CREATE);
    list2priv.put("stemmers", NamingPrivilege.STEM);
  }

  /**
   * Run {@link USDU}.
   * 
   * <pre class="eg">
   * // to print usage
   * usdu.sh
   * // or
   * usdu.bat
   * </pre>
   * 
   * @since 1.3.0
   */
  public static void main(String[] args) {

    Options options = new Options();
    OptionGroup optionGroup = new OptionGroup();
    optionGroup.addOption(new Option("all", false, "find unresolvable subjects from all sources"));
    optionGroup.addOption(new Option("source", true, "find unresolvable subjects from source"));
    optionGroup.addOption(new Option("uuid", true, "find unresolvable subject with member uuid"));
    optionGroup.setRequired(true);
    options.addOptionGroup(optionGroup);
    options.addOption("delete", false, "delete memberships and privileges");
    options.addOption("start", true, "start session as this subject, default GrouperSystem");

    if (args.length == 0) {
      printUsage(options);
      System.exit(0);
    }

    CommandLineParser parser = new GnuParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      printUsage(options);
      System.exit(1);
    }

    try {
      GrouperSession s = null;
      if (line.hasOption("start")) {
        s = GrouperSession.start(SubjectFinder.findByIdentifier(line.getOptionValue("start")));
      } else {
        s = GrouperSession.start(SubjectFinder.findRootSubject());
      }

      if (line.hasOption("uuid")) {
        resolveMember(s, line.getOptionValue("uuid"), line.hasOption("delete"));
      } else if (line.hasOption("all")) {
        resolveMembers(s, line.hasOption("delete"));
      } else if (line.hasOption("source")) {
        resolveMembers(s, SubjectFinder.getSource(line.getOptionValue("source")), line.hasOption("delete"));
      } else {
        printUsage(options);
        System.exit(0);
      }

    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }

    System.exit(0);
  }

  private static void printUsage(Options options) {

    System.out.println();

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(USDU.class.getSimpleName(), options, true);

    System.out.println();
    System.out.println("Unresolvable subjects are printed to stdout.");
    System.out.println();
    System.out.println("If an unresolvable subject is not a member of any groups:");
    System.out.println(" member_uuid='<uuid>' subject='<id>' no_memberships");
    System.out.println();
    System.out.println("For every group or stem and list that an unresolvable subject is a member of:");
    System.out.println(" member_uuid='<uuid>' subject='<id>' group|stem='<name>' list='<name>' [delete]");
    System.out.println();
  }

  /**
   * Find and optionally delete memberships and privileges for the unresolvable
   * subject with given member uuid.
   * 
   * @param s
   *          the Grouper session
   * @param uuid
   *          the uuid of the member
   * @param delete
   *          if true will delete memberships and privileges
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws GroupNotFoundException
   * @throws MemberDeleteException
   * @throws MemberNotFoundException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   * @throws StemNotFoundException
   */
  public static void resolveMember(GrouperSession s, String uuid, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, GroupNotFoundException, MemberDeleteException, MemberNotFoundException,
      RevokePrivilegeException, SchemaException, SourceUnavailableException, StemNotFoundException {

    Member member;
    try {
      member = MemberFinder.findByUuid(s, uuid);
    } catch (MemberNotFoundException e) {
      System.out.println("member with uuid '" + uuid + "' not found");
      return;
    }

    if (isMemberResolvable(s, member)) {
      System.out.println("member " + member + " is resolvable");
      return;
    }

    Set<Member> members = new HashSet<Member>();
    members.add(member);
    resolveMembers(members, delete);
  }

  /**
   * Find and optionally delete memberships and privileges for unresolvable
   * subjects from all sources.
   * 
   * @param s
   *          the Grouper session
   * @param delete
   *          if true will delete memberships and privileges
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws GroupNotFoundException
   * @throws MemberDeleteException
   * @throws MemberNotFoundException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   * @throws StemNotFoundException
   */
  public static void resolveMembers(GrouperSession s, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, GroupNotFoundException, MemberDeleteException, MemberNotFoundException,
      RevokePrivilegeException, SchemaException, SourceUnavailableException, StemNotFoundException {

    resolveMembers(s, null, delete);
  }

  /**
   * Find and optionally delete memberships and privileges for unresolvable
   * subjects from the specified source.
   * 
   * @param s
   *          the Grouper session
   * @param delete
   *          if true will delete memberships and privileges
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws GroupNotFoundException
   * @throws MemberDeleteException
   * @throws MemberNotFoundException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   * @throws StemNotFoundException
   */
  public static void resolveMembers(GrouperSession s, Source source, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, GroupNotFoundException, MemberDeleteException, MemberNotFoundException,
      RevokePrivilegeException, SchemaException, SourceUnavailableException, StemNotFoundException {

    resolveMembers(getUnresolvableMembers(s, source), delete);
  }

  /**
   * Print to stdout and optionally delete memberships and privileges for the
   * given unresolvable subjects.
   * 
   * @param unresolvables
   *          a set of unresolvable members
   * @param delete
   *          if true will delete memberships and privileges
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws GroupNotFoundException
   * @throws MemberDeleteException
   * @throws MemberNotFoundException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   * @throws StemNotFoundException
   */
  protected static void resolveMembers(Set<Member> unresolvables, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, GroupNotFoundException, MemberDeleteException, MemberNotFoundException,
      RevokePrivilegeException, SchemaException, SourceUnavailableException, StemNotFoundException {

    Set<Field> fields = getMemberFields();

    for (Member member : unresolvables) {
      Set<Membership> memberships = getAllImmediateMemberships(member, fields);
      if (memberships.isEmpty()) {
        System.out.println("member_uuid='" + member.getUuid() + "' subject=" + member + " no_memberships");
      } else {
        for (Membership membership : memberships) {

          System.out.print("member_uuid='" + member.getUuid() + "' subject=" + member);
          if (membership.getList().getType().equals(FieldType.LIST)
              || membership.getList().getType().equals(FieldType.ACCESS)) {
            System.out.print(" group='" + membership.getGroup().getName());
          }
          if (membership.getList().getType().equals(FieldType.NAMING)) {
            System.out.print(" stem='" + membership.getStem().getName());
          }
          System.out.print(" list='" + membership.getList().getName() + "'");

          if (delete) {
            System.out.print(" delete");
            if (membership.getList().getType().equals(FieldType.LIST)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getGroup(), membership.getList());
            }
            if (membership.getList().getType().equals(FieldType.ACCESS)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getGroup(), getPrivilege(membership
                  .getList()));
            }
            if (membership.getList().getType().equals(FieldType.NAMING)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getStem(), getPrivilege(membership
                  .getList()));
            }
          }
          System.out.println();
        }
      }
    }
  }

  /**
   * Get memberships for a member for the given fields.
   * 
   * @param member
   * @param fields
   *          a set of 'list' fields
   * @return a set of memberships
   * @throws SchemaException
   */
  protected static Set<Membership> getAllImmediateMemberships(Member member, Set<Field> fields) throws SchemaException {

    Set<Membership> memberships = new LinkedHashSet<Membership>();
    for (Field field : fields) {
      for (Object m : member.getImmediateMemberships(field)) {
        memberships.add((Membership) m);
      }
    }
    return memberships;
  }

  /**
   * Delete unresolvable member from group and field.
   * 
   * @param member
   * @param group
   * @param field
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws MemberDeleteException
   * @throws SourceUnavailableException
   * @throws SchemaException
   */
  public static void deleteUnresolvableMember(Member member, Group group, Field field) throws IllegalArgumentException,
      InsufficientPrivilegeException, MemberDeleteException, SourceUnavailableException, SchemaException {

    group.deleteMember(getUSDUSubject(member), field);
  }

  /**
   * Revoke unresolvable member's privilege from group.
   * 
   * @param member
   * @param group
   * @param privilege
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   */
  public static void deleteUnresolvableMember(Member member, Group group, Privilege privilege)
      throws IllegalArgumentException, InsufficientPrivilegeException, RevokePrivilegeException, SchemaException,
      SourceUnavailableException {

    group.revokePriv(getUSDUSubject(member), privilege);
  }

  /**
   * Revoke unresolvable member's privilege from stem.
   * 
   * @param member
   * @param stem
   * @param privilege
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   */
  public static void deleteUnresolvableMember(Member member, Stem stem, Privilege privilege)
      throws IllegalArgumentException, InsufficientPrivilegeException, RevokePrivilegeException, SchemaException,
      SourceUnavailableException {

    stem.revokePriv(getUSDUSubject(member), privilege);
  }

  /**
   * Return a subject suitable for deleting membership and privileges.
   * 
   * @param member
   * @return a contrived subject
   * @throws IllegalArgumentException
   * @throws SourceUnavailableException
   */
  protected static Subject getUSDUSubject(Member member) throws IllegalArgumentException, SourceUnavailableException {

    return new USDUSubject(member.getSubjectId(), member.getSubjectSourceId(), member.getSubjectType());
  }

  /**
   * Return the identifier of the
   * <code>GrouperSourceAdapter/code>, probably but not
   * definitely 'g:gsa'.
   * 
   * @return
   *  the GrouperSourceAdapter identifier
   */
  protected static String getGrouperSourceAdapterId() {

    if (grouperSourceAdapterId == null) {

      for (Object source : SubjectFinder.getSources()) {
        if (((Source) source) instanceof GrouperSourceAdapter) {
          grouperSourceAdapterId = ((Source) source).getId();
        }
      }

      if (grouperSourceAdapterId == null) {
        throw new RuntimeException("Cannot find GrouperSourceAdapter");
      }
    }

    return grouperSourceAdapterId;
  }

  /**
   * Get fields of which a subject might be a member. Includes all fields of
   * type FieldType.LIST, FieldType.ACCESS, and FieldType.NAMING.
   * 
   * @return set of fields
   * @throws SchemaException
   */
  protected static Set<Field> getMemberFields() throws SchemaException {

    Set<Field> listFields = new LinkedHashSet<Field>();
    for (Object field : FieldFinder.findAllByType(FieldType.LIST)) {
      listFields.add((Field) field);
    }
    for (Object field : FieldFinder.findAllByType(FieldType.ACCESS)) {
      listFields.add((Field) field);
    }
    for (Object field : FieldFinder.findAllByType(FieldType.NAMING)) {
      listFields.add((Field) field);
    }
    return listFields;
  }

  /**
   * Map fields to privileges.
   * 
   * @param field
   * @return the privilege matching the given field or null
   */
  protected static Privilege getPrivilege(Field field) {

    return list2priv.get(field.getName());
  }

  /**
   * Find members whose subjects can not be found by their source.
   * 
   * @param s
   *          GrouperSession
   * @param source
   *          if null will find members from all sources
   * @return unresolvable members
   */
  public static Set<Member> getUnresolvableMembers(GrouperSession s, Source source) {

    Set<Member> members = new LinkedHashSet<Member>();

    for (Object m : MemberFinder.findAll(s, source)) {
      Member member = (Member) m;
      if (!isMemberResolvable(s, member)) {
        members.add(member);
      }
    }

    return members;
  }

  /**
   * Check if this member's subject can be found in a source.
   * 
   * @param s
   * @param member
   * @return Boolean true if member's subject is found in source
   */
  public static boolean isMemberResolvable(GrouperSession s, Member member) {

    /*
     * Speedup ala Gary Brown: Calling member.getSubject() causes a
     * GrouperSubject to be initialised which calls group.getAttributes() which
     * might be expensive for groups with a large number of attributes. If this
     * is the GrouperSourceAdapter (g:gsa) source call GroupFinder.findByUuid
     * instead.
     */
    if (member.getSubjectSourceId().equals(getGrouperSourceAdapterId())) {
      try {
        GroupFinder.findByUuid(s, member.getSubjectId());
        return true;
      } catch (GroupNotFoundException e) {
        return false;
      }
    }

    try {
      member.getSubject();
      return true;
    } catch (SubjectNotFoundException e) {
      return false;
    }
  }

  /**
   * A {@link Subject} implementation which consists of a subject id,
   * {@link Source}, and {@link SubjectType}. The source is looked up from the
   * given source identifier string since Member.getSubjectSource() will return
   * a {@link SubjectNotFoundException}.
   */
  private static class USDUSubject implements Subject {

    private String id;
    private SubjectType type;
    private Source source;

    private USDUSubject(String id, String sourceId, SubjectType type) throws IllegalArgumentException,
        SourceUnavailableException {

      this.id = id;
      this.type = type;
      this.source = SubjectFinder.getSource(sourceId);
    }

    public String getAttributeValue(String arg0) {

      return null;
    }

    public Set<?> getAttributeValues(String arg0) {

      return null;
    }

    public Map<?, ?> getAttributes() {

      return null;
    }

    public String getDescription() {

      return null;
    }

    public String getId() {

      return id;
    }

    public String getName() {

      return id;
    }

    public Source getSource() {

      return source;
    }

    public SubjectType getType() {

      return type;
    }
  }
}
