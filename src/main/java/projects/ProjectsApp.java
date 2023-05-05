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
	
	ProjectService projectService = new ProjectService();
	
	/**
	 * A <code>List</code> object that stores the menu items displayed to the 
	 * user.
	 * 
	 */
	
	// @formatter:off
	private List<String> operations = List.of(
			"1) Quit this application",
			"2) Add a new project"
	);
	// @formatter:on
	
	/**
	 * A <code>Scanner</code> object that will read user input to the terminal.
	 * 
	 */
	
	private Scanner scanner = new Scanner(System.in);
	
	/**
	 * This method is the entry point for the application.
	 * 
	 * @param	args	included for compatibility, unused.
	 * 
	 */
	
	public static void main(String[] args) {
		new ProjectsApp().processUserSelections();
	}

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
				
				// CASE 1: Quit the app
				case 1:
					System.out.println("Exiting the Menu");
					done = true;
					break;
				
				// CASE 2: Add a new project
				case 2:
					createProject();
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
	 * This method creates a <code>Project</code> instance and moves the user through 
	 */
	
	private void createProject() {
		
		String projectName = getStringInput("Enter the project name");
		BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
		BigDecimal actualHours = getDecimalInput("Enter the actual hours");
		Integer difficulty = getIntInput("Enter a difficulty from 1-5 (1 is easier, 5 is harder)");
		boolean difficultyIsValid = checkForValidityOfDifficultyInput(difficulty);
		while (!difficultyIsValid) {
			System.out.println("Please enter a valid integer between 1 and 5 inclusive, or simply press enter if you do not wish to provide a difficulty at this time.");
			difficultyIsValid = checkForValidityOfDifficultyInput(difficulty);
		}
		String notes = getStringInput("Enter the project notes");
		
		Project project = new Project();
		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);
		
		Project dbProject = projectService.addProject(project);
		
		System.out.println("You have successfully created project " + dbProject);
		
	}

	private boolean checkForValidityOfDifficultyInput(Integer difficulty) {
		return Objects.isNull(difficulty) || (Integer.compare(difficulty, 1) >= 0 && Integer.compare(difficulty, 5) <= 0);
	}

	private BigDecimal getDecimalInput(String prompt) {
		String input = getStringInput(prompt);
		try {
			return new BigDecimal(input).setScale(2);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid decimal number.");
		}
	}

	/**
	 * This method prints the available operations, obtains the user selection,
	 * and handles cases where the user instructs the program to continue
	 * without providing an input. It is very much not SOLID.
	 * 
	 * @return	The user-provided input, or -1 if no input is given.
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
	 * @param	prompt	For display to the user, passed to getStringInput()
	 * @return			value of prompt, or <code>null</code>.
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
	 * @param	prompt	The prompt to print.
	 * @return			The trimmed input, or <code>null</code>.
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

	private void printOperations() {
		System.out.println("\nWhat do you wish to do?");
		StringBuilder operationsSB = new StringBuilder();
		operations.forEach(line -> operationsSB.append("\n" + line));
		System.out.println(operationsSB.toString().indent(3));
	}
	
	

}
