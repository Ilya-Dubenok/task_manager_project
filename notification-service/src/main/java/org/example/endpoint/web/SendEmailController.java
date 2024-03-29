package org.example.endpoint.web;


import org.example.core.dto.SimpleEmailTemplateDTO;
import org.example.service.api.IEmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class SendEmailController {

    private IEmailService emailService;

    public SendEmailController(IEmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<?> postUsualEmail(@RequestBody SimpleEmailTemplateDTO simpleEmailTemplateDTO) {

        if (null == simpleEmailTemplateDTO.getReplyTo()) {
            this.emailService.sendMessage(simpleEmailTemplateDTO);

        } else {

            this.emailService.sendEmailWithFeedbackMessage(simpleEmailTemplateDTO);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }



}
