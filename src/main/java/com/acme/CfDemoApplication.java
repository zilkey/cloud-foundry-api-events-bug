package com.acme;

import com.acme.responses.Application;
import com.acme.responses.Event;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.*;

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
        Application app = client.getApplication(applicationName);

        List<Event> events = client.getEvents(app);

        for (Event event: events) {
            System.out.println(event.getType());
            System.out.println(event.getActorName());
        }
    }



}
