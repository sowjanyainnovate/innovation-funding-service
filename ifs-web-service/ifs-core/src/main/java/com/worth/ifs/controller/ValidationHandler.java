package com.worth.ifs.controller;

import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.error.ErrorHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.worth.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static com.worth.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;
import static com.worth.ifs.util.CollectionFunctions.*;

/**
 * A helper class that wraps the standard BindingResult and allows us to more easily work with the various mechanisms that
 * we use to convey Error messages to the front end
 */
public class ValidationHandler {

    private BindingResult bindingResult;
    private BindingResultTarget bindingResultTarget;

    private ValidationHandler(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
    }

    public ValidationHandler addAnyErrors(ErrorHolder errors) {
        return addAnyErrors(errors.getErrors(), fieldErrorsToFieldErrors(), asGlobalErrors());
    }

    public ValidationHandler addAnyErrors(ErrorHolder errors, ErrorToObjectErrorConverter converter, ErrorToObjectErrorConverter... otherConverters) {
        return addAnyErrors(errors.getErrors(), converter, otherConverters);
    }

    private ValidationHandler addAnyErrors(List<Error> errors, ErrorToObjectErrorConverter converter, ErrorToObjectErrorConverter... otherConverters) {
        errors.forEach(e -> {
            List<Optional<ObjectError>> optionalConversionsForThisError = simpleMap(combineLists(converter, otherConverters), fn -> fn.apply(e));
            Optional<Optional<ObjectError>> successfullyConvertedErrorList = simpleFindFirst(optionalConversionsForThisError, Optional::isPresent);

            if (successfullyConvertedErrorList.isPresent()) {
                bindingResult.addError(successfullyConvertedErrorList.get().get());
            }
        });
        return this;
    }

    public ValidationHandler setBindingResultTarget(BindingResultTarget bindingResultTarget) {
        this.bindingResultTarget = bindingResultTarget;
        return this;
    }

    /**
     * If there are currently errors, fail immediately invoking the given failureHandler.  Otherwise, continue to process
     * the successHandler.  Assumption is that we would normally be returning view names from the given suppliers.
     *
     * @param failureHandler
     * @param successHandler
     * @return
     */
    public String failNowOrSucceedWith(Supplier<String> failureHandler, Supplier<String> successHandler) {

        if (hasErrors()) {

            if (bindingResultTarget != null) {
                bindingResultTarget.setBindingResult(bindingResult);
                bindingResultTarget.setObjectErrors(bindingResult.getAllErrors());
            }

            return failureHandler.get();
        }

        return successHandler.get();
    }

    public boolean hasErrors() {
        return bindingResult.hasErrors();
    }

    public List<? extends ObjectError> getAllErrors() {
        return bindingResult.getAllErrors();
    }

    public static ValidationHandler newBindingResultHandler(BindingResult bindingResult) {
        return new ValidationHandler(bindingResult);
    }
}