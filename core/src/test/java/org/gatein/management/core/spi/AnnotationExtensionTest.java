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
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.RuntimeContext;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.MappedAttribute;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelString;
import org.gatein.management.api.model.ModelValue;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class AnnotationExtensionTest
{
   private OperationContext operationContext;
   private RuntimeContext runtimeContext;
   private TestService testService;
   private SubTestService subTestService;

   @Before
   public void init()
   {
      operationContext = mock(OperationContext.class);
      runtimeContext = mock(RuntimeContext.class);
      testService = mock(TestService.class);
      subTestService = mock(SubTestService.class);
      when(testService.subService()).thenReturn(subTestService);

      when(operationContext.getRuntimeContext()).thenReturn(runtimeContext);
      when(runtimeContext.getRuntimeComponent(TestService.class)).thenReturn(testService);
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
   public void testContext_Model() throws Exception
   {
      SimpleManagedResource rootResource = new SimpleManagedResource(null, null, null);
      ExtensionContext context = new ExtensionContextImpl(rootResource, new ManagementProviders());
      context.registerManagedComponent(TestService.class);

      PathAddress address = PathAddress.pathAddress("test-service", "context", "model");
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));
      when(operationContext.newModel()).thenReturn(DmrModelValue.newModel());

      execute(rootResource, OperationNames.READ_RESOURCE, address);

      verify(testService).model(DmrModelValue.newModel());
      reset(testService);

      address = PathAddress.pathAddress("test-service", "context", "modelvalue");
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));
      when(operationContext.newModel(ModelValue.class)).thenReturn(DmrModelValue.newModel().asValue(ModelValue.class));

      execute(rootResource, OperationNames.READ_RESOURCE, address);

      verify(testService).modelvalue(DmrModelValue.newModel().asValue(ModelValue.class));
      reset(testService);

      address = PathAddress.pathAddress("test-service", "context", "modelstring");
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));
      when(operationContext.newModel(ModelString.class)).thenReturn(DmrModelValue.newModel().asValue(ModelString.class));

      execute(rootResource, OperationNames.READ_RESOURCE, address);

      verify(testService).modelstring(DmrModelValue.newModel().asValue(ModelString.class));
      reset(testService);

      address = PathAddress.pathAddress("test-service", "context", "modelobject");
      assertNotNull(rootResource.getOperationHandler(address, OperationNames.READ_RESOURCE));
      when(operationContext.newModel(ModelObject.class)).thenReturn(DmrModelValue.newModel().asValue(ModelObject.class));

      execute(rootResource, OperationNames.READ_RESOURCE, address);

      verify(testService).modelobject(DmrModelValue.newModel().asValue(ModelObject.class));
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

   private BasicResultHandler execute(ManagedResource resource, String opName, String...path)
   {
      return execute(resource, opName, PathAddress.pathAddress(path));
   }

   private BasicResultHandler execute(ManagedResource resource, String opName, PathAddress address)
   {
      OperationHandler fooHandler = resource.getOperationHandler(address, opName);
      BasicResultHandler resultHandler = new BasicResultHandler();
      fooHandler.execute(operationContext, resultHandler);

      return resultHandler;
   }

   @Managed("/test-service")
   public static interface TestService
   {
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

      @Managed("context/model")
      public void model(@ManagedContext Model model);

      @Managed("context/modelvalue")
      public void modelvalue(@ManagedContext ModelValue model);

      @Managed("context/modelstring")
      public void modelstring(@ManagedContext ModelString model);

      @Managed("context/modelobject")
      public void modelobject(@ManagedContext ModelObject model);

      @Managed("sub-service")
      public SubTestService subService();
   }

   @Managed
   public static interface SubTestService
   {
      @Managed("some/path")
      public String somePath();

      @Managed("{abc}")
      public String mappedPath(@MappedPath("abc") String abc);
   }
}
