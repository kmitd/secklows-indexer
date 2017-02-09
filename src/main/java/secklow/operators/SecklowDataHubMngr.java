package secklow.operators;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;

public class SecklowDataHubMngr {

	private static Logger log = LoggerFactory.getLogger(SecklowDataHubMngr.class); // the logger
	private String entityFile = "./docs/datahub_entities.json";
	private Map<String , Map<String,Double>> similarityMap; 
	private List<String> datahubEntities; 
	private Map<String,String> datahubEntityInfo; 	
	
	public SecklowDataHubMngr (){
		similarityMap = new HashMap< String, Map<String, Double>> ();
		datahubEntities = new ArrayList<String>();
		getDatahubEntities();
		datahubEntityInfo = new HashMap<String,String>();
	}
	
	
	public  void setEntityFile (String file){
		entityFile = file;
	}
	public String  getEntityFile(){
		return entityFile;
	}
	
	public List<String> getEntities(){
		return new ArrayList<String>(datahubEntities);
	}
	
	public void addSimilarity(String key, String compared){
		
		if (!similarityMap.containsKey(key)){
			similarityMap.put(key, new HashMap<String,Double>());
		}
		
		double sim = similarity(key, compared);
		similarityMap.get(key).put(compared, sim);
	}
	
	public double getSimilarity(String term1, String term2){
		if (similarityMap.containsKey(term1)){
			if (similarityMap.get(term1).containsKey(term2)) {				
				return similarityMap.get(term1).get(term2);
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}
	
	public double similarity(String s1, String s2) {
		
		  String longer = s1.toLowerCase(), shorter = s2.toLowerCase();
		  if (s1.length() < s2.length()) { // longer should always have greater length
		    longer = s2; shorter = s1;
		  }
		  int longerLength = longer.length();
		  if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
		  return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
	}
	
	private int editDistance(String longer, String shorter){
		
		return StringUtils.getLevenshteinDistance(longer, shorter);
	}
	
	
	private void getDatahubEntities() {
		try {
			
			log.info("Reading entities from file {}", entityFile);
			JsonReader reader = new JsonReader(new FileReader(entityFile));
			
			reader.beginObject();
			
			while (reader.hasNext()) {
			  String dataHubClass = reader.nextName(); // the key (unused but needed!!!)
			  
			  // avoid getting useless entities
				  reader.beginArray();
				  if (!dataHubClass .contains("lsoa-2011") || !dataHubClass .contains("busstop") ) {
					  while (reader.hasNext()){
						String dataHubClassInstance = reader.nextString();
						datahubEntities.add(dataHubClassInstance);
					  }
				  }
				  reader.endArray();
		    }

			reader.endObject();
			reader.close();
			
		} catch (FileNotFoundException  e) {
			log.error("Could not find file ./docs/datahub_entities.json");
		} catch (IOException e) {
			log.error("I/O exception");
	
		}
		
		return ;
	}


	public String getDHinfo(String entity) {
	if (datahubEntityInfo.containsKey(entity)) {
		return datahubEntityInfo.get(entity);
	} else 	{
		try {
			log.debug("Calling {}....", entity);
			Content response = Request.Get(entity).execute().returnContent();
			datahubEntityInfo.put(entity, response.asString());
			return response.asString();
		} catch (ClientProtocolException e) {
			log.error("Client protocol exceptioN! {}", e.getMessage());
			return "";
		} catch (IOException e) {
			log.error("I/O exception! {}", e.getMessage());
	
			return "";
		}
		
	}
		
	}
}
