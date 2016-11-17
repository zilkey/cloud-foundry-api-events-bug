package com.acme;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

@SpringBootApplication
public class CfDemoApplication {

	public static void main(String[] args) throws URISyntaxException {
        if (args.length < 7) {
            System.out.println("Usage: build/libs/cf-demo-0.0.1-SNAPSHOT.jar spring|plain <apihost> <organization> <space> <username> <password> <applicationname>");
            System.exit(1);
        }

        if (args[0].equals("spring")) {
            SpringApplication.run(CfDemoApplication.class, args);
        } else {
            testEventsAPI(args);
        }
	}

    @Component
    class EventPrinter implements CommandLineRunner {
        public void run(String[] args) throws URISyntaxException {
            testEventsAPI(args);
        }
    }

    private static void testEventsAPI(String[] args) throws URISyntaxException {
        String apiHost = args[1];
        String organization = args[2];
        String space = args[3];
        String username = args[4];
        String password = args[5];
        String applicationName = args[6];

        CloudFoundryClient client = new CloudFoundryClient(apiHost);
        client.login(username, password);
    }

    static class Links {
        private String login;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }
    }

    static class PasswordInfo {
        private Links links;

        public Links getLinks() {
            return links;
        }

        public void setLinks(Links links) {
            this.links = links;
        }
    }

    static class ApiInfo {
        private String authorizationEndpoint;

        @JsonProperty("authorization_endpoint")
        public String getAuthorizationEndpoint() {
            return authorizationEndpoint;
        }

        public void setAuthorizationEndpoint(String authorizationEndpoint) {
            this.authorizationEndpoint = authorizationEndpoint;
        }
    }

    static class OAuth {
        private String accessToken;

        @JsonProperty("access_token")
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    static class CloudFoundryClient {
        private String apiHost;
        private RestTemplate restTemplate;

        public CloudFoundryClient(String apiHost) {
            this.apiHost = apiHost;
        }

        public void login(String username, String password) throws URISyntaxException {
            String loginUrl = getLoginURL(apiHost);
            String tokenUrl = getOAuthURL(loginUrl);
            String accessToken = getAccessToken(tokenUrl, username, password);

            System.out.println("Login URL is " + loginUrl);
            System.out.println("Token URL is " + tokenUrl);
            System.out.println("Access Token is " + accessToken);
        }

        public String getLoginURL(String baseURL) throws URISyntaxException {
            RestTemplate restTemplate = getRestTemplate();

            RequestEntity<Void> requestEntity = RequestEntity
                    .get(new URI(baseURL + "/info"))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .build();

            ResponseEntity<ApiInfo> response = restTemplate.exchange(
                    requestEntity,
                    ApiInfo.class
            );

            return response.getBody().getAuthorizationEndpoint();
        }

        public String getOAuthURL(String baseURL) throws URISyntaxException {
            RestTemplate restTemplate = getRestTemplate();

            RequestEntity<Void> requestEntity = RequestEntity
                    .get(new URI(baseURL))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .build();

            ResponseEntity<PasswordInfo> response = restTemplate.exchange(
                    requestEntity,
                    PasswordInfo.class
            );
            return response.getBody().getLinks().getLogin() + "/oauth/token";
        }

        public String getAccessToken(String tokenUrl, String username, String password) throws URISyntaxException {
            RestTemplate restTemplate = getRestTemplate();

            MultiValueMap<String,String> formData = new LinkedMultiValueMap<String,String>();
            formData.add("grant_type","password");
            formData.add("username",username);
            formData.add("password",password);

            RequestEntity<MultiValueMap<String,String>> tokenRequestEntity = RequestEntity
                    .post(new URI(tokenUrl))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .header("Authorization", "Basic Y2Y6")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData);

            System.out.println(tokenRequestEntity);

            ResponseEntity<OAuth> tokenResponse = restTemplate.exchange(
                    tokenRequestEntity,
                    OAuth.class
            );

            return tokenResponse.getBody().getAccessToken();
        }

        private RestTemplate getRestTemplate() {
            if (this.restTemplate == null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
                jacksonConverter.setObjectMapper(mapper);
                FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
                this.restTemplate = new RestTemplate(Arrays.asList(jacksonConverter, formHttpMessageConverter));
            }

            return restTemplate;
        }
    }

}
