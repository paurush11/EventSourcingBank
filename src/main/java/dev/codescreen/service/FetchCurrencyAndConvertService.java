package dev.codescreen.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Service
public class FetchCurrencyAndConvertService {

    private final WebClient webClient;

    private JSONObject jsonObject;

    public FetchCurrencyAndConvertService(WebClient webClient){
        this.webClient = webClient;
        fetchJsonFromApi();
    }

    private void fetchJsonFromApi() {
        String url = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/inr.json";
        webClient.get()  // Specifies the HTTP GET method
                .uri(url)       // Sets the target URL
                .retrieve()     // Initiates the retrieval of the resource
                .bodyToMono(String.class).subscribe(jsonObject->{
                    this.jsonObject = new JSONObject(jsonObject);
                });  // Converts the response body to a Mono containing a String
    }

    public BigDecimal getConversionFactor(String amountCurrency){
        if(this.jsonObject != null){
            String defaultCurrency = "inr";

            JSONObject rates = jsonObject.getJSONObject(defaultCurrency);
            System.out.println(rates);
            System.out.println(amountCurrency);
            return  rates.getBigDecimal(amountCurrency);
        }
        return BigDecimal.ZERO;
    }

}
