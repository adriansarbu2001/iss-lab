package trs.model;

public class Spectator implements IEntity<Long> {
    private long spectatorId;
    private String name;

    public Spectator(long spectatorId, String name) {
        this.spectatorId = spectatorId;
        this.name = name;
    }

    public Spectator(String name) {
        this.name = name;
    }

    public Spectator() {
    }

    @Override
    public Long getId() {
        return spectatorId;
    }

    @Override
    public void setId(Long id) {
        this.spectatorId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
