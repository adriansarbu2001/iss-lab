package trs.model;

public class Admin implements IEntity<Long> {
    private long adminId;
    private String username;
    private String password;

    public Admin(long adminId, String username, String password) {
        this.adminId = adminId;
        this.username = username;
        this.password = password;
    }

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Admin() {

    }

    @Override
    public Long getId() {
        return adminId;
    }

    @Override
    public void setId(Long id) {
        this.adminId = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
