package edu.internet2.middleware.grouperClient.ws.beans;

public class WsRestAssignAttributeDefActionsRequest implements WsRequestBean {
	
	/** Attribute def to which action(s) are being assigned **/
	private WsAttributeDefLookup wsAttributeDefLookup;
	
	/**
     * T to assign, or F to remove assignment
     */
    private String assign;
  
    /**
     * T if assigning, if this list should replace all existing actions
     */
    private String replaceAllExisting;
    
    /** 
     * actions to be assigned 
     */
    private String[] actions;
    
    /** 
     * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 
     */
    private String clientVersion;
    
    /** optional: reserved for future use */
    private  WsParam[] params;
    
    
	/**
     * Attribute def to which action(s) are being assigned
     * @return wsAttributeDefLookup
     */
    public WsAttributeDefLookup getWsAttributeDefLookup() {
      return this.wsAttributeDefLookup;
    }

    /**
     * Attribute def to which action(s) are being assigned
     * @param wsAttributeDefLookup1
     */
    public void setWsAttributeDefLookup(WsAttributeDefLookup wsAttributeDefLookup1) {
      this.wsAttributeDefLookup = wsAttributeDefLookup1;
    }
    
    /**
     * T to assign, or F to remove assignment
     * @return assign
     */
    public String getAssign() {
      return this.assign;
    }

    /**
     * T to assign, or F to remove assignment
     * @param assign1
     */
    public void setAssign(String assign1) {
      this.assign = assign1;
    }
    
    /**
     * @return actions to be assigned
     */
    public String[] getActions() {
	  return actions;
	}

    /**
     * @param actions1 to be assigned
     */
	public void setActions(String[] actions1) {
	  this.actions = actions1;
	}

    /**
     * T if assigning, if this list should replace all existing actions
     * @return replaceAllExisting
     */
    public String getReplaceAllExisting() {
      return this.replaceAllExisting;
    }

    /**
     * T if assigning, if this list should replace all existing actions
     * @param replaceAllExisting1
     */
    public void setReplaceAllExisting(String replaceAllExisting1) {
      this.replaceAllExisting = replaceAllExisting1;
    }
    
    /**
     * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
     * @return version
     */
    public String getClientVersion() {
      return this.clientVersion;
    }

    /**
     * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
     * @param clientVersion1
     */
    public void setClientVersion(String clientVersion1) {
      this.clientVersion = clientVersion1;
    }

    /** if acting as someone else */
    private WsSubjectLookup actAsSubjectLookup;
    
    /**
     * if acting as someone else
     * @return act as subject
     */
    public WsSubjectLookup getActAsSubjectLookup() {
      return this.actAsSubjectLookup;
    }

    /**
     * if acting as someone else
     * @param actAsSubjectLookup1
     */
    public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
      this.actAsSubjectLookup = actAsSubjectLookup1;
    }

    /**
     * optional: reserved for future use
     * @return params
     */
    public WsParam[] getParams() {
      return this.params;
    }

    /**
     * optional: reserved for future use
     * @param params1
     */
    public void setParams(WsParam[] params1) {
      this.params = params1;
    }

}
