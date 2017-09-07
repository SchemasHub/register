package com.tomecode.schemashub.server.register;

/**
 * 
 * @author tomecode.com
 *
 */
public class SpaceException extends Exception {

	private static final long serialVersionUID = -7882356247083933330L;

	public SpaceException(String msg, Exception e) {
		super(msg, e);
	}

	public SpaceException(String msg) {
		super(msg);
	}

	public final static class WorkspaceExistsException extends SpaceException {

		private static final long serialVersionUID = -5374466046499211395L;

		public WorkspaceExistsException(String msg) {
			super(msg);
		}
	}

	public final static class RepositoryExistsException extends SpaceException {

		private static final long serialVersionUID = -5374466046499211395L;

		public RepositoryExistsException(String msg) {
			super(msg);
		}
	}

	public final static class RepositoryVersionExistsException extends SpaceException {

		private static final long serialVersionUID = -5374466046499211395L;

		public RepositoryVersionExistsException(String msg) {
			super(msg);
		}
	}
}
