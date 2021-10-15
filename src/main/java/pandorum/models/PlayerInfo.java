package pandorum.models;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.reactivestreams.client.MongoCollection;

import org.bson.Document;

import pandorum.database.MongoSchema;
import pandorum.database.NonRequired;
import pandorum.database.Required;

public class PlayerInfo extends MongoSchema<String, Object> {
    public PlayerInfo(MongoCollection<Document> collection) {
        super(
            collection,
            new Required<>("uuid", String.class),
            new Required<>("hellomsg", Boolean.class),
            new Required<>("alerts", Boolean.class),
            new Required<>("playtime", Long.class),
            new Required<>("buildings", Long.class),
            new Required<>("waves", Integer.class),
            new NonRequired<>("locale", String.class)
        );
    }

    public Document create(String uuid, Boolean hellomsg, Boolean alerts, String locale, long playtime, long buildings, int waves) {
        HashMap<String, Object> schema = new HashMap<>(Map.of(
                "uuid", uuid,
                "hellomsg", hellomsg,
                "alerts", alerts,
                "playtime", playtime,
                "buildings", buildings,
                "waves", waves
        ));

        schema.put("locale", locale);
        return this.create(schema);
    }
}
