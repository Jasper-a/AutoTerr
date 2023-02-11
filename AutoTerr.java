import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

/**
 * Name: AutoTerr
 * Description: Automation for gathering and formatting territory addresses and phone numbers.
 *
 * @author Jasper Alexander
 * @version 1.0
 */
public class AutoTerr{
	public static void main(String[] args){
		System.out.println("Fetching data..."); //****************************
		
		Scanner fileIn;
		Scanner formIn;
		FileWriter txtOut;
		FileWriter excelOut;
		
		// Get a Scanner for input.txt
		try{
			fileIn = new Scanner(new File("./data/input.txt"));
		}
		catch(FileNotFoundException e){
			fileIn = new Scanner("");
			
			System.out.println("\nError! Cannot find input file");
			System.out.println("Input must be located: ./data/input.txt");
			
			System.exit(0);
		}
		
		System.out.println("Parsing data..."); //*****************************
		
		ArrayList<String> addresses = new ArrayList<String>(0);
		ArrayList<String> phoneNumbers = new ArrayList<String>(0);
		Hashtable<String, String> postalCodeMatches = new Hashtable<String, String>();

		String lineAddress,
			   linePhoneNumber;

		// First delimeter
		fileIn.useDelimiter("\t");

		while(fileIn.hasNext()){
			// Id
			fileIn.next();

			// Address
			lineAddress = fileIn.next().trim();

			// Householer
			fileIn.next();

			// Switch to second delimeter
			fileIn.useDelimiter("\n");
			fileIn.skip("\t");

			// Phone
			linePhoneNumber = fileIn.next().trim();

			// Back to first delimeter
			fileIn.useDelimiter("\t");

			addresses.add(lineAddress);
			phoneNumbers.add(linePhoneNumber);
		}
		
		fileIn.close();
		
		System.out.println("Formatting suites and suffixes..."); //***********

		// First item in each array is the accepted format.
		String[] streetArrayAvenue   = {"Ave", "Av", "Avenue"};
		String[] streetArrayBay      = {"Bay", "By"};
		String[] streetArrayClose    = {"Cl", "Close", "Cs"};
		String[] streetArrayCrescent = {"Cres", "Cr", "Crsnt", "Crsent", "Cresent", "Crescent"};
		String[] streetArrayCove     = {"Cove", "Cv"};
		String[] streetArrayDrive    = {"Dr", "Drv", "Driv", "Drive"};
		String[] streetArrayLane     = {"Lane", "Ln"};
		String[] streetArrayPath     = {"Path"};
		String[] streetArrayPlace    = {"Pl", "Place"};
		String[] streetArrayRoad     = {"Rd", "Road"};
		String[] streetArrayStreet   = {"St", "Str", "Street"};
		String[] streetArrayTrail    = {"Tl", "Tr", "Trail"};
		String[] streetArrayWay      = {"Way", "Wy"};

		String[][] streetArrayNames  = {streetArrayAvenue,
										streetArrayBay,
										streetArrayClose,
										streetArrayCrescent,
										streetArrayCove,
										streetArrayDrive,
										streetArrayLane,
										streetArrayPath,
										streetArrayPlace,
										streetArrayRoad,
										streetArrayStreet,
										streetArrayTrail,
										streetArrayWay};
		
		// Work through each address (suite and suffix together)
		for(int i = 0; i < addresses.size(); i++){
			String address = addresses.get(i);
			String suite = "";
			String suffix = "";

			// Find suite by searching for pound or hyphen character
			findSuite:
			for(int l = 0; l < address.length(); l++){
				// Check each address for # char (suite after street)
				if(address.charAt(l) == '#'){
					suite = address.substring(l + 2); // Skip past "# "

					address = address.substring(0, l - 1);
					addresses.set(i, address);

					break findSuite;
				}
				// Check for - char (suite before street)
				else if(address.charAt(l) == '-'){
					suite = address.substring(0, l); // Exclude the "-"

					address = address.substring(l + 1);
					addresses.set(i, address);

					break findSuite;
				}
			}

			// Find suffix by searching backwards for space character
			findSuffix:
			for(int l = address.length() - 1; l >= 0; l--){
				if(Character.isWhitespace(address.charAt(l))){
					suffix = address.substring(l + 1).toLowerCase();

					// Convert to First Letter Upper Case
					suffix = suffix.substring(0, 1).toUpperCase() + suffix.substring(1);

					address = address.substring(0, l);
					addresses.set(i, address);

					// Exit inner loop with break
					break findSuffix;
				}
			}

			boolean foundSuffixMatch = false;
			
			// Per streetArray...
			matchSuffx:
			for(int l = 0; l < streetArrayNames.length; l++){
				// Per street name in array...
				for(int j = 0; j < streetArrayNames[l].length; j++){
					if(suffix.matches(streetArrayNames[l][j])){
						foundSuffixMatch = true;
						// Set the suffix to its accepted format (at index 0)
						suffix = streetArrayNames[l][0];

						// Exit both inner loops with break
						break matchSuffx;
					}
				}
			}
			// Did not find any matching suffix
			if(!foundSuffixMatch){
				System.out.println("- Unexpected suffix: " + suffix);
			}

			// Attach the formatted suffix to the address
			address += " " + suffix;

			// Attach the suite number to the address
			if(suite.length() > 0){
				address = suite + "-" + address;
			}

			// Update the address in the ArrayList
			addresses.set(i, address);
		}
		
		System.out.println("Removing Duplicates..."); //**********************
		
		// Compare every number with every other number for duplicates
		for(int i = 0; i < phoneNumbers.size(); i++){
			for(int l = 0; l < phoneNumbers.size(); l++){
				// Don't compare previous numbers (already compared, would mess up value of i)
				// Also don't compare to itself
				if(i >= l){
					continue;
				}
				else if(phoneNumbers.get(i).matches(phoneNumbers.get(l))){
					addresses.remove(l);
					phoneNumbers.remove(l);
					l--;
				}
			}
		}
		
		System.out.println("Processing form..."); //**************************

		// Get a Scanner for form.txt
		// Get a FileWriter for text_output.txt
		// Get a FileWriter for excel_output.txt
		try{
			formIn = new Scanner(new File("./data/form.txt"));
			txtOut = new FileWriter("./data/text_output.txt");
			excelOut = new FileWriter("./data/excel_output.txt");
		}
		catch(FileNotFoundException e){
			formIn = new Scanner("");
			txtOut = new FileWriter(new java.io.FileDescriptor());
			excelOut = new FileWriter(new java.io.FileDescriptor());

			System.out.println("\nError! Cannot find form file");
			System.out.println("Form must be located: ./data/form.txt");

			System.exit(0);
		}
		catch(IOException e){
			formIn = new Scanner("");
			txtOut = new FileWriter(new java.io.FileDescriptor());
			excelOut = new FileWriter(new java.io.FileDescriptor());

			System.out.println("\nError! Cannot find output file");
			System.out.println("Text output must be located: ./data/text_output.txt");
			System.out.println("Excel output must be located: ./data/excel_output.txt");
			
			System.exit(0);
		}
		


		// READING THE FORM AND PRINTING THE DATA ****************************
		// *******************************************************************
		// *******************************************************************



		// All code regarding FileWriter must be in try block for IOExceptions
		try{

			// Reading the form and writing text_output...
			// Using formLine as stagger between scan and write (analyze each line without moving scanner)
			String formLine;
			formIn.useDelimiter("\n");

			// Read to first street
			do{
				formLine = formIn.next();

				// Prevent errors from indexing empty string
				if(formLine.length() == 0){
					formLine = " ";
				}

				if(formLine.charAt(0) == '\t'){
					txtOut.write("\t"); // Write tab space despite being trimmed
				}

				txtOut.write(formLine.trim() + "\n");

			} while(formLine.charAt(0) != '\t');

			String formStreet = formLine.trim();

			// Processing the form line by line...
			for(formLine = formIn.next(); formIn.hasNext(); formLine = formIn.next()){	
				formLine = formLine.replaceAll("\\s+$", ""); // Right trim for stupid escape characters that mess up everything (keep tab for street lines)	

				// Skip empty lines or they mess up charAt(0)
				if(formLine.length() == 0){
					txtOut.write("\n");
					continue;
				}

				// Actions for each line type...

				// Street line (starts with tab)
				if(formLine.charAt(0) == '\t'){
					if(formLine.length() > 0){
						formStreet = formLine.trim();
						
						formLine = formLine;
					}
				}
				// Postal code line (starts with letter)
				else if(Character.isLetter(formLine.charAt(0))){
					Scanner postalCodeLine = new Scanner(formLine);
					int rangeStart;
					int rangeEnd;
					String postalCode;

					postalCodeLine.useDelimiter(":");

					postalCode = postalCodeLine.next().trim(); // Postal code

					postalCodeLine.useDelimiter("-"); // Switch to second delimeter
					postalCodeLine.skip(":");

					rangeStart = Integer.parseInt(postalCodeLine.next().trim()); // First number

					if(postalCodeLine.hasNext()){
						rangeEnd = Integer.parseInt(postalCodeLine.next().trim()); // Second number
					}
					else{
						rangeEnd = rangeStart; // Just the one number (apartment)
					}

						// Add all range numbers into postalCodeMatches Hashtable (only corresponding odd/even)
						for(rangeStart = rangeStart; rangeStart <= rangeEnd; rangeStart += 2){
							// Pair is put as [address, postal code] for easy searching by keys
							postalCodeMatches.put(rangeStart + " " + formStreet, postalCode);
						}
				}
				// Address line (starts with number or asterix)
				else if(Character.isDigit(formLine.charAt(0)) || formLine.charAt(0) == '*'){
					String formAddress = formLine;
					boolean isDoNotCall = (formAddress.charAt(0) == '*');
					
					if(isDoNotCall){
						formAddress = formAddress.substring(1);
					}
					
					// Trim suite from apartment (only general address) for Excel
					int excelAddressHyphenIndex = formAddress.indexOf("-");
					String excelAddressWithoutSuite = formAddress.substring(excelAddressHyphenIndex + 1) + " " + formStreet; // No hyphen is "-1" (so +1 to 0, works!)

					formAddress += " " + formStreet;
					
					boolean hasPhoneNumber = false;
					ArrayList<String> excelPhoneNumbers = new ArrayList<String>(0);
					
					// Add each number to formLine and excelLine
					for(int i = 0; i < addresses.size(); i++){
						if(formAddress.matches(addresses.get(i))){
							formLine += "\n" + (isDoNotCall ? "*" : "") + phoneNumbers.get(i);
							excelPhoneNumbers.add("\"" + phoneNumbers.get(i) + "\"");
							hasPhoneNumber = true;
						}
					}
					
					if(!hasPhoneNumber){
						formLine += "\n" + (isDoNotCall ? "*" : "") + "\t-";
					}

					// Create a line for Excel
					String excelLine = formAddress + "\t" +
									  (hasPhoneNumber ? "=" + String.join(" & CHAR(10) & ", excelPhoneNumbers)
									   				   : "-") +
									   "\t" + postalCodeMatches.get(excelAddressWithoutSuite) + "\n";

					if(postalCodeMatches.get(excelAddressWithoutSuite) == null){
						System.out.println("- No postal code for: " + formAddress);
					}

					// Only write Excel line if not DNC
					if(!isDoNotCall){
						excelOut.write(excelLine);
					}
				}
				
				txtOut.write(formLine + "\n");
			}
			
			txtOut.write(formLine);
			txtOut.close();
			excelOut.close();
			formIn.close();

		}
		catch(IOException e){
			System.out.println("\nError! Cannot write to an output file");

			System.exit(0);
		}
		catch(NoSuchElementException e){
			System.out.println("\nError! Problem reading form file");

			System.exit(0);
		}
		
		System.out.println("\nDone!");
	}
}