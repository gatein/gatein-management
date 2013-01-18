/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.management.core.spi;

import org.gatein.management.api.ComponentRegistration;
import org.gatein.management.api.ExternalContext;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.ManagedUser;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.RuntimeContext;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedAfter;
import org.gatein.management.api.annotations.ManagedBefore;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.ManagedRole;
import org.gatein.management.api.annotations.MappedAttribute;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.exceptions.NotAuthorizedException;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelNumber;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelString;
import org.gatein.management.api.model.ModelValue;
import org.gatein.management.api.operation.OperationAttachment;
import org.gatein.management.api.operation.OperationAttributes;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.core.api.ManagementProviders;
import org.gatein.management.core.api.SimpleManagedResource;
import org.gatein.management.core.api.model.DmrModelValue;
import org.gatein.management.core.api.operation.BasicResultHandler;
import org.gatein.management.spi.ExtensionContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class AnnotationExtensionTest
{
   private OperationContext operationContext;
   private OperationAttachment attachment;
   private RuntimeContext runtimeContext;
   private ExternalContext externalContext;
   private TestService testService;
   private SubTestService subTestService;

   @Before
   public void init()
   {
      operationContext = mock(OperationContext.class);
      attachment = mock(OperationAttachment.class);
      runtimeContext = mock(RuntimeContext.class);
      externalContext = mock(ExternalContext.class);
      testService = mock(TestService.class);
      subTestService = mock(SubTestService.class);
      when(testService.subService()).thenReturn(subTestService);

      when(operationContext.getRuntimeContext()).thenReturn(runtimeContext);
      when(operationContext.getExternalContext()).thenReturn(externalContext);
      when(runtimeContext.getRuntimeComponent(TestService.class)).thenReturn(testService);

      when(operationContext.getAttachment(true)).thenReturn(attachment);
      when(attachment.getStream()).thenReturn(null);

      @SuppressWarnings("unchecked")
      Class<Class<? extends ModelValue>> clazz = (Class<Class<? extends ModelValue>>) ModelValue.class.getClass();
      when(operationContext.newModel(any(clazz))).thenAnswer(new Answer<ModelValue>()
      {
         @Override
         public ModelValue answer(InvocationOnMock invocation) throws Throwable
         {
            @SuppressWarnings("unchecked")
            Class<? extends ModelValue> c = (Class<? extends ModelValue>) invocation.getArguments()[0];
            return DmrModelValue.newModel().asValue(c);
         }
      });
   }

   @Test
   public void testAnnotationRegistration()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());

      ComponentRegistration registration = context.registerManagedComponent(TestService.class);
      assertNotNull(registration);

      assertNotNull(rootResource.getSubResource(PathAddress.pathAddress("test-service")));
      assertNull(rootResource.getSubResource(PathAddress.pathAddress("foo")));
   }

   @Test
   public void testOperationHandlers()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      assertNotNull(rootResource.getOperationHandler(PathAddress.pathAddress("test-service"), OperationNames.READ_RESOURCE));
      assertNotNull(rootResource.getOperationHandler(PathAddress.pathAddress("test-service"), "bar-operation"));
      assertNotNull(rootResource.getOperationHandler(PathAddress.pathAddress("test-service", "foo"), OperationNames.READ_RESOURCE));

      // read-resource -> blah()
      assertNotNull(rootResource.getOperationHandler(PathAddress.pathAddress("test-service"), OperationNames.READ_RESOURCE));
      when(testService.blah()).thenReturn("blah called !");
      BasicResultHandler resultHandler = execute(rootResource, OperationNames.READ_RESOURCE, "test-service");
      verify(testService).blah();
      assertEquals("blah called !", resultHandler.getResult());
      reset(testService);

      // bar-operation -> bar()
      assertNotNull(rootResource.getOperationHandler(PathAddress.pathAddress("test-service"), "bar-operation"));
      when(testService.bar()).thenReturn("bar called !");
      resultHandler = execute(rootResource, "bar-operation", "test-service");
      verify(testService).bar();
      assertEquals("bar called !", resultHandler.getResult());
      reset(testService);

      // read-resource -> foo()
      assertNotNull(rootResource.getOperationHandler(PathAddress.pathAddress("test-service", "foo"), OperationNames.READ_RESOURCE));
      when(testService.foo()).thenReturn("foo called !");
      resultHandler = execute(rootResource, OperationNames.READ_RESOURCE, "test-service", "foo");
      verify(testService).foo();
      assertEquals("foo called !", resultHandler.getResult());
      reset(testService);
   }

   @Test
   public void testMappedPath()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      PathAddress address = PathAddress.pathAddress("test-service", "foo", "yoyo");

      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));
      when(operationContext.getAddress()).thenReturn(address);

      execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(testService).foobar("yoyo");
      reset(testService);
   }

   @Test
   public void testMappedAttribute()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      PathAddress address = PathAddress.pathAddress("test-service", "foo-bar");

      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));
      OperationAttributes attributes = mock(OperationAttributes.class);

      String barAttr = "The bar attribute !";
      when(operationContext.getAttributes()).thenReturn(attributes);
      when(attributes.getValue("bar-attr")).thenReturn(barAttr);

      execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(testService).foo_bar(barAttr);
      reset(testService);
   }

   @Test
   public void testContext()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      PathAddress address = PathAddress.pathAddress("test-service", "context");
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));

      execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(testService).context(operationContext);
      reset(testService);
   }

   @Test
   public void testContext_RuntimeContext()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      PathAddress address = PathAddress.pathAddress("test-service", "context", "runtime");
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));

      execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(testService).runtime(runtimeContext);
      reset(testService);
   }

   @Test
   public void testContext_PathAddress() throws Exception
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      PathAddress address = PathAddress.pathAddress("test-service", "context", "address");

      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));
      when(operationContext.getAddress()).thenReturn(address);

      execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(testService).address(address);
      reset(testService);
   }

   @Test
   public void testContext_Attributes() throws Exception
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      PathAddress address = PathAddress.pathAddress("test-service", "context", "attributes");

      OperationAttributes attributes = mock(OperationAttributes.class);
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));
      when(operationContext.getAttributes()).thenReturn(attributes);

      execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(testService).attributes(attributes);
      reset(testService);
   }

   @Test
   public void testContext_User() throws Exception
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      PathAddress address = PathAddress.pathAddress("test-service", "context", "user");

      ManagedUser user = mock(ManagedUser.class);
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));
      when(operationContext.getUser()).thenReturn(user);

      execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(testService).user(user);
      reset(testService);
   }

   @Test
   public void testContext_Model() throws Exception
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      // ModelObject
      PathAddress address = PathAddress.pathAddress("test-service", "context", "modelobject");
      OperationAttributes attributes = mock(OperationAttributes.class);
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));
      when(operationContext.getAttributes()).thenReturn(attributes);

      ModelValue model = DmrModelValue.newModel().asValue(ModelObject.class).set("foo", "bar");
      String data = model.toJsonString(false);
      when(attachment.getStream()).thenReturn(new ByteArrayInputStream(data.getBytes()));

      execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(testService).modelObject((ModelObject) model);
      reset(testService);

      // ModelList
      address = PathAddress.pathAddress("test-service", "context", "modellist");
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));

      model = DmrModelValue.newModel().asValue(ModelList.class).add("foo");
      data = model.toJsonString(false);
      when(attachment.getStream()).thenReturn(new ByteArrayInputStream(data.getBytes()));

      execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(testService).modelList((ModelList) model);
      reset(testService);

      // ModelString
      address = PathAddress.pathAddress("test-service", "context", "modelstring");
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));

      model = DmrModelValue.newModel().asValue(ModelString.class).set("foo");
      data = model.toJsonString(false);
      when(attachment.getStream()).thenReturn(new ByteArrayInputStream(data.getBytes()));

      execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(testService).modelString((ModelString) model);
      reset(testService);

      // ModelNumber
      address = PathAddress.pathAddress("test-service", "context", "modelnumber");
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));

      model = DmrModelValue.newModel().asValue(ModelNumber.class).set(new BigInteger("3"));
      data = model.toJsonString(false);
      when(attachment.getStream()).thenReturn(new ByteArrayInputStream(data.getBytes()));

      execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(testService).modelNumber((ModelNumber) model);
      reset(testService);
   }

   @Test
   public void testSubManagedResources()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      // resources
      assertNotNull(rootResource.getSubResource(PathAddress.pathAddress("test-service", "sub-service")));
      assertNotNull(rootResource.getSubResource(PathAddress.pathAddress("test-service", "sub-service", "some", "path")));
      assertNull(   rootResource.getSubResource(PathAddress.pathAddress("test-service", "sub-service", "some", "paths")));
      assertNotNull(rootResource.getSubResource(PathAddress.pathAddress("test-service", "sub-service", "123")));

      // operation handlers
      assertNull(   rootResource.getOperationHandler(PathAddress.pathAddress("test-service", "sub-service"), OperationNames.READ_RESOURCE));
      assertNotNull(rootResource.getOperationHandler(PathAddress.pathAddress("test-service", "sub-service", "some", "path"), OperationNames.READ_RESOURCE));
      assertNotNull(rootResource.getOperationHandler(PathAddress.pathAddress("test-service", "sub-service", "123"), OperationNames.READ_RESOURCE));

      // read-resource -> somePath()
      when(subTestService.somePath()).thenReturn("somePath called !");
      BasicResultHandler resultHandler = execute(rootResource, OperationNames.READ_RESOURCE, "test-service", "sub-service", "some", "path");
      verify(subTestService).somePath();
      assertEquals("somePath called !", resultHandler.getResult());
      reset(subTestService);

      // read-resource -> mappedPath(123)
      PathAddress address = PathAddress.pathAddress("test-service", "sub-service", "123");
      when(operationContext.getAddress()).thenReturn(address);
      when(subTestService.mappedPath(anyString())).thenReturn("mappedPath called !");
      resultHandler = execute(rootResource, OperationNames.READ_RESOURCE, address);
      verify(subTestService).mappedPath("123");
      assertEquals("mappedPath called !", resultHandler.getResult());
      reset(subTestService);
   }

   @Test
   public void testBeforeAfter()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      when(testService.blah()).thenReturn("blah called !");
      BasicResultHandler resultHandler = execute(rootResource, OperationNames.READ_RESOURCE, "test-service");
      InOrder inOrder = inOrder(testService);
      inOrder.verify(testService).before();
      inOrder.verify(testService).blah();
      assertEquals("blah called !", resultHandler.getResult());
      inOrder.verify(testService).after();
      reset(testService);

      when(testService.subService()).thenReturn(subTestService);
      when(subTestService.somePath()).thenReturn("somePath called !");
      resultHandler = execute(rootResource, OperationNames.READ_RESOURCE, "test-service", "sub-service", "some", "path");
      inOrder = inOrder(testService, subTestService);
      inOrder.verify(testService).before();
      inOrder.verify(subTestService).subBefore();
      //
      inOrder.verify(subTestService).somePath();
      assertEquals("somePath called !", resultHandler.getResult());
      //
      inOrder.verify(subTestService).subAfter();
      inOrder.verify(testService).after();

      reset(testService, subTestService);
   }

   @Test
   public void testPlainService()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      SimpleTestService simpleTestService = mock(SimpleTestService.class);
      when(testService.simpleService()).thenReturn(simpleTestService);
      when(simpleTestService.foo()).thenReturn("foo called !");

      BasicResultHandler resultHandler = execute(rootResource, OperationNames.READ_RESOURCE, "test-service", "simple-service");
      InOrder inOrder = inOrder(testService, simpleTestService);
      inOrder.verify(testService).before();
      inOrder.verify(simpleTestService).foo();
      assertEquals("foo called !", resultHandler.getResult());
      inOrder.verify(testService).after();
      reset(testService, simpleTestService);
   }

   @Test
   public void testSecureService()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(SecureService.class);

      // Test secured resources & operations
      SecureService secureService = mock(SecureService.class);
      SubSecureService subSecureService = mock(SubSecureService.class);
      when(secureService.subServiceSecuredByRoleA()).thenReturn(subSecureService);
      when(runtimeContext.getRuntimeComponent(SecureService.class)).thenReturn(secureService);

      when(externalContext.isUserInRole("roleA")).thenReturn(true);
      when(externalContext.isUserInRole("roleB")).thenReturn(false);
      execute(rootResource, OperationNames.READ_RESOURCE, "secure-service");

      verify(externalContext).isUserInRole("roleA");
      verify(secureService).securedByRoleA();

      try
      {
         execute(rootResource, OperationNames.READ_RESOURCE, "secure-service", "secured-by-role-b");
         fail("Should fail because user doesn't belong to roleB");
      }
      catch (NotAuthorizedException e)
      {
      }
      verify(externalContext).isUserInRole("roleB");
      verify(secureService, never()).securedByRoleB();
   }

   public void testSecureService_SubService()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(SecureService.class);

      // Test secured resources & operations
      SecureService secureService = mock(SecureService.class);
      SubSecureService subSecureService = mock(SubSecureService.class);
      when(secureService.subServiceSecuredByRoleA()).thenReturn(subSecureService);
      when(runtimeContext.getRuntimeComponent(SecureService.class)).thenReturn(secureService);
      when(externalContext.isUserInRole("roleA")).thenReturn(true);
      when(externalContext.isUserInRole("roleB")).thenReturn(false);

      execute(rootResource, OperationNames.READ_RESOURCE, "secure-service", "sub-service");
      verify(externalContext).isUserInRole("roleA");
      verify(secureService).subServiceSecuredByRoleA();

      try
      {
         execute(rootResource, OperationNames.READ_RESOURCE, "secure-service", "sub-service", "sub-service-secured-by-role-b");
         fail("Should fail because user doesn't belong to roleB");
      }
      catch (NotAuthorizedException e)
      {
      }
      verify(externalContext).isUserInRole("roleB");
      verify(subSecureService, never()).securedByRoleB();
   }

   public void testUnSecureService()
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(SecureService.class);
      context.registerManagedComponent(UnSecureService.class);

      // Test secured operations
      UnSecureService unSecureService = mock(UnSecureService.class);
      when(externalContext.isUserInRole("roleA")).thenReturn(true);
      when(externalContext.isUserInRole("roleB")).thenReturn(false);
      when(runtimeContext.getRuntimeComponent(UnSecureService.class)).thenReturn(unSecureService);

      execute(rootResource, OperationNames.READ_RESOURCE, "un-secure", "secured-by-role-a");
      verify(externalContext).isUserInRole("roleA");
      verify(unSecureService).securedByRoleA();

      try
      {
         execute(rootResource, OperationNames.READ_RESOURCE, "un-secure", "secured-by-role-b");
         fail("Should fail because user doesn't belong to roleB");
      }
      catch (NotAuthorizedException e)
      {
      }
      verify(externalContext).isUserInRole("roleB");
      verify(unSecureService, never()).securedByRoleB();
   }

   private BasicResultHandler execute(ManagedResource resource, String opName, String...path)
   {
      return execute(resource, opName, PathAddress.pathAddress(path));
   }

   private BasicResultHandler execute(ManagedResource resource, String opName, PathAddress address)
   {
      OperationHandler handler = resource.getOperationHandler(address, opName);
      BasicResultHandler resultHandler = new BasicResultHandler();
      handler.execute(operationContext, resultHandler);

      return resultHandler;
   }

   @Managed("/test-service")
   public static interface TestService
   {
      @ManagedBefore
      public void before();

      @ManagedAfter
      public void after();

      @Managed
      public String blah();

      @Managed
      @ManagedOperation(name = "bar-operation", description = "bar")
      public String bar();

      @Managed("foo")
      public String foo();

      @Managed("foo/{bar}")
      public void foobar(@MappedPath("bar") String bar);

      @Managed("foo-bar")
      public void foo_bar(@MappedAttribute("bar-attr") String barAttr);

      @Managed("context")
      public void context(@ManagedContext OperationContext context);

      @Managed("context/runtime")
      public void runtime(@ManagedContext RuntimeContext runtimeContext);

      @Managed("context/address")
      public void address(@ManagedContext PathAddress address);

      @Managed("context/attributes")
      public void attributes(@ManagedContext OperationAttributes attributes);

      @Managed("context/user")
      public void user(@ManagedContext ManagedUser user);

      @Managed("context/modelobject")
      public void modelObject(@ManagedContext ModelObject model);

      @Managed("context/modellist")
      public void modelList(@ManagedContext ModelList model);

      @Managed("context/modelstring")
      public void modelString(@ManagedContext ModelString model);

      @Managed("context/modelnumber")
      public void modelNumber(@ManagedContext ModelNumber model);

      @Managed("sub-service")
      public SubTestService subService();

      @Managed("simple-service")
      public SimpleTestService simpleService();
   }

   @Managed
   public static interface SubTestService
   {
      @ManagedBefore
      public void subBefore();

      @ManagedAfter
      public void subAfter();

      @Managed("some/path")
      public String somePath();

      @Managed("{abc}")
      public String mappedPath(@MappedPath("abc") String abc);
   }

   @Managed
   public static interface SimpleTestService
   {
      @Managed
      public String foo();
   }

   @Managed("secure-service")
   @ManagedRole("roleA")
   public static interface SecureService
   {
      @Managed
      public void securedByRoleA();

      @Managed("secured-by-role-b")
      @ManagedRole("roleB")
      public void securedByRoleB();

      @Managed("sub-service")
      public SubSecureService subServiceSecuredByRoleA();
   }

   @Managed
   public static interface SubSecureService
   {
      @Managed
      public void securedByRoleA();

      @Managed("sub-service-secured-by-role-b")
      @ManagedRole("roleB")
      public void securedByRoleB();
   }

   @Managed("un-secure")
   public static interface UnSecureService
   {
      @Managed
      public void unsecure();

      @Managed("secured-by-role-a")
      @ManagedRole("roleA")
      public void securedByRoleA();

      @Managed("secured-by-role-b")
      @ManagedRole("roleB")
      public void securedByRoleB();
   }
}
