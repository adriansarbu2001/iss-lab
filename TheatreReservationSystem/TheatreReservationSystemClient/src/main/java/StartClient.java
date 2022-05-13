import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import trs.client.controller.MainController;
import trs.network.protobuffprotocol.TrsServiceGrpc;

import java.io.IOException;
import java.util.Properties;

public class StartClient extends Application {

    private static final int defaultPort = 55556;
    private static final String defaultServer = "localhost";

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("In start");
        Properties clientProps = new Properties();
        try {
            clientProps.load(StartClient.class.getResourceAsStream("/trs-client.properties"));
            System.out.println("Client properties set. ");
            clientProps.list(System.out);
        } catch (IOException e) {
            System.err.println("Cannot find trs-client.properties " + e);
            return;
        }

        String serverIP = clientProps.getProperty("trs.server.host", defaultServer);
        int serverPort = defaultPort;

        try {
            serverPort = Integer.parseInt(clientProps.getProperty("trs.server.port"));
        } catch (NumberFormatException ex) {
            System.err.println("Wrong port number " + ex.getMessage());
            System.out.println("Using default port: " + defaultPort);
        }
        System.out.println("Using server IP " + serverIP);
        System.out.println("Using server port " + serverPort);

        ManagedChannel channel = NettyChannelBuilder.forAddress(serverIP, serverPort).usePlaintext().build();
        TrsServiceGrpc.TrsServiceStub trsServiceStub = TrsServiceGrpc.newStub(channel);

        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent root = mainLoader.load();
        MainController ctrl = mainLoader.getController();
        ctrl.setServiceStub(trsServiceStub);
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Theatre Reservation System");
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            ctrl.onClose();
        });
        primaryStage.show();
    }
}
