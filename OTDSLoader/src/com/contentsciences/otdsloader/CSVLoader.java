package com.contentsciences.otdsloader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Anthony Miller
 * Content Sciences Limited
 * Note: This code is provided 'as-is' and no liability will be accepted 
 * for any loss or damage should you wish to use this code in your organisation.
 */
public class CSVLoader {

	private String otdsticket;
	private String baseURL;
	private char separator;
	private String token;
	
	// Test Variables
	String userid = "asmith13";
    String givenName = "Adam";
    String lastName = "Smith";
    String password = "L1velink__001";
    
    String JSON_USER_PASSWD_DATA = null;
    String JSON_USER_DATA= null;
    String LOGFILE = null;
    String partition = null;

    // Make instance of Logger to capture logs
    Logger logger = Logger.getLogger(CSVLoader.class.getName());
    
	/**
	 * Public constructor to build CSVLoader object with
	 * Connection details. The connection is closed on success
	 * or failure.
	 * @param connection
	 */
	public CSVLoader(String baseURL, String otdsticket, String token, String LOGFILE, String partition) {
		this.baseURL = baseURL;
		this.otdsticket = otdsticket;
		this.token = token;
		this.LOGFILE = LOGFILE;
		this.partition = partition;
		//Set default separator
		this.separator = ',';
	}
	
	/**
	 * Parse CSV file using OpenCSV library and load in 
	 * given database table. 
	 * @param csvFile Input CSV file
	 * @param tableName Database table name to import data
	 * @param truncateBeforeLoad Truncate the table before inserting 
	 * 			new records.
	 * @throws Exception
	 */
	public void loadCSV(String csvFile, String passwordOnly) throws Exception {

		CSVReader csvReader = null;
		if(null == this.otdsticket) {
			throw new Exception("No connection to OtDS is present.");
		}
		try {
			
			csvReader = new CSVReader(new FileReader(csvFile), this.separator);

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error occured while executing file. "
					+ e.getMessage());
		}

		String[] headerRow = csvReader.readNext();

		if (null == headerRow) {
			throw new FileNotFoundException(
					"No columns defined in given CSV file." +
					"Please check the CSV file format.");
		}

		String questionmarks = StringUtils.repeat("?,", headerRow.length);
		questionmarks = (String) questionmarks.subSequence(0, questionmarks
				.length() - 1);
		
		String[] nextLine;
		try {
			FileHandler fileHandler = new FileHandler(LOGFILE, true);        
		    logger.addHandler(fileHandler);
			//final int batchSize = 1000;
			//int count = 0;
			//Date date = null;
			while ((nextLine = csvReader.readNext()) != null) {

				givenName = nextLine[0];
				lastName = nextLine[1];
				userid = nextLine[2];
				password = nextLine[3];
				
				//if (null != nextLine) {
				//	int index = 1;
				//	for (String string : nextLine) {
				if (passwordOnly.equalsIgnoreCase("False")) {
					// Read from the CSV file the first line of the data and add to local variables

					logger.info("Adding user: " + userid);
					// Get a connection to create the user
					String userData = createUser(getJSON_USER_DATA(userid, givenName, lastName, partition), baseURL);
					
					// Get a connection to update the new users password
					createUserPasswd(getJSON_USER_PASSWD_DATA(password), baseURL);

					if (logger.isLoggable(Level.INFO)) {
						logger.info("Successfully added user: " + userid);
					} else if (logger.isLoggable(Level.SEVERE)) {
						logger.severe("Successfully added user: " + userid);
					}
				} else {
					logger.info("Changing user: " + userid + " password to L1velink__001");
					// Get a connection to create the user
					createUserPasswd(getJSON_USER_PASSWD_DATA("L1velink__001"), baseURL);
					
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Successfully updated  user: " + userid + " password");
					} else if (logger.isLoggable(Level.SEVERE)) {
						logger.severe("Successfully updated  user: " + userid + " password");
					}
					
				}
						
						
						//}
				//	}
				}
		} catch (Exception e) {
			
			e.printStackTrace();
			if (logger.isLoggable(Level.SEVERE)) {
	            logger.severe(e.getMessage());
	        }
			
			
			throw new Exception(
					"Error occured while loading data from file to database."
							+ e.getMessage());
			
		} finally {
			csvReader.close();
		}
	}

	public char getSeparator() {
		return separator;
	}

	public void setSeparator(char separator) {
		this.separator = separator;
	}
	
	public String getJSON_USER_DATA(String userid, String givenName, String lastName, String partition) {
		
       // Create a new user using a POST 
        String JSON_USER_DATA =
        	"{"
        	+ " \"user_partition_id\": \"" + partition + "\","
            + " \"name\": \"" + userid + "\","
            + " \"location\": \"ou=Root,ou=" + partition + ",ou=IdentityProviders,dc=identity,dc=opentext,dc=net\","     
            + " \"description\": \"XYZ Corp Employee\","
            + " \"values\": [" 
            + "   {" 
			+ "      	\"name\": \"givenName\","
			+ "		\"values\":[\"" + givenName + "\"]"
			+ "    },"
			+ "   {" 
			+ "      	\"name\": \"sn\","
			+ "		\"values\":[\"" + lastName + "\"]"
			+ "    },"
            + "    {" 
			+ "      	\"name\": \"UserMustChangePasswordAtNextSignIn\","
			+ "		\"values\":[\"false\"]"
			+ "    },"
			+ "    {" 
			+ "      	\"name\": \"UserCannotChangePassword\","
			+ "		\"values\":[\"true\"]"
			+ "    },"
			+ "    {" 
			+ "      	\"name\": \"PasswordNeverExpires\","
			+ "		\"values\":[\"true\"]"
			+ "    },"
			+ "    {" 
			+ "      	\"name\": \"oTDepartment\","
			+ "		\"values\":[\"XYZ Corp\"]"
			+ "    }"
			+ "  ]" 
        	+ "}";	
        
		return JSON_USER_DATA;
		
	}
	
	// Set the user password JSON string
	public String getJSON_USER_PASSWD_DATA(String password) {
		
		String JSON_USER_PASSWD_DATA = 
         "{"
         + " \"new_password\": \"" + password + "\""	
		+ "}";
		
		return JSON_USER_PASSWD_DATA;
	}
	
	// Create the User
	public String createUser(String JSON_USER_DATA, String baseURL) {
		
		Client client = Client.create();
	    WebResource webResource =client.resource(baseURL + "/otdsws/v1/users");

	    String appKey = "Basic " + token; // appKey is unique number

	    //Get response from RESTful Server get(ClientResponse.class);
	    ClientResponse response = null;
	    
	    response = webResource.type(MediaType.APPLICATION_JSON)
	    				.header("Authorization", appKey)
	                    .header("OTDSTicket", otdsticket)
	                    .post(ClientResponse.class, JSON_USER_DATA);

	    String jsonStr = response.getEntity(String.class);
		
		return jsonStr;
	}
	
	public void createUserPasswd(String JSON_USER_PASSWD_DATA, String baseURL) {
		
		String fulluserid = userid + "@" + partition;
		
		
		// For the URL to call from the response body call above - This is a PUT request
        String uri = baseURL + "/otdsws/v1/users/" + fulluserid + "/password"; 
        System.out.println("URI = " + uri);
        
        Client client = Client.create();
	    WebResource webResource = client.resource(uri);
        
	    String appKey = "Basic " + token; // appKey is unique number

	    ClientResponse response = null;
	    response = webResource.type(MediaType.APPLICATION_JSON)
				.header("Authorization", appKey)
                .header("OTDSTicket", otdsticket)
                .put(ClientResponse.class, JSON_USER_PASSWD_DATA);
	    

	    if (response.getStatus() == 204) {
            System.out.println("Got 204 - No Response - Password updated");
        } else {
        	System.out.println("Error changing users password");
        }
	}

}
