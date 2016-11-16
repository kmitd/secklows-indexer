package soundTests;


import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;


public class Test_SpeechAPI {

	private String apiKey = "AIzaSyD_Esb3wAxUeE63jcgT-9SWCIrxsFf2rEY";
	
	@Test
	public void Test(){
		
	}
	@Ignore
	@Test
	public void testSpeech() throws IOException, URISyntaxException {

		
		String url = "https://speech.googleapis.com/v1/speech:recognize?key="+apiKey;
		
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("Accept", "application/json; charset=utf-8");
		httpPost.setEntity(new FileEntity(new File("/Users/ilariatiddi/Desktop/SecklowSound/audios/requests/request_audio1.json")));
		
		 
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		JSONObject result ;
	    CloseableHttpResponse response2 = httpclient.execute(httpPost);
	    System.err.println(response2.getStatusLine());
	    HttpEntity entity2 = response2.getEntity();
	    
	    // create JSONObject
	    StringWriter writer = new StringWriter();
	    IOUtils.copy(entity2.getContent(), writer, StandardCharsets.UTF_8);
	    String rs = writer.toString();
	    EntityUtils.consume(entity2);
	    result = new JSONObject(rs);
		
	    System.out.println(result);
	    

	
	}

}
