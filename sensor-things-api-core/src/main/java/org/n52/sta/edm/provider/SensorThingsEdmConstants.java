/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.edm.provider;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class SensorThingsEdmConstants {

    public static final String NAMESPACE = "iot";

    public static final String CONTROL_ANNOTATION_PREFIX = "@" + NAMESPACE;

    public static final String ID_ANNOTATION = CONTROL_ANNOTATION_PREFIX + ".id";

    public static final String SELF_LINK_ANNOTATION = CONTROL_ANNOTATION_PREFIX + ".selfLink";

    public static final String NAVIGATION_LINK_ANNOTATION = CONTROL_ANNOTATION_PREFIX + ".navigationLink";

    public static final String NEXT_LINK_ANNOTATION = CONTROL_ANNOTATION_PREFIX + ".nextLink";

}
