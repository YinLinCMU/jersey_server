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

@Path("/parser")   //http://localhost:8080/tradeOffParser/parser
public class tradeOffParser {

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
		 JSONArray columns = new JSONArray();
		 HashSet nk = new HashSet();
		 for(int i = 0; i < 10; i++){
			 JSONObject tmpjo = new JSONObject();
			 tmpjo.put("key", keywords[i]);
			 tmpjo.put("full_name", keywords[i]);
			 tmpjo.put("type", "numeric");
			 tmpjo.put("is_objective", "TRUE");
			 tmpjo.put("goal", "MIN");
			 JSONArray ele = new JSONArray();
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
//		 StringBuilder sb = new StringBuilder();
//		 for(int i = 0; i < keywords.length; i++){
//			 sb.append(keywords[i]);
//		 }
		 return resd.toString();
	 }
	 	
	 public JSONObject crawl_generate(String company_name) throws JSONException{
		 JSONObject json = new JSONObject();
		 try {
			 json = new JSONObject(IOUtils.toString(new URL("http://riskanalysis.mybluemix.net/api/results/"+company_name), Charset.forName("UTF-8")));
		 } catch (Exception ex) {
		      System.err.println(ex);
		    }
		 JSONArray ja = (JSONArray) json.get("records");
		 if(ja.length()!=0){
			 json.put("companyName", company_name);
			 return json;
		 }
		 else{
			 try {
				 URL tmp = new URL("riskanalysis.mybluemix.net/api/crawl/"+company_name);
				 json = new JSONObject(IOUtils.toString(new URL("http://riskanalysis.mybluemix.net/api/results/"+company_name), Charset.forName("UTF-8")));
			 } catch (Exception ex) {
			      System.err.println(ex);
			    }
			 json.put("companyName", company_name);
			 return json;
		 }
	}
	 
	public HashSet<String> get_key(JSONArray data) throws JSONException{
		HashSet<String> rset = new HashSet<String>();
		HashSet<String> hs = new HashSet<String>();
		JSONObject jo = data.getJSONObject(0);
		JSONArray joo = (JSONArray) jo.get("records");
		JSONObject res = (JSONObject) joo.getJSONObject(0).get("keywords");
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
	
	public HashSet<String> filter_keyswords(HashSet<String> keywords, HashSet<String> filter_set){
		HashSet<String> filteredKeys = new HashSet<String>();
		for(String key : keywords){
			if(filter_set.contains(key)){
				filteredKeys.add(key);
			}
		}
		return filteredKeys;
	}
	
	public JSONArray parser(JSONArray d, HashSet<String> nk) throws JSONException{
		JSONArray options = new JSONArray();
		int index = 0;
		for(int i = 0; i < d.length(); i++){
			JSONObject data = d.getJSONObject(i);
			for( int j = 0; j < data.length(); j++){
				JSONArray entry = (JSONArray) data.get("records");
				JSONObject valuesJSON = (JSONObject)entry.getJSONObject(j).get("keywords");
				HashSet<String> values = new HashSet<String>();
				Iterator keys = valuesJSON.keys();
				while(keys.hasNext()){
					String key = (String) keys.next();
					values.add(key);
				}
				values = filter_keyswords(values, nk);
				
				JSONObject ele = new JSONObject();
				ele.put("key", String.valueOf(index));
				StringBuilder tmp = new StringBuilder();
				tmp.append(data.get("companyName"));
				tmp.append(entry.getJSONObject(j).get("year"));
				ele.put("name",tmp.toString());
				ele.put("values", values);
				ele.put("description_html", "Select Biotechnology Portfolio");
			
				options.put(ele);
				index += 1;
			}
		}
		return options;
	}
}


