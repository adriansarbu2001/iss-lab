package trs.persistence;

import trs.model.IEntity;
import trs.model.validator.ValidatorException;
import trs.persistence.repository.RepositoryException;

public interface IRepository<ID, T extends IEntity<ID>> {

    ID save(T t) throws ValidatorException, RepositoryException;

    void delete(ID id) throws RepositoryException;

    Long size() throws RepositoryException;

    T findOne(ID id) throws RepositoryException;

    Iterable<T> findAll() throws RepositoryException;

    void update(T t) throws ValidatorException, RepositoryException;
}
