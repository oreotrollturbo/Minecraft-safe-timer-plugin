package org.oreo.safetimer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.oreo.safetimer.listeners.PCountdown;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Pvp_timer extends JavaPlugin implements Listener, CommandExecutor , TabCompleter {

    private final int safeTime = this.getConfig().getInt("safe-timer") * 60;
    private final int pvpTime = this.getConfig().getInt("pvp-timer") * 60;
    private boolean isPvpEnabled = this.getConfig().getBoolean("start-with-pvp-on");

    private final String mainPvpMessage = this.getConfig().getString("pvp-started-main-message");
    private final String bottomPvpMessage = this.getConfig().getString("pvp-started-bottom-text");

    private final String mainSafeMessage = this.getConfig().getString("pvp-ended-main-message");
    private final String bottomSafeMessage = this.getConfig().getString("pvp-ended-bottom-text");

    private final String mainPausedMessage = this.getConfig().getString("timer-paused-main-message");
    private final String bottomPausedMessage = this.getConfig().getString("timer-paused-bottom-text");


    private final String mainResumedMessage = this.getConfig().getString("timer-resumed-main-message");
    private final String bottomResumedMessage = this.getConfig().getString("timer-resumed-bottom-text");



    public Scoreboard timerBoard = null;
    public Objective timerObj = null;

    private boolean pvpForceEnded = false;
    private boolean safeForceEnded = false;
    private boolean timerPaused = false;
    private int currentTime;

    //###############################################
    //                                              #
    //                INITIALISATION                #
    //                                              #
    //###############################################
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PCountdown(this), this);

        saveDefaultConfig();

        // Create a new scoreboard and objective
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("timer", "dummy", "If you can see this something went wrong :(");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.timerBoard = board;
        this.timerObj = objective;

        if (isPvpEnabled){
            startPvpTimer(board,objective);
        }else {
            startSafeTimer(board, objective);
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    //###############################################
    //                                              #
    //                SAFE TIMER START              #
    //                                              #
    //###############################################

    private void startSafeTimer(Scoreboard board, Objective objective) {

        objective.setDisplayName(ChatColor.GREEN + "PVP DISABLED");
        isPvpEnabled = false;

        currentTime = safeTime;


        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            private boolean secondsLeft = false;

            @Override
            public void run() {
                if (currentTime > 0 && !safeForceEnded) {

                    if (timerPaused){
                        return;
                    }

                    currentTime--;

                    if (secondsLeft) {
                        // Update seconds left
                        Score score = objective.getScore("Seconds left");
                        score.setScore(currentTime);
                    } else {
                        // Update minutes left
                        int minutes = currentTime / 60;
                        Score score = objective.getScore("Minutes left");
                        score.setScore(minutes);

                        // Switch to seconds mode when timer is below 60 seconds
                        if (currentTime <= 60) {
                            secondsLeft = true;
                            // Remove "Minutes left" score
                            board.resetScores("Minutes left");
                            // Set up "Seconds left" score
                            Score secondsScore = objective.getScore("Seconds left");
                            secondsScore.setScore(currentTime);

                            for (Player player : Bukkit.getOnlinePlayers()){
                                player.sendMessage(ChatColor.DARK_RED + "PvP will start soon");
                            }
                        }
                    }

                    // Update the scoreboard for all players
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.setScoreboard(board);
                    }
                } else {
                    getLogger().info("Safe timer has ended");
                    safeForceEnded = false;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.0f);
                        sendTitle(player, ChatColor.RED + mainPvpMessage, ChatColor.YELLOW + bottomPvpMessage, 10, 70, 10);
                    }
                    board.resetScores("Seconds left");
                    Bukkit.getScheduler().cancelTasks(Pvp_timer.this);
                    startPvpTimer(board, objective);
                }
            }
        }, 0L, 20L);
    }

    //###############################################
    //                                              #
    //                 PVP TIMER START              #
    //                                              #
    //###############################################

    private void startPvpTimer(Scoreboard board, Objective objective) {

        objective.setDisplayName(ChatColor.RED + "PVP ENABLED");
        isPvpEnabled = true;

        currentTime = pvpTime;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            private boolean secondsLeft = false;

            @Override
            public void run() {
                if (currentTime > 0 && !pvpForceEnded) {

                    if (timerPaused){
                        return;
                    }

                    currentTime--;

                    if (secondsLeft) {
                        // Update seconds left
                        Score score = objective.getScore("Seconds left");
                        score.setScore(currentTime);
                    } else {
                        // Update minutes left
                        int minutes = currentTime / 60;
                        Score score = objective.getScore("Minutes left");
                        score.setScore(minutes);

                        // Switch to seconds mode when timer is below 60 seconds
                        if (currentTime <= 60) {
                            secondsLeft = true;
                            // Remove "Minutes left" score
                            board.resetScores("Minutes left");
                            // Set up "Seconds left" score
                            Score secondsScore = objective.getScore("Seconds left");
                            secondsScore.setScore(currentTime);

                            for (Player player : Bukkit.getOnlinePlayers()){
                                player.sendMessage(ChatColor.DARK_GREEN + "PvP will end soon");
                            }
                        }
                    }

                    // Update the scoreboard for all players
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.setScoreboard(board);
                    }
                } else {
                    getLogger().info("PvP timer has ended");
                    pvpForceEnded = false;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1.0f, 0.0f);
                        sendTitle(player, ChatColor.GREEN + mainSafeMessage, ChatColor.YELLOW + bottomSafeMessage, 10, 70, 10);
                    }
                    board.resetScores("Seconds left");
                    Bukkit.getScheduler().cancelTasks(Pvp_timer.this);
                    startSafeTimer(board, objective);
                }
            }
        }, 0L, 20L);
    }

    //method to send cool messages to players

    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    private void forceUpdateScore(Scoreboard board, Objective objective,int time){
        board.resetScores("Seconds left");
        Score score = objective.getScore("Minutes left");
        score.setScore(time);
    }


    //###############################################
    //                                              #
    //             PLAYER JOIN HANDLER              #
    //                                              #
    //###############################################

    @EventHandler
    public void damage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player && !isPvpEnabled){
            e.setCancelled(true);
        }
    }


    //###############################################
    //                                              #
    //                COMMAND HANDLER               #
    //                                              #
    //###############################################


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if there are any arguments
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "No command specified.");
            return false;
        }

        String mainCommand = args[0].toLowerCase(); // Primary command keyword
        switch (mainCommand) {
            case "force":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "No subcommand specified for 'force'.");
                    return false;
                }

                String forceSubcommand = args[1].toLowerCase();
                switch (forceSubcommand) {
                    case "truce":
                        sender.sendMessage(ChatColor.AQUA + "Safe timer has been started");
                        pvpForceEnded = true;
                        break;

                    case "pvp":
                        sender.sendMessage(ChatColor.AQUA + "Pvp timer has been started");
                        safeForceEnded = true;
                        break;

                    default:
                        sender.sendMessage(ChatColor.RED + "Unknown subcommand for 'force': " + forceSubcommand);
                        return false;
                }
                break;

            case "timer":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "No subcommand specified for 'timer'.");
                    return false;
                }

                String timerSubcommand = args[1].toLowerCase();
                switch (timerSubcommand) {
                    case "pause":
                        if (timerPaused){
                            sender.sendMessage(ChatColor.AQUA + "Resuming pvp timer");
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1.0f, 0.0f);
                                sendTitle(player, ChatColor.GRAY + mainResumedMessage, ChatColor.YELLOW + bottomResumedMessage, 6, 60, 5);
                            }
                            timerPaused = false;
                        }else {
                            sender.sendMessage(ChatColor.AQUA + "Pausing pvp timer");
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.playSound(player.getLocation(), Sound.ENTITY_CAMEL_AMBIENT, 1.0f, 0.0f);
                                sendTitle(player, ChatColor.GRAY + mainPausedMessage, ChatColor.YELLOW + bottomPausedMessage, 6, 60, 5);
                            }
                            timerPaused = true;
                        }
                        break;

                    case "set":
                        if (args.length < 3) {
                            sender.sendMessage(ChatColor.RED + "No time specified for 'timer set'.");
                            return false;
                        }

                        try {
                            int time = Integer.parseInt(args[2]);
                            if (time == 1){
                                sender.sendMessage(ChatColor.AQUA + "Setting pvp timer to " + time + " minute.");
                            }else {
                                sender.sendMessage(ChatColor.AQUA + "Setting pvp timer to " + time + " minutes.");
                            }
                            currentTime = time * 60;
                            forceUpdateScore(timerBoard,timerObj,time);

                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "Invalid time specified. Please enter a number.");
                            return false;
                        }
                        break;

                    default:
                        sender.sendMessage(ChatColor.RED + "Unknown subcommand for 'timer': " + timerSubcommand);
                        return false;
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown main command: " + mainCommand);
                return false;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        if (!commandSender.isOp()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            // Top-level commands
            return Stream.of("force", "timer")
                    .filter(cmd -> cmd.startsWith(args[0]))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Subcommands for "force"
            if (args[0].equalsIgnoreCase("force")) {
                return Stream.of("truce", "pvp")
                        .filter(subcmd -> subcmd.startsWith(args[1]))
                        .collect(Collectors.toList());
            }

            // Subcommands for "timer"
            if (args[0].equalsIgnoreCase("timer")) {
                return Stream.of("pause", "set")
                        .filter(subcmd -> subcmd.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            // Autocompletion for the time value in "timer set"
            if (args[0].equalsIgnoreCase("timer") && args[1].equalsIgnoreCase("set")) {
                return Stream.of("10", "30", "60")
                        .filter(time -> time.startsWith(args[2]))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

}
