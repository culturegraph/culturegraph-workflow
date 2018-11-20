<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes"/>
  <xsl:template match="records">
    <xsl:apply-templates select="record"/>
  </xsl:template>
  
  <xsl:template match="record">
    <xsl:variable name="idn" select="idn" />
    <xsl:value-of select="$idn"/>
    <xsl:for-each-group select="./*[not(self::idn)]" group-by="name()">
      <xsl:variable name="key" select="current-grouping-key()"/>
      <xsl:variable name="value" select="count(current-group())"/>
      <xsl:value-of select="concat(' ', $key, ' ', $value)"/>
    </xsl:for-each-group>
  </xsl:template>
</xsl:stylesheet>