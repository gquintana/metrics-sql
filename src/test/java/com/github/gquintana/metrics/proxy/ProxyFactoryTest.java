package com.github.gquintana.metrics.proxy;

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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ProxyFactoryTest {
	private final DummyProxyHandler dummyProxyHandler=new DummyProxyHandler(new DummyImpl());

	private Dummy getDummy(ProxyFactory proxyFactory) {
		return proxyFactory.newProxy(dummyProxyHandler, new ProxyClass(Dummy.class.getClassLoader(), Dummy.class));
	}

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

	/*
	public ProxyFactoryTest(ProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
		dummyProxyHandler = new DummyProxyHandler(new DummyImpl());

	}
	*/

	public static Stream<Arguments> getParameters() {
		return Stream.of(
						new ReflectProxyFactory(),
						new CGLibProxyFactory(),
						new CachingProxyFactory())
				.map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("getParameters")
	public void testWork(ProxyFactory proxyFactory) {
		// Act
		String result = getDummy(proxyFactory).work("input");
		// Assert
		assertThat(dummyProxyHandler.getMethodInvocations().size()).isEqualTo(1);
		assertThat(result).isEqualTo("[input]");
	}

	@ParameterizedTest
	@MethodSource("getParameters")
	public void testFail(ProxyFactory proxyFactory) {
		try {
			// Act
			getDummy(proxyFactory).fail("error");
			fail("RuntimeException expected");
		} catch (RuntimeException e) {
			// Assert
			assertThat(dummyProxyHandler.getMethodInvocations().size()).isEqualTo(1);
			assertThat(e.getMessage()).isEqualTo("error");
		}
	}

}
