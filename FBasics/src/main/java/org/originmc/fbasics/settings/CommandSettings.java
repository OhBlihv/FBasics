package org.originmc.fbasics.settings;

import com.google.common.collect.HashMultimap;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventPriority;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.util.SettingsUtils;

import java.util.Set;

@Data
public final class CommandSettings implements ISettings {

    private static final String PREFIX = "commands-";

    private static final String ENABLED = PREFIX + "enabled";

    private static final String PAID_MESSAGE = PREFIX + "paid-message";

    private static final String CANNOT_AFFORD_MESSAGE = PREFIX + "cannot-afford-message";

    private static final String COOLDOWN_MESSAGE = PREFIX + "cooldown-message";

    private static final String WARMUP_START_MESSAGE = PREFIX + "warmup-start-message";

    private static final String WARMUP_DUPLICATE_MESSAGE = PREFIX + "warmup-duplicate-message";

    private static final String WARMUP_FAILED_MESSAGE = PREFIX + "warmup-failed-message";

    private static final String FACTION_MESSAGE = PREFIX + "faction-message";

    private static final String MODIFIERS = PREFIX + "modifiers.";

    private final FBasics plugin;

    private ConfigurationSection configuration;

    private boolean enabled = false;

    private String paidMessage = "";

    private String cannotAffordMessage = "";

    private String cooldownMessage = "";

    private String warmupStartMessage = "";

    private String warmupDuplicateMessage = "";

    private String warmupFailedMessage = "";

    private String factionMessage = "";

    private HashMultimap<EventPriority, CommandModifierSettings> modifiers = HashMultimap.create();

    @Override
    public void load() {
        configuration = plugin.getConfig();
        enabled = configuration.getBoolean(ENABLED, false);
        paidMessage = configuration.getString(PAID_MESSAGE, "");
        cannotAffordMessage = configuration.getString(CANNOT_AFFORD_MESSAGE, "");
        cooldownMessage = configuration.getString(COOLDOWN_MESSAGE, "");
        warmupStartMessage = configuration.getString(WARMUP_START_MESSAGE, "");
        warmupDuplicateMessage = configuration.getString(WARMUP_DUPLICATE_MESSAGE, "");
        warmupFailedMessage = configuration.getString(WARMUP_FAILED_MESSAGE, "");
        factionMessage = configuration.getString(FACTION_MESSAGE, "");

        // Do nothing if modifiers are not defined.
        if (!configuration.contains(MODIFIERS)) {
            modifiers.clear();
            return;
        }

        // Load all modifier settings.
        ConfigurationSection configuration = this.configuration.getConfigurationSection(MODIFIERS);
        for (String modifierName : configuration.getKeys(false)) {
            EventPriority priority = SettingsUtils.getEventPriority(configuration.getString(modifierName + ".priority"));
            loadModifier(priority, configuration);
        }
    }

    private void loadModifier(EventPriority priority, ConfigurationSection configuration) {
        // Remove all unused modifiers.
        Set<CommandModifierSettings> modifiers = this.modifiers.get(priority);
        for (CommandModifierSettings modifier : modifiers) {
            if (!configuration.contains(modifier.getName())) {
                modifiers.remove(modifier);
            }
        }

        for (String modifierName : configuration.getKeys(false)) {
            // Attempt to load an already cached modifier settings, otherwise create a new one.
            CommandModifierSettings modifier = null;
            for (CommandModifierSettings m : modifiers) {
                if (m.getName().equals(modifierName)) {
                    modifier = m;
                }
            }

            if (modifier == null) {
                modifier = new CommandModifierSettings(plugin, configuration.getConfigurationSection(modifierName).getCurrentPath(), modifierName);
            }

            // Load the modifier then add it to the modifier set.
            modifier.load();
            modifiers.add(modifier);
        }
    }

}
