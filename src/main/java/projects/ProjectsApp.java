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
	 * This <code>ProjectService</code> instance is used throughout the class to
	 * access the service layer of the application.
	 * 
	 */
	
	private ProjectService projectService = new ProjectService();
	
	/**
	 * This <code>Project</code> instance is used to hold all retrieved data
	 * regarding the user-selected project, if there is one.
	 * 
	 */
	
	private Project curProject;
	
	/**
	 * This <code>List</code> object that stores the menu items displayed to the 
	 * user.
	 * 
	 */
	
	// @formatter:off
	private List<String> operations = List.of(
			"1) Quit this application",
			"2) Add a new project",
			"3) List projects",
			"4) Select a project",
			"5) View selected project details",
			"6) Update project details"
	);
	// @formatter:on
	
	/**
	 * A <code>Scanner</code> object that will read user input to the terminal.
	 * 
	 */
	
	private Scanner scanner = new Scanner(System.in);
	
	
	
	// -------------------------------------------------------------------------
	// "MAIN" METHOD:
	// -------------------------------------------------------------------------
	
	
	
	/**
	 * This method is the entry point for the application.
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
	 * This method allows the user to make selections. It's not SOLID because
	 * it also attempts to both handle errors and monitor if the application
	 * should continue running, and in actual use cases, it would be better to
	 * put these functionalities in separate classes.
	 * 
	 */
	
	private void processUserSelections() {
		
		boolean done = false;
		
		while (!done) {
			
			try {
				
				int selection = getUserSelection();
				
				switch (selection) {
				
					// CASE 1: Quit the application
				
					case 1:
						done = exitMenu();
						break;
				
					// CASE 2: Add a new project
						
					case 2:
						createProject();
						break;
						
					// CASE 3: Print names of all projects in the table
						
					case 3:
						printProjects();
						break;
						
					// CASE 4: Select project based on user input
						
					case 4:
						selectProject();
						break;
					
					// CASE 5: Display the contents of the curProject field.
						
					case 5:
						viewSelectedProjectDetails();
						break;
						
					// CASE 6: Update selected project
						
					case 6:
						updateProjectDetails();
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
	 * 
	 * This method 
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
			BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours [" + curProject.getEstimatedHours() + "]");
			updatedProject.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
			BigDecimal actualHours = getDecimalInput("Enter the actual hours [" + curProject.getActualHours() + "]");
			updatedProject.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
			Integer difficulty = getValidDifficulty();
			updatedProject.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
			String notes = getStringInput("Enter the project ntoes [" + curProject.getNotes() + "]");
			updatedProject.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);
			updatedProject.setProjectId(curProject.getProjectId());
			
			projectService.modifyProjectDetails(updatedProject);
			curProject = projectService.fetchProjectById(curProject.getProjectId());
		}
		
	}

	/**
	 * This method prints the selected project to the terminal. In cases where
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
	 * This method sets the value of the <code>curProject</code> field based on
	 * project data retrieved from the table. It requires the user to input a
	 * valid projectId after printing a list of projects to the terminal using
	 * <code>printProjects</code>.
	 */
	
	private void selectProject() {
		printProjects();
		Integer projectId = getIntInput("Select a project from the above list by entering its ID (the number to its left)");
		curProject = null;
		curProject = projectService.fetchProjectById(projectId);
		System.out.println("\nYou have selected " + curProject.getProjectName());
		
	}


	private boolean exitMenu() {
		System.out.println("\nExiting the menu.");
		System.out.println("Goodbye!");
		return true;
	}


	/**
	 * This method creates a <code>Project</code> instance and moves the user
	 * through populating its fields with a series of prompts.
	 * 
	 */
	
	private void createProject() {
		
		String projectName = getStringInput("\nEnter the project name");
		BigDecimal estimatedHours = getDecimalInput("\nEnter the estimated hours");
		BigDecimal actualHours = getDecimalInput("\nEnter the actual hours");
		Integer difficulty = getValidDifficulty();
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
	 * This method obtains an integer value representing project difficulty from
	 * the user, but accepts only an integer between 1-5 inclusive.
	 * Otherwise, it will re-prompt the user with a marginally more helpful 
	 * message.
	 * 
	 * @return an Integer that's valid given the constraints.
	 * 
	 */
	
	private Integer getValidDifficulty() {
		Integer difficulty = getIntInput("\nEnter a difficulty from 1-5 (1 is easier, 5 is harder)");
		
		// Check if the difficulty input is valid input
		boolean difficultyIsValid = checkForValidityOfDifficultyInput(difficulty);
		
		// use a while loop to handle invalid input
		while (!difficultyIsValid) {
			// re-prompt the user, set difficulty to their new input, check if it's valid again.
			System.out.println("\nPlease enter a valid integer between 1 and 5 inclusive.");
			difficulty = getIntInput("\nEnter a difficulty from 1-5 (1 is easier, 5 is harder)");
			difficultyIsValid = checkForValidityOfDifficultyInput(difficulty);
		}
		return difficulty;
	}
	
	/**
	 * This method wraps a boolean expression that I didn't want to display more
	 * than once (for readability).
	 * 
	 * @param difficulty The user input value we're testing.
	 * 
	 * @return true if input is null OR between 1 and 5 inclusive, otherwise 
	 * false.
	 */

	private boolean checkForValidityOfDifficultyInput(Integer difficulty) {
		return (Integer.compare(difficulty, 1) >= 0 && Integer.compare(difficulty, 5) <= 0);
	}

	/**
	 * This method prints the available operations, obtains the user selection,
	 * and handles cases where the user instructs the program to continue
	 * without providing an input. It is very much not SOLID.
	 * 
	 * @return The user-provided input, or -1 if no input is given.
	 * 
	 */

	private int getUserSelection() {
		printOperations();
		Integer input = getIntInput("Enter the number of one of the above menu items to continue");
		return Objects.isNull(input) ? -1 : input;
	}
	
	/**
	 * This method attempts to convert user input (obtained by
	 * <code>getStringInput</code>) to an <code>Integer</code> and handles 
	 * cases where the input is <code>null</code>. It also catches any
	 * <code>NumerFormatException</code> and re-throws it as a 
	 * <code>projects.exception.DbException<code>.
	 * 
	 * @param prompt For display to the user, passed to getStringInput()
	 * @return value of prompt, or <code>null</code>.
	 * 
	 */

	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);
		try {
			return Objects.isNull(input) ? null : Integer.valueOf(input);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}
	}

	/**
	 * This method prints the given prompt to the terminal, then obtains the
	 * user input by reading the terminal's next line. It also trims the input.
	 * It also handles cases where no input is given
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
	 * This method prints the contents of the private field 
	 * <code>operations</code> to the terminal using human-readable formatting. 
	 * Additionally, it prepends a message prompting the user to pick from those 
	 * selections.
	 * 
	 */
	
	/**
	 * This method reads the input provided by the user and converts it to a
	 * BigDecimal, if possible.
	 * 
	 * @param prompt The prompt to show the user before reading their input.
	 * @return BigDecimal
	 * 
	 */

	private BigDecimal getDecimalInput(String prompt) {
		String input = getStringInput(prompt);
		try {
			return new BigDecimal(input).setScale(2);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid decimal number.");
		}
	}
	
	/**
	 * This method prints the contents of the operations <code>List</code>
	 * field defined at the top of this class using a 
	 * <code>StringBuilder</code> instance. It also performs some formatting.
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
