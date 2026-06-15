package meteordevelopment.meteorclient.utils.skyblock.terminal.handlers;

import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.skyblock.terminal.TerminalTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.stream.IntStream;

public class PanesHandler extends TerminalHandler {
    public PanesHandler() {
        super(TerminalTypes.PANES);
    }

    @Override
    public List<Integer> solve(List<ItemStack> items) {
        return IntStream.range(0, items.size())
            .filter(i -> items.get(i).getItem() == Items.RED_STAINED_GLASS_PANE)
            .boxed()
            .toList();
    }

    @Override
    protected Color renderSlot(int slotIndex) {
        return new Color(0, 255, 0);
    }
}
