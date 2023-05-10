package projects.service;

import java.util.List;
import java.util.NoSuchElementException;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {
	
	private ProjectDao projectDao = new ProjectDao();

	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}

	public List<Project> fetchAllProjects() {
		return projectDao.fetchAllObjects();
	}

	public Project fetchProjectById(Integer projectId) {
		return projectDao.fetchProjectById(projectId).orElseThrow(
			() -> new NoSuchElementException(
				"Project with project ID=" + projectId
				+ " does not exist."
			)
		);
	}

	public void modifyProjectDetails(Project updatedProject) {
		boolean success = projectDao.modifyProjectDetails(updatedProject);
		if (!success) {
			throw new DbException("Project with ID=" + updatedProject.getProjectId() + " does not exist.");
		}
		
	}

}
