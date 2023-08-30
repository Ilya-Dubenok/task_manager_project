package org.example.config;

import jakarta.persistence.Id;
import jakarta.persistence.Version;
import org.example.config.properties.ApplicationProperties;
import org.example.config.properties.JWTProperties;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.task.Task;
import org.example.service.utils.ChangedFieldsOfEntitySearcher;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.List;

@Configuration
public class Config {
    @Bean
    @ConfigurationProperties("app")
    public ApplicationProperties getProperty() {
        return new ApplicationProperties();
    }

    @Bean
    @ConfigurationProperties("jwt")
    public JWTProperties getJWTProperties() {
        return new JWTProperties();

    }

    @Bean
    public ChangedFieldsOfEntitySearcher<Project> changedFieldsOfEntitySearcherOnProject() {

        return new ChangedFieldsOfEntitySearcher
                .Builder<>(Project.class)
                .setNotToScanAnnotations(List.of(
                        Id.class, CreatedDate.class, LastModifiedDate.class, Version.class
                ))
                .build();

    }

    @Bean
    public ChangedFieldsOfEntitySearcher<Task> changedFieldsOfEntitySearcherOnTask() {

        return new ChangedFieldsOfEntitySearcher
                .Builder<>(Task.class)
                .setNotToScanAnnotations(List.of(
                        Id.class, CreatedDate.class, LastModifiedDate.class, Version.class
                ))
                .build();

    }

}
