package com.intuit.be_a_friend.advices;

import com.intuit.be_a_friend.exceptions.GlobalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Controller
public class CustomErrorHandler implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorHandler.class);

    private final ErrorAttributes errorAttributes;

    public CustomErrorHandler(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public void handleError(WebRequest webRequest) {
        Map<String, Object> errorAttributesMap = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        String message = (String) errorAttributesMap.get("message");
        Integer status = (Integer) errorAttributesMap.get("status");
        String path = (String) errorAttributesMap.get("path");

        String exceptionMessage = (String) webRequest.getAttribute("message", WebRequest.SCOPE_REQUEST);

        // Log the error details
        logger.error("Error occurred on path: {}, with status: {} and message: {}", path, status, message);

        // Throw a custom exception that will be handled by GlobalExceptionHandler
        throw new GlobalErrorException(status, exceptionMessage);
    }
}



