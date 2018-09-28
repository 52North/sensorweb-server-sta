package org.n52.sta.data;

import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Java Bean implementation of {@code AbstractPageRequest} to support offset and
 * limit instead of page and size.
 * 
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @since 1.0.0
 *
 */
public class OffsetLimitPageRequest extends AbstractPageRequest {

    private static final long serialVersionUID = -5115884285975519825L;

    private final Sort sort;

    /**
     * Creates a new {@link OffsetLimitPageRequest}. Pages are zero indexed,
     * thus providing 0 for {@code page} will return the first page.
     * 
     * @param offset
     *            zero-based offset index.
     * @param limit
     *            the size of the page to be returned.
     */
    public OffsetLimitPageRequest(int offset, int limit) {
        this(offset, limit, Sort.unsorted());
    }

    /**
     * Creates a new {@link OffsetLimitPageRequest} with sort parameters
     * applied.
     * 
     * @param offset
     *            zero-based offset index.
     * @param limit
     *            the size of the page to be returned.
     * @param direction
     *            the direction of the {@link Sort} to be specified, can be
     *            {@literal null}.
     * @param properties
     *            the properties to sort by, must not be {@literal null} or
     *            empty.
     */
    public OffsetLimitPageRequest(int offset, int limit, Direction direction, String... properties) {
        this(offset, limit, Sort.by(direction, properties));
    }

    /**
     * Creates a new {@link OffsetLimitPageRequest} with sort parameters
     * applied.
     * 
     * @param offset
     *            zero-based offset index.
     * @param limit
     *            the size of the page to be returned.
     * @param sort
     *            can be {@literal null}.
     */
    public OffsetLimitPageRequest(int offset, int limit, Sort sort) {
        super(offset, limit);
        this.sort = sort;
    }

    @Override
    public long getOffset() {
        return super.getPageNumber();
    }

    @Override
    public int getPageNumber() {
        return (int) super.getOffset() / getPageSize();
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetLimitPageRequest((int) getOffset() + getPageSize(), getPageSize(), getSort());
    }

    @Override
    public Pageable previous() {
        return hasPrevious() ? new OffsetLimitPageRequest((int) getOffset() - getPageSize(), getPageSize(), getSort())
                : this;
    }

    @Override
    public Pageable first() {
        return new OffsetLimitPageRequest(0, getPageSize(), getSort());
    }

    @Override
    public int hashCode() {
        final int prime = 7;
        int result = super.hashCode();
        result = prime * result + sort.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            OffsetLimitPageRequest other = (OffsetLimitPageRequest) obj;
            return this.sort == other.sort;
        }
        return false;
    }

}
