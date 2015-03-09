package nl.sogeti;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sockjs.BridgeOptions;
import io.vertx.ext.sockjs.SockJSServer;
import io.vertx.ext.sockjs.SockJSServerOptions;
import io.vertx.ext.sockjs.impl.RouteMatcher;

public class MainVerticle extends AbstractVerticle {

	private static final String PATH = "app";
	private static final String welcomePage  = "index.html";
	
	@Override
	public void start() throws Exception {
		RouteMatcher matcher = RouteMatcher.routeMatcher();
		
		// Bind index.html to /
		matcher.matchMethod(HttpMethod.GET,"/", req ->req.response().sendFile(PATH+"/"+welcomePage));

	
		// Bind static content folder app to /app
		matcher.matchMethod(HttpMethod.GET,"^\\/"+PATH+"\\/.*",req -> req.response().sendFile(req.path().substring(1)));
		
		
		HttpServer server = Vertx.vertx().createHttpServer()
				.requestHandler(req -> matcher.accept(req));
		
		SockJSServer sockJSServer = SockJSServer.sockJSServer(vertx, server);
		sockJSServer.bridge(new SockJSServerOptions().setPrefix("/eventbus"),
				new BridgeOptions().addInboundPermitted(new JsonObject())
						.addOutboundPermitted(new JsonObject()));
		server.listen(8080);
	}
}
