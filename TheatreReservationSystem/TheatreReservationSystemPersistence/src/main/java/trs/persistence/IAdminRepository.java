package trs.persistence;

import trs.model.Admin;
import trs.persistence.repository.RepositoryException;

public interface IAdminRepository extends IRepository<Long, Admin> {
    Admin findBy(String username, String password) throws RepositoryException;
}
