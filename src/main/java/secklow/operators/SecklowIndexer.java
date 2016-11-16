package secklow.operators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.discou.DiscouIndexer;
import uk.ac.open.kmi.discou.DiscouInputResourceImpl;

public class SecklowIndexer {

	private static Logger log = LoggerFactory.getLogger(SecklowIndexer.class); // the logger
	private String ANNOTATOR = "http://anne.kmi.open.ac.uk/rest/annotate";
	private DiscouIndexer INDEXER;
	private String DESCR_PROPERTY;
	private String TITLE_PROPERTY;
	

	public SecklowIndexer(){
		DESCR_PROPERTY = "http://purl.org/dc/elements/1.1/description";
		TITLE_PROPERTY = "http://purl.org/dc/elements/1.1/title";
		INDEXER = new DiscouIndexer(new File("docs/secklowIndex"), ANNOTATOR ){}; // anna?
		
		INDEXER .open();
	}
	
	public void setAnnotator(String newAnnotator){
		INDEXER = new DiscouIndexer( new File("docs/secklowIndex"), ANNOTATOR ){};
		INDEXER.open();
	}
	
	public void setTitleProp(String newTitleProp){
		TITLE_PROPERTY = newTitleProp;
	}
	
	public void setDescrProp(String newDescrProp){
		DESCR_PROPERTY = newDescrProp;
	}
	
	/**
	 * indexes and annotate content with DBpedia resources
	 */
	public void index(Model model){
		

		try {
			  List<DiscouInputResourceImpl> resources = listResources(model); // TODO extend this to include DH resources
			  
			  for (int i = 0; i < resources.size(); i++){
				  INDEXER .put(resources.get(i));
			  }
			} catch (IOException e) {
			    log.error("",e);
			}finally{
			    try {
			    	INDEXER .commit();
			    } catch (IOException e) {
		        	log.error("Error {}", e.getMessage());
			    }finally{
			        try {
			        	INDEXER .close();
			        } catch (IOException e) {
			        	log.error("Error {}", e.getMessage());
			        }               
			    }
			}	
	}
	
	
	public  List<DiscouInputResourceImpl> listResources(Model model) {
		
		List<DiscouInputResourceImpl> resources = new ArrayList<DiscouInputResourceImpl>();
		
		
		
		ResIterator subjects = model.listSubjects();
		
		Property titleProp = model.getProperty(TITLE_PROPERTY);
		Property descriptionProp = model.getProperty(DESCR_PROPERTY);
		
		int i = 0;
		while (subjects.hasNext()){
			log.info("res {}", i);
			
			i++;
			Resource subject = subjects.next();
			if (!subject.getURI().contains("episode")){
				continue;
			}
		
			
			String title = "";
			String description = "";
			String content = "" ;
			
			if (subject.hasProperty(titleProp)){				
				title =  subject.getProperty(titleProp).getObject().asLiteral().getString();			
			}  
			
			if (subject.hasProperty(descriptionProp)){
				content = subject.getProperty(descriptionProp).getObject().asLiteral().getString();
				content = content.replace("\n", " || ");
				description = content;
			}
						
			log.info("(ep) {} || (t) {}",  subject.toString(), title);
			
			DiscouInputResourceImpl discouNewRes = new DiscouInputResourceImpl(subject.getURI(), title, description, content);
			resources.add(discouNewRes);
		}
		
		return resources;
	}
}
