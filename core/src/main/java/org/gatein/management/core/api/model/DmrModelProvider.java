package org.gatein.management.core.api.model;

import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.model.ModelValue;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DmrModelProvider implements ModelProvider
{
   public static DmrModelProvider INSTANCE = new DmrModelProvider();

   private DmrModelProvider(){}

   @Override
   public Model newModel()
   {
      return DmrModelValue.newModel();
   }

   @Override
   public <T extends ModelValue> T newModel(Class<T> type)
   {
      return DmrModelValue.newModel().asValue(type);
   }
}
