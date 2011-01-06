package org.gatein.management.domain;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class Page
{
//  <xs:complexType name="pageType">
//    <xs:sequence>
//      <xs:element name="name" type="xs:string" minOccurs="1" maxOccurs="1"/>
//      <xs:element name="title" type="xs:string" minOccurs="0" maxOccurs="1"/>
//      <xs:element name="factory-id" type="xs:string" minOccurs="0" maxOccurs="1"/>
//      <xs:element name="access-permissions" type="xs:string" minOccurs="0" maxOccurs="1"/>
//      <xs:element name="edit-permission" type="xs:string" minOccurs="0" maxOccurs="1"/>
//      <xs:element name="show-max-window" type="xs:boolean" minOccurs="0" maxOccurs="1"/>
//      <xs:group ref="containerChildrenGroup"/>
//    </xs:sequence>
//  </xs:complexType>
   private String name;
   private String title;
   private String factoryId; //TODO: Do we need factory id
   private String[] accessPermissions;
   private String[] editPermissions; // preemptive support for multiple edit permissions
   private boolean showMaxWindow;
   private Container container;
}
