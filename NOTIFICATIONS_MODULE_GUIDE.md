# Notifications Module Documentation

## Overview
The **Notifications** module is a new feature that allows users to control how module-related notifications and updates are displayed in Meteor Client. It provides flexible routing of module toggle messages to either chat, toast notifications (popoups), both, or neither.

## Features

### Module Display Modes
The module offers four display modes for module notifications:

1. **Chat** (Default)
   - Displays module toggle messages in the Minecraft chat
   - Shows when a module is enabled/disabled
   - Compatible with existing chat feedback settings

2. **Toast** 
   - Shows popup notifications in the upper right corner
   - Styled like Minecraft advancement popups
   - Non-intrusive and doesn't clutter chat
   - Fully customizable duration

3. **Both**
   - Sends notifications to both chat AND toast simultaneously
   - Provides maximum visibility for important module updates
   - Best for power users who want full awareness

4. **None**
   - Disables all module notifications
   - Silent operation mode
   - Useful for minimal UI setups

## Settings

### Mode
- **Type:** Enum (CHAT, TOAST, BOTH, NONE)
- **Default:** CHAT
- **Description:** Determines where module notifications are displayed
- **Usage:** Select your preferred notification display style

### Module Updates
- **Type:** Boolean
- **Default:** true
- **Description:** Enable/disable toast notifications for module toggle events
- **Note:** Chat notifications still respect the global `chat-feedback` setting in Config

### Toast Duration
- **Type:** Integer (Milliseconds)
- **Default:** 4000 (4 seconds)
- **Min:** 0
- **Description:** How long toast notifications remain visible on screen

## Module Integration

### How It Works
1. When a module is toggled (enabled/disabled), the system checks the Notifications module settings
2. If the Notifications module is active and properly configured:
   - **Chat mode:** Messages are sent to chat (if global chat feedback is enabled)
   - **Toast mode:** A popup toast appears in the upper right corner
   - **Both mode:** Both chat and toast are used
   - **None mode:** No notification is shown
3. If the Notifications module is not loaded/active, the system falls back to chat feedback (legacy behavior)

### Backward Compatibility
The system maintains full backward compatibility:
- If the Notifications module is disabled, module toggle messages appear in chat as before
- Existing `chatFeedback` setting on individual modules still works
- Global `chat-feedback` config option is still respected
- No existing functionality is removed or broken

## Usage Examples

### Power Users
Enable both chat and toast notifications for full awareness:
1. Open the Notifications module settings
2. Set Mode to **BOTH**
3. Adjust Toast Duration to preferred length (e.g., 3000ms)
4. Module updates now appear in chat AND as popups

### Minimal UI
For a clean interface without notifications:
1. Open the Notifications module settings
2. Set Mode to **NONE**
3. Module toggles occur silently

### Chat-Only Setup
For traditional Meteor Client experience:
1. Keep Mode set to **CHAT** (default)
2. Ensure global chat-feedback is enabled in Config
3. Notifications appear only in chat, exactly as before

## File Structure
```
src/main/java/meteordevelopment/meteorclient/systems/modules/misc/
├── Notifications.java (New module)
└── [other misc modules...]
```

## Implementation Details

### Module Toggle Flow
```
Module.toggle() 
  → Module.sendToggledMsg()
    → Notifications.get().showChat() / showToasts()
    → Send to appropriate destination
```

### Toast Rendering
Toasts use the existing `MeteorToast` system:
- Same visual style as advancement popups
- Customizable duration
- Respects toast manager lifecycle

### Configuration Storage
All settings are automatically saved/loaded with the module configuration:
- Settings stored in `.minecraft/config/Meteor/modules/notifications.nbt`
- Persists user preferences across sessions

## Class Reference

### Notifications.java
```java
public class Notifications extends Module {
    public final Setting<Mode> mode;           // Display mode selector
    public final Setting<Boolean> moduleUpdates; // Enable/disable feature
    public final Setting<Integer> toastDuration; // Toast visible time
    
    public static Notifications get();          // Get singleton instance
    public boolean showChat();                   // Check if chat mode enabled
    public boolean showToasts();                 // Check if toast mode enabled
    public void displayToast(String title, String text); // Show toast
    
    public enum Mode {
        CHAT,   // Chat only
        TOAST,  // Toast only
        BOTH,   // Chat + Toast
        NONE    // No notification
    }
}
```

### Module.java (Modified)
The `sendToggledMsg()` method now:
1. Queries the Notifications module for display preferences
2. Routes message to chat if chat mode is enabled
3. Routes message to toast if toast mode is enabled
4. Falls back to chat if Notifications module is unavailable

## Future Enhancement Possibilities

1. **Per-Module Settings:** Allow toggling notifications for specific modules
2. **Notification Filtering:** Filter which module categories show notifications
3. **Custom Toast Styling:** Color/icon customization for toasts
4. **Sound Options:** Optional sound effects with notifications
5. **Notification Queue:** Track notification history
6. **Desktop Notifications:** Send to system notification center

## Testing Checklist

- [ ] Module appears in Misc category
- [ ] Default mode is CHAT
- [ ] Chat notifications work with mode set to CHAT
- [ ] Toast notifications appear with mode set to TOAST
- [ ] Both notifications appear with mode set to BOTH
- [ ] No notifications with mode set to NONE
- [ ] Toast duration setting works correctly
- [ ] Module disabled -> falls back to chat feedback
- [ ] Settings persist after reload
- [ ] Backward compatibility maintained with old chat feedback

## Known Limitations

1. Toast notifications require Minecraft client to be running
2. Toast duration is approximate (Minecraft manages exact timing)
3. Module-specific chatFeedback flag still controls chat output

## Support

For issues or feature requests, refer to the module settings and experiment with different configuration options. The system is designed to be user-friendly and respect all existing Meteor Client settings.
