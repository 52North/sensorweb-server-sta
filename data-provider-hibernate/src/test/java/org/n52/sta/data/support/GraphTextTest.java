package org.n52.sta.data.support;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GraphTextTest {

    @Test
    public void test_default() {
        Set<String> paths = GraphText.GRAPH_DEFAULT.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("identifier", paths.iterator().next());
    }

    @Test
    public void test_platforms() {
        Set<String> paths = GraphText.GRAPH_PLATFORMS.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("platforms.parameters", paths.iterator().next());
    }

    @Test
    public void test_platform() {
        Set<String> paths = GraphText.GRAPH_PLATFORM.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("platform.parameters", paths.iterator().next());
    }

    @Test
    public void test_locations() {
        Set<String> paths = GraphText.GRAPH_LOCATIONS.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("locations.parameters", paths.iterator().next());
    }

    @Test
    public void test_historicalLocations() {
        Set<String> paths = GraphText.GRAPH_HIST_LOCATIONS.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("historicalLocations", paths.iterator().next());
    }

    @Test
    public void test_unit() {
        Set<String> paths = GraphText.GRAPH_UOM.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("unit", paths.iterator().next());
    }

    @Test
    public void test_parameters() {
        Set<String> paths = GraphText.GRAPH_PARAMETERS.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("parameters", paths.iterator().next());
    }

    @Test
    public void test_format() {
        Set<String> paths = GraphText.GRAPH_FORMAT.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("format", paths.iterator().next());
    }

    @Test
    public void test_featureType() {
        Set<String> paths = GraphText.GRAPH_FEATURETYPE.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("featureType", paths.iterator().next());
    }

    @Test
    public void test_procedure() {
        Set<String> paths = GraphText.GRAPH_PROCEDURE.paths();
        Assertions.assertEquals(3, paths.size());
        Assertions.assertTrue(paths.contains("procedure.format"));
        Assertions.assertTrue(paths.contains("procedure.procedureHistory"));
        Assertions.assertTrue(paths.contains("procedure.parameters"));
    }

    @Test
    public void test_phenomenon() {
        Set<String> paths = GraphText.GRAPH_PHENOMENON.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("phenomenon.parameters", paths.iterator().next());
    }

    @Test
    public void test_omObservationType() {
        Set<String> paths = GraphText.GRAPH_OM_OBS_TYPE.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("omObservationType", paths.iterator().next());
    }

    @Test
    public void test_datasets() {
        Set<String> paths = GraphText.GRAPH_DATASETS.paths();
        Assertions.assertEquals(4, paths.size());
        Assertions.assertTrue(paths.contains("datasets.category"));
        Assertions.assertTrue(paths.contains("datasets.unit"));
        Assertions.assertTrue(paths.contains("datasets.omObservationType"));
        Assertions.assertTrue(paths.contains("datasets.parameters"));
    }
    
    @Test
    public void test_feature() {
        Set<String> paths = GraphText.GRAPH_FEATURE.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("feature", paths.iterator().next());
    }

    @Test
    public void test_datasetFirstLastObservation() {
        Set<String> paths = GraphText.GRAPH_DATASET_FIRSTLAST_OBSERVATION.paths();
        Assertions.assertEquals(2, paths.size());
        Assertions.assertTrue(paths.contains("dataset.firstObservation"));
        Assertions.assertTrue(paths.contains("dataset.lastObservation"));
    }

    @Test
    public void test_procedureHistory() {
        Set<String> paths = GraphText.GRAPH_PROCEDUREHISTORY.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("procedureHistory", paths.iterator().next());
    }

    @Test
    public void test_category() {
        Set<String> paths = GraphText.GRAPH_CATEGORY.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("category", paths.iterator().next());
    }

    @Test
    public void test_platformHistLocation() {
        Set<String> paths = GraphText.GRAPH_PLATFORMSHISTLOCATION.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("platforms.historicalLocations", paths.iterator().next());
    }

    @Test
    public void test_locationHistLocation() {
        Set<String> paths = GraphText.GRAPH_LOCATIONHISTLOCATION.paths();
        Assertions.assertEquals(1, paths.size());
        Assertions.assertEquals("locations.historicalLocations", paths.iterator().next());
    }
}
