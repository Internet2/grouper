/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.subject.Subject;

/**
 * member filter for retrieving members.
 * 
 * @author mchyzer
 * 
 */
public enum WsMemberFilter {
	/** retrieve all members (immediate and effective) */
	All {

		/**
		 * get the members from the group
		 * 
		 * @param group
		 * @return the set of members (non null)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Set<Member> getMembers(Group group) {
			return GrouperServiceUtils.nonNull(group.getMembers());
		}

		/**
		 * get the composite memberships from the group
		 * 
		 * @param group
		 * @return the set of members (non null)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Set<Membership> getMemberships(Group group) {
			return GrouperServiceUtils.nonNull(group.getMemberships());
		}

		/**
		 * see if a group has a subject as member
		 * 
		 * @param group
		 * @return true|false
		 */
		@Override
		@SuppressWarnings("unchecked")
		public boolean hasMember(Group group, Subject subject) {
			return group.hasMember(subject);
		}

		/**
		 * see if a group has a subject as member
		 * 
		 * @param group
		 * @return true|false
		 * @throws SchemaException
		 */
		@Override
		@SuppressWarnings("unchecked")
		public boolean hasMember(Group group, Subject subject, Field field)
				throws SchemaException {
			return group.hasMember(subject, field);
		}

		/**
		 * get groups for subject
		 * 
		 * @param member
		 * @return the set of members (non null)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Set<Group> getGroups(Member member) {
			return GrouperServiceUtils.nonNull(member.getGroups());
		}
	},

	/** retrieve non direct (non immediate) members */
	Effective {

		/**
		 * get the composite members from the group
		 * 
		 * @param group
		 * @return the set of members (non null)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Set<Member> getMembers(Group group) {
			return GrouperServiceUtils.nonNull(group.getEffectiveMembers());
		}

		/**
		 * get the composite memberships from the group
		 * 
		 * @param group
		 * @return the set of members (non null)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Set<Membership> getMemberships(Group group) {
			return GrouperServiceUtils.nonNull(group.getEffectiveMemberships());
		}

		/**
		 * see if a group has a subject as member
		 * 
		 * @param group
		 * @return true|false
		 */
		@Override
		@SuppressWarnings("unchecked")
		public boolean hasMember(Group group, Subject subject) {
			return group.hasEffectiveMember(subject);
		}

		/**
		 * see if a group has a subject as member
		 * 
		 * @param group
		 * @return true|false
		 * @throws SchemaException
		 */
		@Override
		@SuppressWarnings("unchecked")
		public boolean hasMember(Group group, Subject subject, Field field)
				throws SchemaException {
			return group.hasEffectiveMember(subject, field);
		}

		/**
		 * get groups for subject
		 * 
		 * @param member
		 * @return the set of members (non null)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Set<Group> getGroups(Member member) {
			return GrouperServiceUtils.nonNull(member.getEffectiveGroups());
		}
	},

	/** return only direct members, not indirect */
	Immediate {

		/**
		 * get the composite members from the group
		 * 
		 * @param group
		 * @return the set of members (non null)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Set<Member> getMembers(Group group) {
			return GrouperServiceUtils.nonNull(group.getImmediateMembers());
		}

		/**
		 * get the composite memberships from the group
		 * 
		 * @param group
		 * @return the set of members (non null)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Set<Membership> getMemberships(Group group) {
			return GrouperServiceUtils.nonNull(group.getImmediateMemberships());
		}

		/**
		 * see if a group has a subject as member
		 * 
		 * @param group
		 * @return true|false
		 */
		@Override
		@SuppressWarnings("unchecked")
		public boolean hasMember(Group group, Subject subject) {
			return group.hasImmediateMember(subject);
		}

		/**
		 * see if a group has a subject as member
		 * 
		 * @param group
		 * @return true|false
		 * @throws SchemaException
		 */
		@Override
		@SuppressWarnings("unchecked")
		public boolean hasMember(Group group, Subject subject, Field field)
				throws SchemaException {
			return group.hasImmediateMember(subject, field);
		}

		/**
		 * get groups for subject
		 * 
		 * @param member
		 * @return the set of members (non null)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Set<Group> getGroups(Member member) {
			return GrouperServiceUtils.nonNull(member.getImmediateGroups());
		}
	},

	/**
	 * if this is a composite group, then return the two groups which make up
	 * the composition (and the group math operator (union, minus, etc)
	 */
	Composite {

		/**
		 * get the composite members from the group
		 * 
		 * @param group
		 * @return the set of members (non null)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Set<Member> getMembers(Group group) {
			return GrouperServiceUtils.nonNull(group.getCompositeMembers());
		}

		/**
		 * get the composite memberships from the group
		 * 
		 * @param group
		 * @return the set of members (non null)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Set<Membership> getMemberships(Group group) {
			return GrouperServiceUtils.nonNull(group.getCompositeMemberships());
		}

		/**
		 * see if a group has a subject as member
		 * 
		 * @param group
		 * @return true|false
		 */
		@Override
		@SuppressWarnings("unchecked")
		public boolean hasMember(Group group, Subject subject) {
			throw new RuntimeException(
					"hasMember with composite is not supported: groupName: "
							+ group.getName() + ", subject: "
							+ subject.getName());
		}

		/**
		 * see if a group has a subject as member
		 * 
		 * @param group
		 * @return true|false
		 */
		@Override
		@SuppressWarnings("unchecked")
		public boolean hasMember(Group group, Subject subject, Field field) {
			throw new RuntimeException(
					"hasMember with composite is not supported: groupName: "
							+ group.getName() + ", subject: "
							+ subject.getName() + ", field: " + field.getName());
		}

		/**
		 * get groups for subject
		 * 
		 * @param member
		 * @return the set of members (non null)
		 */
		@Override
		public Set<Group> getGroups(Member member) {
			throw new RuntimeException(
					"getGroups with composite is not supported: member subject id: "
							+ member.getSubjectId());
		}

	};

	/**
	 * get the members from the group based on type of filter
	 * 
	 * @param group
	 * @return the set of members (non null)
	 */
	public abstract Set<Member> getMembers(Group group);

	/**
	 * get the memberships from the group based on type of filter
	 * 
	 * @param group
	 * @return the set of members (non null)
	 */
	public abstract Set<Membership> getMemberships(Group group);

	/**
	 * see if a subject is in a group
	 * 
	 * @param group
	 * @param subject
	 * @param field
	 * @return the set of members (non null)
	 * @throws SchemaException
	 */
	public abstract boolean hasMember(Group group, Subject subject, Field field)
			throws SchemaException;

	/**
	 * see if a subject is in a group
	 * 
	 * @param group
	 * @param subject
	 * @return the set of members (non null)
	 */
	public abstract boolean hasMember(Group group, Subject subject);

	/**
	 * get groups for subject
	 * 
	 * @param member
	 * @return the set of members (non null)
	 */
	public abstract Set<Group> getGroups(Member member);

	/**
	 * do a case-insensitive matching
	 * 
	 * @param string
	 * @return the enum of null or exception if not found
	 */
	public static WsMemberFilter valueOfIgnoreCase(String string) {
		if (StringUtils.isBlank(string)) {
			return null;
		}
		for (WsMemberFilter wsMemberFilter : WsMemberFilter.values()) {
			if (StringUtils.equalsIgnoreCase(string, wsMemberFilter.name())) {
				return wsMemberFilter;
			}
		}
		StringBuilder error = new StringBuilder(
				"Cant find wsMemberFilter from string: '").append(string);
		error.append("', expecting one of: ");
		for (WsMemberFilter wsMemberFilter : WsMemberFilter.values()) {
			error.append(wsMemberFilter.name()).append(", ");
		}
		throw new RuntimeException(error.toString());
	}
}
