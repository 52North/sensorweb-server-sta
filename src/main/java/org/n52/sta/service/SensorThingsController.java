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
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.ErrorProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.processor.ReferenceCollectionProcessor;
import org.apache.olingo.server.api.processor.ReferenceProcessor;
import org.apache.olingo.server.api.processor.ServiceDocumentProcessor;
import static org.n52.sta.service.SensorThingsController.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@RestController
@RequestMapping(value = "/" + URI)
public class SensorThingsController {
    
    public static final String URI = "sta";
    
    @Autowired
    private CsdlAbstractEdmProvider provider;
    
    @Autowired
    private ServiceDocumentProcessor serviceDocumentProcessor;
    
    @Autowired
    private EntityCollectionProcessor entityCollectionProcessor;
    
    @Autowired
    private EntityProcessor entityProcessor;
    
    @Autowired
    private PrimitiveValueProcessor primitiveValueProcessor;
    
    @Autowired
    private ErrorProcessor errorProcessor;
    
    @Autowired
    private ComplexProcessor complexProcessor;
    
    @Autowired
    private ReferenceCollectionProcessor referenceCollectionProcessor;
    
    @Autowired
    private ReferenceProcessor referenceProcessor;
    
    @RequestMapping("**")
    protected void process(HttpServletRequest request, HttpServletResponse response) {

        // create odata handler and configure it with EdmProvider and Processor
        OData odata = OData.newInstance();
        
        ServiceMetadata edm = odata.createServiceMetadata(provider, new ArrayList<EdmxReference>());
        
        ODataHttpHandler handler = odata.createHandler(edm);
        handler.register(serviceDocumentProcessor);
        handler.register(entityCollectionProcessor);
        handler.register(entityProcessor);
        handler.register(errorProcessor);
        handler.register(primitiveValueProcessor);
        handler.register(complexProcessor);
        handler.register(referenceCollectionProcessor);
        handler.register(referenceProcessor);

        // let the handler do the work
        handler.process(new HttpServletRequestWrapper(request) {
            
            @Override
            public String getServletPath() {
                return URI;
            }
        }, response);
        
    }
}
