package org.gatein.management.api.model;

/**
 * A model provider can be injected into a managed resource by annotating a field with @ManagedContext. This is
 * stateless and can be used by multiple threads.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface ModelProvider
{
   Model newModel();

   <T extends ModelValue> T newModel(Class<T> type);
}
