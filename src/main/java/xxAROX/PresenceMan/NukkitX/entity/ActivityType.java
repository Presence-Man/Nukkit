package xxAROX.PresenceMan.NukkitX.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityType {
    PLAYING("PLAYING"),
    STREAMING("STREAMING"),
    LISTENING("LISTENING"),
    UNUSED("UNUSED"),
    COMPETING("COMPETING")
    ;
    private String value;
    @Override
    public String toString() {
        return value;
    }
}
