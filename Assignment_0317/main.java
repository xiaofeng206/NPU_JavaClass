import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

class AuthException extends Exception {
	public AuthException(String message) {
		super(message);
	}
}

abstract class User {
	private String username;
	private String password;
	protected String displayName;

	public User(String username, String password, String displayName) {
		this.username = username;
		this.password = password;
		this.displayName = displayName;
	}

	public String getUsername() {
		return username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean verifyPassword(String inputPassword) {
		return password.equals(inputPassword);
	}

	public void changePassword(String oldPassword, String newPassword) throws AuthException {
		if (!verifyPassword(oldPassword)) {
			throw new AuthException("Old password is incorrect.");
		}
		if (newPassword == null || newPassword.length() < 4) {
			throw new AuthException("New password must be at least 4 characters.");
		}
		password = newPassword;
	}

	public abstract String getRole();

	public void showDashboard() {
		System.out.println("Welcome, " + displayName + " (" + getRole() + ")");
	}
}

class StandardUser extends User {
	public StandardUser(String username, String password, String displayName) {
		super(username, password, displayName);
	}

	@Override
	public String getRole() {
		return "STANDARD_USER";
	}
}

class AdminUser extends User {
	public AdminUser(String username, String password, String displayName) {
		super(username, password, displayName);
	}

	@Override
	public String getRole() {
		return "ADMIN";
	}

	@Override
	public void showDashboard() {
		System.out.println("Admin panel access granted.");
		super.showDashboard();
	}
}

class LoginManager {
	private ArrayList<User> users = new ArrayList<>();

	public LoginManager() {
		users.add(new AdminUser("admin", "admin123", "System Admin"));
		users.add(new StandardUser("student", "pass123", "Default Student"));
	}

	public void registerUser(String username, String password, String displayName, boolean admin)
			throws AuthException {
		if (username == null || username.isBlank()) {
			throw new AuthException("Username cannot be empty.");
		}
		if (password == null || password.length() < 4) {
			throw new AuthException("Password must be at least 4 characters.");
		}
		if (findUser(username) != null) {
			throw new AuthException("Username already exists.");
		}

		User newUser;
		if (admin) {
			newUser = new AdminUser(username, password, displayName);
		} else {
			newUser = new StandardUser(username, password, displayName);
		}
		users.add(newUser);
	}

	public User login(String username, String password) throws AuthException {
		User user = findUser(username);
		if (user == null) {
			throw new AuthException("Account not found.");
		}
		if (!user.verifyPassword(password)) {
			throw new AuthException("Incorrect password.");
		}
		return user;
	}

	public User findUser(String username) {
		for (User user : users) {
			if (user.getUsername().equals(username)) {
				return user;
			}
		}
		return null;
	}

	public void listUsers() {
		System.out.println("\n=== Registered Users ===");
		if (users.isEmpty()) {
			System.out.println("No users registered.");
			return;
		}
		for (int i = 0; i < users.size(); i++) {
			User u = users.get(i);
			System.out.println((i + 1) + ". " + u.getUsername() + " | " + u.getDisplayName() + " | " + u.getRole());
		}
	}
}

public class main {
	private static final Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		LoginManager manager = new LoginManager();
		boolean running = true;

		System.out.println("=== Terminal Login Management System ===");

		while (running) {
			showMenu();
			int choice = readInt("Choose an option: ");

			try {
				switch (choice) {
					case 1:
						handleRegister(manager);
						break;
					case 2:
						handleLogin(manager);
						break;
					case 3:
						manager.listUsers();
						break;
					case 4:
						running = false;
						System.out.println("Goodbye!");
						break;
					default:
						System.out.println("Invalid menu option. Please choose 1-4.");
				}
			} catch (AuthException e) {
				System.out.println("Error: " + e.getMessage());
			} finally {
				System.out.println("----------------------------------------");
			}
		}

		scanner.close();
	}

	private static void showMenu() {
		System.out.println("\n1) Register");
		System.out.println("2) Login");
		System.out.println("3) List Users");
		System.out.println("4) Exit");
	}

	private static void handleRegister(LoginManager manager) throws AuthException {
		System.out.print("Enter username: ");
		String username = scanner.nextLine().trim();

		System.out.print("Enter password: ");
		String password = scanner.nextLine().trim();

		System.out.print("Enter display name: ");
		String displayName = scanner.nextLine().trim();

		System.out.print("Register as admin? (y/n): ");
		String isAdminText = scanner.nextLine().trim().toLowerCase();
		boolean isAdmin = isAdminText.equals("y") || isAdminText.equals("yes");

		manager.registerUser(username, password, displayName, isAdmin);
		System.out.println("Registration successful.");
	}

	private static void handleLogin(LoginManager manager) throws AuthException {
		System.out.print("Enter username: ");
		String username = scanner.nextLine().trim();

		System.out.print("Enter password: ");
		String password = scanner.nextLine().trim();

		User user = manager.login(username, password);
		user.showDashboard();

		System.out.print("Do you want to change your password? (y/n): ");
		String answer = scanner.nextLine().trim().toLowerCase();
		if (answer.equals("y") || answer.equals("yes")) {
			System.out.print("Old password: ");
			String oldPassword = scanner.nextLine().trim();
			System.out.print("New password: ");
			String newPassword = scanner.nextLine().trim();
			user.changePassword(oldPassword, newPassword);
			System.out.println("Password updated successfully.");
		}
	}

	private static int readInt(String prompt) {
		while (true) {
			try {
				System.out.print(prompt);
				int value = scanner.nextInt();
				scanner.nextLine();
				return value;
			} catch (InputMismatchException e) {
				System.out.println("Please enter a valid number.");
				scanner.nextLine();
			}
		}
	}
}
