import java.util.Date;
import java.util.Map;

public class PortfolioOnDate {

    private Date date;
    private Map<Long,Double> fundToUnitMap;
    private Double invested;
    private Double portfolioValue;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Map<Long, Double> getFundToUnitMap() {
        return fundToUnitMap;
    }

    public void setFundToUnitMap(Map<Long, Double> fundToUnitMap) {
        this.fundToUnitMap = fundToUnitMap;
    }

    public Double getInvested() {
        return invested;
    }

    public void setInvested(Double invested) {
        this.invested = invested;
    }

    public Double getPortfolioValue() {
        return portfolioValue;
    }

    public void setPortfolioValue(Double portfolioValue) {
        this.portfolioValue = portfolioValue;
    }
}
