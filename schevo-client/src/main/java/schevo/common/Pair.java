package schevo.common;

/**
 * 
 * @author Tome (tomecode.com)
 *
 * @param <S>
 * @param <T>
 */
public final class Pair<S, T> {
	private S key;

	private T value;

	public Pair(S key, T value) {
		this.key = key;
		this.value = value;
	}

	public final S getKey() {
		return key;
	}

	public final T getValue() {
		return value;
	}

}
