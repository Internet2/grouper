/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemNotFoundException;

/**
 * <pre>
 * Class to lookup a stem via web service
 * 
 * developers make sure each setter calls this.clearSubject();
 * TODO: add in extensions in the query
 * </pre>
 * @author mchyzer
 */
public class WsStemLookup {
	
	/**
	 * logger 
	 */
	private static final Log LOG = LogFactory.getLog(WsSubjectLookup.class);

	/** find the stem */
	private Stem stem = null;
	
	/** result of stem find */
	private enum StemFindResult {
		
		/** found the stem */
		SUCCESS, 
		
		/** cant find the subject */
		STEM_NOT_FOUND,
		
		/** incvalid query (e.g. if everything blank) */
		INVALID_QUERY;
		
		/**
		 * if this is a successful result
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == SUCCESS;
		}
	}

	/**
	 * uuid of the stem to find
	 */
	private String uuid;
	
	/**
	 * <pre>
	 * 
	 * Note: this is not a javabean property because we dont want it in the web service
	 * </pre>
	 * @return the stem
	 */
	public Stem retrieveStem() {
		return this.stem;
	}

	/**
	 * <pre>
	 * 
	 * Note: this is not a javabean property because we dont want it in the web service
	 * </pre>
	 * @return the subjectFindResult, this is never null
	 */
	public StemFindResult retrieveStemFindResult() {
		return this.stemFindResult;
	}

    /**
     * make sure this is an explicit toString
     */
    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }

	/**
	 * pass in a grouper session
	 * @param grouperSession 
	 */
	public void retrieveStemIfNeeded(GrouperSession grouperSession) {
		//see if we already retrieved
		if (this.stemFindResult != null) {
			return;
		}
		try {
			//assume success (set otherwise if there is a problem)
			this.stemFindResult = StemFindResult.SUCCESS;
			
			boolean hasUuid = !StringUtils.isBlank(this.uuid);
			
			boolean hasName = !StringUtils.isBlank(this.stemName);

			//must have a name or uuid
			if (!hasUuid && !hasName) {
				this.stemFindResult = StemFindResult.INVALID_QUERY;
				String logMessage = "Invalid query: " + this;
				LOG.warn(logMessage);
				//TODO remove:
				System.out.println(logMessage);
				return;
			}
			
			if (hasName) {
				this.stem = StemFinder.findByName(grouperSession, this.stemName);
			} else if (hasUuid) {
				this.stem = StemFinder.findByUuid(grouperSession, this.uuid);
			}
			
		} catch (StemNotFoundException gnf) {
			LOG.warn(this, gnf);
			this.stemFindResult = StemFindResult.STEM_NOT_FOUND;
			//TODO remove
			gnf.printStackTrace();
		}
		
	}

	
	/**
	 * clear the subject if a setter is called
	 */
	private void clearStem() {
		this.stem = null;
		this.stemFindResult = null;
	}
	
	/** name of the stem to find (includes stems, e.g. stem1:stem2:stemName */
	private String stemName;
	
	/** result of subject find */
	private StemFindResult stemFindResult = null;

	/**
	 * uuid of the stem to find
	 * @return the uuid
	 */
	public String getUuid() {
		return this.uuid;
	}

	/**
	 * uuid of the stem to find
	 * @param uuid1 the uuid to set
	 */
	public void setUuid(String uuid1) {
		this.uuid = uuid1;
		this.clearStem();
	}

	/**
	 * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
	 * @return the theName
	 */
	public String getStemName() {
		return this.stemName;
	}

	/**
	 * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
	 * @param theName the theName to set
	 */
	public void setStemName(String theName) {
		this.stemName = theName;
		this.clearStem();
	}

	/**
	 * 
	 */
	public WsStemLookup() {
		//blank
	}

	/**
	 * @param stemName1 
	 * @param uuid1
	 */
	public WsStemLookup(String stemName1, String uuid1) {
		this.uuid = uuid1;
		this.setStemName(stemName1);
	}
		
}
