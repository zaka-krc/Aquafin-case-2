package be.ehb.aquafin.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeerAPIService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public double getNeerslagVoorMaand(int jaar, int maand) {
        try {
            // Bereken start en eind datum van de maand
            LocalDate startDatum = LocalDate.of(jaar, maand, 1);
            LocalDate eindDatum = startDatum.plusMonths(1).minusDays(1);

            // API call naar Belgische meteorologie service
            String cqlFilter = String.format("code = 6447 AND timestamp DURING %sT00:00:00Z/%sT23:59:59Z",
                    startDatum.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    eindDatum.format(DateTimeFormatter.ISO_LOCAL_DATE));

            String encodedFilter = URLEncoder.encode(cqlFilter, StandardCharsets.UTF_8);
            String url = "https://opendata.meteo.be/service/ows" +
                    "?service=wfs" +
                    "&version=2.0.0" +
                    "&request=getFeature" +
                    "&typeNames=synop:synop_data" +
                    "&outputformat=json" +
                    "&CQL_FILTER=" + encodedFilter;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // JSON verwerken
            JsonNode root = mapper.readTree(response.body());
            JsonNode features = root.get("features");

            double totaalNeerslag = 0.0;
            Map<String, Double> dagTotalen = new HashMap<>();

            if (features != null) {
                for (JsonNode feature : features) {
                    JsonNode props = feature.get("properties");
                    if (props != null) {
                        String timestamp = props.get("timestamp").asText().substring(0, 10);
                        JsonNode neerslagNode = props.get("precip_quantity");

                        if (neerslagNode != null && !neerslagNode.isNull()) {
                            double neerslag = neerslagNode.asDouble();
                            dagTotalen.merge(timestamp, neerslag, Double::sum);
                        }
                    }
                }
            }

            // Som van alle dagen in de maand
            totaalNeerslag = dagTotalen.values().stream().mapToDouble(Double::doubleValue).sum();

            // Round to 1 decimal for logging
            double afgerond = Math.round(totaalNeerslag * 10.0) / 10.0;
            System.out.println("API data voor " + jaar + "-" + maand + ": " + afgerond + "mm");
            return totaalNeerslag;

        } catch (Exception e) {
            System.out.println("API fout: " + e.getMessage());
            return 0.0; // Return 0 bij fout
        }
    }

    public Map<Integer, Double> getJaarData(int jaar) {
        Map<Integer, Double> jaarData = new HashMap<>();

        for (int maand = 1; maand <= 12; maand++) {
            double neerslag = getNeerslagVoorMaand(jaar, maand);
            if (neerslag > 0) {
                jaarData.put(maand, neerslag);
            }
        }

        return jaarData;
    }
}