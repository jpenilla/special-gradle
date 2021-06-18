/*
 * Special Gradle Gradle Plugin
 * Copyright (c) 2021 Jason Penilla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.jpenilla.specialgradle.testplugin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.players;

public class TestPlugin extends JavaPlugin implements Listener {
  public TestPlugin() {
    super();
    final CraftServer craftServer = (CraftServer) Bukkit.getServer();
    final CommandDispatcher<CommandSourceStack> dispatcher =
      craftServer.getServer().vanillaCommandDispatcher.getDispatcher();

    // Test registering commands using Brigadier and Minecraft argument types
    // command libraries like Incendo/cloud (which can register to Brigadier internally)
    // provide better integration with the Bukkit API and should be preferred,
    // but this is an option if you want to use it.
    // note commands added in this way will get prefixed with 'minecraft:' instead of your plugin name
    dispatcher.register(literal("special-gradle")
      .requires(permission("special-gradle.command"))
      .then(literal("say_hello")
        .then(argument("players", players())
          .executes(TestPlugin::sayHelloToSelectedPlayers))));
  }

  @Override
  public void onEnable() {
    this.getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  public void handlePlayerJoin(final PlayerJoinEvent event) {
    final ServerPlayer player = ((CraftPlayer) event.getPlayer()).getHandle();

    player.sendMessage(
      new TextComponent("Welcome to the server!")
        .withStyle(style -> style.withColor(ChatFormatting.GOLD)),
      ChatType.SYSTEM,
      Util.NIL_UUID
    );
  }

  private static Predicate<CommandSourceStack> permission(final String permission) {
    return stack -> stack.hasPermission(stack.getServer().getOperatorUserPermissionLevel(), permission);
  }

  private static int sayHelloToSelectedPlayers(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    final Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");

    // Test sending a message using Components.
    // Not as clean as Adventure, but still better than Bungeecord's abysmal Chat API
    final Component message = new TextComponent("Hello from ")
      .withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE)
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click!").withStyle(ChatFormatting.AQUA)))
        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/special-gradle say_hello ")))
      .append(new TextComponent("Special Gradle")
        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC, ChatFormatting.BOLD))
      .append(new TextComponent("!").withStyle(ChatFormatting.WHITE));

    for (final ServerPlayer player : players) {
      player.sendMessage(message, ChatType.SYSTEM, Util.NIL_UUID);
    }

    return players.size();
  }
}
