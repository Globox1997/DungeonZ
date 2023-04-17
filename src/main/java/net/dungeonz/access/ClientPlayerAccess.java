package net.dungeonz.access;

import java.util.List;

public interface ClientPlayerAccess {

    public void setClientDungeonInfo(List<Integer> breakableBlockIdList, List<Integer> placeableBlockIdList, boolean allowElytra);

    public List<Integer> getBreakableBlockIdList();

    public List<Integer> getPlaceableBlockIdList();

    public boolean isElytraAllowed();
}
