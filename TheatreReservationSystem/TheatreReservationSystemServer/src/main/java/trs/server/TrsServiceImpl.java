package trs.server;

import io.grpc.stub.StreamObserver;
import trs.model.Admin;
import trs.model.TheatreShow;
import trs.model.validator.ValidatorException;
import trs.network.protobuffprotocol.TrsProtobufs;
import trs.network.protobuffprotocol.TrsServiceGrpc;
import trs.persistence.IAdminRepository;
import trs.persistence.ITheatreShowRepository;
import trs.persistence.repository.RepositoryException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TrsServiceImpl extends TrsServiceGrpc.TrsServiceImplBase {
    private final IAdminRepository adminRepository;
    private final ITheatreShowRepository theatreShowRepository;
    private final Set<StreamObserver<TrsProtobufs.TrsResponse>> observers;
    private final Set<Long> loggedUsers;

    public TrsServiceImpl(IAdminRepository adminRepository, ITheatreShowRepository theatreShowRepository) {
        this.adminRepository = adminRepository;
        this.theatreShowRepository = theatreShowRepository;
        observers = new LinkedHashSet<>();
        loggedUsers = new LinkedHashSet<>();
    }

    @Override
    public StreamObserver<TrsProtobufs.TrsRequest> addTheatreShowObserver(StreamObserver<TrsProtobufs.TrsResponse> responseObserver) {
        observers.add(responseObserver);

        return new StreamObserver<>() {
            @Override
            public void onNext(TrsProtobufs.TrsRequest request) {
                for (StreamObserver<TrsProtobufs.TrsResponse> observer : observers) {
                    observer.onNext(TrsProtobufs.TrsResponse.newBuilder().setType(TrsProtobufs.TrsResponse.Type.RELOAD_THEATRE_SHOWS).build());
                }
            }

            @Override
            public void onError(Throwable t) {
                observers.remove(responseObserver);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                observers.remove(responseObserver);
                responseObserver.onCompleted();
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
