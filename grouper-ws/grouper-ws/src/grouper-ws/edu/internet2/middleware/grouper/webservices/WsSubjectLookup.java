/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * <pre>
 * template to lookup a subject
 * 
 * developers make sure each setter calls this.clearSubject();
 * 
 * TODO: add logging
 * </pre>
 * @author mchyzer
 */
public class WsSubjectLookup {
	
	/** find the subject */
	private Subject subject = null;
	
	/**
	 * result of a subject find
	 *
	 */
	static enum SubjectFindResult {
		
		/** found the subject */
		SUCCESS, 
		
		/** found multiple results */
		SUBJECT_NOT_UNIQUE, 
		
		/** cant find the subject */
		SUBJECT_NOT_FOUND,
		
		/** invalid query (e.g. if everything blank) */
		INVALID_QUERY,
		
		/** when the source if not available */
		SOURCE_UNAVAILABLE;
		
		/**
		 * if this is a successful result
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == SUCCESS;
		}
	};
	
	/** result of subject find */
	private SubjectFindResult subjectFindResult = null;
	
    /** logger */
    private static final Log LOG = LogFactory.getLog(WsSubjectLookup.class);

    /**
     * 
     */
    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
    
    /**
     * see if there is a blank query (if there is not id or identifier
     * @return true or false
     */
    public boolean isBlank() {
    	return StringUtils.isBlank(this.subjectId) 
    		&& StringUtils.isBlank(subjectIdentifier);
    }
    
	/**
	 * 
	 * @return
	 */
	private void retrieveSubjectIfNeeded() {
		//see if we already retrieved
		if (this.subjectFindResult != null) {
			return;
		}
		try {
			//assume success (set otherwise if ther is a proble
			this.subjectFindResult = SubjectFindResult.SUCCESS;
			
			boolean hasSubjectId = !StringUtils.isBlank(this.subjectId);
			boolean hasSubjectIdentifier = !StringUtils.isBlank(this.subjectIdentifier);
			boolean hasSubjectSource = !StringUtils.isBlank(this.subjectSource);
			boolean hasSubjectType = !StringUtils.isBlank(this.subjectType);

			//must have an id
			if (!hasSubjectId && !hasSubjectIdentifier) {
				this.subjectFindResult = SubjectFindResult.INVALID_QUERY;
				LOG.warn("Invalid query: " + this);
				return;
			}
			
			if (hasSubjectId) {
				
				//cant have source without type
				if (hasSubjectSource && !hasSubjectType) {
					this.subjectFindResult = SubjectFindResult.INVALID_QUERY;
					LOG.warn("Invalid query: " + this);
					return;
				}
				if (hasSubjectType) {
					if (hasSubjectSource) {
						
						this.subject = SubjectFinder.findById(this.subjectId, this.subjectType, this.subjectSource);
						return;
					} 
					this.subject = SubjectFinder.findById(this.subjectId, this.subjectType);
					return;
				}
				this.subject = SubjectFinder.findById(this.subjectId);
				return;
			} else if (hasSubjectIdentifier) {
				
				//cant have source without type
				if (hasSubjectSource && !hasSubjectType) {
					this.subjectFindResult = SubjectFindResult.INVALID_QUERY;
					LOG.warn("Invalid query: " + this);
					return;
				}
				if (hasSubjectType) {
					if (hasSubjectSource) {
						
						this.subject = SubjectFinder.findByIdentifier(this.subjectId, this.subjectType, this.subjectSource);
						return;
					} 
					this.subject = SubjectFinder.findByIdentifier(this.subjectId, this.subjectType);
					return;
				}
				this.subject = SubjectFinder.findByIdentifier(this.subjectId);
				return;
				
			}
			
		} catch (SourceUnavailableException sue) {
			LOG.warn(this, sue);
			this.subjectFindResult = SubjectFindResult.SOURCE_UNAVAILABLE;
		} catch (SubjectNotUniqueException snue) {
			LOG.warn(this, snue);
			this.subjectFindResult = SubjectFindResult.SUBJECT_NOT_UNIQUE;
		} catch (SubjectNotFoundException snfe) {
			LOG.warn(this, snfe);
			this.subjectFindResult = SubjectFindResult.SUBJECT_NOT_FOUND;
		}
		
	}
	
	/**
	 * clear the subject if a setter is called
	 */
	private void clearSubject() {
		this.subject = null;
		this.subjectFindResult = null;
	}
	
	/** the one id of the subject */
	private String subjectId;
	
	/** any identifier of the subject */
	private String subjectIdentifier;
	
	/** optional: type of subject, can be anything in SubjectTypeEnum: PERSON, GROUP, APPLICATION */
	private String subjectType;

	/** optional: source of subject in the subject api source list */
	private String subjectSource;

	/**
	 * optional: type of subject, can be anything in SubjectTypeEnum: PERSON, GROUP, APPLICATION
	 * @return the subjectType
	 */
	public String getSubjectType() {
		return subjectType;
	}

	/**
	 * optional: type of subject, can be anything in SubjectTypeEnum: PERSON, GROUP, APPLICATION
	 * @param subjectType the subjectType to set
	 */
	public void setSubjectType(String subjectType) {
		this.subjectType = subjectType;
		this.clearSubject();
	}

	/**
	 * optional: source of subject in the subject api source list
	 * @return the subjectSource
	 */
	public String getSubjectSource() {
		return subjectSource;
	}

	/**
	 * optional: source of subject in the subject api source list
	 * @param subjectSource the subjectSource to set
	 */
	public void setSubjectSource(String subjectSource) {
		this.subjectSource = subjectSource;
		this.clearSubject();
	}

	/**
	 * <pre>
	 * 
	 * Note: this is not a javabean property because we dont want it in the web service
	 * </pre>
	 * @return the subject
	 */
	public Subject retrieveSubject() {
		this.retrieveSubjectIfNeeded();
		return subject;
	}

	/**
	 * <pre>
	 * 
	 * Note: this is not a javabean property because we dont want it in the web service
	 * </pre>
	 * @return the subjectFindResult, this is never null
	 */
	public SubjectFindResult retrieveSubjectFindResult() {
		this.retrieveSubjectIfNeeded();
		return subjectFindResult;
	}

	/**
	 * id of the subject
	 * @return the subjectId
	 */
	public String getSubjectId() {
		return subjectId;
	}

	/**
	 * id of the subject
	 * @param subjectId the subjectId to set
	 */
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
		this.clearSubject();
	}

	/**
	 * any identifier of the subject
	 * @return the subjectIdentifier
	 */
	public String getSubjectIdentifier() {
		return subjectIdentifier;
	}

	/**
	 * any identifier of the subject
	 * @param subjectIdentifier the subjectIdentifier to set
	 */
	public void setSubjectIdentifier(String subjectIdentifier) {
		this.subjectIdentifier = subjectIdentifier;
		this.clearSubject();
	}
}
