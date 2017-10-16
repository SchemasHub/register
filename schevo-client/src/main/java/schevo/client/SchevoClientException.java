package schevo.client;

import org.json.JSONObject;

/**
 * 
 * @author Tome (tomecode.com)
 *
 */
public class SchevoClientException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5635541198084809654L;

	public SchevoClientException(String message) {
		super(message);
	}

	public SchevoClientException(String message, Exception e) {
		super(message, e);
	}

	public SchevoClientException(JSONObject json) {
		super(json.getString("name") + ": " + json.getString("message"));
	}

}
