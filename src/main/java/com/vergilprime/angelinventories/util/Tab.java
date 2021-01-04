package com.vergilprime.angelinventories.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class Tab {

    public static final int maxTab = 100;

    public static Predicate<String> testPrefix(String[] args) {
        return testPrefix(args[args.length - 1]);
    }

    public static Predicate<String> testPrefix(String prefix) {
        if (prefix != null && prefix.length() > 0) {
            return s -> s.toLowerCase().startsWith(prefix.toLowerCase());
        } else {
            return s -> true;
        }
    }

    public static Stream<String> getPlayerList() {
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName);
    }

}
