package secklow.operators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
	private static Logger log = LoggerFactory.getLogger(SecklowIndexReader.class); // the logger

	public SecklowIndexReader(){
		TITLE_PROPERTY = "http://purl.org/dc/elements/1.1/title";
		DESCR_PROPERTY = "http://purl.org/dc/dcmitype/Text";
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
				Set<String> contents  = new HashSet<String>();
				String[] split = content.split(" ");
				
				for (int j = 1 ; j < split.length; j++){
					
					contents.add(split[j]);
				}
				
				jsonW.beginArray();

				for (String cont : contents){					
					jsonW.value(cont);
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
