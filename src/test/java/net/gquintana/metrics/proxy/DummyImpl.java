package net.gquintana.metrics.proxy;

/**
 */
public class DummyImpl implements Dummy {
	@Override
	public String work(String input) {
		return "["+input+"]";
	}

	@Override
	public void fail(String input) {
		throw new RuntimeException(input);
	}
}
