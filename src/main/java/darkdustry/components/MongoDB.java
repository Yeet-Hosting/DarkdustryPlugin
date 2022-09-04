package darkdustry.components;

import arc.struct.Seq;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.reactivestreams.client.*;
import darkdustry.DarkdustryPlugin;
import reactor.core.publisher.*;

import java.util.List;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.*;
import static darkdustry.PluginVars.config;
import static org.bson.codecs.configuration.CodecRegistries.*;
import static org.bson.codecs.pojo.PojoCodecProvider.builder;

public class MongoDB {

    public static MongoClient client;
    public static MongoCollection<PlayerData> collection;

    public static void connect() {
        try {
            client = MongoClients.create(config.mongoUrl);
            collection = client.getDatabase("darkdustry")
                    .withCodecRegistry(fromRegistries(getDefaultCodecRegistry(), fromProviders(builder().automatic(true).build())))
                    .getCollection("players", PlayerData.class);

            DarkdustryPlugin.info("Database connected.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect to the database: @", e);
        }
    }

    public static Mono<PlayerData> getPlayerData(String uuid) {
        return Mono.from(collection.find(eq("uuid", uuid))).defaultIfEmpty(new PlayerData(uuid));
    }

    public static Flux<PlayerData> getPlayersData(Seq<String> uuids) {
        return Flux.from(collection.find(all("uuid", uuids)));
    }

    public static void setPlayerData(PlayerData data) {
        Mono.from(collection.replaceOne(eq("uuid", data.uuid), data))
                .filter(r -> r.getModifiedCount() < 1)
                .flatMap(r -> Mono.from(collection.insertOne(data)))
                .subscribe();
    }

    public static Mono<Void> setPlayersData(List<PlayerData> data) {
        return Mono.from(collection.bulkWrite(data.stream()
                        .map(p -> new ReplaceOneModel<>(eq("uuid", p.uuid), p))
                        .toList()))
                .then();
    }

    public static class PlayerData {
        public String uuid;
        public String language = "off";

        public boolean welcomeMessage = true;
        public boolean alertsEnabled = true;

        public int playTime = 0;
        public int buildingsBuilt = 0;
        public int gamesPlayed = 0;

        public int rank = 0;

        public PlayerData() {}

        public PlayerData(String uuid) {
            this.uuid = uuid;
        }
    }
}
