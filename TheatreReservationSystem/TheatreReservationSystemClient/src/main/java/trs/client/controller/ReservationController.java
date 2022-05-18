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
import trs.client.DtoUtils;
import trs.model.Seat;
import trs.model.Spectator;
import trs.model.TheatreShow;
import trs.network.protobuffprotocol.TrsProtobufs;
import trs.network.protobuffprotocol.TrsServiceGrpc;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ReservationController {
    private Spectator spectator;
    private TheatreShow currentTheatreSHow;
    private List<Seat> selectedSeats;
    private StreamObserver<TrsProtobufs.TrsRequest> observer;
    private TrsServiceGrpc.TrsServiceStub trsServiceStub;
    Stage parent;

    @FXML
    public Button buttonRezervaLocuri;

    @FXML
    public TextField textFieldNume;

    @FXML
    public void initialize() {

    }

    public void setParent(Stage parent) {
        this.parent = parent;
    }

    public void setServiceStub(TrsServiceGrpc.TrsServiceStub trsServiceStub) {
        this.trsServiceStub = trsServiceStub;
    }

    public void setTheatreShow(TheatreShow theatreShow) {
        this.currentTheatreSHow = theatreShow;
    }

    public void setSelectedSeats(List<Seat> selectedSeats) {
        this.selectedSeats = selectedSeats;
    }

    public void setObserver(StreamObserver<TrsProtobufs.TrsRequest> observer) {
        this.observer = observer;
    }

    public void onButtonRezervaLocuriClick() {
        if (this.currentTheatreSHow == null) return;

        var spectatorToAdd = TrsProtobufs.SpectatorDto.newBuilder()
                .setName(textFieldNume.getText())
                .build();

        var requestSpectator = TrsProtobufs.TrsRequest.newBuilder()
                .setSpectatorDto(spectatorToAdd)
                .build();

        final CountDownLatch finishLatch1 = new CountDownLatch(1);

        StreamObserver<TrsProtobufs.TrsResponse> responseObserverSpectator = new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse response) {
                if (response.getType() == TrsProtobufs.TrsResponse.Type.OK) {
                    spectator = DtoUtils.fromSpectatorDto(response.getSpectatorDto());
                }
                if (response.getType() == TrsProtobufs.TrsResponse.Type.ERROR) {
                    Platform.runLater(() -> showNotification(response.getError(), Alert.AlertType.ERROR));
                }
            }

            @Override
            public void onError(Throwable t) {
                finishLatch1.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch1.countDown();
            }
        };

        this.trsServiceStub.saveSpectator(requestSpectator, responseObserverSpectator);

        try {
            finishLatch1.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (this.spectator == null) return;

        final CountDownLatch finishLatch2 = new CountDownLatch(1);

        StreamObserver<TrsProtobufs.TrsResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsResponse response) {
                if (response.getType() == TrsProtobufs.TrsResponse.Type.ERROR) {
                    Platform.runLater(() -> showNotification(response.getError(), Alert.AlertType.ERROR));
                }
            }

            @Override
            public void onError(Throwable t) {
                finishLatch2.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch2.countDown();
            }
        };

        StreamObserver<TrsProtobufs.TrsRequest> requestObserver = trsServiceStub.saveReservation(responseObserver);

        for (var seat : this.selectedSeats) {
            var reservationDto = TrsProtobufs.ReservationDto.newBuilder()
                    .setTheatreShowDto(DtoUtils.fromTheatreShow(this.currentTheatreSHow))
                    .setSeatDto(DtoUtils.fromSeat(seat))
                    .setSpectatorDto(DtoUtils.fromSpectator(this.spectator))
                    .build();
            var requestReservation = TrsProtobufs.TrsRequest.newBuilder()
                    .setReservationDto(reservationDto)
                    .build();
            requestObserver.onNext(requestReservation);
        }
        requestObserver.onCompleted();

        try {
            finishLatch2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        observer.onNext(null);
        ((Stage) buttonRezervaLocuri.getScene().getWindow()).close();
        parent.show();
    }

    private void showNotification(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setContentText(message);
        alert.showAndWait();
    }
}
