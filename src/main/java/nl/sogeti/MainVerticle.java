package nl.sogeti;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;
import io.vertx.ext.mongo.MongoServiceVerticle;
import io.vertx.ext.sockjs.BridgeOptions;
import io.vertx.ext.sockjs.SockJSServer;
import io.vertx.ext.sockjs.SockJSServerOptions;
import io.vertx.ext.sockjs.impl.RouteMatcher;

public class MainVerticle extends AbstractVerticle {

    private static final String PATH = "app";
    private static final String welcomePage = "index.html";

    @Override
    public void start() throws Exception {
	setUpMongo();
	HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080)).requestHandler(req -> getRouteMatcher().accept(req));

	SockJSServer.sockJSServer(vertx, server).bridge(new SockJSServerOptions().setPrefix("/eventbus"),
		new BridgeOptions().addInboundPermitted(new JsonObject()).addOutboundPermitted(new JsonObject()));

	MongoService proxy = MongoService.createEventBusProxy(vertx, "vertx.mongo");
	vertx.eventBus().consumer("chat", m -> proxy.save("messages", new JsonObject(m.body().toString()), res -> System.out.println(res.result())));

	server.listen();
    }

    private void setUpMongo() {
	DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("address", "vertx.mongo"));
	vertx.deployVerticle(new MongoServiceVerticle(), options, res -> System.out.println(res.result()));
    }

    private RouteMatcher getRouteMatcher() {
	RouteMatcher matcher = RouteMatcher.routeMatcher().matchMethod(HttpMethod.GET, "/", req -> req.response().sendFile(PATH + "/" + welcomePage));
	matcher.matchMethod(HttpMethod.GET, "^\\/" + PATH + "\\/.*", req -> req.response().sendFile(req.path().substring(1)));
	return matcher;
    }
}
