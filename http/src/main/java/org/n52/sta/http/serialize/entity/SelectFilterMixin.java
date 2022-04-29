package org.n52.sta.http.serialize.entity;

import com.fasterxml.jackson.annotation.JsonFilter;
import org.n52.sta.http.controller.ReadController;

@JsonFilter(ReadController.SELECT_FILTER)
public class SelectFilterMixin {
}
