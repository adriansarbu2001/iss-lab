package trs.persistence;

import trs.model.Reservation;
import trs.model.TheatreShow;
import trs.persistence.repository.RepositoryException;

public interface IReservationRepository extends IRepository<Long, Reservation> {
    Iterable<Reservation> findAllBy(TheatreShow theatreShow) throws RepositoryException;
}
