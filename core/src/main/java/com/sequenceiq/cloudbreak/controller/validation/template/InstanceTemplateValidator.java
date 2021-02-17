package com.sequenceiq.cloudbreak.controller.validation.template;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.domain.Template;
import com.sequenceiq.cloudbreak.service.template.TemplateService;
import com.sequenceiq.cloudbreak.validation.ValidationResult;
import com.sequenceiq.cloudbreak.validation.ValidationResult.ValidationResultBuilder;
import com.sequenceiq.cloudbreak.validation.Validator;

@Component
public class InstanceTemplateValidator implements Validator<Template> {

    @Inject
    private TemplateService templateService;

    @Override
    public ValidationResult validate(Template template) {
        ValidationResultBuilder resultBuilder = ValidationResult.builder();
        templateService.findById(template.getId()).ifPresentOrElse(foundTemplate -> {
            resultBuilder.ifError(() -> foundTemplate.getRootVolumeSize() != null
                            && foundTemplate.getRootVolumeSize() < 1,
                    "Root volume size cannot be smaller than 1 gigabyte.");
        }, () -> resultBuilder.error("Template cannot be null in the instance group request."));
        return resultBuilder.build();
    }
}
