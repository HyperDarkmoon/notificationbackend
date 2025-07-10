package org.hyper.notificationbackend.models;

/**
 * Enum representing the fixed set of TVs in the system.
 */
public enum TVEnum {
    TV1("TV 1", "Main Lobby"),
    TV2("TV 2", "Conference Room"),
    TV3("TV 3", "Cafeteria"),
    TV4("TV 4", "Reception");
    
    private final String displayName;
    private final String location;
    private boolean active = true;
    private TVState currentState = TVState.STATIC;
    
    TVEnum(String displayName, String location) {
        this.displayName = displayName;
        this.location = location;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getLocation() {
        return location;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public TVState getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(TVState state) {
        this.currentState = state;
    }
    
    public enum TVState {
        STATIC,  // State 1: showing static data
        TEXT,    // State 2: showing text
        CUSTOM   // State 3: showing custom content
    }
}
