package xxAROX.PresenceMan.NukkitX.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ActivityType {
    COMPETING("COMPETING"),
    LISTENING("LISTENING"),
    PLAYING("PLAYING"),
    STREAMING("STREAMING"),
    UNUSED("UNUSED"),
    ;
    private String value;
    @Override
    public String toString() {
        return value;
    }
}
