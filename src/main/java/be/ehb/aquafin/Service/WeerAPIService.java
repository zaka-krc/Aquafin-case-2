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
import java.util.Random;

@Service
public class WeerAPIService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Zet op true om altijd mock data te gebruiken, false om API te proberen
    private static final boolean FORCE_MOCK_DATA = true;

    public double getNeerslagVoorMaand(int jaar, int maand) {
        // Als FORCE_MOCK_DATA aan staat, gebruik direct mock data
        if (FORCE_MOCK_DATA) {
            return getMockNeerslag(jaar, maand);
        }

        try {
            // Bereken start en eind datum van de maand
            LocalDate startDatum = LocalDate.of(jaar, maand, 1);
            LocalDate eindDatum = startDatum.plusMonths(1).minusDays(1);

            // Check of datum niet in de toekomst ligt
            if (startDatum.isAfter(LocalDate.now())) {
                System.out.println("Datum ligt in de toekomst, gebruik mock data");
                return getMockNeerslag(jaar, maand);
            }

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
                    .timeout(java.time.Duration.ofSeconds(10)) // Timeout toegevoegd
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("API error: HTTP " + response.statusCode());
                System.out.println("Response body: " + response.body());
                System.out.println("Gebruik mock data als fallback");
                return getMockNeerslag(jaar, maand);
            }

            // JSON verwerken
            JsonNode root = mapper.readTree(response.body());
            JsonNode features = root.get("features");

            double totaalNeerslag = 0.0;
            Map<String, Double> dagTotalen = new HashMap<>();

            if (features != null && !features.isEmpty()) {
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

                // Som van alle dagen in de maand
                totaalNeerslag = dagTotalen.values().stream().mapToDouble(Double::doubleValue).sum();

                // Als we geen data hebben gevonden, gebruik mock data
                if (totaalNeerslag == 0.0) {
                    System.out.println("Geen neerslag data gevonden, gebruik mock data");
                    return getMockNeerslag(jaar, maand);
                }
            } else {
                System.out.println("Geen features gevonden, gebruik mock data");
                return getMockNeerslag(jaar, maand);
            }

            // Round to 1 decimal for logging
            double afgerond = Math.round(totaalNeerslag * 10.0) / 10.0;
            System.out.println("API data voor " + jaar + "-" + maand + ": " + afgerond + "mm");
            return totaalNeerslag;

        } catch (Exception e) {
            System.out.println("API fout: " + e.getMessage());
            System.out.println("Gebruik mock data als fallback");
            return getMockNeerslag(jaar, maand);
        }
    }

    // Mock data generator - realistische neerslag voor België
    private double getMockNeerslag(int jaar, int maand) {
        // Gebruik jaar en maand als seed voor consistente data
        Random random = new Random(jaar * 100 + maand);

        // Gemiddelde neerslag per maand in België (in mm)
        double[] basisNeerslag = {
                70.0,  // Januari
                55.0,  // Februari
                65.0,  // Maart
                50.0,  // April
                65.0,  // Mei
                70.0,  // Juni
                80.0,  // Juli
                75.0,  // Augustus
                65.0,  // September
                85.0,  // Oktober
                85.0,  // November
                80.0   // December
        };

        double basis = basisNeerslag[maand - 1];
        // Voeg wat variatie toe (tussen -30% en +30%)
        double variatie = basis * 0.3;
        double neerslag = basis + (random.nextDouble() * 2 - 1) * variatie;

        // Afronden op 1 decimaal
        neerslag = Math.round(neerslag * 10.0) / 10.0;

        System.out.println("Mock data voor " + jaar + "-" + maand + ": " + neerslag + "mm");
        return neerslag;
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