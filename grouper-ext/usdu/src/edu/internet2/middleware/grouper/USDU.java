package edu.internet2.middleware.grouper;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;

/**
 * The Unresolvable Subject Deletion Utility finds and optionally deletes
 * members whose subjects can not be resolved by their source.
 */

public class USDU {

  private static final Log LOG = LogFactory.getLog(USDU.class);

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
    optionGroup.addOption(new Option("all", false, "find members with unresolvable subjects from all sources"));
    optionGroup.addOption(new Option("source", true, "find members with unresolvable subjects from source"));
    optionGroup.addOption(new Option("uuid", true, "find member with unresolvable subject and member uuid"));
    optionGroup.setRequired(true);
    options.addOptionGroup(optionGroup);
    options.addOption("delete", false, "delete members with unresolvable subjects");

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
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());

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
      e.printStackTrace();
      System.err.println(e.getMessage());
      System.exit(1);
    }

    System.exit(0);
  }

  private static void printUsage(Options options) {

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(USDU.class.getSimpleName(), options, true);

    System.out.println();
    System.out.println("Members with unresolvable subjects will be printed with the following format: ");
    System.out.println("'member uuid' 'subject id'/'subject type id'/'subject source id' 'group name' [delete]");
  }

  /**
   * Find and optionally delete unresolvable member with supplied member uuid.
   * 
   * @param s
   * @param uuid
   * @param delete
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws MemberDeleteException
   * @throws SourceUnavailableException
   */
  public static void resolveMember(GrouperSession s, String uuid, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, MemberDeleteException, SourceUnavailableException {

    Member member;
    try {
      member = MemberFinder.findByUuid(s, uuid);
    } catch (MemberNotFoundException e) {
      System.out.println("member with uuid '" + uuid + "' not found");
      return;
    }

    if (isMemberResolvable(member)) {
      System.out.println("member " + member + " is resolvable");
      return;
    }

    if (member.getImmediateGroups().isEmpty()) {
      System.out.println("member " + member + " is not resolvable, but is not a member of any groups");
      return;
    }

    Set<Member> members = new HashSet<Member>();
    members.add(member);
    resolveMembers(members, delete);
  }

  /**
   * Find and optionally delete unresolvable members from all sources.
   * 
   * @param s
   * @param delete
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws MemberDeleteException
   * @throws SourceUnavailableException
   */
  public static void resolveMembers(GrouperSession s, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, MemberDeleteException, SourceUnavailableException {

    resolveMembers(s, null, delete);
  }

  /**
   * Find and optionally delete unresolvable members from given source.
   * 
   * @param s
   * @param source
   * @param delete
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws MemberDeleteException
   * @throws SourceUnavailableException
   */
  public static void resolveMembers(GrouperSession s, Source source, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, MemberDeleteException, SourceUnavailableException {

    resolveMembers(getUnresolvableMembers(s, source), delete);
  }

  private static void resolveMembers(Set<Member> unresolvables, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, MemberDeleteException, SourceUnavailableException {

    for (Member member : unresolvables) {
      for (Object g : member.getImmediateGroups()) {
        Group group = (Group) g;
        System.out.print(member.getUuid() + " " + member + " '" + ((Group) group).getName() + "'");
        if (delete) {
          System.out.print(" delete");
          deleteUnresolvableMember(member, group);
        }
        System.out.println();
      }
    }
  }

  /**
   * Delete unresolvable member from group.
   * 
   * @param member
   * @param group
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws MemberDeleteException
   * @throws SourceUnavailableException
   */
  public static void deleteUnresolvableMember(Member member, Group group) throws IllegalArgumentException,
      InsufficientPrivilegeException, MemberDeleteException, SourceUnavailableException {

    LOG.info("deleting unresolvable member '" + member + "' from group '" + group.getName() + "'");
    group.deleteMember(new USDUSubject(member.getSubjectId(), member.getSubjectSourceId(), member.getSubjectType()));
  }

  /**
   * Find members whose subjects can not be found by their source.
   * 
   * @param s
   *          GrouperSession
   * @param source
   * @return
   */
  public static Set<Member> getUnresolvableMembers(GrouperSession s, Source source) {

    Set<Member> members = new LinkedHashSet<Member>();

    for (Object m : MemberFinder.findAll(s, source)) {
      Member member = (Member) m;
      if (!isMemberResolvable(member) && !member.getImmediateMemberships().isEmpty()) {
        members.add(member);
      }
    }

    return members;
  }

  /**
   * Check if this member's subject be found in a source.
   * 
   * @param member
   * @return Boolean true if member's subject is found in source
   */
  public static boolean isMemberResolvable(Member member) {

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
