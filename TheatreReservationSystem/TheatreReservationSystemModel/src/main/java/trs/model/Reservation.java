package trs.model;

public class Reservation implements IEntity<Long> {
    private long reservationId;
    private TheatreShow theatreShow;
    private Seat seat;
    private Spectator spectator;

    public Reservation(long reservationId, TheatreShow theatreShow, Seat seat, Spectator spectator) {
        this.reservationId = reservationId;
        this.theatreShow = theatreShow;
        this.seat = seat;
        this.spectator = spectator;
    }

    public Reservation(TheatreShow theatreShow, Seat seat, Spectator spectator) {
        this.theatreShow = theatreShow;
        this.seat = seat;
        this.spectator = spectator;
    }

    public Reservation() {
    }

    @Override
    public Long getId() {
        return reservationId;
    }

    @Override
    public void setId(Long id) {
        this.reservationId = id;
    }

    public TheatreShow getTheatreShow() {
        return theatreShow;
    }

    public void setTheatreShow(TheatreShow theatreShow) {
        this.theatreShow = theatreShow;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public Spectator getSpectator() {
        return spectator;
    }

    public void setSpectator(Spectator spectator) {
        this.spectator = spectator;
    }
}
