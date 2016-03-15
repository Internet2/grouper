package edu.internet2.middleware.tierApiAuthzServer.corebeans;


/**
 * base class that beans extends
 * @author mchyzer
 */
public abstract class AsasResponseBeanBase {

  /**
   * construct
   */
  public AsasResponseBeanBase() {
    this.getMeta().setTierHttpStatusCode(200);
    //this.getMeta().setLastModified(StandardApiServerUtils.convertToIso8601(new Date(TaasRestServlet.getStartupTime())));
  }

  /**
   * override this for subject objects to scimify
   * make this a scim response
   */
  public void scimify() {
    this.getMeta().scimify();
  }
  
  /**
   * 
   */
  private String[] schemas = null;

  
  /**
   * @return the schemas
   */
  public String[] getSchemas() {
    return this.schemas;
  }

  
  /**
   * @param theSchemas the schemas to set
   */
  public void setSchemas(String[] theSchemas) {
    this.schemas = theSchemas;
  }

  /**
   * meta about resource
   */
  private TaasMeta meta = new TaasMeta();
  
  /**
   * meta about resource
   * @return the meta
   */
  public TaasMeta getMeta() {
    return this.meta;
  }
  
  /**
   * meta about resource
   * @param _meta the meta to set
   */
  public void setMeta(TaasMeta _meta) {
    this.meta = _meta;
  }
  
}
