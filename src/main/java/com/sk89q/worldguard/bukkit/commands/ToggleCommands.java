// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit.commands;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import static com.sk89q.worldguard.bukkit.LocaleManager.tr;

public class ToggleCommands {
    private final WorldGuardPlugin plugin;

    public ToggleCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"stopfire"}, usage = "[<world>]",
            desc = "Disables all fire spread temporarily", max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public void stopFire(CommandContext args, CommandSender sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = plugin.checkPlayer(sender).getWorld();
        } else {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(world);

        if (!wcfg.fireSpreadDisableToggle) {
            plugin.getServer().broadcastMessage(BukkitUtil.replaceColorMacros(
                    tr("command.stopfire.broadcast", world.getName(), plugin.toName(sender))));
        } else {
            sender.sendMessage(BukkitUtil.replaceColorMacros(tr("command.stopfire.alreadyDisabled")));
        }

        wcfg.fireSpreadDisableToggle = true;
    }

    @Command(aliases = {"allowfire"}, usage = "[<world>]",
            desc = "Allows all fire spread temporarily", max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public void allowFire(CommandContext args, CommandSender sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = plugin.checkPlayer(sender).getWorld();
        } else {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(world);

        if (wcfg.fireSpreadDisableToggle) {
            plugin.getServer().broadcastMessage(BukkitUtil.replaceColorMacros(
                    tr("command.allowfire.broadcast", world.getName(), plugin.toName(sender))));
        } else {
            sender.sendMessage(BukkitUtil.replaceColorMacros(tr("command.allowfire.alreadyEnabled")));
        }

        wcfg.fireSpreadDisableToggle = false;
    }

    @Command(aliases = {"halt-activity", "stoplag", "haltactivity"},
            desc = "Attempts to cease as much activity in order to stop lag", flags = "c", max = 0)
    @CommandPermissions({"worldguard.halt-activity"})
    public void stopLag(CommandContext args, CommandSender sender) throws CommandException {

        ConfigurationManager configManager = plugin.getGlobalStateManager();

        configManager.activityHaltToggle = !args.hasFlag('c');

        if (configManager.activityHaltToggle) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(BukkitUtil.replaceColorMacros(tr("command.stoplag.activityHalted")));
            }

            plugin.getServer().broadcastMessage(BukkitUtil.replaceColorMacros(
                    tr("command.stoplag.activityHalted.broadcast", plugin.toName(sender))));

            for (World world : plugin.getServer().getWorlds()) {
                int removed = 0;

                for (Entity entity : world.getEntities()) {
                    if (BukkitUtil.isIntensiveEntity(entity)) {
                        entity.remove();
                        removed++;
                    }
                }

                if (removed > 10) {
                    sender.sendMessage(BukkitUtil.replaceColorMacros(
                            tr("command.stoplag.entitiesRemoved", removed, world.getName())));
                }
            }

        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(BukkitUtil.replaceColorMacros(tr("command.stoplag.activityEnabled")));
            }

            plugin.getServer().broadcastMessage(BukkitUtil.replaceColorMacros(
                    tr("command.stoplag.activityEnabled.broadcast")));
        }
    }
}
