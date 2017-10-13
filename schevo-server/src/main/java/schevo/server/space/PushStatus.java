package schevo.server.space;

import java.util.ArrayList;
import java.util.List;

/**
 * status of push
 * 
 * @author Tome (tomecode.com)
 *
 */
public class PushStatus {

	/**
	 * time of push
	 */
	private long when;

	/**
	 * 
	 */
	private long pushTime;
	/**
	 * 
	 */
	private List<PushFile> singleFiles;

	private List<PushPackageFile> packageFiles;

	public PushStatus() {
		this.singleFiles = new ArrayList<>();
		this.packageFiles = new ArrayList<>();
	}

	public final long getWhen() {
		return when;
	}

	public final void setWhen(long when) {
		this.when = when;
	}

	public final long getPushTime() {
		return pushTime;
	}

	public final void setPushTime(long pushTime) {
		this.pushTime = pushTime;
	}

	public final List<PushFile> getSingleFiles() {
		return singleFiles;
	}

	public final List<PushPackageFile> getPackageFiles() {
		return packageFiles;
	}

	/**
	 * create/register new {@link PushPackageFile}
	 * 
	 * @param fileName
	 * @return
	 */
	public final PushPackageFile newPkg(String fileName) {
		PushPackageFile packageFile = new PushPackageFile();
		packageFile.setName(fileName);
		this.packageFiles.add(packageFile);
		return packageFile;
	}

	public final PushFile singleFile(String fileName) {
		PushFile pushFile = new PushFile();
		pushFile.setPath(fileName);
		this.singleFiles.add(pushFile);
		return pushFile;
	}

	/**
	 * package file
	 * 
	 * @author Tome (tomecode.com)
	 *
	 */
	public static class PushPackageFile implements Push {

		private String name;
		/**
		 * list of files which was pushed
		 */
		private List<PushFile> content;

		public PushPackageFile() {
			content = new ArrayList<>();
		}

		public final String getName() {
			return name;
		}

		public final void setName(String name) {
			this.name = name;
		}

		/**
		 * list of files
		 * 
		 * @return
		 */
		public final List<PushFile> getContent() {
			return content;
		}

		public final PushFile singleFile(String fileName) {
			PushFile pushFile = new PushFile();
			pushFile.setPath(fileName);
			this.content.add(pushFile);
			return pushFile;
		}

	}

	/**
	 * 
	 * @author Tome (tomecode.com)
	 *
	 */
	public static class PushFile extends Document implements Push {
		private boolean isPushed;

		public PushFile() {

		}

		public final boolean isPushed() {
			return isPushed;
		}

		public final void setPushed(boolean isPushed) {
			this.isPushed = isPushed;
		}
	}

	public interface Push {

	}

}
