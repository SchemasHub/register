package schevo.server.common;

/**
 * 
 * @author tomecode.com
 *
 */
public class ServerBasicException extends Exception {

	private static final long serialVersionUID = 6645270874440404738L;

	public ServerBasicException(String msg, Exception ex) {
		super(msg, ex);
	}

}
