package projects.service;

import java.util.List;
import java.util.NoSuchElementException;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {
	
	/**
	 * Allows access to the Data Access Layer of the application.
	 */
	private ProjectDao projectDao = new ProjectDao();

	
	/**
	 * Passes a <code>Project</code> instance to 
	 * <code>ProjectDao.insertProject</code>.
	 * 
	 * @param project a <code>Project</code> instance to be added to the
	 * table.
	 * @return the <code>Project</code> instance.
	 */
	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}
	
	/**
	 * Obtains all rows of the project table. Does so as a call to 
	 * <code>ProjectDao.fetchAllObjects</code>.
	 * 
	 * @return A <code>List</code> of <code>Project</code>.
	 */

	public List<Project> fetchAllProjects() {
		return projectDao.fetchAllObjects();
	}
	
	/**
	 * Obtains one row of the project table. Does so as a call to 
	 * <code>ProjectDao.fetchProjectById</code>.
	 * 
	 * @param projectId A numerical ID associated with the project to be
	 * fetched.
	 * @return A <code>Project</code> instance.
	 * @throws NoSuchElementException
	 */

	public Project fetchProjectById(Integer projectId) {
		return projectDao.fetchProjectById(projectId).orElseThrow(
			() -> new NoSuchElementException(
				"Project with project ID=" + projectId
				+ " does not exist."
			)
		);
	}
	
	/**
	 * Updates a row of the project table. Does so as a call to
	 * <code>ProjectDao.modifyProjectDetails</code>.
	 * 
	 * @return A <code>boolean</code> representing if the transaction was 
	 * successful.
	 * @throws DbException
	 */

	public boolean modifyProjectDetails(Project updatedProject) {
		boolean success = projectDao.modifyProjectDetails(updatedProject);
		if (!success) {
			throw new DbException("Project with ID=" + updatedProject.getProjectId() + " does not exist.");
		}
		return success;
		
	}
	
	/**
	 * Deletes a row from the project table. Does so as a call to
	 * <code>ProjectDao.deleteProject</code>.
	 * 
	 * @throws DbException
	 */

	public void deleteProject(Integer projectId) {
		boolean success = projectDao.deleteProject(projectId);
		if (!success) {
			throw new DbException("\nThere is no row associated with id " + projectId + " in the project table, delete operation unsuccessful.");
		}
	}

}
