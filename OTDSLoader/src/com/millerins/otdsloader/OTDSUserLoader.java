package com.millerins.otdsloader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @author Anthony Miller (Content Sciences Limited)
 * Note: This code is provided 'as-is' and no liability will be accepted 
 * for any loss or damage should you wish to use this code in your organisation.
 */

public class OTDSUserLoader {

/*		public static String userName = "otadmin@otds.admin";
		public static String password = "livelink";
		public static String token = null;
		public static String otdsticket = null;
		public static String baseURL = "http://192.168.135.200:8080";
		public static String CSVFile = "/Users/anthony/employee.csv";
		public static String LOGFILE = null;
*/		
		public static String userName = null;
		public static String password = null;
		public static String token = null;
		public static String otdsticket = null;
		public static String baseURL = null;
		public static String CSVFile = null;
		public static String LOGFILE = null;
		public static String partition = null;
		public static String passwordOnly = null;
	
		/**
		 * @param args
		 */
		public static void main(String[] args) {
			OTDSUserLoader client = new OTDSUserLoader();
			
			if ((args == null) || (args.length == 0)) {
		        System.out.println("Usage: java OTDSUserLoader <otdsadmin user> <otadmin password> <baseOTDSURL> <partition> <CSVFile> <Log File> <passwordOnly|True|False>");
		        System.exit(0);
		    } else {
		    	
				userName = args[0];
		    	password = args[1];
		    	baseURL = args[2];
		    	partition = args[3];
		    	CSVFile = args[4];
		    	LOGFILE = args[5];
		    	passwordOnly = args[6];
		    }
			
			
			try {
				// Get the OTDS ticket
				client.getCon(baseURL);
				
				CSVLoader loader = new CSVLoader(baseURL, otdsticket, token, LOGFILE, partition);
				loader.loadCSV(CSVFile, passwordOnly);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		// Make a connection to OTDS and store the ticket
		private void getCon(String baseURL) {
			
			try {
								
				// Make an initial connection and return the otdsticket
				Client client = Client.create();
	            WebResource webResource = client.resource(baseURL + "/otdsws/v1/authentication/credentials");

	            
	            // Create a JSON Object to pass into OTDS REST API
	            JSONObject input = new JSONObject();
	            input.put("user_name", userName);
	            input.put("password", password);
	            
	            // Create the client response ensuring that you pass it in JSON format
	            ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, input.toString());
	    
	            
	            if (response.getStatus() != 200) {
	                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
	            }
	 
	            // Split the JSON string into individual tokens
	            String JSON_DATA = response.getEntity(String.class);
	            
	            JSONObject obj = new JSONObject(JSON_DATA);

	            // Now take the ticket, create the user creation JSON String and pass into the correct call.
	            otdsticket = obj.getString("ticket");
	            token = obj.getString("token");
	            
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
}
