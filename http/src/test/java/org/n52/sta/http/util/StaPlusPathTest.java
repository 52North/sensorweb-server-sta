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

        PathSegment relations = segments.get(0);
        MatcherAssert.assertThat(relations.getCollection(), is("Subjects"));
        MatcherAssert.assertThat(relations.getIdentifier(), is(Optional.empty()));
        MatcherAssert.assertThat(relations.getProperty(), is(Optional.empty()));

        PathSegment group = segments.get(1);
        MatcherAssert.assertThat(group.getCollection(), is("Relations"));
        MatcherAssert.assertThat(group.getIdentifier(), is(Optional.of("sdf")));
        MatcherAssert.assertThat(group.getProperty(), is(Optional.empty()));
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
