package darkdustry.discord;

import darkdustry.DarkdustryPlugin;
import darkdustry.commands.DiscordCommands;
import darkdustry.features.Authme;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static arc.Core.app;
import static darkdustry.PluginVars.loginWaiting;
import static darkdustry.discord.Bot.botChannel;
import static darkdustry.discord.Bot.sendMessageToGame;
import static darkdustry.utils.Checks.notAdmin;
import static java.util.Objects.requireNonNull;

public class DiscordListeners extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getMessage().getContentDisplay().isEmpty()) return;
        if (event.isFromGuild() && event.getChannel() == botChannel) // можно кнч объединить в один if, но он будет просто огромным
            app.post(() -> sendMessageToGame(requireNonNull(event.getMember()), event.getMessage()));
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (!loginWaiting.containsKey(event.getMessage()) || notAdmin(event)) return;

        if (event.getComponentId().equals("authme")) {
            switch (event.getValues().get(0)) {
                case "authme.confirm" -> Authme.confirm(event);
                case "authme.deny" -> Authme.deny(event);
                case "authme.info" -> Authme.information(event);
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        DarkdustryPlugin.discord("@ использует /@", requireNonNull(event.getMember()).getEffectiveName(), event.getName());
        DiscordCommands.commands.get(event.getName()).get(event);
    }
}
