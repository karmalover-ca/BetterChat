package ca.karmalover.betterchat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Constants {

    //todo fix this
    public static final Component MESSAGE_PREFIX = Component.text().append(Component.text("BetterChat ", NamedTextColor.AQUA), Component.text(">> ", NamedTextColor.DARK_GRAY)).build();
}
