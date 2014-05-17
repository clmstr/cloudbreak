package com.sequenceiq.provisioning.controller.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.sequenceiq.provisioning.controller.json.ProvisionRequest;
import com.sequenceiq.provisioning.controller.json.RequiredAWSRequestParam;

/**
 * Validates a provision request. Because different parameters belong to
 * different cloud platforms, they must be validated accordingly. This class
 * checks if the required parameters are present and if they match the required
 * format.
 */
public class ProvisionParametersValidator implements ConstraintValidator<ValidProvisionRequest, ProvisionRequest> {

    private List<String> requiredAWSParams = new ArrayList<>();
    private List<String> requiredAzureParams = new ArrayList<>();

    @Override
    public void initialize(ValidProvisionRequest constraintAnnotation) {
        for (RequiredAWSRequestParam param : RequiredAWSRequestParam.values()) {
            requiredAWSParams.add(param.getName());
        }
    }

    @Override
    public boolean isValid(ProvisionRequest request, ConstraintValidatorContext context) {
        boolean valid = true;
        switch (request.getCloudPlatform()) {
        case AWS:
            valid = validateAWSParams(request, context);
            break;
        case AZURE:
            valid = validateAzureParams(request, context);
            break;
        default:
            break;
        }
        return valid;

    }

    private boolean validateAzureParams(ProvisionRequest request, ConstraintValidatorContext context) {
        return validateRequiredParameters(request, context, requiredAzureParams);
    }

    private boolean validateAWSParams(ProvisionRequest request, ConstraintValidatorContext context) {
        // TODO: validate instanceType, sshLocation, etc.. with regex
        return validateRequiredParameters(request, context, requiredAWSParams);
    }

    private boolean validateRequiredParameters(ProvisionRequest request, ConstraintValidatorContext context, List<String> requiredParams) {
        boolean valid = true;
        for (String param : requiredParams) {
            if (!request.getParameters().containsKey(param)) {
                addParameterConstraintViolation(context, param, String.format("%s is required.", param));
                valid = false;
            }
        }
        return valid;
    }

    private void addParameterConstraintViolation(ConstraintValidatorContext context, String key, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("parameters")
                .addBeanNode().inIterable().atKey(key)
                .addConstraintViolation();
    }
}
