<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:d="http://dhis2.org/schema/dxf/2.0"
  >

  <xsl:template match="d:report">
  <div class="report">
    <h2> <xsl:value-of select="@name" /> </h2>
	  
	  <table>
		<tr>
			<td>Resource Data</td>
			<td><a href="{@link}/data">pdf</a></td>
			<td><a href="{@link}/data.xls">xls</a></td>
		</tr>
		<tr>
			<td>ou</td>
			<td colspan="2">organisation unit uid (opt)</td>
		</tr>
		<tr>
			<td>pe</td>
			<td colspan="2">period yyyy-MM-dd (opt)</td>
		</tr>
	  </table><br/>
	  
    <table>
      <tr>
        <td>ID</td>
        <td> <xsl:value-of select="@id" /> </td>
      </tr>
      <tr>
        <td>Last Updated</td>
        <td> <xsl:value-of select="@lastUpdated" /> </td>
      </tr>
      <tr>
        <td>Code</td>
        <td> <xsl:value-of select="@code" /> </td>
      </tr>
    </table>

    <xsl:apply-templates select="d:reportTable" mode="short"/>
  </div>
  </xsl:template>

  <xsl:template match="d:reportTable" mode="short">
    <h3>ReportTable</h3>
    <table class="reportTable">
      <xsl:apply-templates select="." mode="row"/>
    </table>
  </xsl:template>

</xsl:stylesheet>
