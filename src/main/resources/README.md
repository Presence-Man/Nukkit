# Configuration

> Environment variables overwrite the config.yml value.

| Key | Variable name | Type | Description |
|:----:|:-----:|:----:|:----:|
| token                             | `PRESENCE_MAN_TOKEN`                    | String  | The token you obtain from our discord bot. |
| client_id                         | `PRESENCE_MAN_CLIENT_ID`                | String  | Discord application id |
| server                            | `PRESENCE_MAN_SERVER`                   | String  | The name of the server that will be displayed in the presences. |
| update_skin                       | `PRESENCE_MAN_UPDATE_SKIN`              | Boolean | *Player's skin will be sent to our backend server to store the head and show it in the presences.* Should only be enabled on lobby servers, otherwise you may get rate-limited for your entire network. |
| default_presence.enabled          | `PRESENCE_MAN_DEFAULT_ENABLED`          | Boolean | Apply the default presence on join. |
| default_presence.state            | `PRESENCE_MAN_DEFAULT_STATE`            | String | The default presence state. |
| default_presence.details          | `PRESENCE_MAN_DEFAULT_DETAILS`          | String | The default presence details. |
| default_presence.large_image_text | `PRESENCE_MAN_DEFAULT_LARGE_IMAGE_TEXT` | String | The text above the large image. |
| default_presence.large_image_key  | `PRESENCE_MAN_DEFAULT_LARGE_IMAGE_KEY`  | String | The large image asset-key from discord developer application portal. |
