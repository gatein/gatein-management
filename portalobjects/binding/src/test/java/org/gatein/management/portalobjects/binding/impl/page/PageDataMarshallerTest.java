package org.gatein.management.portalobjects.binding.impl.page;

import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageDataMarshallerTest
{
   private PageData customPageData;
   private PageData homePage;

   @Before
   public void createData()
   {
      // Create custom page data
      Portlet portlet = new PortletBuilder().add("pref-name1", "pref-value1").add("pref-name2", "pref-value2").build();
      TransientApplicationState<Portlet> state = new TransientApplicationState<Portlet>("app-ref/portlet-ref", portlet);

      Map<String,String> properties = new HashMap<String,String>();
      properties.put("key1", "value1");
      properties.put("key2", "value2");

      ApplicationData<Portlet> applicationData = new ApplicationData<Portlet>("app-storageId", "app-storageName",
         ApplicationType.PORTLET, state, "app-id", "app-title", "app-icon", "app-description", true, true, false,
         "app-theme", "app-width", "app-height", properties, Arrays.asList("app-/platform/users", "app-/platform/administrators"));

      List<ComponentData> children = new ArrayList<ComponentData>();
      children.add(applicationData);
      customPageData = new PageData("storageId", "id", "custom-page-name", "icon", "template", "factoryId", "title", "description",
         "width", "height", Arrays.asList("/platform/users", "/platform/administrators"),
         children, "ownerType", "ownerId", "editPermission", true);


      // Create out of the box home page
      portlet = new PortletBuilder().add("template", "system:/templates/groovy/webui/component/UIHomePagePortlet.gtmpl").build();
      state = new TransientApplicationState<Portlet>("web/HomePagePortlet", portlet);

      applicationData = new ApplicationData<Portlet>(null, null,
         ApplicationType.PORTLET, state, null, "Home Page portlet", null, null, false, false, false,
         null, null, null, null, Arrays.asList("Everyone"));

      children = new ArrayList<ComponentData>();
      children.add(applicationData);
      
      homePage = new PageData(null, null, "homepage", null, null, null, "Home Page", null,
         null, null, Arrays.asList("Everyone"),
         children, "", "", "*:/platform/administrators", false);
   }

   @Test
   public void testCustomPageDataMarshaller() throws Exception
   {
      PageDataMarshaller marshaller = new PageDataMarshaller();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      marshaller.marshal(customPageData, baos);

      baos.flush();
      baos.close();

      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

      PageData customPage = marshaller.unmarshal(bais);

      Assert.assertNotNull(customPage);
      Assert.assertEquals("custom-page-name", customPage.getName());

      Assert.assertNotNull(customPage.getChildren());
      Assert.assertEquals(1, customPage.getChildren().size());

      Assert.assertTrue(customPage.getChildren().get(0) instanceof ApplicationData);
      ApplicationData applicationData = (ApplicationData) customPage.getChildren().get(0);
      Assert.assertTrue(applicationData.getState() instanceof TransientApplicationState);
      TransientApplicationState state = (TransientApplicationState) applicationData.getState();
      Assert.assertEquals("app-ref/portlet-ref", state.getContentId());
      Assert.assertTrue(state.getContentState() instanceof Portlet);

      //TODO: Test all properties
   }
}
