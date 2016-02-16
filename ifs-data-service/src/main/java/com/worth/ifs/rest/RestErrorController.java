package com.worth.ifs.rest;

import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.rest.RestErrorResponse;
import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.worth.ifs.commons.error.CommonFailureKeys.GENERAL_NOT_FOUND;
import static com.worth.ifs.commons.error.Errors.forbiddenError;
import static com.worth.ifs.commons.error.Errors.internalServerErrorError;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.*;

/**
 *
 */
@RestController
public class RestErrorController extends AbstractErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

    private static final String PATH = "/error";

    public RestErrorController() {
        super(new DefaultErrorAttributes());
    }

    public RestErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @RequestMapping(PATH)
    public ResponseEntity<RestErrorResponse> error(HttpServletRequest request) {
        Map<String, Object> errorAttributes = getErrorAttributes(request, false);
        Integer status = (Integer) errorAttributes.get("status");

        if (status != null) {
            if (NOT_FOUND.value() == status) {
                RestErrorResponse restErrorResponse = new RestErrorResponse(new Error(GENERAL_NOT_FOUND.getErrorKey(), "The requested URL could not be found.", NOT_FOUND));
                return new ResponseEntity<>(restErrorResponse, restErrorResponse.getStatusCode());
            } else if (FORBIDDEN.value() == status) {
                RestErrorResponse restErrorResponse = new RestErrorResponse(forbiddenError("You do not have permission to access the requested URL."));
                return new ResponseEntity<>(restErrorResponse, restErrorResponse.getStatusCode());
            } else {
                String message = (String) errorAttributes.get("message");
                String finalMessage = !isBlank(message) ? message : "An unexpected error occurred.";
                RestErrorResponse restErrorResponse = new RestErrorResponse(new Error(finalMessage, finalMessage, HttpStatus.valueOf(status)));
                return new ResponseEntity<>(restErrorResponse, restErrorResponse.getStatusCode());
            }
        }
        RestErrorResponse fallbackResponse = new RestErrorResponse(internalServerErrorError());
        return new ResponseEntity<>(fallbackResponse, INTERNAL_SERVER_ERROR);
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}