import javax.sql.rowset.spi.SyncResolver;

public class Account {
	private int ID;
	private int balance;
	private int transactions;
	
	public Account(int id , int balance) {
		this.ID = id;
		this.balance = balance;
		this.transactions = 0;
	}
	
	public synchronized void transMoneyTo(int money) {
		this.balance += money;
		transactions++;
	}
	
	public synchronized void transMoneyFrom(int money) {
		this.balance -= money;
		transactions++;
	}
	
	@Override
	public String toString() {
		return "acct:" + ID + " bal:" + balance + " trans:" + transactions;
	}
	
}
