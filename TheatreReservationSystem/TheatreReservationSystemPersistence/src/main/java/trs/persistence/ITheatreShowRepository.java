package trs.persistence;

import trs.model.TheatreShow;
import trs.persistence.repository.RepositoryException;

import java.time.LocalDate;

public interface ITheatreShowRepository extends IRepository<Long, TheatreShow> {
    TheatreShow findBy(LocalDate date) throws RepositoryException;
}
