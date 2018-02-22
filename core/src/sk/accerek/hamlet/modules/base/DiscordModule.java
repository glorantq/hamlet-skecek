package sk.accerek.hamlet.modules.base;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.modules.HamletModule;
import sk.accerek.hamlet.modules.IModule;

@HamletModule(name = "Discord Rich Presence")
public class DiscordModule implements IModule {
    private final DiscordRPC discordRPC = DiscordRPC.INSTANCE;
    private final String APPLICATION_ID = "414206821046419456";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final EventBus eventBus = Hamlet.get().getEventBus();

    @Override
    public void create() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        eventHandlers.ready = () -> {
            logger.debug("Discord RPC Connected!");
        };

        discordRPC.Discord_Initialize(APPLICATION_ID, eventHandlers, true, null);

        new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                discordRPC.Discord_RunCallbacks();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) { }
            }
        }, "Discord Updater Thread").start();

        eventBus.register(this);
    }

    @Override
    public void update() {

    }

    @Override
    public void destroy() {
        eventBus.unregister(this);
        discordRPC.Discord_Shutdown();

        logger.debug("Discord RPC shut down!");
    }

    @Subscribe
    private void handlePresenceUpdate(DiscordPresenceUpdate presenceUpdate) {
        DiscordRichPresence richPresence = new DiscordRichPresence();

        richPresence.state = presenceUpdate.status;
        richPresence.details = presenceUpdate.details;
        richPresence.largeImageKey = presenceUpdate.largeImageKey;
        richPresence.largeImageText = presenceUpdate.largeImageText;
        richPresence.smallImageKey = presenceUpdate.smallImageKey;
        richPresence.smallImageText = presenceUpdate.smallImageText;

        if(presenceUpdate.withTimestamp) {
            richPresence.startTimestamp = System.currentTimeMillis() / 1000;
        }

        discordRPC.Discord_UpdatePresence(richPresence);

        logger.debug("Updated Discord presence!");
    }

    @Builder
    public static class DiscordPresenceUpdate {
        private String status;
        private String details;
        private String largeImageKey;
        private String largeImageText;
        private String smallImageKey;
        private String smallImageText;

        private boolean withTimestamp;
    }
}
