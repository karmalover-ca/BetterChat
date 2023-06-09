package ca.karmalover.betterchat.events;

import ca.karmalover.betterchat.BetterChat;
import dev.vankka.enhancedlegacytext.EnhancedLegacyText;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;

public class PlayerEventHandler  implements Listener {
    private static final TextComponent CHAT_SEPARATOR = Component.text(" >> ", NamedTextColor.DARK_GRAY);

    private final BetterChat core;

    public PlayerEventHandler(BetterChat core) {
        this.core = core;
        Bukkit.getServer().getPluginManager().registerEvents(this, core);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncChatEvent event) {
        if(!event.isAsynchronous()) return;

        Player player = event.getPlayer();
        if(!player.isOnline()) return;

        ItemStack is = new ItemStack(Material.ACACIA_PLANKS, 1);

        event.setCancelled(true);

        sendChatMessageBecauseFUCKMICROSOFT(event.getPlayer(), event.getPlayer().displayName(), event.message(), Audience.audience(Bukkit.getOnlinePlayers()));
    }

    public Component constructPrefixComponent(Player player) {
        LuckPerms luckPerms = core.luckPerms;
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);


        ArrayList<PrefixNode> prefixNodes = new ArrayList<>(user.resolveInheritedNodes(NodeType.PREFIX, QueryOptions.nonContextual()));
        if(prefixNodes.size() < 1) return Component.empty();

        prefixNodes.sort(Comparator.comparingInt(PrefixNode::getPriority));

        String prefix = prefixNodes.get(prefixNodes.size() - 1).getKey().split("\\.")[2];

        return EnhancedLegacyText.get().parse(prefix);
    }

    private Component constructSuffixComponent(Player player) {
        LuckPerms luckPerms = core.luckPerms;
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);


        ArrayList<SuffixNode> suffixNodes = new ArrayList<>(user.resolveInheritedNodes(NodeType.SUFFIX, QueryOptions.nonContextual()));
        if(suffixNodes.size() < 1) return Component.empty();

        suffixNodes.sort(Comparator.comparingInt(SuffixNode::getPriority));

        String suffix = suffixNodes.get(suffixNodes.size() - 1).getKey().split("\\.")[2];

        return EnhancedLegacyText.get().parse(suffix);
    }

    private Component constructNameComponent(Player player) {
        LuckPerms luckPerms = core.luckPerms;
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        Optional<MetaNode> optional = user.getNodes(NodeType.INHERITANCE).stream()
                .map(inheritanceNode -> luckPerms.getGroupManager().getGroup(inheritanceNode.getGroupName()))
                .flatMap((Function<Group, Stream<MetaNode>>) group -> {
                    if (group == null) return Stream.empty();
                    return group.getNodes(NodeType.META).stream();
                })
                .filter(node -> node.getMetaKey().equals("namecolor"))
                .findAny();

        if (optional.isEmpty()) return Component.text(player.getName());

        String nameColorNode = optional.get().getMetaValue();

        return EnhancedLegacyText.get().buildComponent(nameColorNode + player.getName()).build();
    }

    private void sendChatMessageBecauseFUCKMICROSOFT(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        Component prefix = constructPrefixComponent(source);
        Component suffix = constructSuffixComponent(source);
        Component displayName = constructNameComponent(source);


        String displayNamePlain = PlainTextComponentSerializer.plainText().serialize(sourceDisplayName);

        String messagePlain = PlainTextComponentSerializer.plainText().serialize(message);
        if(!message.hasStyling()) {
            message = EnhancedLegacyText.get().parse(messagePlain);
        }

        //Component component = Component.join(JoinConfiguration.separator(CHAT_SEPARATOR), prefix.append(sourceDisplayName).append(suffix), message);
        Component component = Component.text().append(prefix, displayName, suffix, CHAT_SEPARATOR, message).build();

        viewer.sendMessage(component);

        core.getLogger().log(Level.INFO, displayNamePlain + " >> " + messagePlain);
    }
}
