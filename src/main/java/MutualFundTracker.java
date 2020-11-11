import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutualFundTracker {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final ObjectMapper objectMapper;
    private static final Map<String, Long> schemeMap;

    static {
        schemeMap = new HashMap<>();
        schemeMap.put("Axis Focused 25 Growth Direct Plan", 120468L);
        schemeMap.put("Mirae Asset Emerging Bluechip Growth Direct Plan", 118834L);
        schemeMap.put("Axis Bluechip Growth Direct Plan", 120465L);
        schemeMap.put("Axis Mid Cap Growth Direct Plan", 120505L);
        schemeMap.put("SBI Equity Hybrid Growth Direct Plan", 119609L);
        schemeMap.put("Parag Parikh Long Term Equity Growth Direct Plan", 122639L);
        schemeMap.put("SBI Small Cap Growth Direct Plan", 125497L);
        schemeMap.put("Nippon India US Equity Opportunites Growth Direct Plan", 134923L);
        schemeMap.put("Axis Small Cap Growth Direct Plan", 125354L);
        schemeMap.put("NIFTY 50", 101525L);
        objectMapper = new ObjectMapper();
    }

    public static void main(String[] args) throws JsonProcessingException {
        Long benchmark = 101525L;
        Map<Date, List<Transaction>> dateToTransactionMap = parseFile("/tmp/transactions.csv");
        Map<Long, Map<Date, Double>> fundToDateToNavMap = getNavs(schemeMap.values());

        Date startDate = dateToTransactionMap.keySet().stream().sorted().findFirst().get();

        Map<Long,Double> fundToUnitMap = new HashMap<>();
        Double invested = 0d;
        Double benchmarkUnits = 0d;
        for(Date date = startDate; date.before(new Date());) {

            List<Transaction> transactions = dateToTransactionMap.get(date);
            Double value = 0d;
            Double benchmarkNav = getNav(fundToDateToNavMap, date, benchmark);
            if(transactions != null) {
                for(Transaction transaction : transactions) {
                    if(TransactionType.buy.equals(transaction.getTransactionType())) {
                        //Update invested amount
                        invested += transaction.getAmount();

                        //Update fundToUnitMap
                        Double unitsOfFund = fundToUnitMap.get(transaction.getSchemeCode());
                        if(unitsOfFund == null) {
                            unitsOfFund = 0d;
                        }
                        unitsOfFund += transaction.getUnits();
                        fundToUnitMap.put(transaction.getSchemeCode(), unitsOfFund);

                        //Update benchmarkUnits
                        benchmarkUnits += transaction.getAmount() / benchmarkNav;
                    }
                }
            }

            //Update value
            for(Map.Entry<Long, Double> entry : fundToUnitMap.entrySet()) {
                Long scheme = entry.getKey();
                Double units = entry.getValue();

                Double nav = getNav(fundToDateToNavMap, date, scheme);
                value += units * nav;
            }
            Double benchmarkValue = benchmarkNav * benchmarkUnits;

            System.out.println(dateFormat.format(date) + "," + invested + "," + value + "," + benchmarkValue);

            Calendar incrementedDate = Calendar.getInstance();
            incrementedDate.setTime(date);
            incrementedDate.add(Calendar.DATE,1);
            date = incrementedDate.getTime();
        }
    }

    private static Double getNav(Map<Long, Map<Date, Double>> fundToDateToNavMap, Date date, Long scheme) {
        Double nav = fundToDateToNavMap.get(scheme).get(date);
        Date yesterdayDate = new Date(date.getTime());
        while (nav == null) {
            Calendar yesterday = Calendar.getInstance();
            yesterday.setTime(yesterdayDate);
            yesterday.add(Calendar.DATE, -1);
            yesterdayDate = yesterday.getTime();
            nav = fundToDateToNavMap.get(scheme).get(yesterdayDate);
        }
        return nav;
    }

    private static Map<Date, List<Transaction>> parseFile(String filePath) {
        Map<Date, List<Transaction>> transactions = new HashMap<>();
        try {
            File file = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String contentLine = br.readLine();
            contentLine = br.readLine();
            while (StringUtils.isNotBlank(contentLine)) {
                String[] contents = contentLine.split(",");
                String fundName = contents[2];
                Long schemeCode = schemeMap.get(fundName);
                if (schemeCode != null) {
                    Transaction transaction = new Transaction();
                    Date transactionDate = dateFormat.parse(contents[0].trim());
                    transaction.setTransactionDate(transactionDate);
                    transaction.setFundName(fundName);
                    transaction.setSchemeCode(schemeCode);
                    transaction.setTransactionType(TransactionType.valueOf(contents[3]));
                    transaction.setUnits(Double.parseDouble(contents[4]));
                    transaction.setAmount(Double.parseDouble(contents[7]));

                    List<Transaction> transactionsOfDay;
                    if(transactions.get(transactionDate) == null) {
                        transactionsOfDay = new ArrayList<Transaction>();
                        transactions.put(transactionDate, transactionsOfDay);
                    } else {
                        transactionsOfDay = transactions.get(transactionDate);
                    }
                    transactionsOfDay.add(transaction);
                }
                contentLine = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }

    private static Map<Long, Map<Date, Double>> getNavs(Collection<Long> schemeCodes) {
        Map<Long, Map<Date, Double>> navMap = new HashMap<Long, Map<Date, Double>>();
        for (Long schemeCode : schemeCodes) {
            try {
                HttpResponse<String> response = Unirest.get("https://api.mfapi.in/mf/" + schemeCode)
                        .header("content-type", "application/json").asString();
                NAVAPIResultModel resultModel = objectMapper.readValue(response.getBody(),NAVAPIResultModel.class);

                Map<Date, Double> navOfFund = new HashMap<Date, Double>();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                for(Map<String,String> nav : resultModel.getData()) {
                    navOfFund.put(sdf.parse(nav.get("date")), Double.parseDouble(nav.get("nav")));
                }
                navMap.put(schemeCode, navOfFund);
            } catch (UnirestException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return navMap;
    }
}
