<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:marc21="http://culturegraph.org/MARC21fragment"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <xsl:output method="xml" omit-xml-declaration="yes" indent="no" encoding="utf-8"/>
  
  <xsl:template match="records">
    <xsl:apply-templates select="record"/>
  </xsl:template>
  
  <xsl:template match="record">

    <xsl:variable name="resolveOrganisationCode" select="
      map {
        'DE-101' : 'DNB',
        'DE-576' : 'BSZ',
        'DE-599' : 'AGV',
        'DE-600' : 'ZDB',
        'DE-601' : 'GBV',
        'DE-602' : 'KOBV',
        'DE-603' : 'HEB',
        'DE-604' : 'BVB',
        'DE-605' : 'HBZ',
        'AT-OBV' : 'OBV',
        'Uk'     : 'BNB',
        'ItFiC'  : 'CLL',
        'FrPBN'  : 'BNF',
        'OCoLC'  : 'OCLC' }">
    </xsl:variable>

    <xsl:variable name="idn"><xsl:value-of select="idn[1]"/></xsl:variable>
    <xsl:variable name="organisation"><xsl:value-of select="substring(substring-before($idn, ')' ), 2)"/></xsl:variable>
    <xsl:variable name="id"><xsl:value-of select="substring-after($idn, ')')"/></xsl:variable>

    <marc21-excerpt xsi:schemaLocation="http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd">
      <xsl:attribute name="ref">
        <xsl:text>http://hub.culturegraph.org/resource/</xsl:text>
        <xsl:variable name="resolvedName" select="$resolveOrganisationCode($organisation)"/>
        <xsl:choose>
          <xsl:when test="$resolvedName">
            <xsl:value-of select="concat('(', $resolvedName, ')', $id)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat('(', $organisation, ')', $id)"/>
          </xsl:otherwise>
        </xsl:choose> 
      </xsl:attribute>
      <xsl:apply-templates select="node()[starts-with(name(), 'controlfield') or starts-with(name(), 'datafield')]"/>
    </marc21-excerpt>
  </xsl:template>

  <xsl:template match="node()[starts-with(name(), 'controlfield')]">
    <xsl:variable name="fieldName" select="substring-after(name(), '_')"/>
    <marc21:controlfield>
      <xsl:attribute name="tag">
        <xsl:value-of select="$fieldName"/>
      </xsl:attribute>
      <xsl:value-of select="node()[starts-with(name(), 'field')][1]"/>
    </marc21:controlfield>
  </xsl:template>

  <xsl:template match="node()[starts-with(name(), 'datafield')]">
    <xsl:variable name="dataFieldName">
      <xsl:value-of select="replace(substring-after(name(), '_'), '_', ' ')"/>
    </xsl:variable>
    <marc21:datafield>
      <xsl:attribute name="tag"><xsl:value-of select="substring($dataFieldName, 1, 3)"/></xsl:attribute>
      <xsl:attribute name="ind1"><xsl:value-of select="substring($dataFieldName, 4, 1)"/></xsl:attribute>
      <xsl:attribute name="ind2"><xsl:value-of select="substring($dataFieldName, 5, 1)"/></xsl:attribute>
      <xsl:for-each select="node()[starts-with(name(), 'subfield')]">
        <xsl:variable name="subfieldCode">
          <xsl:value-of select="replace(substring-after(name(), '_'), '_', ' ')"/>
        </xsl:variable>
        <marc21:subfield>
          <xsl:attribute name="code"><xsl:value-of select="$subfieldCode"/></xsl:attribute>
          <xsl:value-of select="."/>
        </marc21:subfield>
      </xsl:for-each>
    </marc21:datafield>
  </xsl:template>
</xsl:stylesheet>
