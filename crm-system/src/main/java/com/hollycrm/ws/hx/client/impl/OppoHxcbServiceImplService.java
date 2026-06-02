package com.hollycrm.ws.hx.client.impl;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

/**
 * JAX-WS 生成的 Service 工厂类（老系统 wsimport 生成，已去除 PropertiesUtil 依赖）
 *
 * 使用方式：
 *   URL wsdlUrl = new URL("http://10.202.2.102:8083/oppo/services/OppoGdServiceSrv?wsdl");
 *   OppoHxcbServiceImplService service = new OppoHxcbServiceImplService(wsdlUrl);
 *   OppoHxcbService port = service.getOppoHxcbServiceImplPort();
 */
@WebServiceClient(name = "OppoHxcbServiceImplService", targetNamespace = "http://impl.service.oppo.sdunicomsi.com/", wsdlLocation = "file:/D:/OppoGdServiceSrv.wsdl")
public class OppoHxcbServiceImplService extends Service {

    private final static QName OPPOHXCBSERVICEIMPLSERVICE_QNAME = new QName("http://impl.service.oppo.sdunicomsi.com/",
            "OppoHxcbServiceImplService");

    public OppoHxcbServiceImplService(URL wsdlLocation) {
        super(wsdlLocation, OPPOHXCBSERVICEIMPLSERVICE_QNAME);
    }

    public OppoHxcbServiceImplService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, OPPOHXCBSERVICEIMPLSERVICE_QNAME, features);
    }

    public OppoHxcbServiceImplService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public OppoHxcbServiceImplService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * @return returns OppoHxcbService
     */
    @WebEndpoint(name = "OppoHxcbServiceImplPort")
    public OppoHxcbService getOppoHxcbServiceImplPort() {
        return super.getPort(new QName("http://impl.service.oppo.sdunicomsi.com/", "OppoHxcbServiceImplPort"),
                OppoHxcbService.class);
    }

    /**
     * @param features
     *          A list of {@link javax.xml.ws.WebServiceFeature} to configure on
     *          the proxy. Supported features not in the <code>features</code>
     *          parameter will have their default values.
     * @return returns OppoHxcbService
     */
    @WebEndpoint(name = "OppoHxcbServiceImplPort")
    public OppoHxcbService getOppoHxcbServiceImplPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://impl.service.oppo.sdunicomsi.com/", "OppoHxcbServiceImplPort"),
                OppoHxcbService.class, features);
    }

}
