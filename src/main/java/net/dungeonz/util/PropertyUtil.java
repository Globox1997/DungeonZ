package net.dungeonz.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.state.property.Properties;

public class PropertyUtil {

    public static int getHorizontalFacing(BlockState blockState) {
        if (blockState.contains(Properties.HORIZONTAL_FACING)) {
            return blockState.get(Properties.HORIZONTAL_FACING).getHorizontal();
        }
        return 0;
    }

    public static int getBlockFacing(BlockState blockState) {
        if (blockState.contains(Properties.BLOCK_FACE)) {
            return switch (blockState.get(Properties.BLOCK_FACE)) {
                case FLOOR -> 1;
                case WALL -> 2;
                case CEILING -> 3;
                default -> 0;
            };
        }
        return 0;
    }

    public static BlockFace getBlockFacing(int blockFacing) {
        return switch (blockFacing) {
            case 1 -> BlockFace.FLOOR;
            case 2 -> BlockFace.WALL;
            case 3 -> BlockFace.CEILING;
            default -> BlockFace.FLOOR;
        };
    }
}
