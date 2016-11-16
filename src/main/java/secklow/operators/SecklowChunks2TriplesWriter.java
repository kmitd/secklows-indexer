package secklow.operators;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ilariatiddi
 * given an episode, create nt model
 */
public class SecklowChunks2TriplesWriter {
	
	private static Logger log = LoggerFactory.getLogger(SecklowChunks2TriplesWriter.class); // the logger
	private static String NS_SECKLOW_SOUNDS = "http://data.mksmart.org/secklowsounds/" ;
	private static String NS_SECKLOW_EPISODES = NS_SECKLOW_SOUNDS+"episode/" ;
	
	
	public static void main(String[] args){
		
		// iterate over 
		
		Model model = ModelFactory.createDefaultModel();
		String path = "/Users/ilariatiddi/Documents/KMi/Apps/secklow/output/";
		File folder = new File(path);
		File[] listOfEpisodes = folder.listFiles();
		
		for (File epiFolder : listOfEpisodes){
			if (!epiFolder.isDirectory()){
				continue;
			}
			
			String episode = epiFolder.getName();
			log.info("Episode {}", episode);
			
			model.add( chunks2triple(episode));
						
		}
		
		try {
			model.write(new FileOutputStream("./docs/transcripts.nt"),"N-TRIPLES");
			log.info ("OK ({} triples written).", model.size());
		} catch (FileNotFoundException e) {
			log.error("File ./docs/transcripts.nt not found.");
		}
		
		
	}

	
	
	
	/**
	 * read chunk_file and return transcript string from JSON
	 * @param chunkFile
	 * @return the transcript
	 */
	private static List<String[]> getTranscript(File chunkFile) {
		
		List<String[]> transcripts = new ArrayList<String[]>();
	
		
		try {
			List<String> lines = FileUtils.readLines(chunkFile, "UTF-8");
			for (int i = 0; i < lines.size() ; i++){
				
				// avoid transcript if Speech confidence is < 0.5
				if (lines.get(i).contains("transcript") &&  Double.parseDouble(lines.get(i-1).substring(lines.get(i-1).indexOf(":")+1,lines.get(i-1).lastIndexOf(","))) > 0.5 ){	
					String[] newTranscript = new String[3];
					newTranscript[0] = chunkFile.getName();
					newTranscript[1] = lines.get(i-1).substring(lines.get(i-1).indexOf(":")+1,lines.get(i-1).lastIndexOf(",")-1);
					newTranscript[2] = lines.get(i).substring(25,lines.get(i).length()-1);
					
					transcripts.add(newTranscript);
				}
			}
		} catch (IOException e) {
			log.error("IO exception {}", chunkFile);
		}

			   
		return transcripts;
	}
	

	
	
	/**
	 * all the transcripts are added to the model as one single property 
	 * @param episode
	 * @return
	 */
	public static Model chunks2triple (String episode){
		
		Model model = ModelFactory.createDefaultModel();
		Resource epi = model.createResource(NS_SECKLOW_EPISODES+(episode.split("-")[1]));	
		Property transcriptionProp = model.createProperty("http://purl.org/dc/dcmitype/Text");		
		
		// one episode
		String path = "/Users/ilariatiddi/Documents/KMi/Apps/secklow/output/"+episode+"/transcripts";
		File folder = new File(path);
		File[] listOfTranscripts = folder.listFiles();
		
		StringBuilder transcriptBuilder = new StringBuilder();
		for (File transcriptFile : listOfTranscripts){
			
			List<String[]> transcript= getTranscript(transcriptFile);
			
			for (int i = 0; i < transcript.size(); i++){
				
				// TR_T might be empty;
				if (transcript.get(i)[0] == null) {
					continue;
				}
							
				transcriptBuilder.append (transcript.get(i)[2]);
//				transcriptBuilder.append(System.getProperty("line.separator"));
				transcriptBuilder.append(" ");
			}
			
		}
		
		epi.addProperty( transcriptionProp, model.createTypedLiteral(transcriptBuilder.toString()));
		
		return model;
	}
	
	
	/**
	 * each transcript is added to the model as a single one, including its confidence score and the original chunk file 
	 * @param episode
	 * @return
	 */
	public static Model chunks2triples(String episode){
		
		Model model = ModelFactory.createDefaultModel();
		
		Resource epi = model.createResource(NS_SECKLOW_EPISODES+(episode.split("-")[1]));
		
		Property transcriptionProp = model.createProperty("http://purl.org/dc/dcmitype/Text");
		Property transcriptionConf = model.createProperty(NS_SECKLOW_SOUNDS+"props/transcripts/confidenceScore");
		Property transcriptionChunk = model.createProperty(NS_SECKLOW_SOUNDS+"props/transcripts/fromChunk");
		Property transcriptionText = model.createProperty(NS_SECKLOW_SOUNDS+"props/transcripts/hasText");
		
		
		// one episode
		String path = "/Users/ilariatiddi/Documents/KMi/Apps/secklow/output/"+episode+"/transcripts";
		File folder = new File(path);
		File[] listOfTranscripts = folder.listFiles();
		
		int index = 0;
		for (File transcriptFile : listOfTranscripts){
			
		
			List<String[]> transcripts = getTranscript(transcriptFile);
			
			for (int i = 0 ; i < transcripts.size() ; i ++){
				
				// TR_T might be empty;
				String[] transcript = transcripts.get(i);
				if (transcript[0] == null){
					continue;
				}
				
				Resource res = model.createResource(NS_SECKLOW_EPISODES+"transcripts/tr_"+Integer.toString(index));
				
				res.addProperty(transcriptionChunk, model.createResource(NS_SECKLOW_EPISODES+"chunks/"+transcript[0]));
				res.addProperty(transcriptionConf, model.createTypedLiteral(Double.parseDouble(transcript[1])));
				res.addProperty( transcriptionText, model.createTypedLiteral(transcript[2]));
				

				model.add(epi,transcriptionProp, res);
				
				index++;
			}
			
		}
		
		return model;
	}
	
}
