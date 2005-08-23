<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="xml"  version="1.0" indent="yes" />
 

  <xsl:key name = "obj"  match="folder" use="@id" />
  <xsl:param name="activeNodeId">root</xsl:param>
  <xsl:param name="mode">Manage</xsl:param>
  <xsl:param name="linkMode"></xsl:param>
  <xsl:param name="subject">SuperUser</xsl:param>
  <xsl:param name="stem-separator">/</xsl:param>
  <xsl:param name="forFind">false</xsl:param>
  <xsl:param name="flat">false</xsl:param>
  <xsl:param name="strutsModule"></xsl:param>  

  
  <xsl:template match="xbel">
    <xsl:variable name="activeNode" select="key('obj',$activeNodeId)"/>
    <div>
    <xsl:choose>
    	<xsl:when test="$mode='' and $flat='true'">
    		<xsl:call-template name="my-groups"/>
    	</xsl:when>
    
    	<xsl:otherwise>
    	<div>
  	<xsl:for-each select="$activeNode/ancestor::folder">
  		<xsl:call-template name="link-folder">
  			<xsl:with-param name="folder" select="."/>
  		</xsl:call-template>-
  	</xsl:for-each>
  	<xsl:value-of select="$activeNode/title"/>
  	</div>
  	<xsl:if test="$forFind='true' and $activeNode/info/metadata[@owner='group'] and $activeNode/bookmark[contains(@id,'member')]">
  		<form action="{$strutsModule}populateAssignNewMembers.do">
  				<input type="hidden" name="alreadyChecked" value="true"/>
  				<xsl:for-each select="$activeNode/bookmark[contains(@id,'member')]">
  					<input type="checkbox" name="members" value="{substring-after(@id,'*')}"/> 
  					<xsl:choose>
  						<xsl:when test="contains(substring-after(@id,'*'),'/')">
  							<xsl:variable name="groupId" select="substring-after(@id,'*')"/>
  							<xsl:value-of select="//folder[@id=$groupId]/title"/>
  						</xsl:when>
  						<xsl:otherwise>
  							<xsl:value-of select="substring-after(@id,'*')"/>
  						</xsl:otherwise>
  					</xsl:choose>  <br/>
  				</xsl:for-each>
  				<br/><input type="submit" value="Assign privileges"/>
  			</form>	
  	</xsl:if>
  	<div><ul>
  	<xsl:variable name="data"><div>
  		<xsl:for-each select="$activeNode/folder">
  			<xsl:call-template name="link-folder-list">
  				<xsl:with-param name="folder" select="."/>
  			</xsl:call-template>
  			<xsl:if test="@id='root/uob' and $subject='SuperUser'">
  			<br/>If you would like to try creating stems and groups please use the links under 'MANAGE STEM' below to create your own stem as a 'test'area.
  			</xsl:if>
  		</xsl:for-each></div>
  		
  	</xsl:variable>
  	
  	<xsl:choose>
  		<xsl:when test="$forFind='true'">
  			<form action="{$strutsModule}populateAssignNewMembers.do">
  				<input type="hidden" name="alreadyChecked" value="true"/>
  				<xsl:copy-of select="$data"/>
  				<br/><input type="submit" value="Assign privileges"/>
  			</form>
  		</xsl:when>
  		<xsl:otherwise>
  			<xsl:copy-of select="$data"/>	
  		</xsl:otherwise>
  	</xsl:choose>
 	</ul>
  	</div>
    	</xsl:otherwise>
    </xsl:choose>
    
  
  	
  
  </div>
        
  </xsl:template>
  
  <xsl:template name="link-folder-list">
  	<xsl:param name="folder"/>
  	
  	<xsl:variable name="data">
  		<xsl:call-template name="link-folder">
  			<xsl:with-param name="folder" select="."/>
  		</xsl:call-template>
  	</xsl:variable>
  	<xsl:if test="boolean(normalize-space($data))">
  		<li><xsl:if test="./info/metadata[@owner='stem']"><img src="grouper/images/stem.jpg"/></xsl:if><xsl:copy-of select="$data"/></li>	
  	</xsl:if>
  </xsl:template>
  
  
  <xsl:template name="last-id">
  	<xsl:param name="id"/>
  	<xsl:variable name="after" select="substring-after($id,$stem-separator)"/>
  	<xsl:choose>
  		<xsl:when test="boolean($after)">
  			<xsl:call-template name="last-id">
  				<xsl:with-param name="id" select="$after"/>
  			</xsl:call-template>
  		</xsl:when>
  		<xsl:otherwise>
  			<xsl:value-of select="$id"/>
  		</xsl:otherwise>
  	</xsl:choose>
  </xsl:template>
  
  <xsl:template name="link-folder">
  	<xsl:param name="folder"/>
  	<xsl:choose>
  	<xsl:when test="$folder/info/metadata[@owner='stem']">
  	<xsl:call-template name="link-stem">
  			<xsl:with-param name="folder" select="$folder"/>
  		</xsl:call-template>
  	
  	</xsl:when>
  	<xsl:when test="$forFind='true'">
  	
  	
 		
  		<xsl:call-template name="link-group-for-find">
  			<xsl:with-param name="folder" select="$folder"/>
  		</xsl:call-template>
  	
  		
  	</xsl:when>
  	<xsl:otherwise>
  		<xsl:call-template name="link-group">
  			<xsl:with-param name="folder" select="$folder"/>
  		</xsl:call-template>
  	</xsl:otherwise>
  
  	</xsl:choose>
  </xsl:template>
  
  <xsl:template name="link-stem">
  	<xsl:param name="folder"/>
  	<xsl:choose>
  	<xsl:when test="$mode='Join' and $folder//bookmark[contains(@id,concat('*',$subject)) and contains(@id,'optin')]">
  		<a href="{$strutsModule}browseStems{$linkMode}.do?currentNode={$folder/@id}"><xsl:value-of select="$folder/title"/></a>
  	</xsl:when>
  	<xsl:when test="$mode='Create' and ($subject='SuperUser' 
  									or ($folder//bookmark[contains(@id,concat('*',$subject)) and (contains(@id,'create') or contains(@id,'stem'))]))">
  		<a href="{$strutsModule}browseStems{$linkMode}.do?currentNode={$folder/@id}"><xsl:value-of select="$folder/title"/></a>
  	</xsl:when>
  	  	<xsl:when test="$mode='Manage' and ($subject='SuperUser' or ($folder//bookmark[contains(@id,concat('*',$subject)) and (contains(@id,'create') or contains(@id,'stem')or contains(@id,'admin')or contains(@id,'update'))]))">
  		<a href="{$strutsModule}browseStems{$linkMode}.do?currentNode={$folder/@id}"><xsl:value-of select="$folder/title"/></a>
  	</xsl:when>
  	
  	  	<xsl:when test="$mode='' and $flat='false' and (($folder//bookmark[contains(@id,concat('*',$subject)) and (contains(@id,'member'))]))">
  		<a href="{$strutsModule}browseStems{$linkMode}.do?currentNode={$folder/@id}"><xsl:value-of select="$folder/title"/></a>
  	</xsl:when>
  	<xsl:otherwise>
  	
  	</xsl:otherwise>
  
  	</xsl:choose>
  </xsl:template>
  
  <xsl:template name="link-group">
  	<xsl:param name="folder"/>
  	<xsl:choose>
  	<xsl:when test="$mode='Join' and $folder/bookmark[contains(@id,concat('*',$subject)) and contains(@id,'optin') and not(contains(@id,'member'))]">
  		[<a href="{$strutsModule}populateGroupSummary.do?groupId={$folder/@id}"><xsl:value-of select="$folder/title"/></a>]<br/>
  		
  	</xsl:when>
  	<xsl:when test="$mode='Join' and $folder/bookmark[contains(@id,concat('*',$subject)) and contains(@id,'optin') and contains(@id,'member')]">
  		[<xsl:value-of select="$folder/title"/>] <br/>
  		
  	</xsl:when>
  	<xsl:when test="$mode='' and $folder/bookmark[contains(@id,concat('*',$subject)) and contains(@id,'member') and (contains(@id,'read')or contains(@id,'update')or contains(@id,'admin'))]">
  		[<a href="{$strutsModule}populateGroupSummary.do?groupId={$folder/@id}"><xsl:value-of select="$folder/title"/></a>]<br/>
  		
  	</xsl:when>
  	<xsl:when test="$mode='Manage' and ($subject='SuperUser' or ($folder/bookmark[contains(@id,concat('*',$subject)) and (contains(@id,'read') or contains(@id,'update') or contains(@id,'admin'))]))">
  		[<a href="{$strutsModule}populateGroupSummary.do?groupId={$folder/@id}"><xsl:value-of select="$folder/title"/></a>]<br/>
  		
  	</xsl:when>
  	<xsl:when test="$mode='Create' and ($subject='SuperUser' or ($folder/bookmark[contains(@id,concat('*',$subject)) and (contains(@id,'read') or contains(@id,'update') or contains(@id,'admin'))]))">
  		[<a href="{$strutsModule}populateGroupSummary.do?groupId={$folder/@id}"><xsl:value-of select="$folder/title"/></a>]<br/>
  		
  	</xsl:when>
  	<xsl:when test="($mode='Create' or $mode='' or $mode='Join') and $folder/bookmark[contains(@id,concat('*',$subject)) and contains(@id,'view')]">
  		[<xsl:value-of select="$folder/title"/>]<br/>
  		
  	</xsl:when>
  	<xsl:otherwise>
  	
  	</xsl:otherwise>
  
  	</xsl:choose>
  </xsl:template>
  
  <xsl:template name="link-group-for-find">
  	<xsl:param name="folder"/>
  	<xsl:choose>
  	<xsl:when test="$mode='Join' and $folder/bookmark[contains(@id,concat('*',$subject)) and contains(@id,'optin') and not(contains(@id,'member'))]">
  		<input type="checkbox" name="members" value="{$folder/@id}"/> [<a href="{$strutsModule}browseStemsFind.do?currentNode={$folder/@id}"><xsl:value-of select="$folder/title"/></a>]<br/>
  		
  	</xsl:when>
  	<xsl:when test="$mode='Join' and $folder/bookmark[contains(@id,concat('*',$subject)) and contains(@id,'optin') and contains(@id,'member')]">
  		<input type="checkbox" name="members" value="{$folder/@id}"/> [<xsl:value-of select="$folder/title"/>] <br/>
  		
  	</xsl:when>
  	<xsl:when test="$mode='' and $folder/bookmark[contains(@id,concat('*',$subject)) and contains(@id,'member') and (contains(@id,'read')or contains(@id,'update')or contains(@id,'admin'))]">
  		<input type="checkbox" name="members" value="{$folder/@id}"/> [<a href="{$strutsModule}browseStemsFind.do?currentNode={$folder/@id}"><xsl:value-of select="$folder/title"/></a>]<br/>
  		
  	</xsl:when>
  	<xsl:when test="$mode='Manage' and ($subject='SuperUser' or ($folder/bookmark[contains(@id,concat('*',$subject)) and (contains(@id,'read') or contains(@id,'update') or contains(@id,'admin'))]))">
  		<input type="checkbox" name="members" value="{$folder/@id}"/> [<a href="{$strutsModule}browseStemsFind.do?currentNode={$folder/@id}"><xsl:value-of select="$folder/title"/></a>]<br/>
  		
  	</xsl:when>
  	<xsl:when test="$mode='Create' and ($subject='SuperUser' or ($folder/bookmark[contains(@id,concat('*',$subject)) and (contains(@id,'read') or contains(@id,'update') or contains(@id,'admin'))]))">
  		<input type="checkbox" name="members" value="{$folder/@id}"/> [<a href="{$strutsModule}browseStemsFind.do?currentNode={$folder/@id}"><xsl:value-of select="$folder/title"/></a>]<br/>
  		
  	</xsl:when>
  	<xsl:when test="($mode='Create' or $mode='' or $mode='Join') and $folder/bookmark[contains(@id,concat('*',$subject)) and contains(@id,'view')]">
  	<input type="checkbox" name="members" value="{$folder/@id}"/>[<xsl:value-of select="$folder/title"/>]<br/>
  		
  	</xsl:when>
  	<xsl:otherwise>
  	
  	</xsl:otherwise>
  
  	</xsl:choose>
  </xsl:template>
  
  
  <xsl:template name="my-groups">
  	<xsl:for-each select="//bookmark[contains(@id,concat('*',$subject)) and contains(@id,'member')]">
  		<xsl:if test="../info/metadata[@owner='group']">
  				[<a href="{$strutsModule}populateGroupSummary.do?groupId={../@id}"><xsl:value-of select="../title"/></a>]<br/>	
  		</xsl:if>
  	</xsl:for-each>
  </xsl:template>
  
  
  
  
</xsl:stylesheet>