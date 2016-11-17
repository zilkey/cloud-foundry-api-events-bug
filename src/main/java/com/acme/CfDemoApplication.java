package com.acme;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationEvent;
import org.cloudfoundry.operations.applications.GetApplicationEventsRequest;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.util.List;

@SpringBootApplication
public class CfDemoApplication {

	public static void main(String[] args) {
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
        public void run(String[] args) {
            testEventsAPI(args);
        }
    }

    private static void testEventsAPI(String[] args) {
        String apiHost = args[1];
        String organization = args[2];
        String space = args[3];
        String username = args[4];
        String password = args[5];
        String applicationName = args[6];

        DefaultConnectionContext connectionContext = DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .build();

        PasswordGrantTokenProvider tokenProvider = PasswordGrantTokenProvider.builder()
                .password(password)
                .username(username)
                .build();

        ReactorCloudFoundryClient cloudFoundryClient = ReactorCloudFoundryClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();

        ReactorDopplerClient dopplerClient = ReactorDopplerClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();

        ReactorUaaClient uaaClient = ReactorUaaClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();

        DefaultCloudFoundryOperations cloudFoundryOperations = DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                .uaaClient(uaaClient)
                .dopplerClient(dopplerClient)
                .organization(organization)
                .space(space)
                .build();

        List<ApplicationEvent> events;

        for (int i = 1; i < 5; i++)  {
            System.out.println("Getting " + i + " events");
            events = cloudFoundryOperations
                    .applications()
                    .getEvents(GetApplicationEventsRequest
                            .builder()
                            .name(applicationName)
                            .maxNumberOfEvents(i)
                            .build())
                    .collectList()
                    .block();

            for (ApplicationEvent event : events) {
                System.out.println("  " + event);
            }
        }
    }

}
