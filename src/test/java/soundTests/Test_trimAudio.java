package soundTests;



import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.junit.Test;





public class Test_trimAudio {
	


	
	public void testIBMSpeechApi() throws InterruptedException, FileNotFoundException
	{
//		SpeechToText service = new SpeechToText();
//		service.setUsernameAndPassword("8db4745c-cb50-4870-a87f-7ee630579fa4", "wzSuuUiPpvEQ");
//		
//		service.setEndPoint("https://stream.watsonplatform.net/speech-to-text/api");
//
//		File audio = new File("./docs/test4.wav");
//		RecognizeOptions options = new RecognizeOptions.Builder()
//		.continuous(true)
//		.keywords(new String[]{"Milton Keynes", "Secklow Sounds", "Community Radio", "Sounds"})
//		.keywordsThreshold(0.01)
//		.maxAlternatives(3)
//		.wordAlternativesThreshold(0.01)
//		.wordConfidence(true)
//		.model("en-UK_BroadbandModel")
//		.contentType(HttpMediaType.AUDIO_WAV).build() ;
//		SpeechResults transcript = service.recognize(audio, options).execute();
		
//		System.out.println(transcript);
		
	}	
	
	@Test
	public void testSphinx() throws IOException{
	
		
	}
	
	
	
	public void testTrimAudio() throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedInputStream in = new BufferedInputStream(new FileInputStream("docs/test2.wav"));

		int read;
		byte[] buff = new byte[1024];
		while ((read = in.read(buff)) > 0)
		{
		    out.write(buff, 0, read);
		}
		out.flush();
		in.close();
		byte audio[]  = out.toByteArray(); // this is in time domain
		
		
		System.out.println("Audio lenght: "+audio.length);
		
		// we need to at know what point of time each frequency appeared.
		// need to convert out from the time domain to the frequency domain. 
		// sliding window =  chunk of data transformed
		// estimate number of chunks to analyze in every second 
		final int totalSize = audio.length;
		int chunkSize = 4;
		int sampledChunkSize = totalSize/ chunkSize;

		//When turning into frequency domain we'll need complex numbers:
		Complex[][] results = new Complex[sampledChunkSize][];
		
		
		// iterate through all the chunks and perform FFT analysis on each
		for(int times = 0;times < sampledChunkSize; times++) {
		    Complex[] complex = new Complex[chunkSize];
		    
		    for(int i = 0;i < chunkSize ;i++) {
		        //Put the time domain data into a complex number with imaginary part as 0:
		        complex[i] = new Complex(audio[(times*chunkSize)+i], 0);
		        
		    }
		    //Perform FFT analysis on the chunk:
		    FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
            results[times] = fft.transform(complex, TransformType.FORWARD);
		}
		
		
		for (int t = 0; t < results.length; t++) {
			 
			
//			// real components at even indices and imaginary components at odd indices
//			System.out.println(Arrays.toString(results[t]));
//			
//			//To get the magnitude of the spectrum at index i :
//			Double re = results[t][2 * i].abs();
//			Double im = results[t][2 * i + 1].abs();
//			 
//			double[] magnitude;
//			magnitude[i] = Math.sqrt(re*re+im*im);

			
		}
		
		// 85 to 180 Hzadult male /  adult female  = 165 to 255 H
			
//		for (int i = 0; i < results.length ; i++){
//			System.out.println(Arrays.toString(results[i]));
////			double magnitude = Math.log10(results[i][1].abs()+1);
////			System.out.println(magnitude);
//		} 
		
	
	}

	
	public int getIndex(int freq) {
	    int i = 0;
	    final int[] RANGE = new int[] { 40, 80, 120, 180, 300 };
	    while (RANGE[i] < freq)
	        i++;
	    return i;
	}
	    


}
