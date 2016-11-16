package app;


import java.io.File;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import secklow.operators.SecklowIndexReader;
import secklow.operators.SecklowIndexer;

public class SecklowMain {
	
	private static Logger log = LoggerFactory.getLogger(SecklowMain.class); // the logger
	
	public static void main(String[] args){
		
	//	indexData("docs/nt");
		
		
		
		annotate("docs/nt");
		
	}

	public static void indexData(String path) {
		SecklowIndexer sIexer = new  SecklowIndexer();
		Model model = ModelFactory.createDefaultModel();
		
		File folder = new File(path);
		File[] listOfNTs = folder.listFiles();
		
		for (File ntFile : listOfNTs){
			model.add(RDFDataMgr.loadModel(ntFile.getAbsolutePath(), Lang.NT)) ;
			log.info("Added {}, model has {} triples ",ntFile.getName(),model.size());
		}
		
		
		model.add(RDFDataMgr.loadModel("./docs/transcripts.nt", Lang.NT));
		log.info("Added {}, model has {} triples ",model.size());
		
		sIexer.setDescrProp("http://purl.org/dc/dcmitype/Text");
		
		log.info("Indexing....");
		sIexer.index(model);
		
		log.info("Done!");
		
	}
	
	public static void annotate (String path){
		
		SecklowIndexReader sReader = new SecklowIndexReader();	
		sReader.setDescrProp("http://purl.org/dc/dcmitype/Text");
		
		Model model = ModelFactory.createDefaultModel();
		
		File folder = new File(path);
		File[] listOfNTs = folder.listFiles();
		
		for (File ntFile : listOfNTs){
			model.add(RDFDataMgr.loadModel(ntFile.getAbsolutePath(), Lang.NT)) ;
			log.info("Added {}, model has {} triples ",ntFile.getName(),model.size());
		}
		
		model.add(RDFDataMgr.loadModel("./docs/transcripts.nt", Lang.NT));
		log.info("Added transcripts, model has {} triples ", model.size());
		
		
		log.info("Annotating....");	
		
		sReader.readIndexAdnWriteJson(model);
	
	}
	
	
	
	
	
	
}
