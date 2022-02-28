package com.techelevator.tenmo;

import com.techelevator.tenmo.exceptions.InvalidTransferIdChoice;
import com.techelevator.tenmo.exceptions.InvalidUserChoiceException;
import com.techelevator.tenmo.exceptions.UserNotFoundException;
import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;
import com.techelevator.view.ConsoleService;
import jdk.swing.interop.SwingInterOpUtils;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;

	private AccountService accountService;
	private UserService userService;
	private TransferTypeService transferTypeService;
	private TransferStatusService transferStatusService;
	private TransferService transferService;


    public static void main(String[] args) {
    	App app = new App(
				new ConsoleService(System.in, System.out),
				new AuthenticationService(API_BASE_URL),
				new AccountService(API_BASE_URL),
				new UserService(API_BASE_URL),
				new TransferTypeService(API_BASE_URL),
				new TransferStatusService(API_BASE_URL),
				new TransferService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService, AccountService accountService, UserService userService, TransferTypeService transferTypeService, TransferStatusService transferStatusService, TransferService transferService) {
		this.console = console;
		this.authenticationService = authenticationService;
		this.accountService = accountService;
		this.userService = userService;
		this.transferTypeService = transferTypeService;
		this.transferStatusService = transferStatusService;
		this.transferService = transferService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {
		Double bal = accountService.getBalance(currentUser);
		System.out.println("Your current account balance is:  $" + bal.toString());
	}

	private void viewTransferHistory() {

		Transfer[] transfers = transferService.getTransfersFromUserId(currentUser, currentUser.getUser().getId());
		System.out.println("-------------------------------");
		System.out.println("Transfers");
		System.out.println(String.format("%-8s %-14s %-8s","ID","From/To","Amount"));
		System.out.println("-------------------------------");

		for(Transfer transfer :  transfers){

			String fromUser = "From" + ":  " + userService.getUserById(currentUser, accountService.getAccountById(currentUser, transfer.getAccountFrom()).getUser_id()).getUsername();
			String toUser = String.format("To: %-2s", userService.getUserById(currentUser, accountService.getAccountById(currentUser, transfer.getAccountTo()).getUser_id()).getUsername());


			System.out.println(String.format("%-8d %-14s $%,.2f", transfer.getId(), fromUser, transfer.getAmount()));
			System.out.println(String.format("%-8d %-14s $%,.2f", transfer.getId(), toUser, transfer.getAmount()));
		}



		int choiceId = console.getUserInputInteger("Please enter the Transfer ID to view details or a transfer, or input 0 to go back");
		Transfer choice = validateTransferIdChoice(choiceId, transfers, currentUser);
		if (choice != null){
			System.out.println("-------------------------------");
			System.out.println("Transfer Details");
			System.out.println("-------------------------------");
			System.out.println("Id: " + choice.getId());
			System.out.println("From: " + choice.getAccountFrom());
			System.out.println("To: " + choice.getAccountTo());
			System.out.println("Type: " + choice.getTransferTypeId());
			System.out.println("Status: " + choice.getTransferStatusId());
			System.out.println("Amount $: " + choice.getAmount());
		}


	}

	private void viewPendingRequests() {
		// TODO Auto-generated method stub

	}

	private void sendBucks() {
		User[] users = userService.getAllUsers(currentUser);
		System.out.println("-------------------------------");
		System.out.println("Users");
		System.out.println(String.format("%-8s %-14s","ID","Name"));
		System.out.println("-------------------------------");

		console.printUsersToDisplay(users);

		int userChoice = console.getUserInputInteger("Please enter the ID of the person you want to send money to, or input 0 to cancel");
		if (validateUserChoice(userChoice, users, currentUser)){
			String amnt = console.getUserInput("Enter amount of funds to send");
			makeAtransfer(userChoice, amnt, "Send", "Approved");
		}
	}

	private void requestBucks() {
		// TODO Auto-generated method stub
		
	}
	
	private void exitProgram() {
		System.exit(0);
	}

	private Transfer makeAtransfer(int accountSenderuserId, String amntStr, String transferType, String status){
		int transferTypeId = transferTypeService.getTransferType(currentUser, transferType).getTransferTypeId();
		int transferStatusId = transferStatusService.getTransferStatusByDesc(currentUser, status).getTransferStatusId();
		int senderId;
		int receiverId;

		if(transferType.equals("Send")){
			receiverId = accountService.getAccountByUserId(currentUser, accountSenderuserId).getAccount_id();
			senderId = accountService.getAccountByUserId(currentUser, currentUser.getUser().getId()).getAccount_id();
		} else {
			receiverId = accountService.getAccountByUserId(currentUser, currentUser.getUser().getId()).getAccount_id();
			senderId = accountService.getAccountByUserId(currentUser,accountSenderuserId).getAccount_id();
		}

		double amnt = Double.parseDouble(amntStr);


		Transfer transfer = new Transfer();
		transfer.setAccountFrom(senderId);
		transfer.setAccountTo(receiverId);
		transfer.setAmount(amnt);
		transfer.setTransferStatusId(transferStatusId);
		transfer.setTransferTypeId(transferTypeId);

		transferService.createTransfer(currentUser,  transfer);
		return transfer;
	}

	private Transfer validateTransferIdChoice(int choice, Transfer[] transfers, AuthenticatedUser authenticatedUser){
		Transfer choiceToValidate = null;
		if(choice != 0){
			try {
				boolean isvalid = false;
				for(Transfer transfer : transfers){
					if(transfer.getId() == choice){
						isvalid = true;
						choiceToValidate = transfer;
						break;
					}
				}
				if(isvalid == false){
					throw new InvalidTransferIdChoice();
				}
			} catch (InvalidTransferIdChoice e) {
				System.out.println(e.getMessage());
			}
		}
		return choiceToValidate;
	}

	private boolean validateUserChoice(int userIdChoice, User[] users, AuthenticatedUser currentUser) {
		if(userIdChoice != 0) {
			try {
				boolean validUserIdChoice = false;

				for (User user : users) {
					if(userIdChoice == currentUser.getUser().getId()) {
						throw new InvalidUserChoiceException();
					}
					if (user.getId() == userIdChoice) {
						validUserIdChoice = true;
						break;
					}
				}
				if (validUserIdChoice == false) {
					throw new UserNotFoundException();
				}
				return true;
			} catch (UserNotFoundException | InvalidUserChoiceException e) {
				System.out.println(e.getMessage());
			}
		}
		return false;
	}

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}


}
