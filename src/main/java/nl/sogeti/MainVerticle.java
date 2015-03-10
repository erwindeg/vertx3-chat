package nl.sogeti;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;
import io.vertx.ext.mongo.MongoServiceVerticle;
import io.vertx.ext.sockjs.BridgeOptions;
import io.vertx.ext.sockjs.SockJSServer;
import io.vertx.ext.sockjs.SockJSServerOptions;
import io.vertx.ext.sockjs.impl.RouteMatcher;

import java.util.List;
import java.util.UUID;

public class MainVerticle extends AbstractVerticle {

    private static final String PATH = "app";
    private static final String welcomePage = "index.html";
    private String channel = UUID.randomUUID().toString();
    MongoService proxy;

    @Override
    public void start() throws Exception {
	
	proxy = setUpMongo();
	RouteMatcher matcher = getRouteMatcher();
	vertx.eventBus().consumer("chat", this::saveMessages);
	vertx.eventBus().consumer(channel, this::saveMessages);
//	vertx.eventBus().consumer("history",
//		m -> proxy.find("messages", new JsonObject(), res -> vertx.eventBus().send(((JsonObject)m.body()).getString("channel"), new JsonArray(res.result()).toString())));
	vertx.eventBus().consumer("history",
		m -> proxy.find("messages", new JsonObject(), res -> sendMessages(((JsonObject)m.body()).getString("channel"),res)));
	matcher.matchMethod(HttpMethod.GET, "/api/history", req -> proxy.find("messages", new JsonObject(), res -> req.response().end(new JsonArray(res.result()).toString())));

	setUpServer(matcher).listen();
    }

   
    private void saveMessages(Message message) {
	proxy.save("messages", new JsonObject(message.body().toString()), res -> System.out.println(res.result()));
    }

    private void sendMessages(String channel, AsyncResult<List<JsonObject>> result){
	
	for(JsonObject message : result.result()){
		vertx.eventBus().send(channel, message);	
	}
    }
    
    private void sendHistoryRequest(AsyncResult<String> result){
	vertx.eventBus().publish("history", new JsonObject().put("channel",channel));
    }

    private HttpServer setUpServer(RouteMatcher matcher) {
	HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(9080)).requestHandler(req -> matcher.accept(req));
	SockJSServer.sockJSServer(vertx, server).bridge(new SockJSServerOptions().setPrefix("/eventbus"),
		new BridgeOptions().addInboundPermitted(new JsonObject()).addOutboundPermitted(new JsonObject()));
	return server;

    }
    
    private MongoService setUpMongo() {
	DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("address", "vertx.mongo"));
	vertx.deployVerticle(new MongoServiceVerticle(), options, this::sendHistoryRequest);
	return MongoService.createEventBusProxy(vertx, "vertx.mongo");
    }

    private RouteMatcher getRouteMatcher() {
	RouteMatcher matcher = RouteMatcher.routeMatcher().matchMethod(HttpMethod.GET, "/", req -> req.response().sendFile(PATH + "/" + welcomePage));
	matcher.matchMethod(HttpMethod.GET, "^\\/" + PATH + "\\/.*", req -> req.response().sendFile(req.path().substring(1)));
	return matcher;
    }
}
