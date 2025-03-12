package by.alex.apiservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Public API endpoint";
    }

    @GetMapping("/private")
    public String privateEndpoint(HttpServletRequest request) {
        logRequestHeaders(request);
        return "Private API endpoint";
    }


    private void logRequestHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headers = request.getHeaders(headerName);
                while (headers.hasMoreElements()) {
                    String headerValue = headers.nextElement();
                    log.info("Request Header: {} = {}", headerName, headerValue);
                }
            }
        }
    }
}
