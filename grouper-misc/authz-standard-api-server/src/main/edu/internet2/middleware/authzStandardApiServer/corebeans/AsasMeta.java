package edu.internet2.middleware.authzStandardApiServer.corebeans;

import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerJsonIndenter;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;

/**
 * This is the meta object in a response, or extend this if 
 * @author mchyzer
 *
 */
public class AsasMeta {
  
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
