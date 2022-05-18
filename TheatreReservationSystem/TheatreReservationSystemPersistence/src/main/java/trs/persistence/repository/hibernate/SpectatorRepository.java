package trs.persistence.repository.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import trs.model.Spectator;
import trs.model.validator.IValidator;
import trs.model.validator.ValidatorException;
import trs.persistence.ISpectatorRepository;
import trs.persistence.repository.RepositoryException;

import java.util.List;

public class SpectatorRepository implements ISpectatorRepository {
    static SessionFactory sessionFactory;
    private final IValidator<Spectator> validator;
    private static final Logger logger= LogManager.getLogger();

    public SpectatorRepository(IValidator<Spectator> validator) {
        sessionFactory = SessionFactoryUtils.getSessionFactory();
        logger.info("Initializing SpectatorRepository");
        this.validator = validator;
    }

    @Override
    public Long save(Spectator spectator) throws ValidatorException, RepositoryException  {
        validator.validate(spectator);
        logger.traceEntry("saving spectator {} ", spectator);

        Long id = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                id = (Long)session.save(spectator);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Insert error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la salvarea spectatorului!");
            }
        }

        logger.traceExit();
        return id;
    }

    @Override
    public void delete(Long id) {
    }

    @Override
    public Long size() {
        return null;
    }

    @Override
    public Spectator findOne(Long id) {
        return null;
    }

    @Override
    public Iterable<Spectator> findAll() throws RepositoryException {
        logger.traceEntry();

        List<Spectator> result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("select s from Spectator s", Spectator.class).list();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find all error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la gasirea spectatorilor!");
            }
        }

        logger.traceExit();
        return result;
    }

    @Override
    public void update(Spectator seat) {
    }
}
