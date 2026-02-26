package org.juv25d;

import org.juv25d.di.Container;
import org.juv25d.filter.*;
import org.juv25d.logging.ServerLogging;
import org.juv25d.http.HttpParser;
import org.juv25d.router.SimpleRouter;
import org.juv25d.util.ConfigLoader;

import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    public static void main(String[] args) {

        ConfigLoader config = ConfigLoader.getInstance();
        Logger logger = ServerLogging.getLogger();
        HttpParser httpParser = new HttpParser();

        Container container = new Container("org.juv25d");

        container.bind(org.juv25d.router.Router.class, SimpleRouter.class);

        Pipeline pipeline = Bootstrap.init(container, "org.juv25d");

        DefaultConnectionHandlerFactory handlerFactory =
            new DefaultConnectionHandlerFactory(httpParser, logger, pipeline);

        Server server = new Server(
            config.getPort(),
            logger,
            handlerFactory
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");
            try {
                pipeline.stop();
                logger.info("Shutdown successful");
            }catch (Exception ex){
                logger.log(Level.SEVERE, "Error stopping server", ex);
            }finally {
                logger.info("Shutdown hook finished");
            }
        }));

        server.start();
    }
}
