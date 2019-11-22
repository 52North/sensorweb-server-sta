package org.n52.sta.serdes.json;

public interface AbstractJSONEntity {

    String INVALID_REFERENCED_ENTITY =
            "Invalid Entity. Only @iot.id may be present when referencing an existing entity!";

    String INVALID_INLINE_ENTITY =
            "Invalid Entity. Not all required properties present! Missing: ";

    String INVALID_BACKREFERENCE =
            "Invalid nesting of Entites!";
}
