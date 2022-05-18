package trs.client.controller;

import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import trs.network.protobuffprotocol.TrsProtobufs;
import trs.network.protobuffprotocol.TrsServiceGrpc;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class LoginController {
    private TrsServiceGrpc.TrsServiceStub trsServiceStub;
    Stage parent;

    @FXML
    Button buttonAutentificare;
    @FXML
    TextField textFieldUsername;
    @FXML
    TextField passwordFieldPassword;

    @FXML
    public void initialize() {

    }

    public void setParent(Stage parent) {
        this.parent = parent;
    }

    public void setServiceStub(TrsServiceGrpc.TrsServiceStub trsServiceStub) {
        this.trsServiceStub = trsServiceStub;
    }

    public void onButtonAutentificareClick() {
        TrsProtobufs.AdminDto adminDto = TrsProtobufs.AdminDto.newBuilder().setUsername(textFieldUsername.getText()).setPassword(passwordFieldPassword.getText()).build();
        TrsProtobufs.TrsRequest request = TrsProtobufs.TrsRequest.newBuilder().setAdminDto(adminDto).build();

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<TrsProtobufs.TrsResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse response) {
                Platform.runLater(() -> {
                    try {
                        if (response.getType() == TrsProtobufs.TrsResponse.Type.OK) {
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("admin-view.fxml"));
                            Parent root = fxmlLoader.load();
                            AdminController ctrl = fxmlLoader.getController();

                            ctrl.setServiceStub(trsServiceStub);
                            ctrl.setAdminDto(response.getAdminDto());
                            Scene scene = new Scene(root, 600, 400);
                            Stage stage = new Stage();
                            stage.setTitle("Admin");
                            stage.setScene(scene);
                            Stage primaryStage = (Stage) buttonAutentificare.getScene().getWindow();
                            primaryStage.setUserData(parent.getUserData());

                            stage.setOnCloseRequest(event -> {
                                ctrl.logout();
                                primaryStage.show();
                            });

                            ctrl.setParent(primaryStage);
                            clearTextFields();
                            primaryStage.hide();
                            stage.show();
                        }

                        if (response.getType() == TrsProtobufs.TrsResponse.Type.ERROR) {
                            showNotification(response.getError(), Alert.AlertType.ERROR);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        trsServiceStub.login(request, responseObserver);

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showNotification(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearTextFields() {
        textFieldUsername.clear();
        passwordFieldPassword.clear();
    }
}
