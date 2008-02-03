/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;

/**
 * member filter for retrieving members.
 * @author mchyzer
 *
 */
public enum WsMemberFilter {
	/** retrieve all members (immediate and effective) */
    All {
    	
    	/**
    	 * get the members from the group
         * @param group
         * @return the set of members (non null)
    	 */
    	@Override
		@SuppressWarnings("unchecked")
		public Set<Member> getMembers(Group group) {
    		return GrouperServiceUtils.nonNull(group.getMembers());
    	}
    }, 
    
    /** retrieve non direct (non immediate) members */
    EffectiveMembers {
    	
    	/**
    	 * get the composite members from the group
         * @param group
         * @return the set of members (non null)
    	 */
    	@Override
    	@SuppressWarnings("unchecked")
		public Set<Member> getMembers(Group group) {
    		return GrouperServiceUtils.nonNull(group.getEffectiveMembers());
    	}
    }, 
    
    /** return only direct members, not indirect */
    ImmediateMembers {
    	
    	/**
    	 * get the composite members from the group
         * @param group
         * @return the set of members (non null)
    	 */
    	@Override
    	@SuppressWarnings("unchecked")
		public Set<Member> getMembers(Group group) {
    		return GrouperServiceUtils.nonNull(group.getImmediateMembers());
    	}
    }, 
    
    /** if this is a composite group, then return the two groups
     * which make up the composition (and the group math operator (union, minus, etc)
     */
    CompositeMembers {
    	
    	/**
    	 * get the composite members from the group
         * @param group
         * @return the set of members (non null)
    	 */
    	@Override
    	@SuppressWarnings("unchecked")
		public Set<Member> getMembers(Group group) {
    		return GrouperServiceUtils.nonNull(group.getCompositeMembers());
    	}
    };

    /**
     * get the members from the group based on type of filter
     * @param group
     * @return the set of members (non null)
     */
    public abstract Set<Member> getMembers(Group group);

    /**
     * do a case-insensitive matching
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
    	StringBuilder error = new StringBuilder("Cant find wsMemberFilter from string: '").append(string);
    	error.append("', expecting one of: ");
    	for (WsMemberFilter wsMemberFilter : WsMemberFilter.values()) {
    		error.append(wsMemberFilter.name()).append(", ");
    	}
    	throw new RuntimeException(error.toString());
    }
}
