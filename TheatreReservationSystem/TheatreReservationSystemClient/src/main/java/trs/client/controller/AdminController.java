package trs.client.controller;

import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import trs.client.DtoUtils;
import trs.model.TheatreShow;
import trs.network.protobuffprotocol.TrsProtobufs;
import trs.network.protobuffprotocol.TrsServiceGrpc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AdminController {
    private TrsServiceGrpc.TrsServiceStub trsServiceStub;
    private Stage parent;
    private TrsProtobufs.AdminDto adminDto;
    private StreamObserver<TrsProtobufs.TrsRequest> observer = null;

    public ObservableList<TheatreShow> modelTheatreShow = FXCollections.observableArrayList();
    @FXML
    public TableView<TheatreShow> tableViewTheatreShows;
    @FXML
    public TableColumn<TheatreShow, Long> tableColumnIdSpectacol;
    @FXML
    public TableColumn<TheatreShow, String> tableColumnNume;
    @FXML
    public TableColumn<TheatreShow, LocalDate> tableColumnData;
    @FXML
    public TextField textFieldIdSpectacol;
    @FXML
    public TextField textFieldNume;
    @FXML
    public DatePicker datePickerData;
    @FXML
    public Button buttonLogOut;

    @FXML
    public void initialize() {
        initializeTableViewTheatreShows();
    }

    private void initializeTableViewTheatreShows() {
        tableColumnIdSpectacol.setCellValueFactory(new PropertyValueFactory<>("Id"));
        tableColumnNume.setCellValueFactory(new PropertyValueFactory<>("Name"));
        tableColumnData.setCellValueFactory(new PropertyValueFactory<>("Date"));
        tableViewTheatreShows.setItems(modelTheatreShow);
        tableViewTheatreShows.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                textFieldIdSpectacol.setText(String.valueOf(newSelection.getId()));
                textFieldNume.setText(newSelection.getName());
                datePickerData.setValue(newSelection.getDate());
            }
        });
    }

    public void setParent(Stage parent) {
        this.parent = parent;
    }

    public void setServiceStub(TrsServiceGrpc.TrsServiceStub trsServiceStub) {
        this.trsServiceStub = trsServiceStub;
        observer = trsServiceStub.addTheatreShowObserver(getResponseObserver());
        refreshModel();
    }

    public void setAdminDto(TrsProtobufs.AdminDto adminDto) {
        this.adminDto = adminDto;
    }

    public void logout() {
        if (observer != null) {
            this.observer.onCompleted();
        }

        TrsProtobufs.TrsRequest request = TrsProtobufs.TrsRequest.newBuilder()
                .setAdminDto(this.adminDto)
                .build();
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<TrsProtobufs.TrsResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse response) {
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

        trsServiceStub.logout(request, responseObserver);

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        textFieldIdSpectacol.clear();
        textFieldNume.clear();
        datePickerData.setValue(null);
    }

    private void refreshModel() {
        TrsProtobufs.TrsRequest request = TrsProtobufs.TrsRequest.newBuilder()
                .build();
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<TrsProtobufs.TrsResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse response) {
                if (response.getType() == TrsProtobufs.TrsResponse.Type.OK) {
                    List<TrsProtobufs.TheatreShowDto> dtos = response.getTheatreShowDtosList();
                    List<TheatreShow> all = new ArrayList<>();
                    for (var dto : dtos) {
                        all.add(DtoUtils.fromTheatreShowDto(dto));
                    }
                    modelTheatreShow.setAll(all);
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

        trsServiceStub.findAllTheatreShow(request, responseObserver);

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
                refreshModel();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    public void onButtonLogOutClick() {
        logout();
        ((Stage) buttonLogOut.getScene().getWindow()).close();
        parent.show();
    }

    public void onButtonAdaugaClick() {
        String name = textFieldNume.getText();
        LocalDate date = datePickerData.getValue();

        if (date == null) {
            showNotification("Data invalida!", Alert.AlertType.ERROR);
            return;
        }

        TrsProtobufs.TheatreShowDto theatreShowDto = TrsProtobufs.TheatreShowDto.newBuilder()
                .setName(name)
                .setDateDto(DtoUtils.fromDate(date))
                .setAdminDto(this.adminDto)
                .build();

        TrsProtobufs.TrsRequest request = TrsProtobufs.TrsRequest.newBuilder()
                .setTheatreShowDto(theatreShowDto)
                .build();

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<TrsProtobufs.TrsResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse response) {
                if (response.getType() == TrsProtobufs.TrsResponse.Type.ERROR) {
                    Platform.runLater(() -> {
                        showNotification(response.getError(), Alert.AlertType.ERROR);
                    });
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

        trsServiceStub.saveTheatreShow(request, responseObserver);

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        observer.onNext(null);
    }

    public void onButtonModificaClick() {
        String id = textFieldIdSpectacol.getText();
        String name = textFieldNume.getText();
        LocalDate date = datePickerData.getValue();

        if (date == null) {
            showNotification("Data invalida!", Alert.AlertType.ERROR);
            return;
        }

        TrsProtobufs.TheatreShowDto theatreShowDto;
        try {
            theatreShowDto = TrsProtobufs.TheatreShowDto.newBuilder()
                    .setId(Long.parseLong(id))
                    .setName(name)
                    .setDateDto(DtoUtils.fromDate(date))
                    .setAdminDto(this.adminDto)
                    .build();
        } catch (NumberFormatException ex) {
            showNotification("ID-ul trebuie sa fie numar!", Alert.AlertType.ERROR);
            return;
        }

        TrsProtobufs.TrsRequest request = TrsProtobufs.TrsRequest.newBuilder()
                .setTheatreShowDto(theatreShowDto)
                .build();

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<TrsProtobufs.TrsResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse response) {
                if (response.getType() == TrsProtobufs.TrsResponse.Type.ERROR) {
                    Platform.runLater(() -> {
                        showNotification(response.getError(), Alert.AlertType.ERROR);
                    });
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

        trsServiceStub.updateTheatreShow(request, responseObserver);

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        observer.onNext(null);
    }

    public void onButtonStergeClick() {
        String id = textFieldIdSpectacol.getText();

        if (id.equals("")) {
            showNotification("ID-ul trebuie sa fie numar!", Alert.AlertType.ERROR);
            return;
        }

        TrsProtobufs.TrsRequest request;
        try {
            request = TrsProtobufs.TrsRequest.newBuilder()
                    .setId(Long.parseLong(id))
                    .build();
        } catch (NumberFormatException ex) {
            showNotification("ID-ul trebuie sa fie numar!", Alert.AlertType.ERROR);
            return;
        }

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<TrsProtobufs.TrsResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse response) {
                if (response.getType() == TrsProtobufs.TrsResponse.Type.OK) {
                    Platform.runLater(() -> {
                        clearFields();
                    });
                }
                if (response.getType() == TrsProtobufs.TrsResponse.Type.ERROR) {
                    Platform.runLater(() -> {
                        showNotification(response.getError(), Alert.AlertType.ERROR);
                    });
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

        trsServiceStub.deleteTheatreShow(request, responseObserver);

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        observer.onNext(null);
    }

    private void showNotification(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setContentText(message);
        alert.showAndWait();
    }
}
