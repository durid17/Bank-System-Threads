
public class Transaction {
	private int idFrom;
	private int idTo;
	private int money;
	
	public Transaction(int idFrom , int idTo , int money) {
		this.idFrom = idFrom;
		this.idTo = idTo;
		this.money = money;
	}
	
	public int getIDFrom() {
		return idFrom;
	}
	
	public int getIDTo() {
		return idTo;
	}
	
	public int getMoney() {
		return money;
	}
}
