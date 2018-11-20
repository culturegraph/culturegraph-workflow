<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="records">
        <titles>
            <xsl:apply-templates select="record"/>
        </titles>
    </xsl:template>

    <xsl:template match="record">
        <title>
            <xsl:value-of select="title"/>
        </title>
    </xsl:template>
</xsl:stylesheet>