package com.demo.uipath;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.util.ArrayList;
import org.json.*;


public class UiPath {
	private static String accessToken;
	private static String releaseKey;
	private int robotID = 0;
	private String varRobotName = "your-robot-name";
	
	private String tenant_name = "DefaultTenant";
	private String content_type = "application/json";
	private String folderID = "2609346";
	private String url="https://cloud.uipath.com/einfoezswpft/DefaultTenant";
	private String token_url="https://account.uipath.com/oauth/token";
	

	public void getAccessToken() throws Exception {
		  
		  String auth_json_file_path = "src/API_Auth.json";
		  String authJsonStr = new String(Files.readAllBytes(Paths.get(auth_json_file_path)));
		  
		  var request = HttpRequest.newBuilder()
				  		.uri(URI.create(token_url))
				  		.header("Content-Type", content_type)
				  		.header("X-UIPATH-TenantName", tenant_name)
				  		.POST(HttpRequest.BodyPublishers.ofString(authJsonStr))
				  		.build();
		  
		  
		  var client = java.net.http.HttpClient.newHttpClient();
		  var response = client.send(request, HttpResponse.BodyHandlers.ofString());
		  
		  JSONObject json = new JSONObject(response.body());
		  accessToken = "Bearer "+json.getString("access_token");
		  System.out.println("Access Token : "+accessToken);
		  
}
	public void getRobotID() throws Exception
	{
		var request = HttpRequest.newBuilder()
				.uri(URI.create(url+"/orchestrator_/odata/Sessions/UiPath.Server.Configuration.OData.GetGlobalSessions?$expand=Robot($expand=License)"))
		  		.header("Authorization",accessToken)
		  		.GET()
		  		.build();

		var client  = HttpClient.newHttpClient();
		var response = client.send(request,HttpResponse.BodyHandlers.ofString());
		
		JSONObject json = new JSONObject(response.body());
		JSONArray field = json.getJSONArray("value");
		
		for(int i = 0;i < field.length();i++)
		{
			JSONObject explrObject = field.getJSONObject(i).getJSONObject("Robot");  
			if(explrObject.getString("Name").contains(varRobotName))
			{
				robotID = explrObject.getInt("Id");
				break;
			}
		}
		System.out.println("Robot ID : "+robotID);
	}

	public void getReleaseKey(String processName) throws Exception
	{
		var request = HttpRequest.newBuilder()
						.uri(URI.create(url+"/orchestrator_/odata/Releases"))
						.header("Content-Type", content_type)
				  		.header("X-UIPATH-TenantName", tenant_name)
				  		.header("X-UIPATH-OrganizationUnitId", folderID)
				  		.header("Authorization",accessToken)
				  		.GET()
				  		.build();

		var client  = HttpClient.newHttpClient();
		var response = client.send(request,HttpResponse.BodyHandlers.ofString());
		
		JSONObject json = new JSONObject(response.body());
	     
		JSONArray field = json.getJSONArray("value");
		
		for(int i = 0;i < field.length();i++)
		{
			 JSONObject explrObject = field.getJSONObject(i);  
			 if(explrObject.getString("Name").contains(processName))
			 {
				 releaseKey = explrObject.getString("Key");
				 break;
			 }
		}
		System.out.println("Release Key : "+releaseKey);
	}
	public void startJob() throws Exception
	{
		ArrayList<Integer> array = new ArrayList<Integer>();
		array.add(robotID);
		
		JSONObject obj=new JSONObject();
		obj.put("ReleaseKey", releaseKey);
		obj.put("Strategy","Specific");
		obj.put("RobotIds",array);
		obj.put("JobsCount", 0);
		obj.put("Source", "Manual");
		
		
		JSONObject mainObj=new JSONObject();
		mainObj.put("startInfo", obj);
		var request = HttpRequest.newBuilder()
			  		.uri(URI.create(url+"/orchestrator_/odata/Jobs/UiPath.Server.Configuration.OData.StartJobs"))
			  		.header("Content-Type", content_type)
			  		.header("X-UIPATH-TenantName", tenant_name)
			  		.header("X-UIPATH-OrganizationUnitId", folderID)
			  		.header("Authorization",accessToken)
			  		.POST(HttpRequest.BodyPublishers.ofString(mainObj.toString()))
			  		.build();
		 var client = java.net.http.HttpClient.newHttpClient();
		 var response = client.send(request, HttpResponse.BodyHandlers.ofString());
		 
		 System.out.println(response.body());
		  
	}
public static void main(String[] args) throws Exception{
	
}
}
