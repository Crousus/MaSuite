package dev.masa.masuite.paper.commands.home;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.masa.masuite.common.objects.MaSuiteMessage;
import dev.masa.masuite.paper.utils.BukkitPluginMessage;
import org.bukkit.entity.Player;

@CommandAlias("delhome|deletehome|removehome")
public class DeleteHomeCommand extends BaseCommand {

    @Default()
    @CommandPermission("masuite.home.delete")
    @Description("Delete home")
    @CommandCompletion("@homes")
    public void deleteHome(Player player, @Single @Default("home") String home) {
        BukkitPluginMessage bpm = new BukkitPluginMessage(player, MaSuiteMessage.HOMES_DELETE, home);
        bpm.send();
    }

    @Default()
    @CommandPermission("masuite.home.delete.others")
    @Description("Delete other player's home")
    @CommandCompletion("@masuite_players")
    public void deleteHome(Player player, String user, @Single String home) {
        BukkitPluginMessage bpm = new BukkitPluginMessage(player, MaSuiteMessage.HOMES_DELETE_OTHERS, user, home);
        bpm.send();
    }

}
