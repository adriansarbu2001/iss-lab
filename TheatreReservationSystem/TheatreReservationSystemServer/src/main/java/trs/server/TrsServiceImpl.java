package trs.server;

import io.grpc.stub.StreamObserver;
import trs.model.*;
import trs.model.validator.ValidatorException;
import trs.network.protobuffprotocol.TrsProtobufs;
import trs.network.protobuffprotocol.TrsServiceGrpc;
import trs.persistence.*;
import trs.persistence.repository.RepositoryException;

import java.time.LocalDate;
import java.util.*;

public class TrsServiceImpl extends TrsServiceGrpc.TrsServiceImplBase {

    private final IAdminRepository adminRepository;
    private final ITheatreShowRepository theatreShowRepository;
    private final ISeatRepository seatRepository;
    private final IReservationRepository reservationRepository;
    private final ISpectatorRepository spectatorRepository;
    private final Set<StreamObserver<TrsProtobufs.TrsResponse>> theatreShowObservers;
    private final Set<StreamObserver<TrsProtobufs.TrsResponse>> seatObservers;
    private final Set<Long> loggedUsers;

    public TrsServiceImpl(
            IAdminRepository adminRepository,
            ITheatreShowRepository theatreShowRepository,
            ISeatRepository seatRepository,
            IReservationRepository reservationRepository,
            ISpectatorRepository spectatorRepository
    ) {
        this.adminRepository = adminRepository;
        this.theatreShowRepository = theatreShowRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.spectatorRepository = spectatorRepository;
        theatreShowObservers = new LinkedHashSet<>();
        seatObservers = new LinkedHashSet<>();
        loggedUsers = new LinkedHashSet<>();
    }

    @Override
    public StreamObserver<TrsProtobufs.TrsRequest> addTheatreShowObserver(StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        theatreShowObservers.add(responseObserver);
        System.out.println("Theatre show observer added\n");

        return new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsRequest request) {
                for (StreamObserver<TrsProtobufs.TrsResponse> observer : theatreShowObservers) {
                    observer.onNext(TrsProtobufs.TrsResponse.newBuilder().setType(TrsProtobufs.TrsResponse.Type.RELOAD_THEATRE_SHOWS).build());
                }
            }

            @Override
            public void onError(Throwable t) {
                theatreShowObservers.remove(responseObserver);
                responseObserver.onError(t);
                System.out.println("Theatre show observer removed\n");
            }

            @Override
            public void onCompleted() {
                theatreShowObservers.remove(responseObserver);
                responseObserver.onCompleted();
                System.out.println("Theatre show observer removed\n");
            }
        };
    }

    @Override
    public StreamObserver<TrsProtobufs.TrsRequest> addSeatObserver(StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        seatObservers.add(responseObserver);
        System.out.println("Seat observer added\n");

        return new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsRequest request) {
                for (StreamObserver<TrsProtobufs.TrsResponse> observer : seatObservers) {
                    observer.onNext(TrsProtobufs.TrsResponse.newBuilder().setType(TrsProtobufs.TrsResponse.Type.RELOAD_SEATS).build());
                }
            }

            @Override
            public void onError(Throwable t) {
                seatObservers.remove(responseObserver);
                responseObserver.onError(t);
                System.out.println("Seat observer removed\n");
            }

            @Override
            public void onCompleted() {
                seatObservers.remove(responseObserver);
                responseObserver.onCompleted();
                System.out.println("Seat observer removed\n");
            }
        };
    }

    @Override
    public void login(TrsProtobufs.TrsRequest request, StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        System.out.println("Am primit request LOGIN");
        TrsProtobufs.AdminDto adminDto = request.getAdminDto();
        TrsProtobufs.TrsResponse response;
        try {
            Admin admin = adminRepository.findBy(adminDto.getUsername(), adminDto.getPassword());
            if (admin != null) {
                if (loggedUsers.contains(admin.getId())) {
                    response = TrsProtobufs.TrsResponse.newBuilder()
                            .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                            .setError("User already logged in.")
                            .build();
                } else {
                    response = TrsProtobufs.TrsResponse.newBuilder()
                            .setType(TrsProtobufs.TrsResponse.Type.OK)
                            .setAdminDto(DtoUtils.fromAdmin(admin))
                            .build();
                    loggedUsers.add(admin.getId());
                }

            } else {
                response = TrsProtobufs.TrsResponse.newBuilder()
                        .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                        .setError("Authentication failed.")
                        .build();

            }
        } catch (RepositoryException ex) {
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                    .setError(ex.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void logout(TrsProtobufs.TrsRequest request, StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        System.out.println("Am primit request LOGOUT");
        TrsProtobufs.AdminDto adminDto = request.getAdminDto();
        TrsProtobufs.TrsResponse response;
        try {
            Admin admin = adminRepository.findBy(adminDto.getUsername(), adminDto.getPassword());
            if (loggedUsers.remove(admin.getId())) {
                response = TrsProtobufs.TrsResponse.newBuilder()
                        .setType(TrsProtobufs.TrsResponse.Type.OK)
                        .build();
            } else {
                response = TrsProtobufs.TrsResponse.newBuilder()
                        .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                        .setError("User " + admin.getId() + " is not logged in.")
                        .build();
            }
        } catch (RepositoryException ex) {
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                    .setError(ex.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void findAllTheatreShow(TrsProtobufs.TrsRequest request, StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        System.out.println("Am primit request FIND_ALL_THEATRE_SHOW");
        List<TrsProtobufs.TheatreShowDto> all = new ArrayList<>();
        TrsProtobufs.TrsResponse response;
        try {
            for (TheatreShow ts : theatreShowRepository.findAll()) {
                all.add(DtoUtils.fromTheatreShow(ts));
            }
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.OK)
                    .addAllTheatreShowDtos(all)
                    .build();
        } catch (RepositoryException ex) {
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                    .setError(ex.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void findAllSeats(TrsProtobufs.TrsRequest request, StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        System.out.println("Am primit request FIND_ALL_SEATS");
        List<TrsProtobufs.SeatDto> all = new ArrayList<>();
        TrsProtobufs.TrsResponse response;
        try {
            for (Seat seat : seatRepository.findAll()) {
                all.add(DtoUtils.fromSeat(seat));
            }
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.OK)
                    .addAllSeatDtos(all)
                    .build();
        } catch (RepositoryException ex) {
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                    .setError(ex.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void findAllReservationsByTheatreShow(TrsProtobufs.TrsRequest request, StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        System.out.println("Am primit request FIND_ALL_RESERVATIONS_BY_THEATRE_SHOW");
        List<TrsProtobufs.ReservationDto> all = new ArrayList<>();
        TrsProtobufs.TrsResponse response;
        try {
            for (Reservation reservation : reservationRepository.findAllBy(DtoUtils.fromTheatreShowDto(request.getTheatreShowDto()))) {
                all.add(DtoUtils.fromReservation(reservation));
            }
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.OK)
                    .addAllReservationDtos(all)
                    .build();
        } catch (RepositoryException ex) {
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                    .setError(ex.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void findTodaysTheatreShow(TrsProtobufs.TrsRequest request, StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        System.out.println("Am primit request FIND_TODAYS_THEATRE_SHOW");
        TrsProtobufs.TrsResponse response;
        try {
            TheatreShow theatreShow = theatreShowRepository.findBy(LocalDate.now());
            if (theatreShow != null) {
                response = TrsProtobufs.TrsResponse.newBuilder()
                        .setType(TrsProtobufs.TrsResponse.Type.OK)
                        .setTheatreShowDto(DtoUtils.fromTheatreShow(theatreShow))
                        .build();

            } else {
                response = TrsProtobufs.TrsResponse.newBuilder()
                        .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                        .setError("Nu s-a gasit niciun spectacol de teatru azi!")
                        .build();

            }
        } catch (RepositoryException ex) {
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                    .setError(ex.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void saveSpectator(TrsProtobufs.TrsRequest request, StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        System.out.println("Am primit request SAVE_SPECTATOR");

        Spectator spectator = DtoUtils.fromSpectatorDto(request.getSpectatorDto());
        TrsProtobufs.TrsResponse response;
        try {
            Long id = spectatorRepository.save(spectator);
            spectator.setId(id);
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.OK)
                    .setSpectatorDto(DtoUtils.fromSpectator(spectator))
                    .build();
        } catch (ValidatorException | RepositoryException ex) {
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                    .setError(ex.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<TrsProtobufs.TrsRequest> saveReservation(StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        System.out.println("Am primit request SAVE_RESERVATION");
        return new StreamObserver<>() {
            String errors = "";
            int count = 0;
            @Override
            public void onNext(TrsProtobufs.TrsRequest request) {
                try {
                    reservationRepository.save(DtoUtils.fromReservationDto(request.getReservationDto()));
                } catch (ValidatorException | RepositoryException e) {
                    count++;
                    if (count > 1) {
                        errors = "Eroare la salvarea rezervarilor!";
                    } else {
                        errors = e.getMessage();
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                TrsProtobufs.TrsResponse response;
                if (Objects.equals(errors, "")) {
                    response = TrsProtobufs.TrsResponse.newBuilder()
                            .setType(TrsProtobufs.TrsResponse.Type.OK)
                            .build();
                } else {
                    response = TrsProtobufs.TrsResponse.newBuilder()
                            .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                            .setError(errors)
                            .build();
                }
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void saveTheatreShow(TrsProtobufs.TrsRequest request, StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        System.out.println("Am primit request SAVE_THEATRE_SHOW");
        TheatreShow theatreShow = DtoUtils.fromTheatreShowDto(request.getTheatreShowDto());
        TrsProtobufs.TrsResponse response;
        try {
            theatreShowRepository.save(theatreShow);
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.OK)
                    .build();
        } catch (ValidatorException | RepositoryException ex) {
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                    .setError(ex.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void updateTheatreShow(TrsProtobufs.TrsRequest request, StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        System.out.println("Am primit request UPDATE_THEATRE_SHOW");
        TrsProtobufs.TheatreShowDto tsd = request.getTheatreShowDto();
        TheatreShow theatreShow = DtoUtils.fromTheatreShowDto(tsd);
        TrsProtobufs.TrsResponse response;
        try {
            theatreShowRepository.update(theatreShow);
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.OK)
                    .build();
        } catch (ValidatorException | RepositoryException ex) {
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                    .setError(ex.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }

    @Override
    public void deleteTheatreShow(TrsProtobufs.TrsRequest request, StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        System.out.println("Am primit request DELETE_THEATRE_SHOW");
        long id = request.getId();
        TrsProtobufs.TrsResponse response;
        try {
            theatreShowRepository.delete(id);
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.OK)
                    .build();
        } catch (RepositoryException ex) {
            response = TrsProtobufs.TrsResponse.newBuilder()
                    .setType(TrsProtobufs.TrsResponse.Type.ERROR)
                    .setError(ex.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        System.out.println("Am trimis raspuns " + response.getType() + "\n");
        responseObserver.onCompleted();
    }
}
