package schevo.server.api;

import java.util.Set;

public final class WorkspacesDto {

	private Set<String> workspaces;

	public WorkspacesDto(Set<String> workspaces) {
		this.workspaces = workspaces;
	}

	public final Set<String> getWorkspaces() {
		return workspaces;
	}

	public final void setWorkspaces(Set<String> workspaces) {
		this.workspaces = workspaces;
	}

}
