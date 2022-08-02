package org.n52.sta.api.service;

import java.util.Optional;

import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.EntityProvider;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.api.exception.ProviderException;
import org.n52.sta.api.path.Request;

public interface EntityService<T> {

    boolean exists(String id) throws ProviderException;

    Optional<T> getEntity(Request request) throws ProviderException;

    EntityPage<T> getEntities(Request request) throws ProviderException;

    T save(T entity) throws EditorException;

    T update(T entity) throws EditorException;

    void delete(String id) throws EditorException;

    EntityProvider<?> unwrapProvider();

    EntityEditor<?> unwrapEditor();

}
