import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import schevo.common.FileWalker;
import schevo.server.Config;
import schevo.server.SpaceException;
import schevo.server.space.FetchStream;
import schevo.server.space.PushStatus;
import schevo.server.space.Repository;
import schevo.server.space.RepositoryVersion;
import schevo.server.space.SpacesFsLocal;
import schevo.server.space.Workspace;

public class PushDocs {

	public static final void main(String[] arg) throws SpaceException, Exception {

		System.setProperty(Config.CONFIG_SPACE_LOCAL_DIR, System.getProperty("user.dir") + File.separator + "target/space02");

		SpacesFsLocal spaces = SpacesFsLocal.get();
		Workspace workspace = spaces.getWorkspace("oracle") == null ? spaces.newWorkspace("oracle") : spaces.getWorkspace("oracle");
		Repository repository = workspace.getRepository("union") == null ? workspace.newRepository("union") : workspace.getRepository("union");
		RepositoryVersion version = repository.getVersion("1.0.0") == null ? repository.newVersion("1.0.0") : repository.getVersion("1.0.0");

		MultipartFile[] mfs = new MultipartFile[] {
				//
				new MockMultipartFile("app.zip", "app.zip", "", Files.readAllBytes(new File("d:/Oracle/ws/union/KBP/kbp-common-mds/soa-infra/apps.zip").toPath())), //
				new MockMultipartFile("SprNP_Routing.dvm", "SprNP_Routing.dvm", "", Files.readAllBytes(new File("D:/Oracle/ws/union/KBP/kbp-common-mds/soa-infra/apps/dvm/SprNP_Routing.dvm").toPath()))//
				//
		};

		Thread.sleep(1000);
		PushStatus pushStatus = version.pushDocuments(mfs);

		FetchStream fs = version.fetchDocuments();

		try (OutputStream os = Files.newOutputStream(FileWalker.mkFile(Paths.get("d:/Install", fs.getFileName())), StandardOpenOption.WRITE)) {
			org.apache.commons.io.IOUtils.copy(fs.getIs(), os);
		}

		pushStatus.toString();
	}
}
