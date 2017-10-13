# schevo

 Schema evolution in reality (this is POC/MVP)
  
# Legend
	
 - document: is object/asset which represent one (single) file of type: WSDL, XSD, JSON Schema, Swagger(OpenAPi), RAML, ...), document(s) are stored in space
 - space: is the place where documents are stored; this place is represent as: workspace/repository/version = space (spaceRef i.e. reference to space)
 - workspace: is place where is at least one repository
 - repository: is place where versions are stored
 - version: (of repository) is place where are documents


# REST API

	- Create new workspace
	(POST) /spaces/v1/:workspaceName

	- Get workspace by name
	(GET) /spaces/v1/:workspaceName

	- Create new repository in workspace
	(POST) /spaces/v1/:workspaceName/:repositoyName

	- Get repository by name in workspace
	(GET) /spaces/v1/:workspaceName/:repositoyName

	- Create new version of repository in particular workspace
	(POST) /spaces/v1/:workspaceName/:repositoyName/:repositoryVersion

	- Get version of repository from workspace
	(GET) /spaces/v1/:workspaceName/:repositoyName/:repositoryVersion

	- Upload document(s) to particular space
	(POST) /spaces/pushDocuments?spaceRef=?&file=[1...N]

	Where:
		(Parameter|Required|1) spaceRef=<path to target space e.g.: workspace/repository/repositoryVersion>
		(Parameter|Required|1..N) file=<file, which will be uploaded (push-ed)> to particular version of repository>

	- Fetch document(s) from particular space
	(POST) /spaces/fetchDocuments?spaceRef=?&type=[ZIP|...]

	

--------------------------
	- Last version
	(GET) /spaces/v1/:workspaceName/:repositoyName/last
	- Last or filter
	(GET) /spaces/v1/:workspaceName/:repositoyName/filter?from=&to=

	- Get file
	(GET) /spaces/v1/:workspaceName/:repositoyName/:repositoryVersion/content/<filepath>?version=1.0.0
	samples:
	(GET) /spaces/v1/oracle/union/1.0.0/content/sk/core/bankindg.xsd/?version=2.0.0
	(GET) /spaces/v1/oracle/union/1.0.2/content/sk/core/bankindg.xsd/?version=1.0.0
	(GET) /spaces/v1/oracle/union/2.0.2/content/sk/core/bankindg.xsd/?version=1.0.0


	{
		'id':'',
		'file':<file path>,
		'date':'',
		'comment':''
	}



	- Upload document(s) to particular space
	(POST) /spaces/v1/:workspaceName/:repositoyName/:repositoryVersion/push?[files...]
	- Fetch document(s) from particular space
	(POST) /spaces/v1/:workspaceName/:repositoyName/:repositoryVersion/fetch?[files...]

--------------------------
 

 