package trs.server;

import trs.model.*;
import trs.network.protobuffprotocol.TrsProtobufs;

import java.time.LocalDate;

public class DtoUtils {
    public static TrsProtobufs.AdminDto fromAdmin(Admin admin) {
        return TrsProtobufs.AdminDto.newBuilder()
                .setId(admin.getId())
                .setUsername(admin.getUsername())
                .setPassword(admin.getPassword())
                .build();
    }

    public static Admin fromAdminDto(TrsProtobufs.AdminDto adminDto) {
        return new Admin(adminDto.getId(), adminDto.getUsername(), adminDto.getPassword());
    }

    public static TrsProtobufs.DateDto fromDate(LocalDate date) {
        return TrsProtobufs.DateDto.newBuilder()
                .setDay(date.getDayOfMonth())
                .setMonth(date.getMonthValue())
                .setYear(date.getYear())
                .build();
    }

    public static LocalDate fromDateDto(TrsProtobufs.DateDto dateDto) {
        return LocalDate.of(dateDto.getYear(), dateDto.getMonth(), dateDto.getDay());
    }

    public static TrsProtobufs.TheatreShowDto fromTheatreShow(TheatreShow theatreShow) {
        return TrsProtobufs.TheatreShowDto.newBuilder()
                .setId(theatreShow.getId())
                .setName(theatreShow.getName())
                .setDateDto(fromDate(theatreShow.getDate()))
                .setAdminDto(fromAdmin(theatreShow.getAdmin()))
                .build();
    }

    public static TheatreShow fromTheatreShowDto(TrsProtobufs.TheatreShowDto theatreShowDto) {
        return new TheatreShow(
                theatreShowDto.getId(),
                theatreShowDto.getName(),
                fromDateDto(theatreShowDto.getDateDto()),
                fromAdminDto(theatreShowDto.getAdminDto()));
    }

    public static TrsProtobufs.SeatDto fromSeat(Seat seat) {
        return TrsProtobufs.SeatDto.newBuilder()
                .setId(seat.getId())
                .setLodge(seat.getLodge())
                .setRow(seat.getRow())
                .setNumber(seat.getNumber())
                .setPrice(seat.getPrice())
                .build();
    }

    public static Seat fromSeatDto(TrsProtobufs.SeatDto seatDto) {
        return new Seat(
                seatDto.getId(),
                seatDto.getLodge(),
                seatDto.getRow(),
                seatDto.getNumber(),
                seatDto.getPrice());
    }

    public static TrsProtobufs.SpectatorDto fromSpectator(Spectator spectator) {
        return TrsProtobufs.SpectatorDto.newBuilder()
                .setId(spectator.getId())
                .setName(spectator.getName())
                .build();
    }

    public static Spectator fromSpectatorDto(TrsProtobufs.SpectatorDto spectatorDto) {
        return new Spectator(
                spectatorDto.getId(),
                spectatorDto.getName());
    }

    public static TrsProtobufs.ReservationDto fromReservation(Reservation reservation) {
        return TrsProtobufs.ReservationDto.newBuilder()
                .setId(reservation.getId())
                .setTheatreShowDto(fromTheatreShow(reservation.getTheatreShow()))
                .setSeatDto(fromSeat(reservation.getSeat()))
                .setSpectatorDto(fromSpectator(reservation.getSpectator()))
                .build();
    }

    public static Reservation fromReservationDto(TrsProtobufs.ReservationDto reservationDto) {
        return new Reservation(
                reservationDto.getId(),
                fromTheatreShowDto(reservationDto.getTheatreShowDto()),
                fromSeatDto(reservationDto.getSeatDto()),
                fromSpectatorDto(reservationDto.getSpectatorDto()));
    }
}
