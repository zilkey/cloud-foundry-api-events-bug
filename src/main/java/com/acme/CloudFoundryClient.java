package com.acme;

import com.acme.responses.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CloudFoundryClient {
    private String apiHost;
    private String accessToken;
    private RestTemplate restTemplate;

    public CloudFoundryClient(String apiHost) {
        this.apiHost = apiHost;
    }

    public void login(String username, String password) throws URISyntaxException {
        String loginUrl = getLoginURL(apiHost);
        String tokenUrl = getOAuthURL(loginUrl);
        accessToken = getAccessToken(tokenUrl, username, password);
    }

    String getLoginURL(String baseURL) throws URISyntaxException {
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

    String getOAuthURL(String baseURL) throws URISyntaxException {
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

    String getAccessToken(String tokenUrl, String username, String password) throws URISyntaxException {
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

    public Application getApplication(String appName) throws URISyntaxException {
        RestTemplate restTemplate = getRestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(this.apiHost + "/v2/apps")
                .queryParam("q", "name IN " + appName);

        System.out.println(builder.build().toUri());

        RequestEntity<Void> tokenRequestEntity = RequestEntity
                .get(builder.build().toUri())
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + this.accessToken)
                .build();

        System.out.println(tokenRequestEntity);

        ResponseEntity<PaginatedResource<Application>> responseEntity = restTemplate.exchange(
                tokenRequestEntity,
                new ParameterizedTypeReference<PaginatedResource<Application>>() {}
        );

        Resource<Application> applicationResource = responseEntity.getBody().getResources().get(0);
        applicationResource.getEntity().setGuid(applicationResource.getMetadata().get("guid"));
        return applicationResource.getEntity();
    }

    public List<Event> getEvents(Application application) throws URISyntaxException {
        RestTemplate restTemplate = getRestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(this.apiHost + "/v2/events")
                .queryParam("q", "actee IN " + application.getGuid())
                .queryParam("order-direction", "desc");

        System.out.println(builder.build().toUri());

        RequestEntity<Void> tokenRequestEntity = RequestEntity
                .get(builder.build().toUri())
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + this.accessToken)
                .build();

        ResponseEntity<PaginatedResource<Event>> responseEntity = restTemplate.exchange(
                tokenRequestEntity,
                new ParameterizedTypeReference<PaginatedResource<Event>>() {}
        );

        ArrayList<Event> events = new ArrayList<Event>();

        for(Resource<Event> resource : responseEntity.getBody().getResources()) {
            events.add(resource.getEntity());
        }

        return events;
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

    private RestTemplate getStringRestTemplate() {
        FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
        StringHttpMessageConverter converter = new StringHttpMessageConverter();
        return new RestTemplate(Arrays.asList(converter, formHttpMessageConverter));
    }
}