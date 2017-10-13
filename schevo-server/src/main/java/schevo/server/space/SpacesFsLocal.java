package schevo.server.space;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import schevo.common.FileWalker;
import schevo.common.Utils;
import schevo.server.Config;
import schevo.server.SpaceException;
import schevo.server.SpaceException.WorkspaceExistsException;

/**
 * Space on local FS i.e. all files are stored in local FS, singleton
 * 
 * @author tomecode.com
 *
 */
// TODO: provide support for apache zookeeper
public class SpacesFsLocal {

	// TODO: check size of local space

	private static final Logger log = Logger.getLogger(SpacesFsLocal.class);

	@JsonIgnore
	private static volatile SpacesFsLocal me;

	@JsonIgnore
	private Hashtable<String, Workspace> workspaces;

	/**
	 * root path
	 */
	@JsonIgnore
	private Path fsPathRoot;

	private SpacesFsLocal() {
		this.workspaces = new Hashtable<>();

		String path = Utils.strOrNull(System.getProperty(Config.CONFIG_SPACE_LOCAL_DIR));
		if (path == null) {
			log.info("Property: space.dir is empty, user.dir will used");
			path = System.getProperty("user.dir");
		}
		fsPathRoot = Paths.get(path);
		log.info("Initializing space in dir: " + fsPathRoot);

		try {
			// create the root directory if not exist
			FileWalker.mkDirs(fsPathRoot);
		} catch (Exception e) {
			log.error("Failed to initialized space.dir, reason: " + e.getMessage(), e);
			throw new RuntimeException("Failed to initialized space.dir, reason: " + e.getMessage(), e);
		}

		initWorkspaces();
	}

	/**
	 * init workspace
	 */
	private final void initWorkspaces() {
		log.info("Init workspaces");

		try {
			for (Path fsWorkspaceDir : Files.newDirectoryStream(this.fsPathRoot)) {
				initWorkspace(fsWorkspaceDir);
			}
		} catch (IOException e) {
			log.error("Failed to init workspaces: " + this.fsPathRoot + " reason: " + e.getMessage(), e);
		}
	}

	private final void initWorkspace(Path fsWorkspaceDir) {
		log.info("Init workspace: " + fsWorkspaceDir);

		String on = fsWorkspaceDir.getFileName().toString();
		try {
			Workspace workspace = new Workspace(this.fsPathRoot, on);
			this.workspaces.put(on, workspace);
		} catch (SpaceException e) {
			log.error("Failed to init workspace: " + on + " reason: " + e.getMessage(), e);
		}

	}

	public final static SpacesFsLocal get() {
		if (me == null) {
			SpacesFsLocal instance = me;
			synchronized (SpacesFsLocal.class) {
				me = instance;
				if (me == null) {
					me = instance = new SpacesFsLocal();
				}
			}
		}
		return me;
	}

	public final Path getFsPathRoot() {
		return fsPathRoot;
	}

	/**
	 * create new workspace
	 * 
	 * @param name
	 * @return
	 * @throws SpaceException
	 */
	public final Workspace newWorkspace(String name) throws SpaceException {
		name = SpaceName.toName(name);
		if (name == null) {
			throw new SpaceException("Worksapce name can't be null or emtpy!");
		}

		Workspace workspace = this.workspaces.get(name);
		if (workspace == null) {
			workspace = new Workspace(this.fsPathRoot, name);
			this.workspaces.put(name, workspace);
		} else {
			throw new WorkspaceExistsException("Workspace: " + name + " already exists!");
		}

		return workspace;
	}

	/**
	 * return the space by name
	 * 
	 * @param name
	 * @return
	 */
	public final Workspace getWorkspace(String name) {
		name = SpaceName.toName(name);
		if (name == null) {
			return null;
		}
		return this.workspaces.get(name);
	}

	public final Set<String> getWorkspaceNames() {
		return this.workspaces.keySet();
	}
}
