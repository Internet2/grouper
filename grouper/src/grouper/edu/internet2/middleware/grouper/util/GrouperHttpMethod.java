package edu.internet2.middleware.grouper.util;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;


/**
 * Types of http methods complete with constructors.
 */
public enum GrouperHttpMethod{
	/**
	 * Post method
	 */
	post {
		@Override
		public HttpUriRequest newHttpMethod(String url) {
			return new HttpPost(url);
		}
	},
	
	/**
	 * Put method.
	 */
	put {
		@Override
		public HttpUriRequest newHttpMethod(String url) {
			return new HttpPut(url);
		}
	},
	
	/**
	 * Delete method.
	 */
	delete {
		@Override
		public HttpUriRequest newHttpMethod(String url) {
			return new HttpDelete(url);
		}
	},
	
	 /**
   * Patch method.
   */
  patch {
    @Override
    public HttpUriRequest newHttpMethod(String url) {
      return new HttpPatch(url);
    }
  },
  
	/**
	 * Get method.
	 */
	get {
		@Override
		public HttpUriRequest newHttpMethod(String url) {
			return new HttpGet(url);
		}
	};
	
	/**
	 * Constructor.
	 * @param url is the url to call.
	 * @return a new http method.
	 */
	public abstract HttpUriRequest newHttpMethod(String url);
	
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperHttpMethod valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperHttpMethod.class, 
        string, exceptionOnNull);

  }

	
}