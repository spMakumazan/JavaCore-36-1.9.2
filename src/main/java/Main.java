import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    public static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build()) {
            HttpGet request = new HttpGet("https://api.nasa.gov/planetary/apod?api_key=O5b7F5v63qy6DjD5SP0AN8C5LAhEJRV4sjcln8e9");
            request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                ServerAnswer answer = mapper.readValue(response.getEntity().getContent(), new TypeReference<>() {
                });
                String url = answer.getUrl();

                try (CloseableHttpClient newHttpClient = HttpClientBuilder.create()
                        .setDefaultRequestConfig(RequestConfig.custom()
                                .setConnectTimeout(5000)
                                .setSocketTimeout(30000)
                                .setRedirectsEnabled(false)
                                .build())
                        .build()) {
                    HttpGet newRequest = new HttpGet(url);
                    try (CloseableHttpResponse newResponse = newHttpClient.execute(newRequest)) {
                        byte[] newAnswer = newResponse.getEntity().getContent().readAllBytes();
                        String[] urlStrToArr = url.split("/");
                        String fileName = urlStrToArr[urlStrToArr.length - 1];
                        try (FileOutputStream fos = new FileOutputStream(fileName)) {
                            fos.write(newAnswer, 0, newAnswer.length);
                        }
                    }
                }
            }
        }
    }
}