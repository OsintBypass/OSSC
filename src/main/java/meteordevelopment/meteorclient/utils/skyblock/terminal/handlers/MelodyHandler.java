package meteordevelopment.meteorclient.utils.skyblock.terminal.handlers;

import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.skyblock.terminal.TerminalTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MelodyHandler extends TerminalHandler {
    private static final Set<Integer> CLICKABLE_SLOTS = Set.of(16, 25, 34, 43);

    public boolean endsOnly = false;
    public int button = -1;
    public int current = -1;
    public int correct = -1;

    public MelodyHandler() {
        super(TerminalTypes.MELODY);
    }

    @Override
    public void updateSlot(List<ItemStack> items, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= type.windowSize) return;
        solution.clear();
        solution.addAll(solve(items));
    }

    @Override
    public List<Integer> solve(List<ItemStack> items) {
        int greenPos = -1;
        int magentaPos = -1;

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItem() == Items.LIME_STAINED_GLASS_PANE) greenPos = i;
            if (items.get(i).getItem() == Items.MAGENTA_STAINED_GLASS_PANE && i / 9 == 0) magentaPos = i;
        }

        if (greenPos != -1) {
            button = (greenPos / 9) - 1;
            current = (greenPos % 9) - 1;
        }
        if (magentaPos != -1) {
            correct = magentaPos - 1;
        }

        List<Integer> result = new ArrayList<>();
        if (greenPos != -1 && current == correct && button >= 0 && button <= 3) {
            if (!endsOnly || (current != 0 && current != 4)) {
                result.add(16 + button * 9);
            }
        }
        return result;
    }

    @Override
    public boolean canClick(int slotIndex, int button) {
        return CLICKABLE_SLOTS.contains(slotIndex) && solution.contains(slotIndex);
    }

    @Override
    public Color getSlotColor(int slotIndex) {
        return renderSlot(slotIndex);
    }

    @Override
    protected Color renderSlot(int slotIndex) {
        int row = slotIndex / 9;
        int col = slotIndex % 9;

        if (row == 0 && col - 1 == correct && correct != -1) {
            return new Color(128, 0, 128);
        }
        if (row - 1 == button && col - 1 == current && button != -1 && current != -1) {
            return new Color(0, 255, 0);
        }
        if (col == 7 && row >= 1 && row <= 4) {
            if (solution.contains(slotIndex)) return new Color(0, 255, 0);
            return new Color(0, 80, 0);
        }
        return null;
    }
}
