package com.vergilprime.angelinventories.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chat {

    public final static String mainPrefix = ChatColor.AQUA + "AngelInventories> " + ChatColor.GRAY;
    public final static String errorPrefix = ChatColor.AQUA + "AngelInventories" + ChatColor.DARK_RED + "> " + ChatColor.GRAY;
    public final static String itemColor = ChatColor.YELLOW + "";
    private final static Pattern itemPattern = Pattern.compile("(\\{[0-9]+\\})");

    public final static String gold = ChatColor.GOLD + "";

    public static String format(String prefix, String msg, Object... objs) {
        msg = prefix + String.join("\n" + prefix, msg.split("\n"));
        Matcher matcher = itemPattern.matcher(msg);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, itemColor + "$1" + ChatColor.getLastColors(msg.substring(0, matcher.start())));
        }
        matcher.appendTail(buffer);
        msg = buffer.toString();
        return MessageFormat.format(msg, objs);
    }

    public static String msg(String prefix, CommandSender receiver, String msg, Object... objs) {
        msg = format(prefix, msg, objs);
        if (receiver != null) {
            receiver.sendMessage(msg);
        }
        return msg;
    }

    public static String main(CommandSender receiver, String msg, Object... objs) {
        return msg(mainPrefix, receiver, msg, objs);
    }

    public static String error(CommandSender receiver, String msg, Object... objs) {
        return msg(errorPrefix, receiver, msg, objs);
    }
}
