package com.construtech.wsdlproxy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Controller
public class ProxyController {

    @Value("${proxy.scheme}")
    private String proxyScheme;

    @Value("${proxy.host}")
    private String proxyHost;

    @Value("${proxy.port}")
    private Integer proxyPort;

    private Logger logger = LoggerFactory.getLogger(ProxyController.class);

    @RequestMapping(method = RequestMethod.GET, path = "/**", produces = "application/xml")
    public ResponseEntity<String> get(HttpServletRequest request){

        String path = request.getServletPath() + "?" + request.getQueryString();
        String proxyUrl = proxyScheme + "://" + proxyHost + ":" + proxyPort + path;
        logger.info("Proxy url for current request: {}", proxyUrl);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(proxyUrl, String.class);
        logger.info("Got {} status from proxy for current request", responseEntity.getStatusCodeValue());

        String content = responseEntity.getBody();

        content = content
                .replaceAll("(schemaLocation=\"http://.*?/)", "schemaLocation=\"http://localhost:8080/")
                .replaceAll("(location=\"http://.*?/)", "location=\"http://localhost:8080/");

        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/**", produces = "application/xml")
    public ResponseEntity<String> post(@RequestBody String body, HttpServletRequest request){

        logger.info("Received post request");

        String path = request.getServletPath() + "?" + request.getQueryString();
        String proxyUrl = proxyScheme + "://" + proxyHost + ":" + proxyPort + path;

        logger.info("Proxy post request url {}", proxyUrl);

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> requestHeaders = request.getHeaderNames();
        String headerName = requestHeaders.nextElement();
        do{
            String headerValue = request.getHeader(headerName);
            headers.add(headerName, headerValue);
            headerName = requestHeaders.nextElement();
        }while (headerName != null);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(proxyUrl, HttpMethod.POST, entity, String.class);
            return response;
        }catch (HttpClientErrorException | HttpServerErrorException e){
            return new ResponseEntity<String>(e.getResponseBodyAsString(), e.getStatusCode());
        }
    }
}
