package trs.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import trs.network.protobuffprotocol.TrsServiceGrpc;

import java.io.IOException;

public class MainController {
    private TrsServiceGrpc.TrsServiceStub trsServiceStub;

    @FXML
    public Button buttonAutentificareAdmin;

    public void setServiceStub(TrsServiceGrpc.TrsServiceStub trsServiceStub) {
        this.trsServiceStub = trsServiceStub;
    }

    public void onButtonAutentificareAdminClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = fxmlLoader.load();
            LoginController ctrl = fxmlLoader.getController();
            ctrl.setServiceStub(this.trsServiceStub);
            Scene scene = new Scene(root, 350, 200);
            Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setScene(scene);
            Stage primaryStage = (Stage) this.buttonAutentificareAdmin.getScene().getWindow();

            stage.setOnCloseRequest(event -> {
                primaryStage.show();
            });

            ctrl.setParent(primaryStage);
            primaryStage.hide();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
