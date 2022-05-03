package trs.model;

import java.time.LocalDate;
import java.util.Date;

public class TheatreShow implements IEntity<Long> {
    private long theatreShowId;
    private String name;
    private LocalDate date;
    private Admin admin;

    public TheatreShow(long theatreShowId, String name, LocalDate date, Admin admin) {
        this.theatreShowId = theatreShowId;
        this.name = name;
        this.date = date;
        this.admin = admin;
    }

    public TheatreShow(String name, LocalDate date, Admin admin) {
        this.name = name;
        this.date = date;
        this.admin = admin;
    }

    public TheatreShow() {

    }

    @Override
    public Long getId() {
        return theatreShowId;
    }

    @Override
    public void setId(Long id) {
        this.theatreShowId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }
}
