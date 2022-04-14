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
package org.n52.sta.http.common;

import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * @author <a href="mailto:c.autermann@52north.org">Christian Autermann</a>
 */
public class CustomUrlPathHelper extends ExtendableUrlPathHelper {

    private static final String SLASH = "/";
    private static final String ENCODED_SLASH = UriUtils.encode(SLASH, StandardCharsets.UTF_8);
    private static final String DOUBLE_ENCODED_SLASH = UriUtils.encode(ENCODED_SLASH, StandardCharsets.UTF_8);
    private static final Pattern ENCODED_SLASH_PATTERN =
        Pattern.compile(ENCODED_SLASH,
                        Pattern.CASE_INSENSITIVE | Pattern.LITERAL);

    @Override
    public String getServletPath(HttpServletRequest request) {
        String servletPath = getSanitizedPath(super.getServletPath(request));
        String contextPath = getContextPath(request);
        String requestUri = getSanitizedPath(super.decodeRequestString(request,
                                                                       removeSemicolonContent(getRequestUri(request))));
        String pathWithinApplication = getRemainingPath(requestUri, contextPath, true);
        if (pathWithinApplication != null) {
            // Normal case: URI contains context path.
            pathWithinApplication = StringUtils.hasText(pathWithinApplication) ? pathWithinApplication : SLASH;
        } else {
            pathWithinApplication = requestUri;
        }
        return servletPath.equals(pathWithinApplication) ? "" : servletPath;
    }

    @Override
    public String decodeRequestString(HttpServletRequest request, String source) {
        String[] split = source.split("\\?", 2);
        split[0] = ENCODED_SLASH_PATTERN.matcher(split[0]).replaceAll(DOUBLE_ENCODED_SLASH);
        return super.decodeRequestString(request, split.length <= 1 ? split[0] : split[0] + '?' + split[1]);
    }

    @Override
    public Map<String, String> decodePathVariables(HttpServletRequest request, Map<String, String> vars) {
        Map<String, String> decodedVars = new LinkedHashMap<>(vars.size());
        vars.forEach((key, value) -> decodedVars.put(key, ENCODED_SLASH_PATTERN.matcher(value).replaceAll(SLASH)));
        return super.decodePathVariables(request, decodedVars);
    }
}
