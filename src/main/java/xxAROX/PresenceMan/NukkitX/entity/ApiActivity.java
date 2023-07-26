package xxAROX.PresenceMan.NukkitX.entity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.*;
import lombok.experimental.Accessors;
import xxAROX.PresenceMan.NukkitX.PresenceMan;

import javax.annotation.Nullable;
import java.io.InputStreamReader;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter @Setter @Accessors(chain = true)
public class ApiActivity {
    public ActivityType type;
    public String state;
    public String details;
    public Long end = null;
    public String large_icon_key = null;
    public String large_icon_text = null;
    public String small_icon_key = null;
    public String small_icon_text = null;
    public Integer party_max_player_count = null;
    public Integer party_player_count = null;

    public String serialize(){
        JsonObject json = new JsonObject();
        json.addProperty("client_id", "HERE");
        json.addProperty("state", state);
        json.addProperty("details", details);
        json.addProperty("end", end);
        json.addProperty("large_icon_key", large_icon_key);
        json.addProperty("large_icon_text", large_icon_text);
        json.addProperty("small_icon_key", small_icon_key);
        json.addProperty("small_icon_text", small_icon_text);
        json.addProperty("party_max_player_count", party_max_player_count);
        json.addProperty("party_player_count", party_player_count);
        return json.toString();
    }

    public static ApiActivity deserialize(String input){
        JsonObject json = new Gson().fromJson(input, JsonObject.class);
        String __type = (json.has("type") && !json.get("type").isJsonNull() ? json.get("type").getAsString() : ActivityType.UNUSED.toString()).toUpperCase();
        ActivityType type = ActivityType.valueOf(__type);
        String state = (json.has("state") && !json.get("state").isJsonNull() ? json.get("state").getAsString() : null);
        String details = (json.has("details") && !json.get("details").isJsonNull() ? json.get("details").getAsString() : null);
        Long end = (json.has("end") && !json.get("end").isJsonNull() ? json.get("end").getAsLong() : null);
        String large_icon_key = (json.has("large_icon_key") && !json.get("large_icon_key").isJsonNull() ? json.get("large_icon_key").getAsString() : null);
        String large_icon_text = (json.has("large_icon_text") && !json.get("large_icon_text").isJsonNull() ? json.get("large_icon_text").getAsString() : null);
        String small_icon_key = (json.has("small_icon_key") && !json.get("small_icon_key").isJsonNull() ? json.get("small_icon_key").getAsString() : null);
        String small_icon_text = (json.has("small_icon_text") && !json.get("small_icon_text").isJsonNull() ? json.get("small_icon_text").getAsString() : null);
        Integer party_max_player_count = (json.has("party_max_player_count") && !json.get("party_max_player_count").isJsonNull() ? json.get("party_max_player_count").getAsInt() : null);
        Integer party_player_count = (json.has("'party_player_count'") && !json.get("'party_player_count'").isJsonNull() ? json.get("'party_player_count'").getAsInt() : null);
        return new ApiActivity(type, state, details, end, large_icon_key, large_icon_text, small_icon_key, small_icon_text, party_max_player_count, party_player_count);
    }

    public final static class Defaults {
        public static ApiActivity default_activity(){
            return PresenceMan.default_activity;
        }

        /**
         * time = System.currentTimeMillis(): long
         */
        public static ApiActivity ends_in(long time, @Nullable ApiActivity base){
            if (base == null) base = default_activity();
            base.end = time;
            return base;
        }
        public static ApiActivity players_left(int current_players, int max_players, @Nullable ApiActivity base){
            if (base == null) base = default_activity();
            base.party_player_count = current_players;
            base.party_max_player_count = max_players;
            return base;
        }
    }
}
