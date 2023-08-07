package org.example.core.dto;

import java.util.UUID;

public class UserDTO {

    private UUID uuid;

    public UserDTO() {
    }

    public UserDTO(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
