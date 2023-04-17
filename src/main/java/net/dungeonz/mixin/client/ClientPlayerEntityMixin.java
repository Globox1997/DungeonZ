package net.dungeonz.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;

import net.dungeonz.access.ClientPlayerAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin implements ClientPlayerAccess {

    private List<Integer> breakableBlockIdList = new ArrayList<Integer>();
    private List<Integer> placeableBlockIdList = new ArrayList<Integer>();
    private boolean allowElytra = false;

    @Override
    public void setClientDungeonInfo(List<Integer> breakableBlockIdList, List<Integer> placeableBlockIdList, boolean allowElytra) {

        this.breakableBlockIdList = breakableBlockIdList;
        this.placeableBlockIdList = placeableBlockIdList;
        this.allowElytra = allowElytra;
    }

    @Override
    public List<Integer> getBreakableBlockIdList() {
        return this.breakableBlockIdList;
    }

    @Override
    public List<Integer> getPlaceableBlockIdList() {
        return this.placeableBlockIdList;
    }

    @Override
    public boolean isElytraAllowed() {
        return allowElytra;
    }
}
