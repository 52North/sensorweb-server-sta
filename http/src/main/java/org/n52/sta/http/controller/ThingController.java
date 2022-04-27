package org.n52.sta.http.controller;

import org.n52.sta.api.EntityPage;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.service.EntityService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class ThingController {

    private final EntityService<Thing> entityService;

    public ThingController(EntityService<Thing> entityService) {
        this.entityService = entityService;
    }
    
    @GetMapping(path = "/greet", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityPage<Thing> greet() {
        return entityService.getEntities();
    }
}
