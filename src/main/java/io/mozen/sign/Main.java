package io.mozen.sign;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.MultipartConfigElement;
import java.nio.file.Files;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static int port = 8080;
    public static String path = "/docx2pdf";


    public static void main(String[] args) throws Exception {

        parseArgs(args);
        logger.info("*:" + port + path);

        Server server = new Server();

        // Server header off
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);
        HttpConnectionFactory httpFactory = new HttpConnectionFactory(httpConfig);
        ServerConnector httpConnector = new ServerConnector(server, httpFactory);
        httpConnector.setPort(port);
        server.setConnectors(new Connector[]{httpConnector});

        // servlet config
        ServletContextHandler handler = new ServletContextHandler();
        String tmpPath = Files.createTempDirectory("jetty").toFile().getAbsolutePath();
        handler.addServlet(TheServlet.class, path)
                .getRegistration()
                .setMultipartConfig(new MultipartConfigElement(tmpPath));

        server.setHandler(handler);

        // start server
        try {
            server.start();
            server.join();

        } catch (Throwable tr) {
            logger.error("Server error", tr);

        } finally {
            server.destroy();
            logger.info("Server stopped.");
        }
    }

    private static void parseArgs(String[] args) {
        if (args.length > 2) {
            logger.error("Use java -jar docx2pdf.jar [port [path]]");
            System.exit(-1);
        }

        if (args.length > 1) {
            path = args[1];
        }

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Throwable ex) {
                logger.error("Argument error", ex);
            }
        }
    }

}
