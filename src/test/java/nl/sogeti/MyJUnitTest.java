package nl.sogeti;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * Example of an asynchronous unit test written in JUnit style using vertx-unit
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@RunWith(VertxUnitRunner.class)
public class MyJUnitTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
      Async async = context.async();
      vertx = Vertx.vertx();
      vertx.deployVerticle(MainVerticle.class.getName(), ar -> {
          if (ar.succeeded()) {
              async.complete();
          } else {
              context.fail("Could not deploy verticle");
          }
      }); 
    }

    @Test
    public void testHello(TestContext context) {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        HttpClientRequest req = client.get(8080, "localhost", "/app/test.html");
        req.exceptionHandler(err -> {
            context.fail();
        });
        req.handler(resp -> {
            context.assertEquals(200, resp.statusCode());
            Buffer entity = Buffer.buffer();
            resp.handler(entity::appendBuffer);
            resp.endHandler(v -> {
                context.assertEquals("test", entity.toString("UTF-8"));
                async.complete();
            });
        });
        req.end();
    }

    @After
    public void tearDown(TestContext context) {
        Async async = context.async();
        vertx.close(ar -> {
            async.complete();
        });
    }

    @Test
    public void testSockBridge(TestContext context) {
	HttpClient client = vertx.createHttpClient();
	Async async = context.async();
	client.websocket(8080,"localhost","/eventbus", new Handler<WebSocket>() {

	    @Override
	    public void handle(WebSocket ws) {
		async.complete();
	    }
	});

    }

    @Test
    public void testSendMessage(TestContext test) {
	Async async = test.async();
	vertx.eventBus().consumer("chat", message -> {
	    test.assertEquals("test message", ((JsonObject) message.body()).getString("message"));
	    async.complete();
	});
	vertx.eventBus().publish("chat", new JsonObject("{\"message\": \"test message\"}"));

    }
}
