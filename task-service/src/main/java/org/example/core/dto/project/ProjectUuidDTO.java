package org.example.core.dto.project;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ProjectUuidDTO {

    @NotNull(message = "uuid проекта не должен быть null")
    private UUID uuid;

    public ProjectUuidDTO() {
    }

    public ProjectUuidDTO(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
