package trs.persistence.repository.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import trs.model.Reservation;
import trs.model.TheatreShow;
import trs.model.validator.IValidator;
import trs.model.validator.ValidatorException;
import trs.persistence.IReservationRepository;
import trs.persistence.repository.RepositoryException;

import java.util.List;

public class ReservationRepository implements IReservationRepository {
    static SessionFactory sessionFactory;
    private final IValidator<Reservation> validator;
    private static final Logger logger= LogManager.getLogger();

    public ReservationRepository(IValidator<Reservation> validator) {
        sessionFactory = SessionFactoryUtils.getSessionFactory();
        logger.info("Initializing ReservationRepository");
        this.validator = validator;
    }

    @Override
    public Long save(Reservation reservation) throws ValidatorException, RepositoryException {
        validator.validate(reservation);
        logger.traceEntry("saving reservation {} ", reservation);

        Reservation res = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                res = (Reservation) session.save(reservation);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Insert error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la salvarea rezervarii!");
            }
        }

        logger.traceExit();
        return res.getId();
    }

    @Override
    public void delete(Long id) {
    }

    @Override
    public Long size() {
        return null;
    }

    @Override
    public Reservation findOne(Long id) {
        return null;
    }

    @Override
    public Iterable<Reservation> findAll() throws RepositoryException {
        return null;
    }

    @Override
    public void update(Reservation seat) {
    }

    @Override
    public Iterable<Reservation> findAllBy(TheatreShow theatreShow) throws RepositoryException {
        logger.traceEntry();

        List<Reservation> result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery(
                        "select r from Reservation r " +
                                "join fetch r.theatreShow ts " +
                                "join fetch ts.admin " +
                                "join fetch r.seat " +
                                "join fetch r.spectator " +
                                "where r.theatreShow = ?1",
                                Reservation.class)
                        .setParameter(1, theatreShow)
                        .list();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find all error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la gasirea rezervarilor!");
            }
        }

        logger.traceExit();
        return result;
    }
}
