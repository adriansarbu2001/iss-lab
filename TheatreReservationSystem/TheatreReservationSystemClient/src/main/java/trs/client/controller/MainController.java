package trs.client.controller;

import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import trs.client.DtoUtils;
import trs.model.Seat;
import trs.model.TheatreShow;
import trs.network.protobuffprotocol.TrsProtobufs;
import trs.network.protobuffprotocol.TrsServiceGrpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MainController {
    private TrsServiceGrpc.TrsServiceStub trsServiceStub;
    private StreamObserver<TrsProtobufs.TrsRequest> observer = null;
    private TheatreShow currentTheatreShow = null;

    public ObservableList<Seat> modelSeats = FXCollections.observableArrayList();
    @FXML
    public TableView<Seat> tableViewSeats;
    @FXML
    public Label labelCurrentTheatreShow;
    @FXML
    public TableColumn<Seat, Integer> tableColumnLoja;
    @FXML
    public TableColumn<Seat, Integer> tableColumnRand;
    @FXML
    public TableColumn<Seat, Integer> tableColumnNumar;
    @FXML
    public TableColumn<Seat, Integer> tableColumnPret;
    @FXML
    public Button buttonRezervaLocuri;
    @FXML
    public Button buttonAutentificareAdmin;

    @FXML
    public void initialize() {
        initializeTableViewSeats();
    }

    private void initializeTableViewSeats() {
        tableColumnLoja.setCellValueFactory(new PropertyValueFactory<>("Lodge"));
        tableColumnRand.setCellValueFactory(new PropertyValueFactory<>("Row"));
        tableColumnNumar.setCellValueFactory(new PropertyValueFactory<>("Number"));
        tableColumnPret.setCellValueFactory(new PropertyValueFactory<>("Price"));
        tableViewSeats.setItems(modelSeats);
        tableViewSeats.getSelectionModel().setSelectionMode(
                SelectionMode.MULTIPLE
        );
    }

    public void setServiceStub(TrsServiceGrpc.TrsServiceStub trsServiceStub) {
        this.trsServiceStub = trsServiceStub;
        observer = trsServiceStub.addSeatObserver(getResponseObserver());
        setTheatreShow();
        findAllSeats();
        refreshTable();
    }

    private void setTheatreShow() {
        TrsProtobufs.TrsRequest request = TrsProtobufs.TrsRequest.newBuilder()
                .build();

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<TrsProtobufs.TrsResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse response) {
                if (response.getType() == TrsProtobufs.TrsResponse.Type.OK) {
                    currentTheatreShow = DtoUtils.fromTheatreShowDto(response.getTheatreShowDto());
                    labelCurrentTheatreShow.setText(currentTheatreShow.getName());
                }
                if (response.getType() == TrsProtobufs.TrsResponse.Type.ERROR) {
                    labelCurrentTheatreShow.setText("Astazi nu are loc niciun spectacol de teatru");
                    showNotification(response.getError(), Alert.AlertType.ERROR);
                }
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

        trsServiceStub.findTodaysTheatreShow(request, responseObserver);

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void findAllSeats() {
        TrsProtobufs.TrsRequest request = TrsProtobufs.TrsRequest.newBuilder()
                .build();

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<TrsProtobufs.TrsResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse response) {
                if (response.getType() == TrsProtobufs.TrsResponse.Type.OK) {
                    List<TrsProtobufs.SeatDto> dtos = response.getSeatDtosList();
                    List<Seat> all = new ArrayList<>();
                    for (var dto : dtos) {
                        all.add(DtoUtils.fromSeatDto(dto));
                    }
                    modelSeats.setAll(all);
                }
                if (response.getType() == TrsProtobufs.TrsResponse.Type.ERROR) {
                    showNotification(response.getError(), Alert.AlertType.ERROR);
                }
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

        trsServiceStub.findAllSeats(request, responseObserver);

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void refreshTable() {
        if (currentTheatreShow == null) {
            buttonRezervaLocuri.setDisable(true);
            modelSeats.clear();
            return;
        }

        TrsProtobufs.TrsRequest request = TrsProtobufs.TrsRequest.newBuilder()
                .setTheatreShowDto(DtoUtils.fromTheatreShow(currentTheatreShow))
                .build();

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<TrsProtobufs.TrsResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse response) {
                if (response.getType() == TrsProtobufs.TrsResponse.Type.OK) {
                    List<TrsProtobufs.ReservationDto> dtos = response.getReservationDtosList();
                    List<Long> ids = new ArrayList<>();
                    for (var dto : dtos) {
                        ids.add(dto.getSeatDto().getId());
                    }
                    tableViewSeats.setRowFactory(tableView -> new TableRow<>() {
                        @Override
                        protected void updateItem(Seat item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null) {
                                setStyle("");
                            }
                            else if (ids.contains(item.getId())) {
                                setStyle("-fx-background-color: #ff8080;");
                                setDisable(true);
                            }
                            else {
                                setStyle("-fx-background-color: #80ff80;");
                                setDisable(false);
                            }
                        }
                    });
                }
                if (response.getType() == TrsProtobufs.TrsResponse.Type.ERROR) {
                    showNotification(response.getError(), Alert.AlertType.ERROR);
                }
                Platform.runLater(() -> {
                    tableViewSeats.getSelectionModel().clearSelection();
                    tableViewSeats.refresh();
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

        trsServiceStub.findAllReservationsByTheatreShow(request, responseObserver);

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private StreamObserver<TrsProtobufs.TrsResponse> getResponseObserver() {
        return new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse value) {
                Platform.runLater(() -> refreshTable());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
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
            primaryStage.setUserData(currentTheatreShow);

            stage.setOnCloseRequest(event -> primaryStage.show());

            ctrl.setParent(primaryStage);
            primaryStage.hide();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onButtonRezervaLocuriClick() {
        if (this.tableViewSeats.getSelectionModel().getSelectedItems().size() == 0) {
            showNotification("Nu ati selectat nicio rezervare!", Alert.AlertType.ERROR);
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("reservation-view.fxml"));
            Parent root = fxmlLoader.load();
            ReservationController ctrl = fxmlLoader.getController();
            ctrl.setServiceStub(this.trsServiceStub);
            ctrl.setTheatreShow(this.currentTheatreShow);
            ctrl.setSelectedSeats(this.tableViewSeats.getSelectionModel().getSelectedItems().stream().toList());
            ctrl.setObserver(this.observer);
            Scene scene = new Scene(root, 600, 300);
            Stage stage = new Stage();
            stage.setTitle("Rezerva loc");
            stage.setScene(scene);
            Stage primaryStage = (Stage) this.buttonRezervaLocuri.getScene().getWindow();
            primaryStage.setUserData(currentTheatreShow);

            stage.setOnCloseRequest(event -> primaryStage.show());

            ctrl.setParent(primaryStage);
            primaryStage.hide();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClose() {
        if (observer != null) {
            this.observer.onCompleted();
        }
    }

    private void showNotification(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setContentText(message);
        alert.showAndWait();
    }
}
