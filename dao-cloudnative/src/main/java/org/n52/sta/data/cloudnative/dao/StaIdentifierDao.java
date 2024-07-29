package org.n52.sta.data.cloudnative.dao;

import org.n52.sta.api.dto.StaDTO;

import javax.transaction.Transactional;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Transactional
public interface StaIdentifierDao<T extends StaDTO> extends StaEntityDao {


}
