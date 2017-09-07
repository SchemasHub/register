package com.tomecode.schemashub.server.common;

import java.io.InputStream;

/**
 * 
 * @author tomecode.com
 *
 */
public final class DownloadStream implements AutoCloseable {

	private InputStream is;

	private String fileName;

	private String contentType;

	private long contentLength;

	public DownloadStream(InputStream is, String fileName, String contentType, long contentLength) {
		super();
		this.is = is;
		this.fileName = fileName;
		this.contentType = contentType;
		this.contentLength = contentLength;

	}

	public final InputStream getIs() {
		return is;
	}

	public final String getFileName() {
		return fileName;
	}

	public final String getContentType() {
		return contentType;
	}

	public final long getContentLength() {
		return contentLength;
	}

	@Override
	public final void close() throws Exception {
		if (is != null) {
			is.close();
		}
	}

}