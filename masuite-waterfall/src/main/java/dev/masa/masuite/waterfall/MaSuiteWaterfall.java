package dev.masa.masuite.waterfall;

import dev.masa.masuite.common.AbstractMaSuitePlugin;
import dev.masa.masuite.common.configuration.MaSuiteConfig;
import dev.masa.masuite.common.configuration.MessagesConfig;
import dev.masa.masuite.common.objects.MaSuiteMessage;
import dev.masa.masuite.common.services.DatabaseService;
import dev.masa.masuite.common.services.HomeService;
import dev.masa.masuite.common.services.UserService;
import dev.masa.masuite.common.services.WarpService;
import dev.masa.masuite.waterfall.listeners.home.DeleteHomeMessageListener;
import dev.masa.masuite.waterfall.listeners.home.ListHomeMessageListener;
import dev.masa.masuite.waterfall.listeners.home.SetHomeMessageListener;
import dev.masa.masuite.waterfall.listeners.home.TeleportHomeMessageListener;
import dev.masa.masuite.waterfall.listeners.teleport.TeleportMessageListener;
import dev.masa.masuite.waterfall.listeners.teleport.TeleportRequestListener;
import dev.masa.masuite.waterfall.listeners.user.UserLeaveListener;
import dev.masa.masuite.waterfall.listeners.user.UserLoginListener;
import dev.masa.masuite.waterfall.listeners.warp.DeleteWarpMessageListener;
import dev.masa.masuite.waterfall.listeners.warp.ListWarpMessageListener;
import dev.masa.masuite.waterfall.listeners.warp.SetWarpMessageListener;
import dev.masa.masuite.waterfall.listeners.warp.TeleportWarpMessageListener;
import dev.masa.masuite.waterfall.services.TeleportationService;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;

@Accessors(fluent = true)
public final class MaSuiteWaterfall extends AbstractMaSuitePlugin<MaSuiteWaterfallLoader> {

    @Getter
    private MaSuiteWaterfallLoader loader = null;

    @Getter
    private UserService userService;

    @Getter
    private DatabaseService databaseService;

    @Getter
    private HomeService homeService;

    @Getter
    private WarpService warpService;

    @Getter
    private TeleportationService teleportationService;

    @Getter
    private MaSuiteConfig config;

    @Getter
    private MessagesConfig messages;

    private BungeeAudiences adventure;

    public BungeeAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        }
        return this.adventure;
    }

    @Override
    public void loader(MaSuiteWaterfallLoader loader) throws IllegalStateException {
        if (this.loader != null) {
            throw new IllegalStateException("Plugin is already initialized");
        }
        this.loader = loader;
    }

    @Override
    public void onEnable() {
        this.adventure = BungeeAudiences.create(this.loader);
        // Generate configs
        this.generateConfigs();

        // Initialize services
        try {
            this.databaseService = new DatabaseService(this.config().database().databaseAddress(), this.config().database().databasePort(), this.config().database().databaseName(), this.config().database().databaseUsername(), this.config().database().databasePassword());
        } catch (SQLException ex) {
            ex.printStackTrace();
            this.loader.getLogger().log(Level.SEVERE, "Could not connect to database.");
        }

        this.userService = new UserService(databaseService);
        this.homeService = new HomeService(databaseService);
        this.warpService = new WarpService(databaseService);
        this.teleportationService = new TeleportationService(this);

        // Add listeners
        this.loader.getProxy().getPluginManager().registerListener(this.loader, new UserLoginListener(this));
        this.loader.getProxy().getPluginManager().registerListener(this.loader, new UserLeaveListener(this));

        this.loader.getProxy().getPluginManager().registerListener(this.loader, new SetHomeMessageListener(this));
        this.loader.getProxy().getPluginManager().registerListener(this.loader, new TeleportHomeMessageListener(this));
        this.loader.getProxy().getPluginManager().registerListener(this.loader, new DeleteHomeMessageListener(this));
        this.loader.getProxy().getPluginManager().registerListener(this.loader, new ListHomeMessageListener(this));

        this.loader.getProxy().getPluginManager().registerListener(this.loader, new SetWarpMessageListener(this));
        this.loader.getProxy().getPluginManager().registerListener(this.loader, new TeleportWarpMessageListener(this));
        this.loader.getProxy().getPluginManager().registerListener(this.loader, new DeleteWarpMessageListener(this));
        this.loader.getProxy().getPluginManager().registerListener(this.loader, new ListWarpMessageListener(this));

        this.loader.getProxy().getPluginManager().registerListener(this.loader, new TeleportMessageListener(this));
        this.loader.getProxy().getPluginManager().registerListener(this.loader, new TeleportRequestListener(this));

        this.loader.getProxy().registerChannel(MaSuiteMessage.MAIN.channel);
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    private void generateConfigs() {
        // config.yml
        YamlConfigurationLoader configLoader = YamlConfigurationLoader.builder()
                .file(new File("plugins/MaSuite/config.yml"))
                .defaultOptions(opts ->
                        opts.shouldCopyDefaults(true)
                )
                .nodeStyle(NodeStyle.BLOCK)
                .build();

        CommentedConfigurationNode configNode;
        try {
            configNode = configLoader.load();
            this.config = MaSuiteConfig.loadFrom(configNode);
            this.config.saveTo(configNode);
            configLoader.save(configNode);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }

        // messages.yml
        YamlConfigurationLoader messagesLoader = YamlConfigurationLoader.builder()
                .file(new File("plugins/MaSuite/messages.yml"))
                .defaultOptions(opts -> opts.shouldCopyDefaults(true))
                .nodeStyle(NodeStyle.BLOCK)
                .build();

        CommentedConfigurationNode messagesNode;
        try {
            messagesNode = messagesLoader.load();
            this.messages = MessagesConfig.loadFrom(messagesNode);
            this.messages.saveTo(messagesNode);
            messagesLoader.save(messagesNode);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }
}
