package meteordevelopment.meteorclient.systems.modules.rift;

import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class RiftPlaceholderModule extends Module {
    public RiftPlaceholderModule() {
        super(Categories.Rift, "rift-placeholder", "Example Rift module placeholder.");
        serialize = false;
        autoSubscribe = false;
        chatFeedback = false;
        favorite = false;
    }
}
