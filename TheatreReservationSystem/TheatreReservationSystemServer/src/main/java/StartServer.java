import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import trs.network.protobuffprotocol.TrsServiceGrpc;
import trs.server.TrsServiceImpl;

import java.io.IOException;
import java.util.Properties;

public class StartServer {
    private static final int defaultPort = 55556;

    public static void main(String[] args) {
        Properties serverProps;
        try {
            serverProps = getProps();
        } catch (IOException e) {
            System.err.println("Cannot find trs-server.properties " + e);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(serverProps.getProperty("trs.server.port"));
        } catch (NumberFormatException ex) {
            System.out.println("Wrong port number! Using default port...");
            port = defaultPort;
        }

        Server server = ServerBuilder
                .forPort(port)
                .addService(getService())
                .build();

        try {
            server.start();
            System.out.println("Theatre Reservation System server started on port " + port + "...");

            /*
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Theatre Reservation System server is shutting down!");
                server.shutdown();
            }));
             */

            server.awaitTermination();
        } catch (IOException e) {
            System.err.println("Error starting the server" + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Error awaitTermination" + e.getMessage());
        }
    }

    public static Properties getProps() throws IOException {
        Properties serverProps = new Properties();
        serverProps.load(StartServer.class.getResourceAsStream("/trs-server.properties"));
        System.out.println("Server properties set. ");
        serverProps.list(System.out);
        return serverProps;
    }

    public static TrsServiceGrpc.TrsServiceImplBase getService(){
        ApplicationContext context = new ClassPathXmlApplicationContext("trs-beans.xml");
        return context.getBean(TrsServiceImpl.class);
    }
}
