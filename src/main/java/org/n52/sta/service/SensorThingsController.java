/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service;

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.ServiceDocumentProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@RestController
@RequestMapping(value = "/sta")
public class SensorThingsController {

    private static String URI = "sta/";

//    @Autowired
//    ApplicationContext ctx;
    @Autowired
    private CsdlAbstractEdmProvider provider;

    @Autowired
    private ServiceDocumentProcessor serviceDocumentProcessor;

    @Autowired
    private EntityCollectionProcessor entityCollectionProcessor;

    @RequestMapping("*")
    protected void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);

        // create odata handler and configure it with EdmProvider and Processor
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(provider, new ArrayList<EdmxReference>());

        ODataHttpHandler handler = odata.createHandler(edm);
        handler.register(serviceDocumentProcessor);
        handler.register(entityCollectionProcessor);

        // let the handler do the work
        handler.process(new HttpServletRequestWrapper(request) {

            @Override
            public String getServletPath() {
                return URI;
            }
        }, response);

    }
}
