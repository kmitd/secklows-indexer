package secklow.operators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.discou.DiscouReader;
import uk.ac.open.kmi.discou.DiscouResource;

import com.google.gson.stream.JsonWriter;

public class SecklowIndexReader {
	
	private String INDEX = "secklowIndex";
	private String TITLE_PROPERTY;
	private String DESCR_PROPERTY;
	private SecklowDataHubMngr DATAHUB_MANAGER;
	private static Logger log = LoggerFactory.getLogger(SecklowIndexReader.class); // the logger
	
	public SecklowIndexReader(){
		TITLE_PROPERTY = "http://purl.org/dc/elements/1.1/title";
		DESCR_PROPERTY = "http://purl.org/dc/dcmitype/Text";
		DATAHUB_MANAGER = new SecklowDataHubMngr();
	}
	
	public void setIndex(String ix){
		INDEX = ix;
	}
	
	
	public void setTitleProp(String newTitleProp){
		TITLE_PROPERTY = newTitleProp;
	}
	
	public void setDescrProp(String newDescrProp){
		DESCR_PROPERTY = newDescrProp;
	}
	
	
	
	/**
	 * reads and get DBpedia resources
	 */
	public void readIndexAdnWriteJson(Model model){
		
		File index = new File("docs",INDEX);
		JsonWriter jsonW ;
		
		Property titleProp = model.getProperty(TITLE_PROPERTY);
		Property descriptionProp = model.getProperty(DESCR_PROPERTY);
		
		try {
			jsonW = new JsonWriter(new FileWriter("docs/secklow_ann.json"));
			jsonW.setIndent("\t");

			
			DiscouReader reader = new DiscouReader(index);
			reader.open();
			
			jsonW.beginArray();
			for (int i = 0 ; i < reader.count() ; i++){ 
//			for (int i = 0 ; i < 20 ; i++){ 	 
				// log
				if (i % 10 == 0) {
					log.info("Count {}/{}...", i, reader.count());
				}
				
				// new resource
				DiscouResource discouRes = reader.getFromDocId(i);
				
				// from nt
				Resource modelRes = model.getResource(discouRes.getUri());
			
				jsonW.beginObject();
				jsonW.name("name").value(discouRes.getUri());

				
				if (modelRes.hasProperty(titleProp)){					
					String title = modelRes.getProperty(titleProp).getObject().asLiteral().getString();
					jsonW.name("title").value(title);			
				} else {
					jsonW.name("title").value("");
				}
				
				if (modelRes.hasProperty(descriptionProp)){					
					String discoDesc = modelRes.getProperty(descriptionProp).getObject().asLiteral().getString();
					jsonW.name("descr").value(discoDesc);			
				} else {
					jsonW.name("descr").value("");
				}
				
			
				jsonW.name("entities");
				
				// annotated content
				String content = discouRes.getContent();
				HashMap<String,Integer> contents  = new HashMap<String,Integer>();
				String[] split = content.split(" ");


				for (int j = 1 ; j < split.length; j++){
					String ent = split[j];
					if(contents.containsKey(ent)){
						int count = contents.get(ent);
						count+=1;
						contents.put(ent, count);
					} else{
						contents.put(ent, 1);
					}
				}
				
				jsonW.beginArray();

				log.debug("Matching DH to {} DBpedia entities...", contents.keySet().size());
				for (Entry<String, Integer> cont : contents.entrySet()){					
					jsonW.beginObject();
					jsonW.name("text").value(cont.getKey().replace("http://dbpedia.org/resource/", ""));
					jsonW.name("dbentity").value(cont.getKey());
//					jsonW.beginObject();
					jsonW.name("weight").value(cont.getValue());
					jsonW.name("dh");
					jsonW.beginArray();
					
					for (String dhEntity : DATAHUB_MANAGER.getEntities()) {
						
						String trim1 = cont.getKey().substring(cont.getKey().lastIndexOf("/")+1).replace("_", " ");
						String trim2 = dhEntity.substring(dhEntity.lastIndexOf("/")+1).replace("_", " ");
						
						double sim;
						
						// XXX : is it worth doing it?
//						if (DATAHUB_MANAGER.getSimilarity(trim1, trim2) == -1){
//							DATAHUB_MANAGER.addSimilarity(trim1, trim2);
//						}
//						sim =  DATAHUB_MANAGER.getSimilarity(trim1, trim2);
						
						sim = DATAHUB_MANAGER.similarity(trim1, trim2);
						if (sim >= 0.9) {	
							String datahubContent = DATAHUB_MANAGER.getDHinfo(dhEntity);
							jsonW.beginObject();
							jsonW.name(dhEntity).value(datahubContent);
							jsonW.endObject();
							log.info("{}, {}, sim={}",trim1, trim2, sim);
						}
					}
					
					jsonW.endArray();
					
	
					
//					jsonW.endObject();	
					
					
					jsonW.endObject();
				}
				
				jsonW.endArray();
				
				jsonW.endObject();
			}
			jsonW.endArray();
			
			jsonW.close();
		} catch (IOException e) {
			log.error("Error {}", e.getMessage());
		}
		
		log.info("OK!");
	}
	
	
	


}
