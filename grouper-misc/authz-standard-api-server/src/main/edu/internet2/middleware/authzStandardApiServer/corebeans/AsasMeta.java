package edu.internet2.middleware.authzStandardApiServer.corebeans;

import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerJsonIndenter;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;

/**
 * This is the meta object in a response, or extend this if 
 * @author mchyzer
 *
 */
public class AsasMeta {
  
  
  /**
   * 
   */
  public AsasMeta() {
    super();
    this.setSuccess(true);
    this.setStatusCode("SUCCESS");
  }

  /**
   * response status text code
   */
  private String statusCode;

  /**
   * true or false if valid request
   */
  private boolean success;
  
  
  
  /**
   * response status text code
   * @return the statusCode
   */
  public String getStatusCode() {
    return this.statusCode;
  }

  
  /**
   * response status text code
   * @param statusCode1 the statusCode to set
   */
  public void setStatusCode(String statusCode1) {
    this.statusCode = statusCode1;
  }


  /** 
   * if there are warnings, they will be there
   */
  private StringBuilder resultWarning = new StringBuilder();

  /**
   * append error message to list of error messages
   * 
   * @param warning
   */
  public void appendWarning(String warning) {
    if (this.resultWarning.length() > 0) {
      this.resultWarning.append(", ");
    }
    this.resultWarning.append(warning);
  }

  /**
   * if there are warnings, they will be there
   * @return any warnings
   */
  public String getWarning() {
    return StandardApiServerUtils.trimToNull(this.resultWarning.toString());
  }

  /**
   * the builder for warnings
   * @return the builder for warnings
   */
  public StringBuilder warnings() {
    return this.resultWarning;
  }


  /**
   * @param resultWarnings1 the resultWarnings to set
   */
  public void setWarning(String resultWarnings1) {
    this.resultWarning = StandardApiServerUtils.isBlank(resultWarnings1) ? new StringBuilder() : new StringBuilder(resultWarnings1);
  }

  
  /**
   * true or false if valid request
   * @return the success
   */
  public boolean isSuccess() {
    return success;
  }

  
  /**
   * true or false if valid request
   * @param success the success to set
   */
  public void setSuccess(boolean success) {
    this.success = success;
  }



  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    AsasMeta asacMetaResponse = new AsasMeta();
    asacMetaResponse.setLastModified("abc");
    asacMetaResponse.setSelfUri("bcd");
    
    String json = StandardApiServerUtils.jsonConvertToNoWrap(asacMetaResponse);
    
    json = new StandardApiServerJsonIndenter(json).result();
    
    System.out.println(json);
    
    asacMetaResponse = (AsasMeta)StandardApiServerUtils.jsonConvertFrom(json, AsasMeta.class);

    System.out.println(asacMetaResponse.getLastModified());
    System.out.println(asacMetaResponse.getSelfUri());
    
    json = "{\"lastModified\":\"abc\", \"selfUri\":\"bcd\", \"xyzWhatever\":\"cde\"}";

    asacMetaResponse = (AsasMeta)StandardApiServerUtils.jsonConvertFrom(json, AsasMeta.class);

    System.out.println(asacMetaResponse.getLastModified());
    System.out.println(asacMetaResponse.getSelfUri());

    String xml = StandardApiServerUtils.xmlConvertTo(asacMetaResponse);
    
    xml = StandardApiServerUtils.indent(xml, true);
    
    System.out.println(xml);
    
    asacMetaResponse = StandardApiServerUtils.xmlConvertFrom(xml, AsasMeta.class);
    
    System.out.println(asacMetaResponse.getLastModified());
    System.out.println(asacMetaResponse.getSelfUri());

    xml = "<metaResponse x=\"whatever\"><lastModified>abc</lastModified>" +
        "<selfUri>bcd</selfUri>" +
        "<somethingElseXyz>sdfsdf</somethingElseXyz></metaResponse>";

    asacMetaResponse = StandardApiServerUtils.xmlConvertFrom(xml, AsasMeta.class);
    
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
  
  /** name of the structure (struct) returned, the same as the xml outer element */
  private String structureName;

  /**
   * name of the structure (struct) returned, the same as the xml outer element
   * @return the structure name
   */
  public String getStructureName() {
    return structureName;
  }

  /**
   * name of the structure (struct) returned, the same as the xml outer element
   * @param structureName1
   */
  public void setStructureName(String structureName1) {
    this.structureName = structureName1;
  }
  
}
