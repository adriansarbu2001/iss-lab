package trs.model;

public interface IEntity<ID> {
    ID getId();
    void setId(ID id);
}
