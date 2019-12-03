package com.bdaim.api.service;


import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.bdaim.api.Dto.ApiData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
public class CreateXML {
    public static final Logger logger = LoggerFactory.getLogger(CreateXML.class);

    public void createXML(ApiData apiData) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = documentBuilderFactory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element api = document.createElement("api");
            api.setAttribute("xmlns", "http://ws.apache.org/ns/synapse");
            api.setAttribute("name", apiData.getApiName());
            api.setAttribute("context", apiData.getContext());
            api.setAttribute("version", apiData.getApiVersion());
            api.setAttribute("version-type", "context");
            Element resource = document.createElement("resource");
            resource.setAttribute("methods", "GET");
            resource.setAttribute("url-mapping", "/http://localhost:6006/test/addressPost");
            resource.setAttribute("faultSequence", "fault");
            Element inSequence = document.createElement("inSequence");
            Element cache = document.createElement("cache");
            cache.setAttribute("scope", "per-host");
            cache.setAttribute("collector", "false");
            cache.setAttribute("hashGenerator", "org.wso2.caching.digest.REQUESTHASHGenerator");
            Element implementation = document.createElement("implementation");
            implementation.setAttribute("type", "memory");
            implementation.setAttribute("maxSize", "500");
            cache.appendChild(implementation);
            inSequence.appendChild(cache);
            Element filter = document.createElement("filter");
            filter.setAttribute("source", "$ctx:AM_KEY_TYPE");
            filter.setAttribute("regex", "PRODUCTION");
            Element then = document.createElement("then");
            Element property = document.createElement("property");
            property.setAttribute("name", "api.ut.backendRequestTime");
            property.setAttribute("expression", "get-property('SYSTEM_TIME')");
            Element send = document.createElement("send");
            Element endpoint = document.createElement("endpoint");
            Element http = document.createElement("http");
            endpoint.setAttribute("name", "opt--测106_APIproductionEndpoint_0");
            http.setAttribute("uri-template", "http://sdsfd/resource");
            endpoint.appendChild(http);
            send.appendChild(endpoint);
            then.appendChild(property);
            then.appendChild(send);
            Element else1 = document.createElement("else");
            Element sequence = document.createElement("sequence");
            sequence.setAttribute("key", "_sandbox_key_error_");
            else1.appendChild(sequence);
            filter.appendChild(then);
            filter.appendChild(else1);
            inSequence.appendChild(cache);
            inSequence.appendChild(filter);
            Element outSequence = document.createElement("outSequence");
            Element class1 = document.createElement("class");
            Element cache1 = document.createElement("cache");
            class1.setAttribute("name", "org.wso2.carbon.apimgt.usage.publisher.APIMgtResponseHandler");
            cache1.setAttribute("scope", "per-host");
            cache1.setAttribute("collector", "true");
            outSequence.appendChild(class1);
            outSequence.appendChild(cache1);
            resource.appendChild(inSequence);
            resource.appendChild(outSequence);
            Element handlers = document.createElement("handlers");
            Element handler = document.createElement("handler");
            Element property1 = document.createElement("property");
            handler.setAttribute("class", "org.wso2.carbon.apimgt.gateway.handlers.security.CORSRequestHandler");
            property1.setAttribute("name", "apiImplementationType");
            property1.setAttribute("value", "ENDPOINT");
            handler.appendChild(property1);
            Element handler1 = document.createElement("handler");
            handler1.setAttribute("class", "org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler");
            Element handler2 = document.createElement("handler");
            handler2.setAttribute("class", "org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleHandler");
            Element property3 = document.createElement("property");
            property3.setAttribute("name", "policyKey");
            property3.setAttribute("value", "gov:/apimgt/applicationdata/tiers.xml");
            Element property2 = document.createElement("property");
            property2.setAttribute("name", "policyKeyApplication");
            property2.setAttribute("value", "gov:/apimgt/applicationdata/app-tiers.xml");
            Element property4 = document.createElement("property");
            property4.setAttribute("name", "id");
            property4.setAttribute("value", "A");
            Element property5 = document.createElement("property");
            property5.setAttribute("name", "policyKeyResource");
            property5.setAttribute("value", "gov:/apimgt/applicationdata/res-tiers.xml");
            handler2.appendChild(property3);
            handler2.appendChild(property2);
            handler2.appendChild(property4);
            handler2.appendChild(property5);
            Element handler3 = document.createElement("handler");
            Element handler4 = document.createElement("handler");
            handler3.setAttribute("class", "org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler");
            handler4.setAttribute("class",
                    "org.wso2.carbon.apimgt.usage.publisher.APIMgtGoogleAnalyticsTrackingHandler");
            Element property6 = document.createElement("property");
            property6.setAttribute("name", "configKey");
            property6.setAttribute("value", "gov:/apimgt/statistics/ga-config.xml");
            handler4.appendChild(property6);
            Element handler5 = document.createElement("handler");
            handler5.setAttribute("class", "org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler");
            handlers.appendChild(handler);
            handlers.appendChild(handler1);
            handlers.appendChild(handler2);
            handlers.appendChild(handler3);
            handlers.appendChild(handler4);
            handlers.appendChild(handler5);
            api.appendChild(resource);
            api.appendChild(handlers);

            document.appendChild(api);
            // 创建转换工厂，然后将创建的document转换输出到文件中或控制台
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(document), new StreamResult(new File("newXml.xml")));

            // 将document中的信息转换为字符串输出到控制台中
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));

        } catch (Exception e) {
            // TODO: handle exception
            logger.info(e.getMessage());
            logger.info("xml文件生成失败");
        }


    }
}
