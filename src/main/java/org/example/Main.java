package org.example;
import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.HttpClientOptions;
import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.*;

import okhttp3.Request;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws Exception {
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.

        final String clientId = "6dffb3e6-85e6-49b4-b663-bf896d46c81f";
        final String tenantId = "de29a90e-98e1-4ce0-a3b8-f9f38f3fd6d6"; // or "common" for multi-tenant apps
        final String userName = "program@rrorbit.in";
        final String password = "password*900";
        final List<String> scopes = Arrays.asList(".default");

        System.setProperty("java.net.useSystemProxies","true");
        System.setProperty("proxyHost","localhost");
        System.setProperty("proxyPort","8888");


        final UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder()
                .clientId(clientId)
                .tenantId(tenantId)
                .username(userName)
                .password(password)
                .build();

        // .httpClient(azHttpClient)

        if (null == scopes || null == credential) {
            throw new Exception("Unexpected error");
        }
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                scopes, credential);

        final GraphServiceClient<Request> graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider).buildClient();

        // GET https://graph.microsoft.com/v1.0/me?$select=displayName,jobTitle
        UserDeltaCollectionPage usersDeltaPage = graphClient.users().delta().buildRequest().select("userPrincipalName,displayName").top(2).get();
        int i =1;
        while (usersDeltaPage != null) {
            System.out.println("Page Iteration # " + j);
            final List<User> userList = usersDeltaPage.getCurrentPage();

            System.out.println( java.time.LocalDateTime.now() + " : No of Users found: " + userList.size());
            for (User user : userList) {
                System.out.println(user.displayName + " : " + user.userPrincipalName + " : " + user.id);
            }
            final UserDeltaCollectionRequestBuilder usersDeltaNextPage = usersDeltaPage.getNextPage();
            // getNextpage returns null when the UsersDeltaPage.requestBuilder == null (debug variable contents)
            // UsersDeltaPage.requestBuilder is null when the http response do not contain @odata.nextlink property
            if (usersDeltaNextPage == null) {
                // No more pages to retrieve from the users list.
                if(usersDeltaPage.deltaLink != null) {
                    System.out.println("We seem to have a Delta Link");
                    System.out.println("Will try with Delta Link after 60 seconds...");
                    Thread.sleep(60000);
                    usersDeltaPage = graphClient.users().delta().buildRequest().deltaLink(usersDeltaPage.deltaLink).get();
                }
            } else {
                Thread.sleep(2000); // Added this to avoid multiple skiptoken requests in the very first attempt (The value is zero anyway)
                usersDeltaPage = usersDeltaNextPage.buildRequest().get();
            }

           i++;
        }

    }
}