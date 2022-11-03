package darkdustry.discord;

import darkdustry.features.Authme;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static arc.Core.app;
import static darkdustry.PluginVars.loginWaiting;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.notAdmin;

public class DiscordListeners extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.isFromGuild() || event.getMessage().getContentRaw().isEmpty()) return;

        if (handleMessage(new Context(event))) return;

        if (event.isFromGuild() && event.getChannel() == botChannel)
            app.post(() -> sendMessageToGame(event.getMember(), event.getMessage()));
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!loginWaiting.containsKey(event.getMessage()) || notAdmin(event)) return;

        if (event.getComponentId().equals("authme"))
            switch (event.getValues().get(0)) {
                case "authme.confirm" -> Authme.confirm(event);
                case "authme.deny" -> Authme.deny(event);
                case "authme.info" -> Authme.information(event);
            }
    }
}