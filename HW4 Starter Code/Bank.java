import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Bank {
	
	private static BlockingQueue<Transaction> queue;
	private static CountDownLatch latch;
	private static final int QUEUE_SIZE = 20;
	private static final int NUM_OF_ACCOUNTS = 20; 
	private static final int BALANCE = 1000;
	private final static Transaction nullTrans = new Transaction(-1,0,0);
	private static Account[] accs;
	
	private class Worker extends Thread{
		@Override
		public void run() {
			while(true) {
				try {
					Transaction trans = queue.take();
					if(trans.equals(nullTrans)) {
						latch.countDown();
						break;
					}
					int money = trans.getMoney();
					accs[trans.getIDFrom()].withdraw(money);
					accs[trans.getIDTo()].deposit(money);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public Bank(int threadNum) {
		queue = new ArrayBlockingQueue<Transaction> (QUEUE_SIZE);
		latch = new CountDownLatch(threadNum);
		accs = new Account[NUM_OF_ACCOUNTS];
		for(int i = 0 ; i < NUM_OF_ACCOUNTS ; i++) {
			accs[i] = new Account( i , BALANCE);
		}
		for(int i = 0 ; i < threadNum ; i++) {
			Worker thread = new Worker();
			thread.start();
		}
	}
	
	public void makeTransactions(String filename , int threadNum) {
		try {
			Scanner sc = new Scanner(new File(filename));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] arguments = line.split(" ");
				Transaction trans = new Transaction(Integer.parseInt(arguments[0]),
						Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
				queue.put(trans);
			}
            sc.close();
			for(int i = 0 ; i < threadNum ; i++) {
				queue.put(nullTrans);
			}
		} catch (FileNotFoundException | InterruptedException e1) {
			e1.printStackTrace();
		}
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(int i = 0 ; i < NUM_OF_ACCOUNTS ; i++) {
			System.out.println(accs[i]);
		}
	}
	
	public static void main(String[] args) {
		String filename = "/home/demetre/Desktop/eclipse-workspace/hw4Threads/HW4 Starter Code/" + args[0];
		
		int threadNum = Integer.parseInt(args[1]);
		Bank bank = new Bank(threadNum);
		bank.makeTransactions(filename, threadNum);
	}
}
