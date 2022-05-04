package org.n52.sta.http.serialize.out;

import java.util.Collection;
import java.util.Objects;

import org.n52.sta.api.EntityPage;
import org.n52.sta.api.entity.Identifiable;

public class CollectionNode<T extends Identifiable> {

    private final EntityPage<T> page;

    private final String requestUrl;

    public CollectionNode(EntityPage<T> page, String requestUrl) {
        Objects.requireNonNull(page, "page must not be null!");
        Objects.requireNonNull(requestUrl, "requestUrl must not be null");
        this.requestUrl = requestUrl;
        this.page = page;
    }

    public long getTotalEntityCount() {
        return page.getTotalCount();
    }

    public Collection<T> getEntities() {
        return page.getEntities();
    }

    public boolean hasNextPage() {
        return page.hasNextPage();
    }

}
