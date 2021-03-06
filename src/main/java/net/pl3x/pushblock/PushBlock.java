package net.pl3x.pushblock;

import net.pl3x.pushblock.configuration.ConfManager;
import net.pl3x.pushblock.listeners.BlokListener;
import net.pl3x.pushblock.listeners.PlayerListener;
import net.pl3x.pushblock.listeners.WorldListener;
import net.pl3x.pushblock.listeners.blok.Blok;
import net.pl3x.pushblock.listeners.blok.BlokManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;

public class PushBlock extends JavaPlugin {

    private BlokManager blokManager;
    private ConfManager confManager;

    @Override
    public void onEnable() {
        if (!new File(getDataFolder() + File.separator + "config.yml").exists())
            saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlokListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(this), this);

        blokManager = new BlokManager();
        confManager = ConfManager.getConfManager();

        loadAllBloks();

        log(getName() + " v" + getDescription().getVersion() + " by BillyGalbreath enabled!");
    }

    @Override
    public void onDisable() {
        confManager.forceSave();
        log(getName() + " Disabled.");
    }

    public void log(Object obj) {
        if (getConfig().getBoolean("color-logs", true)) {
            getServer().getConsoleSender().sendMessage(colorize("&3[&d" + getName() + "&3]&r " + obj));
        } else {
            Bukkit.getLogger().log(Level.INFO, "[" + getName() + "] " + ((String) obj).replaceAll("(?)\u00a7([a-f0-9k-or])", ""));
        }
    }

    public void debug(Object obj) {
        if (getConfig().getBoolean("debug-mode", false))
            log(obj);
    }

    public String colorize(String str) {
        return str.replaceAll("(?i)&([a-f0-9k-or])", "\u00a7$1");
    }

    public BlokManager getBlokManager() {
        return blokManager;
    }

    public void loadAllBloks() {
        ConfManager cm = ConfManager.getConfManager();
        if (!cm.exists() || cm.get("blocks") == null)
            return;
        Map<String, Object> opts = cm.getConfigurationSection("blocks").getValues(false);
        if (opts.keySet().isEmpty())
            return;
        for (String idStr : opts.keySet()) {
            Integer id;
            try {
                id = Integer.valueOf(idStr);
            } catch (Exception e) {
                debug("Malformed ID in blocks.yml: ID: " + idStr);
                continue;
            }
            String worldName = cm.getString("blocks." + idStr + ".w");
            Integer x = cm.getInt("blocks." + idStr + ".x");
            Integer y = cm.getInt("blocks." + idStr + ".y");
            Integer z = cm.getInt("blocks." + idStr + ".z");
            Integer ox = cm.getInt("blocks." + idStr + ".ox");
            Integer oy = cm.getInt("blocks." + idStr + ".oy");
            Integer oz = cm.getInt("blocks." + idStr + ".oz");
            Blok blok;
            if (ox == null || oy == null || oz == null)
                blok = new Blok(id, worldName, x, y, z);
            else
                blok = new Blok(id, worldName, x, y, z, ox, oy, oz);
            blokManager.addBlok(blok);
            debug("Loaded block from config. Id: " + idStr + " Location: " + worldName + " " + x + "," + y + "," + z);
        }
    }

}
