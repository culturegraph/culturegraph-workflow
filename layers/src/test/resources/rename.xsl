<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="titles">
        <covers>
            <xsl:apply-templates select="title"/>
        </covers>
    </xsl:template>

    <xsl:template match="title">
        <cover>
            <xsl:value-of select="."/>
        </cover>
    </xsl:template>
</xsl:stylesheet>