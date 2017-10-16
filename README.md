# schevo

 Schema evolution in reality (this is POC/MVP)
  
# Legend
	
 - document: is object/asset which represent one (single) file of type: WSDL, XSD, JSON Schema, Swagger(OpenAPi), RAML, ...), document(s) are stored in space
 - space: is the place where documents are stored; this place is represent as: workspace/repository/version = space (spaceRef i.e. reference to space)
 - workspace: is place where is at least one repository
 - repository: is place where versions are stored
 - version: (of repository) is place where are documents


# API: Workspace

	- Create a new workspace
		(URL|POST) /spaces/:workspaceName
	- Where:
		(PathVariable|Required|1:1) :workspaceName=<name of particular workspace>


	- Get workspace by name
		(URL|GET) /spaces/:workspaceName
	- Where:
		(PathVariable|Required|1:1) :workspaceName=<name of particular workspace>

# API: Repository

	- Create a new repository in particular workspace
		(URL|POST) /spaces/:workspaceName/:repositoryName
	- Where:
		(PathVariable|Required|1:1) :workspaceName=<name of particular workspace>
		(PathVariable|Required|1:1) :repositoryName=<name of particular repository>


	- Get repository by name from particular workspace
		(URL|GET) /spaces/:workspaceName/:repositoryName
	- Where:
		(PathVariable|Required|1:1) :workspaceName=<name of particular workspace>
		(PathVariable|Required|1:1) :repositoryName=<name of particular repository>

# API: Repository version

	- Create a new version of repository in particular workspace
		(URL|POST) /spaces/:workspaceName/:repositoryName/:repositoryVersionName
	- Where:
		(PathVariable|Required|1:1) :workspaceName=<name of particular workspace>
		(PathVariable|Required|1:1) :repositoryName=<name of particular repository>
		(PathVariable|Required|1:1) :repositoryVersionName=<name of repository version>


	- Get version of repository from workspace
		(URL|GET) /spaces/:workspaceName/:repositoryName/:repositoryVersionName
	- Where:
		(PathVariable|Required|1:1) :workspaceName=<name of particular workspace>
		(PathVariable|Required|1:1) :repositoryName=<name of particular repository>
		(PathVariable|Required|1:1) :repositoryVersionName=<name of repository version>

# API: Upload (push) document(s) to space

	- Upload document(s) to particular space
		(URL|POST) /documents/push?spaceRef=?&file=[1...N]
	- Where:
		(Parameter|Required|1:1) spaceRef=<path to target space e.g.: workspace/repository/repositoryVersion>
		(Parameter|Required|1:N) file=<file, which will be uploaded (push-ed)> to particular version of repository>

# API: Download (fetch) document(s) from space

	- Fetch document(s) from particular space
		(URL|POST) /documents/fetch?spaceRef=?&downloadAs=[ZIP|...]
	- Where:
		(Parameter|Required|1:1) spaceRef=<path to target space e.g.: workspace/repository/repositoryVersion>	
		(Parameter|Required|1:N) file=<file, which will be uploaded (push-ed)> to particular version of repository>
		(Parameter|Optional|1:1|Default) downloadAs=<type of > | default value: ZIP

