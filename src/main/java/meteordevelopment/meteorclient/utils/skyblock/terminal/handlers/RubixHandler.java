package meteordevelopment.meteorclient.utils.skyblock.terminal.handlers;

import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.skyblock.terminal.TerminalTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.StainedGlassPaneBlock;

import java.util.*;

public class RubixHandler extends TerminalHandler {
    private static final List<DyeColor> COLOR_ORDER = List.of(
        DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.GREEN, DyeColor.BLUE, DyeColor.RED
    );
    private static final List<Integer> VALID_SLOTS = List.of(12, 13, 14, 21, 22, 23, 30, 31, 32);

    private int lastGoal = -1;

    public RubixHandler() {
        super(TerminalTypes.RUBIX);
    }

    @Override
    public List<Integer> solve(List<ItemStack> items) {
        int[] slots = new int[9];
        int[] colors = new int[9];
        int count = 0;

        for (int i = 0; i < items.size(); i++) {
            if (!VALID_SLOTS.contains(i)) continue;
            ItemStack s = items.get(i);
            if (s.getItem() == Items.AIR) continue;
            if (!(s.getItem() instanceof BlockItem bi)) continue;
            if (!(bi.getBlock() instanceof StainedGlassPaneBlock p)) continue;
            if (p.getColor() == DyeColor.BLACK) continue;
            int idx = COLOR_ORDER.indexOf(p.getColor());
            if (idx == -1) continue;
            slots[count] = i;
            colors[count] = idx;
            count++;
        }

        if (count == 0) return List.of();

        if (lastGoal != -1) {
            return computeForTarget(slots, colors, count, lastGoal);
        }

        int bestGoal = 0;
        int bestSize = Integer.MAX_VALUE;
        List<Integer> best = List.of();
        for (int t = 0; t < 5; t++) {
            List<Integer> temp = computeForTarget(slots, colors, count, t);
            int realSize = getRealSize(temp);
            if (realSize < bestSize) {
                bestSize = realSize;
                best = temp;
                bestGoal = t;
            }
        }

        lastGoal = bestGoal;
        return best;
    }

    private List<Integer> computeForTarget(int[] slots, int[] colors, int count, int goal) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (colors[i] == goal) continue;
            int d = dist(colors[i], goal);
            for (int c = 0; c < d; c++) result.add(slots[i]);
        }
        return result;
    }

    private static int dist(int pane, int target) {
        return pane > target ? (target + 5) - pane : target - pane;
    }

    private static int getRealSize(List<Integer> list) {
        int size = 0;
        for (int slot : new HashSet<>(list)) {
            int freq = Collections.frequency(list, slot);
            size += freq >= 3 ? 5 - freq : freq;
        }
        return size;
    }

    @Override
    public void simulateClick(int slotIndex, int button) {
        if (!solution.contains(slotIndex)) return;
        int freq = Collections.frequency(solution, slotIndex);
        if (button == 0) {
            if (freq <= 2) {
                solution.remove(Integer.valueOf(slotIndex));
            }
        } else {
            if (freq == 3) {
                solution.add(slotIndex);
            } else if (freq == 4) {
                solution.removeIf(i -> i == slotIndex);
            }
        }
    }

    @Override
    public boolean canClick(int slotIndex, int button) {
        if (!solution.contains(slotIndex)) return false;
        int needed = Collections.frequency(solution, slotIndex);
        return !((needed < 3 && button == 1) || ((needed == 3 || needed == 4) && button != 1));
    }

    @Override
    public int getClickButton(int slotIndex) {
        int freq = Collections.frequency(solution, slotIndex);
        return freq >= 3 ? 1 : 0;
    }

    @Override
    protected Color renderSlot(int slotIndex) {
        int amount = Collections.frequency(solution, slotIndex);
        int clicksRequired = amount < 3 ? amount : amount - 5;
        if (clicksRequired == 0) return null;
        return switch (clicksRequired) {
            case 1 -> new Color(0, 255, 0);
            case 2 -> new Color(0, 200, 0);
            case -1 -> new Color(200, 0, 0);
            default -> new Color(150, 0, 0);
        };
    }

    @Override
    public String getSlotText(int slotIndex) {
        int amount = Collections.frequency(solution, slotIndex);
        int clicksRequired = amount < 3 ? amount : amount - 5;
        if (clicksRequired == 0) return null;
        return String.valueOf(clicksRequired);
    }
}
