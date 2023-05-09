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

@SuppressWarnings("unused")
public class ProjectDao extends DaoBase {
	
	/**
	 * Constants representing the table names.
	 * 
	 */
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";
	
	/**
	 * This method 
	 * @param project
	 * @return
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

	public Optional<Project> fetchProjectById(Integer projectId) {
		
		// @formatter:off
		String sql = ""
				+ "SELECT * FROM " + ProjectDao.PROJECT_TABLE
				+ " WHERE project_id = ?";
		// @formatter:on
		
		try(Connection conn = DbConnection.getConnection()) {
			
			startTransaction(conn);
			
			try {
				
				Project project= null;
				
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

}
