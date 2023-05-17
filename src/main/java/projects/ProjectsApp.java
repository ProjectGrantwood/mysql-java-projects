package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

/**
 * This is the driver class for a menu-driven application allowing the 
 * user to perform CRUD operations on a MySQL database that stores information
 * about various DIY projects.
 * 
 * @author ProjectGrantwood
 * 
 */

public class ProjectsApp {
	
	
	
	// -------------------------------------------------------------------------
	// FIELDS:
	// -------------------------------------------------------------------------
	
	/**
	 * Used to access the service layer of the application.
	 * 
	 */
	
	private ProjectService projectService = new ProjectService();
	
	/**
	 * Holds all retrieved data regarding the user-selected project.
	 * 
	 */
	
	private Project curProject;
	
	/**
	 * Contains menu items displayed to the user.
	 * 
	 */
	
	// @formatter:off
	private List<String> operations = List.of(
			"1) Add a new project",
			"2) List projects",
			"3) Select a project",
			"4) View selected project details",
			"5) Update project details",
			"6) Delete a project",
			"7) Quit this application"
	);
	// @formatter:on
	
	/**
	 * Used to read user input to the terminal.
	 * 
	 */
	
	private Scanner scanner = new Scanner(System.in);
	
	
	
	// -------------------------------------------------------------------------
	// "MAIN" METHOD:
	// -------------------------------------------------------------------------
	
	
	
	/**
	 * The entry-point for the menu-driven application.
	 * 
	 * @param args included for compatibility, unused.
	 * 
	 */
	
	public static void main(String[] args) {
		new ProjectsApp().processUserSelections();
	}
	
	
	// -------------------------------------------------------------------------
	// ALL OTHER METHODS:
	// -------------------------------------------------------------------------
	
	
	/**
	 * Manages user selections, as well as monitoring if the menu-driven
	 * application is still running. Prints any <code>Exception</code> to the
	 * terminal with some additional text.
	 * 
	 */
	
	private void processUserSelections() {
		
		boolean done = false;
		
		while (!done) {
			
			try {
				
				int selection = getUserSelection();
				
				switch (selection) {
						
					// CASE 1: Add a new project
						
					case 1:
						createProject();
						break;
						
					// CASE 2: Print names of all projects in the table
						
					case 2:
						printProjects();
						break;
						
					// CASE 3: Select project based on user input
						
					case 3:
						selectProject();
						break;
					
					// CASE 4: Display the contents of the curProject field.
						
					case 4:
						viewSelectedProjectDetails();
						break;
						
					// CASE 5: Update selected project
						
					case 5:
						updateProjectDetails();
						break;
						
					// CASE 7: delete a project
						
					case 6:
						deleteProject();
						break;
						
					case 7:
						System.out.println("\nExiting the menu.");
						System.out.println("Goodbye!");
						done = true;
						break;
				
					// DEFAULT CASE: used if input isn't recognized.
						
					default:
						System.out.println("\n" + selection + " is not a valid selection. Try again.");
						
				}
				
			} catch (Exception e) {
				
				StringBuilder errorMessage = new StringBuilder("\nError: ");
				errorMessage.append(e);
				errorMessage.append(" Try again.");
				
				System.out.println(errorMessage.toString());
				
			}
		}
		
	}
	
	/**
	 * Allows the user to select a project to delete. Includes an additional
	 * prompt for them to verify that they want to delete the project. If the
	 * project to be deleted has the same <code>projectId</code> as the 
	 * <code>curProject</code> class variable, sets <code>curProject</code> to
	 * <code> null </code>.
	 */
	
	private void deleteProject() {
		printProjects();
		Integer projectId = getIntInput("Please select the number ID of the project you wish to delete", false);
		String confirm = getStringInput("Are you sure you wish to delete project " + projectId + "? Type [y] to confirm, [n] to abort");
		if (confirm.equalsIgnoreCase("y")) {
			projectService.deleteProject(projectId);
			System.out.println("Project " + projectId + " was successfully deleted.");
			curProject = Objects.nonNull(curProject) ? null : curProject;
		} else {
			System.out.println("\nAborting project deletion.");
		}
		
		
	}


	/**
	 * 
	 * Guides the user through each column in the <code>curProject</code> class
	 * variable, allowing them to input new
	 * values for each. Does so by creating a new <code>Project</code> instance
	 * and assigning it either the updated values, or copies them from the
	 * <code>curProject</code> object if the user does not provide a new value.
	 * 
	 * If the transaction with the SQL database is successful, fetches the
	 * the project row from the database with 
	 * <code>ProjectService.fetchProjectbyId</code>, which populates a new
	 * <code>Project</code> object with that data, and assigns it to the
	 * <code>curProject</code> class variable.
	 * 
	 */
	
	private void updateProjectDetails() {
		if (Objects.isNull(curProject)) {
			System.out.println("\nYou currently have no project selected. Press 4 at the main menu to select a project.");
		} else {
			Project updatedProject = new Project();
			
			/* For each column in the project table, display the current value,
			 * Then set it to the user input. Finally set the ID to the same
			 * as that of the current project.
			 */
			
			String projectName = getStringInput("Enter the project name [" + curProject.getProjectName() + "]");
			updatedProject.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
			
			
			BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours [" + curProject.getEstimatedHours() + "]", true);
			updatedProject.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
			BigDecimal actualHours = getDecimalInput("Enter the actual hours [" + curProject.getActualHours() + "]", true);
			updatedProject.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
			Integer difficulty = getValidDifficulty(true);
			updatedProject.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
			String notes = getStringInput("Enter the project notes [" + curProject.getNotes() + "]");
			updatedProject.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);
			updatedProject.setProjectId(curProject.getProjectId());
			
			boolean success = projectService.modifyProjectDetails(updatedProject);
			if (success) {
				System.out.println("Project with ID=" + updatedProject.getProjectId() + " successfully updated.");
				curProject = projectService.fetchProjectById(curProject.getProjectId());
			}
		}
		
	}

	/**
	 * Prints the selected project to the terminal. The selected project is held
	 * in the <code>curProject</code> class variable. In cases where
	 * there is no project selected, an additional message guides the user 
	 * toward the menu item where they can select a project.
	 * 
	 */
	
	private void viewSelectedProjectDetails() {
		if (Objects.isNull(curProject)) {
			System.out.println("\nYou currently have no project selected. Press 4 at the main menu to select a project.");
		} else {
			System.out.println("\nHere are the details of the currently selected project:");
			System.out.println(curProject);
		}
	}


	/**
	 * Sets the value of the <code>curProject</code> class variable based on
	 * project data retrieved from the table. Requires the user to input a
	 * valid <code>projectId</code> after printing a list of projects to the by
	 * calling <code>printProjects</code>.
	 */
	
	private void selectProject() {
		printProjects();
		Integer projectId = getIntInput("Select a project from the above list by entering its ID (the number to its left)", false);
		curProject = null;
		curProject = projectService.fetchProjectById(projectId);
		System.out.println("\nYou have selected " + curProject.getProjectName());
		
	}

	/**
	 * Creates a <code>Project</code> instance and moves the user
	 * through populating its fields with a series of prompts.
	 * 
	 */
	
	private void createProject() {
		
		String projectName = getStringInput("\nEnter the project name");
		BigDecimal estimatedHours = getDecimalInput("\nEnter the estimated hours", false);
		BigDecimal actualHours = getDecimalInput("\nEnter the actual hours", false);
		Integer difficulty = getValidDifficulty(false);
		String notes = getStringInput("\nEnter the project notes");
		
		Project project = new Project();
		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);
		
		Project dbProject = projectService.addProject(project);
		
		System.out.println("\nYou have successfully created project " + dbProject);
		
	}

	/**
	 * Obtains an <code>Integer</code> value representing project difficulty 
	 * from the user. If the value is not betweeen 1-5 inclusive,
	 * re-prompts the user with a marginally more helpful message.
	 * 
	 * @param allowNull To be passed down to <code>getIntInput()</code>, as well
	 * 					as the validity check. Specifies if <code>null</code>
	 * 					input is allowed.
	 * 
	 * @return an <code>Integer</code> that's valid given the constraints.
	 * 
	 */
	
	private Integer getValidDifficulty(boolean allowNull) {
		Integer difficulty = getIntInput("\nEnter a difficulty from 1-5 (1 is easier, 5 is harder)", allowNull);
		
		// Check if the difficulty input is valid input
		boolean difficultyIsValid = checkForValidityOfDifficultyInput(difficulty, allowNull);
		
		// use a while loop to handle invalid input
		while (!difficultyIsValid) {
			// re-prompt the user, set difficulty to their new input, check if it's valid again.
			System.out.println("\nPlease enter a valid integer between 1 and 5 inclusive.");
			difficulty = getIntInput("\nEnter a difficulty from 1-5 (1 is easier, 5 is harder)", allowNull);
			difficultyIsValid = checkForValidityOfDifficultyInput(difficulty, allowNull);
		}
		return difficulty;
	}
	
	/**
	 * Tests that an <code>Integer</code> is between 1 and 5 inclusive, or
	 * <code>null</code>, if the appropriate parameter is set to true.
	 * 
	 * @param difficulty The <code>Integer</code> being tested.
	 * 		  allowNull  accept null input as valid.
	 * 
	 * @return <code>true</code> if input is <code>null</code> (if
	 * <code>allowNull</code> is true) OR between 1 and 5 inclusive, 
	 * otherwise false.
	 */

	private boolean checkForValidityOfDifficultyInput(Integer difficulty, boolean allowNull) {
		return (allowNull ? Objects.isNull(difficulty) : false) || (Integer.compare(difficulty, 1) >= 0 && Integer.compare(difficulty, 5) <= 0);
	}

	/**
	 * Prints the available operations, obtains the user selection,
	 * and handles cases where the user instructs the program to continue
	 * without providing an input.
	 * 
	 * @return The user-provided input, or <code>-1</code> if no input is given.
	 * 
	 */

	private int getUserSelection() {
		printOperations();
		Integer input = getIntInput("Enter the number of one of the above menu items to continue", false);
		return Objects.isNull(input) ? -1 : input;
	}
	
	/**
	 * Attempts to convert user input (obtained by
	 * <code>getStringInput</code>) to an <code>Integer</code> and handles 
	 * cases where the input is <code>null</code>. 
	 * 
	 * @param prompt For display to the user, passed to <code>getStringInput</code>
	 * @param allowNull If blank input is valid, this <code>boolean</code> 
	 * 					prevents a <code>NullPointerException</code> from being 
	 * 					thrown.
	 * @return value of prompt, or <code>null</code>.
	 * @throws <code>DbException</code>.
	 * 
	 */

	private Integer getIntInput(String prompt, boolean allowNull) {
		String input = getStringInput(prompt);
		try {
			return Objects.isNull(input) ? null : Integer.valueOf(input);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		} catch (NullPointerException e) {
			if (allowNull) {
				return null;
			}
			throw new DbException("Please enter a number.");
		}
	}

	/**
	 * Prints the given prompt to the terminal, then obtains the
	 * user input by reading the terminal's next line. Trims the input, and
	 * handles cases where no input is given.
	 * 
	 * @param prompt The prompt to print.
	 * @return The trimmed input, or <code>null</code>.
	 * 
	 */
	
	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String input = scanner.nextLine();
		return input.isBlank() ? null : input.trim();
	}
	
	/**
	 * Tries to convert user input to a <code>BigDecimal</code>.
	 * 
	 * @param prompt Shown to the user before obtaining the input.
	 * @param allowNull If blank input is valid, this boolean prevents a
	 * 					<code>NullPointerException</code> from being thrown.
	 * @return the user input as a BigDecimal.
	 * 
	 */

	private BigDecimal getDecimalInput(String prompt, boolean allowNull) {
		String input = getStringInput(prompt);
		try {
			return new BigDecimal(input).setScale(2);
		} 
		catch (NumberFormatException e) {
			throw new DbException("Please enter a valid decimal number.");
		}
		catch (NullPointerException e) {
			if (allowNull) {
				return null;
			}
			throw new DbException("You must provide an input here.");
		}
	}
	
	/**
	 * Prints the contents of the <code>operations</code> field using a 
	 * <code>StringBuilder</code> instance. Performs some formatting.
	 * 
	 */

	private void printOperations() {
		System.out.println("\nWhat do you wish to do?");
		StringBuilder operationsSB = new StringBuilder();
		operations.forEach(line -> operationsSB.append("\n" + line));
		System.out.println(operationsSB.toString().indent(3));
		System.out.println(
				Objects.isNull(curProject) 
				? "\nThere is no project currently selected."
				: "\nThe currently selected project is: " + curProject.getProjectName()
		);
	}
	
	/**
	 * Prints the contents of the <code>operations</code> field to the terminal 
	 * using human-readable formatting. Additionally prepends a message 
	 * prompting the user to pick from those selections.
	 * 
	 */
	
	private void printProjects() {
		List<Project> projects = projectService.fetchAllProjects();
		StringBuilder projectsSB = new StringBuilder();
		projects.forEach(project -> 
			projectsSB.append(
					"\n" 
					+ project.getProjectId() 
					+ ": "
					+ project.getProjectName()
				)
			);
		System.out.println("\nProjects:");
		System.out.println(projectsSB.toString().indent(3));
	}
	

}
