package com.andrea;

import me.legrange.haveibeenpwned.HaveIBeenPwndApi;
import me.legrange.haveibeenpwned.HaveIBeenPwndException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Command Line Password App!
 *
 */
public class App {

    public static int PASSWORD_MINIMUM = 12;
    public static int PASSWORD_MAXIMUM = 128;
    public static String PASSWORD_FILE = "passwords.txt";
    public static HaveIBeenPwndApi hibp = new HaveIBeenPwndApi();

    //This is for when there is an error, to display the error text in red on the console
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) {
        List<String> errorList = new ArrayList<String>();
        String userInput;
        System.out.println("Type (Exit) to stop program: otherwise \n Enter Password: ");

        Scanner scanner = new Scanner(System.in);

       while (scanner.hasNextLine()){
            userInput = scanner.nextLine(); //grabs user input

            if(userInput.equalsIgnoreCase("exit")) {
                scanner.close();
                System.exit(0);
            }

            //Validate user input of password
            if (isValidPassword(userInput, errorList)) {
                writeToPasswordFile(userInput);
            } else{
                for (String error : errorList) {
                    System.out.println(ANSI_RED + "ERROR: " + error + ANSI_RESET);
                }
            }

            System.out.println("Type (Exit) to stop program: otherwise \n Enter Another Password: ");
        }
    }

    private static void writeToPasswordFile(String userInput) {
        FileWriter writer = null;
        try {
            try {
                writer = new FileWriter(PASSWORD_FILE, true);

                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
                String formattedDate = sdf.format(date);

                /*
                  Assuming that we use the text file as a history for password, I thought it would be best to add a
                  timestamp for when the user input a password and then re-run the program and the file gets updated.
                  We would be able to view the password even if there's multiple of the same exact password,
                  the timestamp will always be different.
                 */

                writer.write(formattedDate + " , Password :  " + userInput);
                writer.write("\r\n"); // write new line
            } finally {
                writer.close(); //this ensures that FileWriter will always close
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isValidPassword( String password, List<String> errorList) {

        boolean validPassword=true;

        Pattern upperCasePattern = Pattern.compile("[A-Z ]");
        Pattern lowerCasePattern = Pattern.compile("[a-z ]");
        Pattern numericPattern = Pattern.compile("[0-9 ]");
        errorList.clear();

        if(password.length() < PASSWORD_MINIMUM){
            //If the password is less then 12 character
            errorList.add("Password needs to be at least " + PASSWORD_MINIMUM + " characters!");
            validPassword=false;
        }

        if(password.length() > PASSWORD_MAXIMUM){
            //If the password is greater than 128 characters
            //On average, a password doesn't exceed 128 characters
            errorList.add("Password cannot exceed " + PASSWORD_MAXIMUM + " characters!");
            validPassword = false;
        }

        if (!lowerCasePattern.matcher(password).find()) {
            //If there are no letters
            errorList.add("Password must contain one letter!");
            validPassword=false;
        }

        if (!upperCasePattern.matcher(password).find()) {
            //If there isn't at least one uppercase letter
            errorList.add("Password must contain one uppercase character!");
            validPassword=false;
        }

        if (!numericPattern.matcher(password).find()) {
            //If there are no numbers
            errorList.add("Password must contain one number!");
            validPassword=false;
        }

        if(validPassword){ //If statement is to prevent unnecessary HTTP/API call
           if(checkPlainPasswords(password)){ //use HaveIBeenPwndApi to compare password plain text
               validPassword = false;
               errorList.add("Password is COMPROMISED!!!!");
           }

            if(checkHashedPassword(password)){ //use HaveIBeenPwndApi to compare password hashes
                validPassword = false;
                errorList.add("Password Hash is COMPROMISED!!!!");
            }
        }
        return validPassword;

    }

    private static boolean checkPlainPasswords(String password) {
        boolean compromised;

        try {
            compromised = hibp.isPlainPasswordPwned(password);
        } catch (HaveIBeenPwndException e) {
            compromised = true;
            e.printStackTrace();
        }

        return compromised;
    }

    private static boolean checkHashedPassword(String password){

        boolean compromised;

        try {
            compromised = hibp.isHashPasswordPwned(hibp.makeHash(password));
        } catch (HaveIBeenPwndException e) {
            compromised = true;
            e.printStackTrace();
        }
        return compromised;
    }
}
