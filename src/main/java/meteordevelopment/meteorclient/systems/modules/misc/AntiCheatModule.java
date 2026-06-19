package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class AntiCheatModule extends Module {
    public AntiCheatModule() {
        super(Categories.Misc, "anticheat", "This is a toggle that chooses which modules to render based on the criteria set by the user. For example, if you choose \"blatant\", you will be able to see all the modules. If you choose \"legit\", then you will only be able to see the undetectable or genuinely legit modules. FYI THIS ISN'T A DISABLER.");
        serialize = false;
        autoSubscribe = false;
        chatFeedback = false;
        favorite = false;
    }
}
