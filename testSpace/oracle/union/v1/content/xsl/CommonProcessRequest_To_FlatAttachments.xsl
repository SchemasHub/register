<?xml version="1.0" encoding="UTF-8" ?>
<?oracle-xsl-mapper
  <!-- SPECIFICATION OF MAP SOURCES AND TARGETS, DO NOT MODIFY. -->
  <mapSources>
    <source type="XSD">
      <schema location="oramds:/apps/xsd/CommonProcess_1.4.0.xsd"/>
      <rootElement name="CommonProcessRequest" namespace="http://xmlns.oracle.com/CommonProcess/CommonProcessComposite/CommonProcess"/>
      <param name="processInput" />
    </source>
  </mapSources>
  <mapTargets>
    <target type="XSD">
      <schema location="oramds:/apps/xsd/CommonProcess_1.4.0.xsd"/>
      <rootElement name="flatAttachments" namespace="http://xmlns.oracle.com/CommonProcess/CommonProcessComposite/CommonProcess"/>
    </target>
  </mapTargets>
  <!-- GENERATED BY ORACLE XSL MAPPER 11.1.1.7.0(build 140714.2046.0132) AT [MON MAR 23 18:11:45 CET 2015]. -->
?>
<xsl:stylesheet version="1.0"
                xmlns:bpws="http://schemas.xmlsoap.org/ws/2003/03/business-process/"
                xmlns:xp20="http://www.oracle.com/XSL/Transform/java/oracle.tip.pc.services.functions.Xpath20"
                xmlns:mhdr="http://www.oracle.com/XSL/Transform/java/oracle.tip.mediator.service.common.functions.MediatorExtnFunction"
                xmlns:bpel="http://docs.oasis-open.org/wsbpel/2.0/process/executable"
                xmlns:oraext="http://www.oracle.com/XSL/Transform/java/oracle.tip.pc.services.functions.ExtFunc"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:dvm="http://www.oracle.com/XSL/Transform/java/oracle.tip.dvm.LookupValue"
                xmlns:hwf="http://xmlns.oracle.com/bpel/workflow/xpath"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:med="http://schemas.oracle.com/mediator/xpath"
                xmlns:ids="http://xmlns.oracle.com/bpel/services/IdentityService/xpath"
                xmlns:bpm="http://xmlns.oracle.com/bpmn20/extensions"
                xmlns:ns0="http://xmlns.oracle.com/CommonProcess/CommonProcessComposite/CommonProcess"
                xmlns:xdk="http://schemas.oracle.com/bpel/extension/xpath/function/xdk"
                xmlns:xref="http://www.oracle.com/XSL/Transform/java/oracle.tip.xref.xpath.XRefXPathFunctions"
                xmlns:ns1="http://xmlns.oracle.com/CommonProcess/CommonProcessComposite/CommonDocument"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:bpmn="http://schemas.oracle.com/bpm/xpath"
                xmlns:ora="http://schemas.oracle.com/xpath/extension"
                xmlns:socket="http://www.oracle.com/XSL/Transform/java/oracle.tip.adapter.socket.ProtocolTranslator"
                xmlns:con="http://xmlns.oracle.com/CommonProcess/CommonProcessComposite/CommonObjects"
                xmlns:ldap="http://schemas.oracle.com/xpath/extension/ldap"
                exclude-result-prefixes="xsi xsl ns0 ns1 xsd con bpws xp20 mhdr bpel oraext dvm hwf med ids bpm xdk xref bpmn ora socket ldap">
  <xsl:template match="/">
    <ns0:flatAttachments>
      <xsl:apply-templates select="/ns0:CommonProcessRequest/ns0:attachements/con:attachement"/>
    </ns0:flatAttachments>
  </xsl:template>
  
  <xsl:template match="con:attachement">
      <ns0:flatAttachment>
        <ns0:xmlFileId><xsl:value-of select="con:xmlFile/con:id"/></ns0:xmlFileId>
        <ns0:xmlFileName><xsl:value-of select="con:xmlFile/con:name"/></ns0:xmlFileName>
        <ns0:xmlFileUrlMetadata><xsl:value-of select="con:xmlFile/con:url/con:metadata"/></ns0:xmlFileUrlMetadata>
        <ns0:xmlFileUrlWeblocation><xsl:value-of select="con:xmlFile/con:url/con:weblocation"/></ns0:xmlFileUrlWeblocation>
        <ns0:pdfFileId><xsl:value-of select="con:pdfFile/con:id"/></ns0:pdfFileId>
        <ns0:pdfFileName><xsl:value-of select="con:pdfFile/con:name"/></ns0:pdfFileName>
        <ns0:pdfFileUrlMetadata><xsl:value-of select="con:pdfFile/con:url/con:metadata"/></ns0:pdfFileUrlMetadata>
        <ns0:pdfFileUrlWeblocation><xsl:value-of select="con:pdfFile/con:url/con:weblocation"/></ns0:pdfFileUrlWeblocation>
        <ns0:docMetadataDigitalDeliveryDate><xsl:value-of select="con:docMetaData/con:digitalDeliveryDate"/></ns0:docMetadataDigitalDeliveryDate>
        <ns0:docMetadataPaperDeliveryDate><xsl:value-of select="con:docMetaData/con:paperDeliveryDate"/></ns0:docMetadataPaperDeliveryDate>
        <ns0:docMetadataDmsInfoDDocTitle><xsl:value-of select="con:docMetaData/con:dmsInfo/con:dDocTitle"/></ns0:docMetadataDmsInfoDDocTitle>
      </ns0:flatAttachment>
  </xsl:template>
</xsl:stylesheet>