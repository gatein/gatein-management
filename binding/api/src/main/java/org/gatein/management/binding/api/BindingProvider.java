package org.gatein.management.binding.api;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface BindingProvider
{
   <T> BindingContext<T> createContext(Class<T> type) throws BindingException;

   void registerMarshaller(Class<? extends Marshaller> marshallerClass) throws BindingException;

   void load();

   void unload();
}
