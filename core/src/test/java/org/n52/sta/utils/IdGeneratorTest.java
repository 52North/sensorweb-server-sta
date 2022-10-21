/*
 * Copyright (C) 2018-2022 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
package org.n52.sta.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IdGeneratorTest {

    @Test
    public void test_result_phenomeonTime() {
        String identifier = "123-1456-789";
        String phenomenonTime = "2022-04-20T22:00:00.000Z";
        String result = "1.0";
        String resultTime = null;
        String verticalFrom = null;
        String verticalTo = null;

        StringBuffer buffer = new StringBuffer();
        buffer.append(identifier).append(phenomenonTime).append(resultTime).append(verticalFrom).append(verticalTo);
        String generate1 = getId(identifier, phenomenonTime, resultTime, result, verticalFrom, verticalTo);
        String generate2 = getId(identifier, phenomenonTime, resultTime, result, verticalFrom, verticalTo);
        assertEquals(generate1, generate2);
    }

    @Test
    public void test_result_phenomeonTime_resultTime() {
        String identifier = "123-1456-789";
        String phenomenonTime = "2022-04-20T22:00:00.000Z";
        String result = "1.0";
        String resultTime = "2022-04-20T22:00:00.000Z";
        String verticalFrom = null;
        String verticalTo = null;

        StringBuffer buffer = new StringBuffer();
        buffer.append(identifier).append(phenomenonTime).append(resultTime).append(verticalFrom).append(verticalTo);
        String generate1 = getId(identifier, phenomenonTime, resultTime, result, verticalFrom, verticalTo);
        String generate2 = getId(identifier, phenomenonTime, resultTime, result, verticalFrom, verticalTo);
        assertEquals(generate1, generate2);
    }

    @Test
    public void test_result_phenomeonTime_resultTime_vertical() {
        String identifier = "123-1456-789";
        String phenomenonTime = "2022-04-20T22:00:00.000Z";
        String result = "1.0";
        String resultTime = "2022-04-20T22:00:00.000Z";
        String verticalFrom = "0.0";
        String verticalTo = "0.0";

        StringBuffer buffer = new StringBuffer();
        buffer.append(identifier).append(phenomenonTime).append(resultTime).append(verticalFrom).append(verticalTo);
        String generate1 = getId(identifier, phenomenonTime, resultTime, result, verticalFrom, verticalTo);
        String generate2 = getId(identifier, phenomenonTime, resultTime, result, verticalFrom, verticalTo);
        assertEquals(generate1, generate2);
    }

    @Test
    public void test_result_phenomeonTime_resultTime_verticalFromTo() {
        String identifier = "123-1456-789";
        String phenomenonTime = "2022-04-20T22:00:00.000Z";
        String result = "1.0";
        String resultTime = "2022-04-20T22:00:00.000Z";
        String verticalFrom = "0.0";
        String verticalTo = "2.0";

        StringBuffer buffer = new StringBuffer();
        buffer.append(identifier).append(phenomenonTime).append(resultTime).append(verticalFrom).append(verticalTo);
        String generate1 = getId(identifier, phenomenonTime, resultTime, result, verticalFrom, verticalTo);
        String generate2 = getId(identifier, phenomenonTime, resultTime, result, verticalFrom, verticalTo);
        assertEquals(generate1, generate2);
    }

    private String getId(String identifier, String phenomenonTime, String resultTime, String result,
            String verticalFrom, String verticalTo) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(identifier).append(phenomenonTime).append(result).append(resultTime).append(verticalFrom)
                .append(verticalTo);
        return buffer.toString();
    }

}
