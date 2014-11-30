package net.gquintana.metrics.proxy;

/*
 * #%L
 * Metrics SQL
 * %%
 * Copyright (C) 2014 Open-Source
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import net.gquintana.metrics.util.ParametersBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class ProxyFactoryTest {
	private final ProxyFactory proxyFactory;
	private final Dummy dummy;
	private final DummyProxyHandler dummyProxyHandler;

	private static class DummyProxyHandler extends ProxyHandler<Dummy> {
		private final List<MethodInvocation<Dummy>> methodInvocations = new ArrayList<>();

		public DummyProxyHandler(Dummy delegate) {
			super(delegate);
		}

		public List<MethodInvocation<Dummy>> getMethodInvocations() {
			return methodInvocations;
		}

		@Override
		protected Object invoke(MethodInvocation<Dummy> delegatingMethodInvocation) throws Throwable {
			methodInvocations.add(delegatingMethodInvocation);
			return super.invoke(delegatingMethodInvocation);
		}
	}

	public ProxyFactoryTest(ProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
		dummyProxyHandler = new DummyProxyHandler(new DummyImpl());
		dummy = proxyFactory.newProxy(dummyProxyHandler, new ProxyClass(Dummy.class.getClassLoader(), Dummy.class));
	}

	@Parameterized.Parameters
	public static Collection<Object[]> getParameters() {
		return new ParametersBuilder()
				.add(new ReflectProxyFactory())
				.add(new CGLibProxyFactory())
				.add(new CachingProxyFactory())
				.build();
	}

	@Test
	public void testWork() {
		// Act
		String result = dummy.work("input");
		// Assert
		assertEquals(1, dummyProxyHandler.getMethodInvocations().size());
		assertEquals("[input]", result);
	}

	@Test
	public void testFail() {
		try {
			// Act
			dummy.fail("error");
			fail("RuntimeException expected");
		} catch (RuntimeException e) {
			// Assert
			assertEquals(1, dummyProxyHandler.getMethodInvocations().size());
			assertEquals("error", e.getMessage());
		}
	}

}