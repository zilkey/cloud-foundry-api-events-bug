View some nice output (running with Spring):

```
./gradlew build && java -jar build/libs/cf-demo-0.0.1-SNAPSHOT.jar spring 
    api.run.pivotal.io "Your organization" your-space user@example.com abc-someapikey-zyx articulate
```

View much more raw output:

```
./gradlew build && java -jar build/libs/cf-demo-0.0.1-SNAPSHOT.jar plain 
    api.run.pivotal.io "Your organization" your-space user@example.com abc-someapikey-zyx articulate
```