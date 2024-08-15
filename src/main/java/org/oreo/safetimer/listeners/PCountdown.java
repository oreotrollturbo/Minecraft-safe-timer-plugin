package org.oreo.safetimer.listeners;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.oreo.safetimer.Pvp_timer;

public class PCountdown implements Listener {//This is how long(in secs) you want the countdown to be!

    private Pvp_timer plugin; //Making it so we can access the scoreboard board and objective o

    public PCountdown(Pvp_timer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void pjoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer(); //Defining the player
        //Getting online players
        p.setScoreboard(plugin.timerBoard); //Making it so the player can see the scoreboard
    }}

