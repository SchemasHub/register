<?xml version="1.0" encoding="UTF-8" ?>
<?oracle-xsl-mapper
  <!-- SPECIFICATION OF MAP SOURCES AND TARGETS, DO NOT MODIFY. -->
  <mapSources>
    <source type="XSD">
      <schema location="oramds:/apps/xsd/CommonProcess_1.3.0.xsd"/>
      <rootElement name="CommonProcessRequest" namespace="http://xmlns.oracle.com/CommonProcess/CommonProcessComposite/CommonProcess"/>
      <param name="processInput" />
    </source>
  </mapSources>
  <mapTargets>
    <target type="XSD">
      <schema location="oramds:/apps/xsd/CommonProcess_1.3.0.xsd"/>
      <rootElement name="CommonProcessRequest" namespace="http://xmlns.oracle.com/CommonProcess/CommonProcessComposite/CommonProcess"/>
    </target>
  </mapTargets>
  <!-- GENERATED BY ORACLE XSL MAPPER 11.1.1.7.0(build 140714.2046.0132) AT [MON APR 20 14:26:59 CEST 2015]. -->
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
    <ns0:CommonProcessRequest>
      <xsl:copy-of select="/ns0:CommonProcessRequest/ns0:xmlFile"/>
      <xsl:copy-of select="/ns0:CommonProcessRequest/ns0:pdfFile"/>
      <xsl:copy-of select="/ns0:CommonProcessRequest/ns0:docMetaData"/>
      <ns0:docContent>
        <con:root>
          <ns1:document>
            <xsl:copy-of select="/ns0:CommonProcessRequest/ns0:docContent/con:root/ns1:document/ns1:field[@name!='Dávka 514']"/>
            
            <xsl:if test="count(/ns0:CommonProcessRequest/ns0:docContent/con:root/ns1:document/ns1:field[@name='Dávka 514'])=1">
              <ns1:field level="document" name="Dávka 514">
                <xsl:attribute name="value">
                  <xsl:value-of select="/ns0:CommonProcessRequest/ns0:docContent/con:root/ns1:document/ns1:field/text()"/>
                </xsl:attribute>
              </ns1:field>
            </xsl:if>
          </ns1:document>
        </con:root>
      </ns0:docContent>
      <xsl:copy-of select="/ns0:CommonProcessRequest/ns0:attachements"/>
    </ns0:CommonProcessRequest>
  </xsl:template>
</xsl:stylesheet>