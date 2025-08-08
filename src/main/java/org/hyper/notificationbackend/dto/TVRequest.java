package org.hyper.notificationbackend.dto;

import org.hyper.notificationbackend.models.TV;

/**
 * DTO for TV creation and update requests
 */
public class TVRequest {
    private String name;
    private String displayName;
    private String description;
    private String location;
    private Boolean active;

    // Constructors
    public TVRequest() {}

    public TVRequest(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
        this.active = true;
    }

    public TVRequest(String name, String displayName, String description, String location) {
        this(name, displayName);
        this.description = description;
        this.location = location;
    }

    // Convert to TV entity
    public TV toEntity() {
        TV tv = new TV();
        tv.setName(this.name);
        tv.setDisplayName(this.displayName);
        tv.setDescription(this.description);
        tv.setLocation(this.location);
        if (this.active != null) {
            tv.setActive(this.active);
        }
        return tv;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "TVRequest{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", active=" + active +
                '}';
    }
}
