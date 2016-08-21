package is.ejb.dl.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "WalletData")
public class WalletDataEntity {
	@Id
	@GeneratedValue
	private int id;
	private int userId;
	private double balance = 0;
	private String isoCurrencyCode;
	private int transactionCounter = 0;

	@Transient
	private double potentialQuidcoRewards = 0;

	@Transient
	private double potentialSnapdealRewards = 0;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public int getTransactionCounter() {
		return transactionCounter;
	}

	public void setTransactionCounter(int transactionCounter) {
		this.transactionCounter = transactionCounter;
	}

	public String getIsoCurrencyCode() {
		return isoCurrencyCode;
	}

	public void setIsoCurrencyCode(String isoCurrencyCode) {
		this.isoCurrencyCode = isoCurrencyCode;
	}

	public double getPotentialQuidcoRewards() {
		return potentialQuidcoRewards;
	}

	public void setPotentialQuidcoRewards(double potentialQuidcoRewards) {
		this.potentialQuidcoRewards = potentialQuidcoRewards;
	}

	public double getPotentialSnapdealRewards() {
		return potentialSnapdealRewards;
	}

	public void setPotentialSnapdealRewards(double potentialSnapdealRewards) {
		this.potentialSnapdealRewards = potentialSnapdealRewards;
	}

}
