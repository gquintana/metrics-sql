package net.gquintana.metrics.proxy;

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