package trs.persistence.repository.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import trs.model.TheatreShow;
import trs.model.validator.IValidator;
import trs.model.validator.ValidatorException;
import trs.persistence.ITheatreShowRepository;
import trs.persistence.repository.RepositoryException;

import java.time.LocalDate;
import java.util.List;

public class TheatreShowRepository implements ITheatreShowRepository {
    static SessionFactory sessionFactory;
    private final IValidator<TheatreShow> validator;
    private static final Logger logger= LogManager.getLogger();

    public TheatreShowRepository(IValidator<TheatreShow> validator) {
        sessionFactory = SessionFactoryUtils.getSessionFactory();
        logger.info("Initializing TheatreShowRepository");
        this.validator = validator;
    }

    @Override
    public Long save(TheatreShow theatreShow) throws ValidatorException, RepositoryException {
        validator.validate(theatreShow);
        logger.traceEntry("saving theatreShow {} ", theatreShow);

        Long id = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                id = (Long)session.save(theatreShow);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Insert error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la salvarea spectacolului!\nHint: Probabil ai adaugat 2 spectacole cu aceeasi data!");
            }
        }

        logger.traceExit();
        return id;
    }

    @Override
    public void delete(Long id) throws RepositoryException {
        logger.traceEntry("Removing theatreShow {} ", id);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                TheatreShow toDelete = new TheatreShow();
                toDelete.setId(id);
                session.delete(toDelete);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Remove error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la stergerea spectacolului!");
            }
        }

        logger.traceExit();
    }

    @Override
    public Long size() throws RepositoryException {
        logger.traceEntry();

        Long size = -1L;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                size = (Long)session.createQuery("select count(all TheatreShow) from TheatreShow").getSingleResult();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Size error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la citirea lungimii!");
            }
        }

        logger.traceExit();
        return size;
    }

    @Override
    public TheatreShow findOne(Long id) throws RepositoryException {
        logger.traceEntry();

        TheatreShow result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("select ts from TheatreShow ts join fetch ts.admin where ts.id = ?1", TheatreShow.class)
                        .setParameter(1, id)
                        .uniqueResult();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find one error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la gasirea spectacolului!");
            }
        }

        logger.traceExit();
        return result;
    }

    @Override
    public Iterable<TheatreShow> findAll() throws RepositoryException {
        logger.traceEntry();

        List<TheatreShow> result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("select ts from TheatreShow ts join fetch ts.admin", TheatreShow.class).list();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find all error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la gasirea spectacolelor!");
            }
        }

        logger.traceExit();
        return result;
    }

    @Override
    public void update(TheatreShow theatreShow) throws ValidatorException, RepositoryException {
        validator.validate(theatreShow);
        logger.traceEntry("Updating theatreShow {} ", theatreShow);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.update(theatreShow);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Update error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la modificarea spectacolului!");
            }
        }

        logger.traceExit();
    }

    @Override
    public TheatreShow findBy(LocalDate date) throws RepositoryException {
        logger.traceEntry();

        TheatreShow result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("select ts from TheatreShow ts join fetch ts.admin where ts.date = ?1", TheatreShow.class)
                        .setParameter(1, date)
                        .uniqueResult();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find one error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la gasirea spectacolului!");
            }
        }

        logger.traceExit();
        return result;
    }
}
