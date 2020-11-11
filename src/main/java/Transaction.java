import java.util.Date;

public class Transaction {
    private Date transactionDate;
    private String fundName;
    private Long schemeCode;
    private TransactionType transactionType;
    private Double units;
    private Double amount;

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public Long getSchemeCode() {
        return schemeCode;
    }

    public void setSchemeCode(Long schemeCode) {
        this.schemeCode = schemeCode;
    }

    public Double getUnits() {
        return units;
    }

    public void setUnits(Double units) {
        this.units = units;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}
