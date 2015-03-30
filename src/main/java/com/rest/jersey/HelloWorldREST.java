package com.rest.jersey;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/helloworld")   //http://localhost:8080/helloworld/rest/helloworld
public class HelloWorldREST {

	 @GET
	 //@Produces(MediaType.TEXT_PLAIN)
	 public String get_comp(@QueryParam("company") String complist) throws JSONException {
		 String[] complist_arr = complist.split(":");//get company list
		 JSONArray comp_data = new JSONArray();
		 
		 String subject = "Risk";
		 for(int i = 0; i < complist_arr.length; i++){
			 JSONObject jo = crawl_generate(complist_arr[i]);
			 comp_data.put(jo);
		 }
		 JSONObject resd = new JSONObject();
		 HashSet<String> hs = get_key(comp_data);
		 String[] keywords = hs.toArray(new String[hs.size()]);
		 JSONArray ele = new JSONArray();
		 JSONObject tmpjo = new JSONObject();
		 JSONArray columns = new JSONArray();
		 HashSet nk = new HashSet();
		 for(int i = 0; i < 10; i++){
			 tmpjo.put("key", keywords[i]);
			 tmpjo.put("full_name", keywords[i]);
			 tmpjo.put("type", "numeric");
			 tmpjo.put("is_objective", "TRUE");
			 tmpjo.put("goal", "MIN");
			 ele.put(tmpjo);
			 columns.put(ele);
			 nk.add(keywords[i]);
		 }
		 hs = new HashSet(nk);
		 resd.put("columns", columns);
		 resd.put("subject", subject);
		 JSONArray pd = new JSONArray();
		 pd = parser(comp_data, nk);
		 resd.put("options", pd);
		 return resd.toString();
	 }
	 	
	 public JSONObject crawl_generate(String company_name){
		 JSONObject json = new JSONObject();
		 try {
			 json = new JSONObject(IOUtils.toString(new URL("http://riskanalysis.mybluemix.net/api/results/"+company_name), Charset.forName("UTF-8")));
		 } catch (Exception ex) {
		      System.err.println(ex);
		    }
		 return json;
	}
	 
	public HashSet<String> get_key(JSONArray data) throws JSONException{
		HashSet<String> rset = new HashSet<String>();
		HashSet<String> hs = new HashSet<String>();
		JSONObject jo = data.getJSONObject(0);
		JSONArray joo = (JSONArray) jo.get("records");
		JSONObject res = (JSONObject) joo.getJSONObject(0).get("keywords");
		System.out.print(res.toString());
		//add jsonobject into set
		Iterator keys = res.keys();
		while(keys.hasNext()){
			String key = (String)keys.next();
			rset.add(key);
			
		}
		//rset.add(res);
		for(int i = 1; i < data.length(); i++){
			jo = data.getJSONObject(1);
			joo = (JSONArray) jo.get("records");
			res = (JSONObject)joo.getJSONObject(0).get("keywords");
			//add jsonobject into set
			keys = res.keys();
			while(keys.hasNext()){
				String key = (String)keys.next();
				//intersection
				if(rset.contains(key)){
					hs.add(key);
				}
				
			}
		}
		
		return hs;
	}

}


