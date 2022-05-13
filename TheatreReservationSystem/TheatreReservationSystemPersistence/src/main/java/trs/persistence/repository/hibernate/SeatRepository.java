package trs.persistence.repository.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import trs.model.Seat;
import trs.model.validator.IValidator;
import trs.persistence.ISeatRepository;
import trs.persistence.repository.RepositoryException;

import java.util.List;

public class SeatRepository implements ISeatRepository {
    static SessionFactory sessionFactory;
    private final IValidator<Seat> validator;
    private static final Logger logger= LogManager.getLogger();

    public SeatRepository(IValidator<Seat> validator) {
        sessionFactory = SessionFactoryUtils.getSessionFactory();
        logger.info("Initializing SeatRepository");
        this.validator = validator;
    }

    @Override
    public void save(Seat seat) {
    }

    @Override
    public void delete(Long id) {
    }

    @Override
    public Long size() {
        return null;
    }

    @Override
    public Seat findOne(Long id) {
        return null;
    }

    @Override
    public Iterable<Seat> findAll() throws RepositoryException {
        logger.traceEntry();

        List<Seat> result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("select s from Seat s", Seat.class).list();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find all error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la gasirea locurilor!");
            }
        }

        logger.traceExit();
        return result;
    }

    @Override
    public void update(Seat seat) {
    }
}
