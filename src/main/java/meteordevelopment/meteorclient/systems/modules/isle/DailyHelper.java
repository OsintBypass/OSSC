package meteordevelopment.meteorclient.systems.modules.isle;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DailyHelper extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Regex to extract the item name preceding the "x10" suffix
    private final Pattern BRING_MISSION_PATTERN = Pattern.compile("^(.*?)\\s*x10$");

    private boolean hasAlertedMissing = false;
    private int tickDelay = 0;

    public DailyHelper() {
        super(Categories.Isle, "daily-helper", "Helps with daily tasks on Crimson Isle.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        // Throttle checks to once every 20 ticks (1 second) to prevent memory exhaustion
        if (tickDelay++ < 20) return;
        tickDelay = 0;

        if (mc.player == null || mc.getConnection() == null) return;

        // Verify location - check for Crimson Isle via scoreboard
        if (!isOnCrimsonIsle()) return; 

        boolean foundFactionWidget = false;

        for (PlayerInfo entry : mc.getConnection().getOnlinePlayers()) {
            Component displayName = entry.getTabListDisplayName();
            if (displayName == null) continue;

            String rawText = displayName.getString();
            
            // Detect the start of the Faction quests section
            if (rawText.contains("Faction quests:")) {
                foundFactionWidget = true;
                continue; // Move to the next lines which contain the actual quests
            }

            // Skip if this looks like a completed quest (contains checkmark or is empty)
            if (rawText.isEmpty() || rawText.contains("✓") || rawText.contains("[x]") || rawText.contains("[X]")) continue;

            Matcher matcher = BRING_MISSION_PATTERN.matcher(rawText.trim());
            
            if (matcher.matches()) {
                String neededItem = matcher.group(1).trim();
                triggerMissionHelper(neededItem);
                
                // Break to avoid spamming multiple identical alerts
                break; 
            }
        }

        handleMissingWidget(foundFactionWidget);
    }

    private void triggerMissionHelper(String item) {
        // Format the ID for the Bazaar (Spaces to underscores, uppercase)
        String bzId = item.toUpperCase().replace(" ", "_");

        MutableComponent message = Component.literal("Bring mission helper: " + item)
            .withStyle(ChatFormatting.GOLD)
            .withStyle(style -> style
                .withClickEvent(new MeteorClickEvent("/bz " + bzId))
            );

        ChatUtils.sendMsg(message);

        // Copy exact amount to clipboard
        mc.keyboardHandler.setClipboard("10");
    }

    private void handleMissingWidget(boolean found) {
        if (!found && !hasAlertedMissing) {
            // Trigger Meteor Notification
            mc.getToastManager().addToast(new MeteorToast.Builder("Widget Missing")
                .text("Faction quests widget not found in tab.")
                .icon(Items.BARRIER)
                .build());
            hasAlertedMissing = true;
        } else if (found && hasAlertedMissing) {
            // Reset the toggle if the widget reappears
            hasAlertedMissing = false;
        }
    }

    private boolean isOnCrimsonIsle() {
        // Check if player is on Crimson Isle by checking scoreboard for specific lines
        if (mc.player == null || mc.level == null || mc.level.getScoreboard() == null) return false;
        
        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective sidebarObjective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        
        return sidebarObjective != null && 
               sidebarObjective.getDisplayName().getString().contains("Crimson");
    }
}
