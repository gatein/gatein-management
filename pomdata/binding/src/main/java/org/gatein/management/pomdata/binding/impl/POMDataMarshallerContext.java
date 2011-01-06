package org.gatein.management.pomdata.binding.impl;

import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.spi.portlet.Portlet;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class POMDataMarshallerContext
{
   private ModelDataStorage modelDataStorage;

   public POMDataMarshallerContext(ModelDataStorage modelDataStorage)
   {
      this.modelDataStorage = modelDataStorage;
   }

   //@SuppressWarnings("unchecked")
   public <S> String getContentId(ApplicationState<S> state) throws Exception
   {
      return modelDataStorage.getId(state);
   }

   public Portlet getPortlet(ApplicationState<Portlet> portlet) throws Exception
   {
      return modelDataStorage.load(portlet, ApplicationType.PORTLET);
   }
}
