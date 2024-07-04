/*
 * Copyright (c) Jan-Michael Sohn
 * All rights reserved.
 * Only people with the explicit permission from Jan Sohn are allowed to modify, share or distribute this code.
 */

package xxAROX.PresenceMan.Nukkit.entity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import xxAROX.PresenceMan.Nukkit.PresenceMan;

import javax.annotation.Nullable;

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
    public Integer party_max_player_count = null;
    public Integer party_player_count = null;

    public ApiActivity(ActivityType type, String state, String details, Long end, String large_icon_key, String large_icon_text, Integer party_max_player_count, Integer party_player_count){
        this.type = type;
        this.state = state;
        this.details = details;
        this.end = end;
        this.large_icon_key = large_icon_key;
        this.large_icon_text = large_icon_text;
        this.party_max_player_count = party_max_player_count;
        this.party_player_count = party_player_count;
    }

    public ApiActivity(ActivityType type, String state, String details, Long end, String large_icon_key, String large_icon_text){
        this.type = type;
        this.state = state;
        this.details = details;
        this.end = end;
        this.large_icon_key = large_icon_key;
        this.large_icon_text = large_icon_text;
    }

    public JsonObject serialize(){
        JsonObject json = new JsonObject();
        json.addProperty("client_id", PresenceMan.client_id);
        json.addProperty("type", type.toString());
        json.addProperty("state", state);
        json.addProperty("details", details);
        json.addProperty("end", end);
        json.addProperty("large_icon_key", large_icon_key);
        json.addProperty("large_icon_text", large_icon_text);
        json.addProperty("party_max_player_count", party_max_player_count);
        json.addProperty("party_player_count", party_player_count);
        return json;
    }

    public static ApiActivity deserialize(String input){
        JsonObject json = new Gson().fromJson(input, JsonObject.class);
        String __type = (json.has("type") && !json.get("type").isJsonNull() ? json.get("type").getAsString() : ActivityType.PLAYING.toString()).toUpperCase();
        ActivityType type = ActivityType.valueOf(__type);
        String state = (json.has("state") && !json.get("state").isJsonNull() ? json.get("state").getAsString() : null);
        String details = (json.has("details") && !json.get("details").isJsonNull() ? json.get("details").getAsString() : null);
        Long end = (json.has("end") && !json.get("end").isJsonNull() ? json.get("end").getAsLong() : null);
        String large_icon_key = (json.has("large_icon_key") && !json.get("large_icon_key").isJsonNull() ? json.get("large_icon_key").getAsString() : null);
        String large_icon_text = (json.has("large_icon_text") && !json.get("large_icon_text").isJsonNull() ? json.get("large_icon_text").getAsString() : null);
        Integer party_max_player_count = (json.has("party_max_player_count") && !json.get("party_max_player_count").isJsonNull() ? json.get("party_max_player_count").getAsInt() : null);
        Integer party_player_count = (json.has("'party_player_count'") && !json.get("'party_player_count'").isJsonNull() ? json.get("'party_player_count'").getAsInt() : null);
        return new ApiActivity(type, state, details, end, large_icon_key, large_icon_text, party_max_player_count, party_player_count);
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
