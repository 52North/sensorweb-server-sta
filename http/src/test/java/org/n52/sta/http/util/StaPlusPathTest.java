/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta.http.util;

import java.util.List;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.n52.grammar.StaPlusPathGrammar;
import org.n52.grammar.StaPlusPathLexer;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.path.PathSegment;
import org.n52.sta.api.path.SelectPath;
import org.n52.sta.http.util.path.PathFactory;
import org.n52.sta.http.util.path.StaPath;
import org.n52.sta.http.util.path.StaPlusPathVisitor;

public class StaPlusPathTest {

    private PathFactory factory;

    @BeforeEach
    public void setUp() {
        this.factory = new PathFactory(StaPlusPathGrammar::new, StaPlusPathVisitor::new, StaPlusPathLexer::new);
    }

    @Test
    public void assert_observations_path_is_a_collection() throws STAInvalidUrlException {
        StaPath<? extends Identifiable> path = factory.parse("/Observations");
        MatcherAssert.assertThat(path.getPathType(), is(SelectPath.PathType.collection));
    }

    @Test
    public void assert_relations_path_is_a_collection() throws STAInvalidUrlException {
        StaPath<? extends Identifiable> path = factory.parse("/Relations");
        MatcherAssert.assertThat(path.getPathType(), is(SelectPath.PathType.collection));
    }

    @Test
    public void assert_groups_has_member_relations() throws STAInvalidUrlException {
        StaPath<? extends Identifiable> path = factory.parse("/Groups(sdf)/Relations");
        MatcherAssert.assertThat(path.getPathType(), is(SelectPath.PathType.collection));
        List<PathSegment> segments = path.getPathSegments();
        MatcherAssert.assertThat(segments.size(), is(2));

        PathSegment relations = segments.get(0);
        MatcherAssert.assertThat(relations.getCollection(), is("Relations"));
        MatcherAssert.assertThat(relations.getIdentifier(), is(Optional.empty()));
        MatcherAssert.assertThat(relations.getProperty(), is(Optional.empty()));

        PathSegment group = segments.get(1);
        MatcherAssert.assertThat(group.getCollection(), is("Groups"));
        MatcherAssert.assertThat(group.getIdentifier(), is(Optional.of("sdf")));
        MatcherAssert.assertThat(group.getProperty(), is(Optional.empty()));
    }

    @Test
    public void assert_relation_has_member_subject() throws STAInvalidUrlException {

        // TODO why these examples do not fail?
        //StaPath<? extends Identifiable> path = factory.parse("/Relations(sdf)/Observations");
        //StaPath<? extends Identifiable> path = factory.parse("/Things(sdf)/Relations(sdf);

        StaPath<? extends Identifiable> path = factory.parse("/Relations(sdf)/Subject");
        MatcherAssert.assertThat(path.getPathType(), is(SelectPath.PathType.entity));
        List<PathSegment> segments = path.getPathSegments();
        MatcherAssert.assertThat(segments.size(), is(2));

        PathSegment subject = segments.get(0);
        MatcherAssert.assertThat(subject.getCollection(), is("Subject"));
        MatcherAssert.assertThat(subject.getIdentifier(), is(Optional.empty()));
        MatcherAssert.assertThat(subject.getProperty(), is(Optional.empty()));

        PathSegment relations = segments.get(1);
        MatcherAssert.assertThat(relations.getCollection(), is("Relations"));
        MatcherAssert.assertThat(relations.getIdentifier(), is(Optional.of("sdf")));
        MatcherAssert.assertThat(relations.getProperty(), is(Optional.empty()));
    }

    @Test
    public void assert_relation_has_member_object() throws STAInvalidUrlException {
        StaPath<? extends Identifiable> path = factory.parse("/Relations(sdf)/Object");
        MatcherAssert.assertThat(path.getPathType(), is(SelectPath.PathType.entity));
        List<PathSegment> segments = path.getPathSegments();
        MatcherAssert.assertThat(segments.size(), is(2));

        PathSegment object = segments.get(0);
        MatcherAssert.assertThat(object.getCollection(), is("Object"));
        MatcherAssert.assertThat(object.getIdentifier(), is(Optional.empty()));
        MatcherAssert.assertThat(object.getProperty(), is(Optional.empty()));

        PathSegment relations = segments.get(1);
        MatcherAssert.assertThat(relations.getCollection(), is("Relations"));
        MatcherAssert.assertThat(relations.getIdentifier(), is(Optional.of("sdf")));
        MatcherAssert.assertThat(relations.getProperty(), is(Optional.empty()));
    }

    @Test
    public void assert_observation_has_member_subjects() throws STAInvalidUrlException {
        StaPath<? extends Identifiable> path = factory.parse("/Observations(sdf)/Subjects");
        MatcherAssert.assertThat(path.getPathType(), is(SelectPath.PathType.collection));
        List<PathSegment> segments = path.getPathSegments();
        MatcherAssert.assertThat(segments.size(), is(2));

        PathSegment subjects = segments.get(0);
        MatcherAssert.assertThat(subjects.getCollection(), is("Subjects"));
        MatcherAssert.assertThat(subjects.getIdentifier(), is(Optional.empty()));
        MatcherAssert.assertThat(subjects.getProperty(), is(Optional.empty()));

        PathSegment observations = segments.get(1);
        MatcherAssert.assertThat(observations.getCollection(), is("Observations"));
        MatcherAssert.assertThat(observations.getIdentifier(), is(Optional.of("sdf")));
        MatcherAssert.assertThat(observations.getProperty(), is(Optional.empty()));
    }

    @Test
    public void assert_observation_has_member_objects() throws STAInvalidUrlException {
        StaPath<? extends Identifiable> path = factory.parse("/Observations(sdf)/Objects");
        MatcherAssert.assertThat(path.getPathType(), is(SelectPath.PathType.collection));
        List<PathSegment> segments = path.getPathSegments();
        MatcherAssert.assertThat(segments.size(), is(2));

        PathSegment objects = segments.get(0);
        MatcherAssert.assertThat(objects.getCollection(), is("Objects"));
        MatcherAssert.assertThat(objects.getIdentifier(), is(Optional.empty()));
        MatcherAssert.assertThat(objects.getProperty(), is(Optional.empty()));

        PathSegment observations = segments.get(1);
        MatcherAssert.assertThat(observations.getCollection(), is("Observations"));
        MatcherAssert.assertThat(observations.getIdentifier(), is(Optional.of("sdf")));
        MatcherAssert.assertThat(observations.getProperty(), is(Optional.empty()));
    }

    @Test
    public void assert_relation_has_property_role() throws STAInvalidUrlException {
        StaPath<? extends Identifiable> path = factory.parse("/Relations(sdf)/role");
        MatcherAssert.assertThat(path.getPathType(), is(SelectPath.PathType.property));
        List<PathSegment> segments = path.getPathSegments();
        MatcherAssert.assertThat(segments.size(), is(1));

        PathSegment relations = segments.get(0);
        MatcherAssert.assertThat(relations.getCollection(), is("Relations"));
        MatcherAssert.assertThat(relations.getIdentifier(), is(Optional.of("sdf")));
        MatcherAssert.assertThat(relations.getProperty(), is(Optional.of("role")));
    }
}
