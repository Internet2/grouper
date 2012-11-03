package edu.internet2.middleware.authzStandardApiClient.corebeans;

import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientJsonIndenter;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientUtils;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientXstreamUtils;

/**
 * This is the meta object in a response, or extend this if 
 * @author mchyzer
 *
 */
public class AsacMetaResponse {
  
  public static void main(String[] args) {
    
    AsacMetaResponse asacMetaResponse = new AsacMetaResponse();
    asacMetaResponse.setLastModified("abc");
    asacMetaResponse.setSelfUri("bcd");
    
    String json = StandardApiClientUtils.jsonConvertToNoWrap(asacMetaResponse);
    
    json = new StandardApiClientJsonIndenter(json).result();
    
    System.out.println(json);
    
    asacMetaResponse = (AsacMetaResponse)StandardApiClientUtils.jsonConvertFrom(json, AsacMetaResponse.class);

    System.out.println(asacMetaResponse.getLastModified());
    System.out.println(asacMetaResponse.getSelfUri());
    
    json = "{\"lastModified\":\"abc\", \"selfUri\":\"bcd\", \"xyzWhatever\":\"cde\"}";

    asacMetaResponse = StandardApiClientUtils.jsonConvertFrom(json, AsacMetaResponse.class);

    System.out.println(asacMetaResponse.getLastModified());
    System.out.println(asacMetaResponse.getSelfUri());

    String xml = StandardApiClientUtils.xmlConvertTo(asacMetaResponse);
    
    xml = StandardApiClientUtils.indent(xml, true);
    
    System.out.println(xml);
    
    asacMetaResponse = StandardApiClientUtils.xmlConvertFrom(xml, AsacMetaResponse.class);
    
    System.out.println(asacMetaResponse.getLastModified());
    System.out.println(asacMetaResponse.getSelfUri());

    xml = "<metaResponse x=\"whatever\"><lastModified>abc</lastModified>" +
    		"<selfUri>bcd</selfUri>" +
    		"<somethingElseXyz>sdfsdf</somethingElseXyz></metaResponse>";

    asacMetaResponse = StandardApiClientUtils.xmlConvertFrom(xml, AsacMetaResponse.class);
    
    System.out.println(asacMetaResponse.getLastModified());
    System.out.println(asacMetaResponse.getSelfUri());

  
    
  }
  
  /** timestamp this resource was last modified, e.g. 2012-10-04T03:10Z */
  private String lastModified;
  
  /**
   * timestamp this resource was last modified, e.g. 2012-10-04T03:10Z
   * @return the lastModified
   */
  public String getLastModified() {
    return this.lastModified;
  }

  
  /**
   * @param lastModified1 the lastModified to set
   */
  public void setLastModified(String lastModified1) {
    this.lastModified = lastModified1;
  }

  /**
   * Self URI to this resource 
   * e.g. https://groups.institution.edu/groupsApp/groupsApi 
   */
  private String selfUri;
  
  /**
   * Self URI to this resource 
   * e.g. https://groups.institution.edu/groupsApp/groupsApi 
   * @return the selfUri
   */
  public String getSelfUri() {
    return this.selfUri;
  }

  
  /**
   * Self URI to this resource 
   * e.g. https://groups.institution.edu/groupsApp/groupsApi 
   * @param selfUri1 the selfUri to set
   */
  public void setSelfUri(String selfUri1) {
    this.selfUri = selfUri1;
  }
  
  
  
}
