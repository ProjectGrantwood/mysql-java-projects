package projects.dao;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

/**
 * 
 * @author ProjectGrantwood
 *
 * This class represents the data access layer for the ProjectsApp menu-driven
 * application.
 *
 */

@SuppressWarnings("unused")
public class ProjectDao extends DaoBase {
	
	/**
	 * Constant representing the name of the category table.
	 */
	private static final String CATEGORY_TABLE = "category";
	/**
	 * Constant representing the name of the material table.
	 */
	private static final String MATERIAL_TABLE = "material";
	/**
	 * Constant representing the name of the project table.
	 */
	private static final String PROJECT_TABLE = "project";
	/**
	 * Constant representing the name of the project_category join table.
	 */
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	/**
	 * Constant representing the name of the step table.
	 */
	private static final String STEP_TABLE = "step";
	
	/**
	 * Adds a new row to the projects table based on the values contained in the
	 * <code>Project</code> instance passed as a parameter.
	 * 
	 * @param project A <code>Project</code> instance.
	 * @return the <code>Project</code> instance.
	 * @throws <code>DbException</code>
	 */
	public Project insertProject(Project project) {
		
		// @formatter:off
		String sql = ""
				+ "INSERT INTO " + ProjectDao.PROJECT_TABLE + " "
				+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
				+ "VALUES "
				+ "(?, ?, ?, ?, ?)";
		// @formatter:on
		
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				setParameter(statement, 1, project.getProjectName(), String.class);
				setParameter(statement, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(statement, 3, project.getActualHours(), BigDecimal.class);
				setParameter(statement, 4, project.getDifficulty(), Integer.class);
				setParameter(statement, 5, project.getNotes(), String.class);
				statement.executeUpdate();
				Integer projectId = getLastInsertId(conn, ProjectDao.PROJECT_TABLE);
				commitTransaction(conn);
				project.setProjectId(projectId);
				return project;
			}
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException e) {
			throw new DbException(e);
		}
	}

	/**
	 * Fetches all rows in the projects table.
	 * 
	 * @return a <code>List</code> of <code>Project</code>.
	 * @throws <code>DbException</code>
	 */
	
	public List<Project> fetchAllObjects() {
		
		// @formatter:off
		String sql = ""
				+ "SELECT project_id, project_name, estimated_hours, actual_hours, difficulty, notes "
				+ "FROM " + ProjectDao.PROJECT_TABLE;
		// @formatter: on
		
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				try (ResultSet results = statement.executeQuery()) {
					List<Project> projects = new LinkedList<>();
					while (results.next()) {
						projects.add(extract(results, Project.class));
					}
					return projects;
				}
			}
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException e) {
			throw new DbException(e);
		}
	}

	/**
	 * Fetches a specific row from the projects table.
	 * 
	 * @param projectId The numerical ID associated with the project to be
	 * 					fetched.
	 * @return an <code>Optional</code> object representing the row.
	 * @throws <code>DbException</code>
	 */
	
	public Optional<Project> fetchProjectById(Integer projectId) {
		
		// @formatter:off
		String sql = ""
				+ "SELECT * FROM " + ProjectDao.PROJECT_TABLE
				+ " WHERE project_id = ?";
		// @formatter:on
		
		try(Connection conn = DbConnection.getConnection()) {
			
			startTransaction(conn);
			
			try {
				
				Project project = null;
				
				try (PreparedStatement statement = conn.prepareStatement(sql)) {
					
					setParameter(statement, 1, projectId, Integer.class);
					
					try(ResultSet rs = statement.executeQuery()) {
						
						if (rs.next()) {
							project = extract(rs, Project.class);
						}
						
					}
				}
				
				if (Objects.nonNull(project)) {
					
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				
				}
				
				commitTransaction(conn);
				
				return Optional.ofNullable(project);
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException e) {
			throw new DbException(e);
		}
	}
	
	/**
	 * Obtains all rows of the category table corresponding to the provided 
	 * projectId.
	 * 
	 * @param conn A <code>Connection</code> object.
	 * @param projectId The numerical ID of the project for which the rows from
	 * 					the category table are being fetched.
	 * @return A <code>List</code> of <code>Category</code>.
	 */

	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		
		// @formatter:off
		String sql = ""
				+ "SELECT c.* FROM " + ProjectDao.CATEGORY_TABLE + " c "
				+ "JOIN " + ProjectDao.PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
				+ "WHERE project_id = ?";
		// @formatter:on
		
		try (PreparedStatement statement = conn.prepareStatement(sql)){
			
			setParameter(statement, 1, projectId, Integer.class);
			
			try(ResultSet rs = statement.executeQuery()) {
				
				List<Category> categories = new LinkedList<>();
				
				while(rs.next()) {
					categories.add(extract(rs, Category.class));
				}
				
				return categories;
				
			}
		}
	}
	
	/**
	 * Obtains all rows of the step table corresponding to the provided 
	 * projectId.
	 * 
	 * @param conn A <code>Connection</code> object.
	 * @param projectId The numerical ID of the project for which the rows from
	 * 					the step table are being fetched.
	 * @return A <code>List</code> of <code>Step</code>.
	 */


	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		
		// @formatter:off
		String sql = ""
				+ "SELECT * FROM " + ProjectDao.STEP_TABLE
				+ " WHERE project_id = ?";
		// @formatter:on
		
		try (PreparedStatement statement = conn.prepareStatement(sql)){
			
			setParameter(statement, 1, projectId, Integer.class);
			
			try(ResultSet rs = statement.executeQuery()) {
				
				List<Step> steps = new LinkedList<>();
				
				while(rs.next()) {
					steps.add(extract(rs, Step.class));
				}
				
				return steps;
				
			}
		}
	}
	
	/**
	 * Obtains all rows of the material table corresponding to the provided 
	 * projectId.
	 * 
	 * @param conn A <code>Connection</code> object.
	 * @param projectId The numerical ID of the project for which the rows from
	 * 					the material table are being fetched.
	 * @return A <code>List</code> of <code>Material</code>.
	 */


	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		
		// @formatter:off
		String sql = ""
				+ "SELECT * FROM " + ProjectDao.MATERIAL_TABLE
				+ " WHERE project_id = ?";
		// @formatter:on
		
		try (PreparedStatement statement = conn.prepareStatement(sql)){
			
			setParameter(statement, 1, projectId, Integer.class);
			
			try(ResultSet rs = statement.executeQuery()) {
				
				List<Material> materials = new LinkedList<>();
				
				while(rs.next()) {
					materials.add(extract(rs, Material.class));
				}
				
				return materials;
				
			}
		}
	}
	
	/**
	 * Updates a row of the project table based on the values contained in the
	 * <code>Project</code> instance passed to it.
	 * 
	 * @param updatedProject The <code>Project</code> instance containing the
	 * values to be updated.
	 * @return A <code>boolean</code> representing the success of the
	 * transaction.
	 * @throws <code>DbException</code>
	 */

	public boolean modifyProjectDetails(Project updatedProject) {
		
		// @formatter:off
		String sql = ""
				+ "UPDATE " + ProjectDao.PROJECT_TABLE + " SET "
				+ "project_name = ?, "
				+ "estimated_hours = ?, "
				+ "actual_hours = ?, "
				+ "difficulty = ?, "
				+ "notes = ? "
				+ "WHERE project_id = ?";
		// @formatter:on
		
		try (Connection conn = DbConnection.getConnection()) {
			
			startTransaction(conn);
			
			try(PreparedStatement statement = conn.prepareStatement(sql)) {
				setParameter(statement, 1, updatedProject.getProjectName(), String.class);
				setParameter(statement, 2, updatedProject.getEstimatedHours(), BigDecimal.class);
				setParameter(statement, 3, updatedProject.getActualHours(), BigDecimal.class);
				setParameter(statement, 4, updatedProject.getDifficulty(), Integer.class);
				setParameter(statement, 5, updatedProject.getNotes(), String.class);
				setParameter(statement, 6, updatedProject.getProjectId(), Integer.class);
				int success = statement.executeUpdate();
				commitTransaction(conn);
				return success == 1;
			}
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
			
		} 
		catch (SQLException e) {
			throw new DbException(e);
		}
		
	}
	
	/**
	 * Deletes a row of the project table.
	 * 
	 * @param ProjectId The numerical ID corresponding to the row to be deleted.
	 * @return A <code>boolean</code> representing the success of the
	 * transaction.
	 * @throws <code>DbException</code>
	 */

	public boolean deleteProject(Integer projectId) {
		// @formatter:off
		String sql = ""
				+ "DELETE FROM " + ProjectDao.PROJECT_TABLE
				+ " WHERE project_id = ?";
		// @formatter:on
		
		try (Connection conn = DbConnection.getConnection()) {
			
			startTransaction(conn);
			
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				setParameter(statement, 1, projectId, Integer.class);
				int success = statement.executeUpdate();
				commitTransaction(conn);
				return success == 1;
			}
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException e) {
			throw new DbException(e);
		}
		
	}

}
