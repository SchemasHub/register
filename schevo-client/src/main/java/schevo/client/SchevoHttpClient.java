package schevo.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Internal simple HTTP client for Schevo client
 * 
 * @author Tome (tomecode.com)
 *
 */
final class SchevoHttpClient {

	public static final JSONObject doGetJson(URI uri) throws SchevoClientException, IOException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			try (CloseableHttpResponse response = httpclient.execute(new HttpGet(uri))) {
				StatusLine statusLine = response.getStatusLine();
				if (HttpStatus.SC_OK == statusLine.getStatusCode()) {
					return toJson(response.getEntity());
				} else {
					processHttpStatus(uri, response, statusLine);
				}
			}
		}
		return null;
	}

	public static final void doGetBinary(URI uri, RepsonseHandler repsonseHandler) throws ClientProtocolException, SchevoClientException, IOException   {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			try (CloseableHttpResponse response = httpclient.execute(new HttpGet(uri))) {
				StatusLine statusLine = response.getStatusLine();
				if (HttpStatus.SC_OK == statusLine.getStatusCode()) {
					repsonseHandler.responseContent(response);
				} else {
					processHttpStatus(uri, response, statusLine);
				}
			}
		}
	}

	/**
	 * 
	 * @author Tome (tomecode.com)
	 *
	 */
	public static interface RepsonseHandler {

		void responseContent(CloseableHttpResponse response) throws UnsupportedOperationException, IOException;

	}

	private final static void processHttpStatus(URI uri, CloseableHttpResponse response, StatusLine statusLine) throws ClientProtocolException, SchevoClientException, IOException {
		if (HttpStatus.SC_BAD_REQUEST == statusLine.getStatusCode()) {
			throw new SchevoClientException(toJson(response.getEntity()));
		}
		throw new SchevoClientException("Unknown error for url: " + uri + " reason:" + statusLine.toString());
	}

	/**
	 * parse JSON content to {@link JSONObject}
	 * 
	 * @param httpEntity
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private final static JSONObject toJson(HttpEntity httpEntity) throws ClientProtocolException, IOException {
		try (InputStream is = httpEntity.getContent()) {
			return new JSONObject(new JSONTokener(httpEntity.getContent()));
		}

	}
}
