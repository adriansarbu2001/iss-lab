package trs.client;

import trs.model.Admin;
import trs.model.TheatreShow;
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
}
