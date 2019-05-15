import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import javax.swing.*;

public class WebWorker extends Thread {

	private String urlString;
	private WebFrame.Luncher luncher;
	private int index;
	private CountDownLatch latch;
		
	public WebWorker(String urlString , WebFrame.Luncher luncher , int index , CountDownLatch latch) {
		this.urlString = urlString;
		this.luncher = luncher;
		this.index = index;
		this.latch = latch;
	}
	
	
	@Override
	public void run() {
 		download();
	}
	
	private void finishWork(String info) {
		luncher.allDone();
		luncher.updateRow(info, index);
		luncher.finishThread();
	}
	
	private void download() {
		long startTime = System.currentTimeMillis();
		luncher.startThread();
		InputStream input = null;
		StringBuilder contents = null;
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
		
			// Set connect() to throw an IOException
			// if connection does not succeed in this many msecs.
			connection.setConnectTimeout(5000);
			
			connection.connect();
			input = connection.getInputStream();

			BufferedReader reader  = new BufferedReader(new InputStreamReader(input));
		
			char[] array = new char[1000];
			int len;
			contents = new StringBuilder(1000);
			boolean flag= false;
			while ((len = reader.read(array, 0, array.length)) > 0) {
				if(isInterrupted()) {
					flag = true;
					break;
				}
				contents.append(array, 0, len);
				Thread.sleep(100);
			}
			long time = System.currentTimeMillis() - startTime;
			SimpleDateFormat simpleFormat = new SimpleDateFormat("HH:mm:ss");
			String date = simpleFormat.format(new Date());
			String info = date + " " + time + "ms " + contents.length() + " bytes";
			if(flag) info = "err";
			finishWork(info);
		}	
		// Otherwise control jumps to a catch...
		catch(MalformedURLException ignored) {
			finishWork("err");
		}
		catch(InterruptedException exception) {
			finishWork("err");
		}
		catch(IOException ignored1) {
			finishWork("err");
		}
		// "finally" clause, to close the input stream
		// in any case
		finally {
			latch.countDown();
			try{
				if (input != null) input.close();
			}
			catch(IOException ignored) {}
		}
	}
}
