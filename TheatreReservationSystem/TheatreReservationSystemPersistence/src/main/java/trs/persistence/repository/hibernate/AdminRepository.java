package trs.persistence.repository.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import trs.model.Admin;
import trs.model.validator.IValidator;
import trs.model.validator.ValidatorException;
import trs.persistence.IAdminRepository;
import trs.persistence.repository.RepositoryException;

import java.util.List;

public class AdminRepository implements IAdminRepository {
    static SessionFactory sessionFactory;
    private final IValidator<Admin> validator;
    private static final Logger logger= LogManager.getLogger();

    public AdminRepository(IValidator<Admin> validator) {
        sessionFactory = SessionFactoryUtils.getSessionFactory();
        logger.info("Initializing AdminRepository");
        this.validator = validator;
    }

    @Override
    public void save(Admin admin) throws ValidatorException, RepositoryException {
        validator.validate(admin);
        logger.traceEntry("saving admin {} ", admin);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(admin);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Insert error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la salvarea adminului!");
            }
        }

        logger.traceExit();
    }

    @Override
    public void delete(Long id) throws RepositoryException {
        logger.traceEntry("Removing admin {} ", id);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                Admin toDelete = new Admin();
                toDelete.setId(id);
                session.delete(toDelete);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Remove error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la stergerea adminului!");
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
                size = (Long)session.createQuery("select count(all Admin) from Admin").getSingleResult();
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
    public Admin findOne(Long id) throws RepositoryException {
        logger.traceEntry();

        Admin result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("from Admin where id = ?1", Admin.class)
                        .setParameter(1, id)
                        .uniqueResult();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find one error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la gasirea unui admin!");
            }
        }

        logger.traceExit();
        return result;
    }

    @Override
    public Iterable<Admin> findAll() throws RepositoryException {
        logger.traceEntry();

        List<Admin> result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("from Admin", Admin.class).list();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find all error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la gasirea adminilor!");
            }
        }

        logger.traceExit();
        return result;
    }

    @Override
    public void update(Admin admin) throws ValidatorException, RepositoryException {
        validator.validate(admin);
        logger.traceEntry("Updating Admin {} ", admin);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.update(admin);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Update error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la modificarea adminului!");
            }
        }

        logger.traceExit();
    }

    @Override
    public Admin findBy(String username, String password) throws RepositoryException {
        logger.traceEntry();

        Admin result = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                result = session.createQuery("from Admin where username = ?1 and password = ?2", Admin.class)
                        .setParameter(1, username)
                        .setParameter(2, password)
                        .uniqueResult();
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Find by error " + ex);
                if (tx != null)
                    tx.rollback();
                throw new RepositoryException("Eroare la gasirea adminului!");
            }
        }

        logger.traceExit();
        return result;
    }
}
