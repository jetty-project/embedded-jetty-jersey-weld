package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MainTest
{
    private Server server;

    public Server startServer(Main.WeldMode weldMode) throws Exception
    {
        server = Main.newServer(0, weldMode);
        server.start();
        return server;
    }

    @AfterEach
    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    @ParameterizedTest
    @EnumSource(Main.WeldMode.class)
    public void testRestTest(Main.WeldMode weldMode) throws Exception
    {
        startServer(weldMode);

        URI uri = server.getURI().resolve("/rest/test");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .header("Accept", "application/json")
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode(), is(200));
    }
}
