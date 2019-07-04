/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package katun.smartcontract.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;




/**
 *
 * @author aochoa@katun.tech
 */
public class Service {
	
	private String accessToken;
	private final String MAINURL = "http://api.katun.tech:8080";
	private final String USERREGHURL = String.format("%s/user",MAINURL);
	private final String USERAUTHURL = String.format("%s/user/auth",MAINURL);	
	private final String GRURL = String.format("%s/api",MAINURL);
	private final String CTEMSGEVT = "sendRawTransaction";
	private final String QRYTRANEVT = "getTransactionByHash";
	private final String CTESCEVT = "createSmartContract";
	private final String QRYSCEVT = "getContractByHash";
	private final String QRYSCbnEVT = "getContractByName";
	private final String EXECSCEVT = "executeContractByHash";	
	private JsonObject jsonObjectResult;
	
	/**
	* @author: valeria@katun.tech
	* Verifies identity of User Credentials. Creates accesToken used as Bearer Header to grant access to Rest Services.
	* <br>
	* Success response:
	* <br>
	* <pre>
	* { 
		"status":  true, 
		"publicKey": "Tmh7i7QsUVaUpEuse89qxH", 
		"secretKey": "2a7BDUh7voswd3qVQNo2r1XEvw6Yrq1wuu1nuaN43P1F" 
	  }	 
	* </pre>
	* @param user User nickName previously registered in BlockChain network.
	* @param email User password/secret.
	*
	*/
	public void getCreateUser(String user, String email) {
        JsonObjectBuilder job = Json.createObjectBuilder();
        StringBuilder sb = new StringBuilder();
		JsonReader jr;
        try {
            CloseableHttpClient client = HttpClients.createDefault();

            HttpPost request = new HttpPost(USERREGHURL);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");
			
            job.add("user", user)
                    .add("email", email)
                    .add("identifier", "summit")
                    .add("password", "WBqpDkgBkDdI");

            StringEntity strEnt = new StringEntity(job.build().toString());
            request.setEntity(strEnt);

            CloseableHttpResponse response = client.execute(request);

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            client.close();
			
			jr = Json.createReader(new StringReader(sb.toString()));
			jsonObjectResult = jr.readObject();
			
            System.out.format("\nRequest Json Body Content:%s\n", jsonObjectResult.toString());

        } catch (IOException ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
	
	
	/**
	 * @author aochoa@katun.tech
	 * Verifies identity of User Credentials. Creates accesToken used as Bearer Header to grant access to Rest Services.
	 * <br>
	 * Set assign accessToken private class attribute with "<b>accessToken</b>" Json Value returned from Post request execution.
	 * <br>
	 * accessToken attribute is used as Bearer Token header to grant access to API JSON RPC Rest services.
	 * <br>
	 * @param publicKey User publicKey obtained from user registration with getCreateUser.
	 * @param secretString User secretKey.
	 * @throws Exception in case of HTTP common errors.
	 * @see getCreateUser(String user, String email)
	 */
	public void setUserCredencials(String publicKey, String secretString) throws Exception {
		JsonObjectBuilder job = Json.createObjectBuilder();
		JsonReader jr;
		StringBuilder sb = new StringBuilder();		
		
		try {
			
			CloseableHttpClient client = HttpClients.createDefault();
			
			HttpPost request = new HttpPost(USERAUTHURL);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");			
			
			job.add("public", publicKey)
			.add("secret", secretString);
			
			StringEntity strEnt = new StringEntity(job.build().toString());			
			request.setEntity(strEnt);
			
			CloseableHttpResponse response = client.execute(request);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
			
			while ((line = rd.readLine()) != null) {
				sb.append(line); 
			}
			
			client.close();
			
			jr = Json.createReader(new StringReader(sb.toString()));
			jsonObjectResult = jr.readObject();
			
			accessToken = jsonObjectResult.getString("accessToken");
			
			if(accessToken.isEmpty())
				throw new Exception("Can't grant access with credentials.");
			
			System.out.format("\ngrants:[%s]\n",accessToken);
			
		} catch (Exception ex) {
			throw ex;
		}
				
	}
	
	/**
	 * @author: valeria@katun.tech
	 * Event that allows to inject information directly to the BlockChain Katun, using sender (Hash or ID), 
	 * receiver (Hash or ID) and the data to be injected.
	 * <br>
	 * Generates an Internal JsonObject with event result accesed from getServiceResult() method.
	 * <br>
	 * <b>It's necessary to use setUserCredencials before run this method.</b>
	 * @param senderHash Sender information, Hash or ID prefferently
	 * @param receiverHash Receiver information, Hash or ID prefferently
	 * @param data Data to be injected to the BlockChain Katun
	 * @throws Exception in case of HTTP common errors.
	 * @see getServiceResult()
	 * @see setUserCredencials(String publicKey, String secretString)
	 * @see execute(URI build)
	 */
	public void sendKatunTransaction(String senderHash, String receiverHash, String data) throws Exception{		

		URIBuilder uriBuilder = new URIBuilder(GRURL);
		uriBuilder.setParameter("action", CTEMSGEVT);
		uriBuilder.setParameter("sender", senderHash);
		uriBuilder.setParameter("receiver", receiverHash);
		uriBuilder.setParameter("data", data);

		execute(uriBuilder.build());
	}
	
	/**
	 * @author: valeria@katun.tech
	 * Event that allows to query data transactions on BlockChain Katun Network.
	 * <br>
	 * Generates an Internal JsonObject with event result accesed from getServiceResult() method.
	 * <br>
	 * <b>It's necessary to use setUserCredencials before run this method.</b>
	 * @param txHash String with Transaction ID to be searched in BlockChain Katun network.
	 * @throws Exception in case of HTTP common errors.
	 * @see getServiceResult()
	 * @see setUserCredencials(String publicKey, String secretString)
	 * @see execute(URI build)
	 */
	public void queryTransactionByHash(String txHash) throws Exception{
		
			URIBuilder uriBuilder = new URIBuilder(GRURL);
			uriBuilder.setParameter("action", QRYTRANEVT);
			uriBuilder.setParameter("txHash", txHash);

			execute(uriBuilder.build());
	}
	
	/**
	 * @author aochoa@katun.tech
	 * Event that allows to query Smart Contract transactions on BlockChain Katun Network.
	 * <br>
	 * Generates an Internal JsonObject with event result accesed from getServiceResult() method.
	 * <br>
	 * <b>It's necessary to use setUserCredencials before run this method.</b>
	 * @param txContract String with Transaction ID to be searched in BlockChain Katun network.
	 * @throws Exception in case of HTTP common errors.
	 * @see getServiceResult()
	 * @see setUserCredencials(String publicKey, String secretString)
	 * @see execute(URI build)
	 */
	public void queryContractByHash(String txContract) throws Exception{
		URIBuilder uriBuilder = new URIBuilder(GRURL);
		uriBuilder.setParameter("action", QRYSCEVT);
		uriBuilder.setParameter("txContract", txContract);

		execute(uriBuilder.build());
	}
	
	/**
	 * @author aochoa@katun.tech
	 * Event that allows to query Smart Contract transactions on BlockChain Katun Network by Contract Name
	 * <br>
	 * Generates an Internal JsonObject with event result accesed from getServiceResult() method.
	 * <br>
	 * <b>It's necessary to use setUserCredencials before run this method.</b>
	 * @param contractName String with Transaction ID to be searched in BlockChain Katun network.
	 * @throws Exception in case of HTTP common errors.
	 * @see getServiceResult()
	 * @see setUserCredencials(String publicKey, String secretString)
	 * @see execute(URI build)
	 */
	public void queryContractByName(String contractName) throws Exception{
		URIBuilder uriBuilder = new URIBuilder(GRURL);
		uriBuilder.setParameter("action", QRYSCbnEVT);
		uriBuilder.setParameter("contractName", contractName);

		execute(uriBuilder.build());
	}
	
	/**		
 	 * @author aochoa@katun.tech
	 * Event that allows to create and deploy Smart Contracts in BlockChain Katun. It's necessary to use setUserCredencials before run this method.
	 * <br>
	 * Generates an Internal JsonObject with event result accesed from getServiceResult() method.
	 * <br>
	 * <b>It's necessary to use setUserCredencials before run this method.</b>
	 * @param contractFile File pointer of Smart Contract. Following Java Standard's it's necessary that contract name be the same as the class with .java extension (i.e contractName.java)	 
	 * @throws Exception in case of HTTP common errors.
	 * @see getServiceResult()
	 * @see setUserCredencials(String publicKey, String secretString)
	 */
	public void createSmartContract(File contractFile) throws Exception {
		String fileName = contractFile.getName();
		StringBuilder sbResContent = new StringBuilder();
		CloseableHttpClient client = HttpClients.createDefault();
		
		URIBuilder uriBuilder = new URIBuilder(GRURL);
		uriBuilder.setParameter("action", CTESCEVT);
		
		HttpPost request = new HttpPost(uriBuilder.build());
		accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6ImthdHVuLnRlY2giLCJfaWQiOiI1ZDFkMTllYmQ5YzE4ZjJkYWQ1ZjI3MzkiLCJleHAiOjE1NjIyMTcyNjh9.rrIG9XnMaEctSsAJIMni470VXs5Vxg6iT9jF680fbiFhqwG6wi_tPeTe9HD9xyvhimU7RoWM8h_gZlyUFxjV7g";
		request.addHeader("Authorization", String.format("Bearer %s", accessToken));
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addBinaryBody("file", contractFile, ContentType.create("text/x-java-source"), fileName);
		HttpEntity multipart = builder.build();
		request.setEntity(multipart);

		CloseableHttpResponse response = client.execute(request);		
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line;
		while ((line = rd.readLine()) != null) {
			sbResContent.append(line);
		}
		
		client.close();
		
		JsonReader jr = Json.createReader(new StringReader(sbResContent.toString()));
		jsonObjectResult = jr.readObject();
		
		System.out.format("\nJSON Body request content:%s\n", jsonObjectResult.toString());		
	}	
	
	/**
	 * @author aochoa@katun.tech
	 * Event that allows to create a Smart Contract using Java standard code, and InvocationString.
	 * <br>
	 * Generates an Internal JsonObjectBuilder with event result accesed by getServiceResult() as a JsonObjet.
	 * <br>
	 * <b>It's necessary to use setUserCredencials before run this method.</b>
	 * @param txContract Java code. It´s strictly necessary to define class as <b>public</b>
	 * @param invString String with the nomenclature of the smart contract methods/functions  to be invoked/executed following an structured syntax.
	 * @throws Exception in case of HTTP common errors.
	 * @see validateInvocationString(String functionString)
	 * @see getServiceResult()
	 * @see setUserCredencials(String publicKey, String secretString)
	 * @see executePostJson(URI build, JsonObject jsonObject)
	 */
	public void executeContractByHash(String txContract, String invString) throws Exception{
		JsonObjectBuilder job = Json.createObjectBuilder();
		URIBuilder uriBuilder = new URIBuilder(GRURL);
		uriBuilder.setParameter("action", EXECSCEVT);
		uriBuilder.setParameter("txContract", txContract);
		//uriBuilder.setParameter("execute", invString);
		job.add("execute", invString);
		
		
		executePostJson(uriBuilder.build(),job.build());
	}	
	/**
	 * @author aochoa@katun.tech
	 * In case of Smart Contracts execution, creation, querying and information injection. A JSON can be built as request result in JSON RPC 2 Standard.  
	 * 
	 * @return JsonObject with Tx Information.
	 */	
	public JsonObject getServiceResult() {
		return jsonObjectResult;
	}	
	
	/**
	 * @author aochoa@katun.tech
	 * 
	 * This methos is complementary to Rest Services functionality.
	 * <br>
	 * Executes only GET requests.
	 * 
	 * @param build URI referent to service connection.
	 * @throws Exception In case of common HTTP Error.
	 */
	
	private void execute(URI build) throws Exception {
		
		accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6ImthdHVuLnRlY2giLCJfaWQiOiI1ZDFkMTllYmQ5YzE4ZjJkYWQ1ZjI3MzkiLCJleHAiOjE1NjIyMTc4OTR9.C19yFbCY4wrBCcYbXaLaozju94Vp4YW7MIdGudgLCegPC9yM6r-LIJ6ByP-GOIrNlQAohodiKY72E9InT91qMQ";
		
		StringBuilder sbResContent = new StringBuilder();
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(build);
		
		request.setHeader("Authorization", String.format("Bearer %s", accessToken));				
		
		HttpResponse response = client.execute(request);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line;
		while ((line = rd.readLine()) != null) {
			sbResContent.append(line);
		}

		client.close();
		
		JsonReader jr = Json.createReader(new StringReader(sbResContent.toString()));
		jsonObjectResult = jr.readObject();
		
		System.out.format("\nJson Body Request Content:%s\n", jsonObjectResult.toString());
	}
	/**
	 * This method is complementary to Rest Services functionality.
	 * <br>
	 * Executes only POST requests.
	 * @param build URI referent to service connection.
	 * @param jsonObject Json string body to set as entity to POST request.
	 * @throws Exception In case of common HTTP Error.
	 */
	private void executePostJson(URI build, JsonObject jsonObject) throws Exception {
		
		
		accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6ImthdHVuLnRlY2giLCJfaWQiOiI1ZDFkMTllYmQ5YzE4ZjJkYWQ1ZjI3MzkiLCJleHAiOjE1NjIyMTc4OTR9.C19yFbCY4wrBCcYbXaLaozju94Vp4YW7MIdGudgLCegPC9yM6r-LIJ6ByP-GOIrNlQAohodiKY72E9InT91qMQ";
		
		StringBuilder sbResContent = new StringBuilder();
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost request = new HttpPost(build);
		request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
		request.setHeader("Authorization", String.format("Bearer %s", accessToken));				
		            

        StringEntity strEnt = new StringEntity(jsonObject.toString());
        request.setEntity(strEnt);
		HttpResponse response = client.execute(request);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line;
		while ((line = rd.readLine()) != null) {
			sbResContent.append(line);
		}

		client.close();
		
		JsonReader jr = Json.createReader(new StringReader(sbResContent.toString()));
		jsonObjectResult = jr.readObject();
		
		System.out.format("\nJson Body Requet content:%s\n", jsonObjectResult.toString());
	}	
	
}