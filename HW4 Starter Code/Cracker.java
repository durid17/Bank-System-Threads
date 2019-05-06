import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

public class Cracker {
	
	private class Worker extends Thread{
		int startIndex;
		int finishIndex;
		int maxLen;
		boolean found = false;

		public Worker(int startIndex , int finishIndex , int maxLen) {
			this.startIndex = startIndex;
			this.finishIndex = finishIndex;
			this.maxLen = maxLen;
		}
		
		@Override
		public void run() {
			for(int i = startIndex; i <= finishIndex ; i++) {
				String s = "" + CHARS[i];
				rec(s , maxLen - 1);
			}
			latch.countDown();
		}
		
		private void rec(String s , int len) {
			if(found) return;
			String possibleAnswer = getHash(s);
			if(possibleAnswer.equals(hash)) {
				found = true;
				System.out.println(s);
			}
			if(len <= 0) return;
			for(int i = 0 ; i < CHARS.length ; i++) {
				rec(s + CHARS[i] , len - 1);
			}
		}
		
	}
	// Array of chars used to produce strings
	public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789.,-!".toCharArray();	
	private static CountDownLatch latch;
	private String hash;
	
	public void Crack(int maxLength , int numberOfThreads, String hash) {
		this.hash = hash;
		latch = new CountDownLatch(numberOfThreads);
		int len = CHARS.length / numberOfThreads;
		for(int i = 0 ; i < numberOfThreads ; i++) {
			int start = i * len;
			int finish = 0;
			if(i == numberOfThreads - 1) {
				finish = CHARS.length - 1;
			}else finish = (i + 1) * len - 1;

			Worker worker = new Worker(start , finish , maxLength);
			worker.start();
		}
	}
	
	/*
	 Given a byte[] array, produces a hex String,
	 such as "234a6f". with 2 chars for each byte in the array.
	 (provided code)
	*/
	public static String hexToString(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i<bytes.length; i++) {
			int val = bytes[i];
			val = val & 0xff;  // remove higher bits, sign
			if (val<16) buff.append('0'); // leading 0
			buff.append(Integer.toString(val, 16));
		}
		return buff.toString();
	}
	
	/*
	 Given a string of hex byte values such as "24a26f", creates
	 a byte[] array of those values, one byte value -128..127
	 for each 2 chars.
	 (provided code)
	*/
	public static byte[] hexToArray(String hex) {
		byte[] result = new byte[hex.length()/2];
		for (int i = 0; i < hex.length(); i+=2) {
			result[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
		}
		return result;
	}
	
	public static String getHash(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			return hexToString(md.digest(s.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private static void crackThePassword(String hash , String len , String threads) {
		int maxLength = Integer.parseInt(len);
		int numberOfThreads = Integer.parseInt(threads);
		Cracker cracker = new Cracker();
		cracker.Crack(maxLength , numberOfThreads , hash);
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("All done");
	}
	
	public static void main(String[] args) {
		if(args.length == 1) {
			System.out.println(getHash(args[0]));
		}else if(args.length == 3) crackThePassword(args[0] , args[1] , args[2]);
	}
	
	// possible test values:
	// a 86f7e437faa5a7fce15d1ddcb9eaeaea377667b8
	// fm adeb6f2a18fe33af368d91b09587b68e3abcb9a7
	// a! 34800e15707fae815d7c90d49de44aca97e2d759
	// xyz 66b27417d37e024c46526c2f6d358a754fc552f3

}
