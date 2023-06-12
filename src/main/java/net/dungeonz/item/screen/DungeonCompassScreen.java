package net.dungeonz.item.screen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.lang3.StringUtils;

import net.dungeonz.init.ItemInit;
import net.dungeonz.network.DungeonClientPacket;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class DungeonCompassScreen extends Screen {

    private static final Identifier TEXTURE = new Identifier("dungeonz:textures/gui/dungeon_compass.png");
    private final Text title = Text.translatable("compass.compass_screen.title");

    private ButtonWidget doneButton;
    private final WidgetButtonPage[] dungeons = new WidgetButtonPage[7];

    private List<String> dungeonIds = new ArrayList<String>();
    private String dungeonType;

    private int selectedIndex;
    private int indexStartOffset;
    private boolean scrolling;
    private int backgroundWidth = 105;
    private int backgroundHeight = 185;
    private int x;
    private int y;

    public DungeonCompassScreen(String dungeonType, List<String> dungeonIds) {
        super(NarratorManager.EMPTY);
        this.dungeonType = dungeonType;
        this.dungeonIds = dungeonIds;
    }

    @Override
    protected void init() {
        super.init();

        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        int k = y + 16 + 2;
        for (int l = 0; l < 7; ++l) {
            this.dungeons[l] = this.addDrawableChild(new WidgetButtonPage(x + 5, k, l, button -> {
                this.selectedIndex = ((WidgetButtonPage) button).getIndex() + this.indexStartOffset;
                this.dungeonType = this.dungeonIds.get(this.selectedIndex);
                this.updateDoneButtonState();
            }));
            if (!dungeonType.equals("") && this.dungeonIds.size() > l && this.dungeonIds.get(l).equals(dungeonType)) {
                this.dungeons[l].active = false;
            }
            k += 20;
        }

        this.doneButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("compass.compass_screen.calibrate"), button -> {
            this.onDone();
        }).dimensions(this.x + this.backgroundWidth / 2 - 48, this.y + this.backgroundHeight - 25, 97, 20).build());
        this.doneButton.active = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);

        context.drawText(this.textRenderer, this.title, this.x + this.backgroundWidth / 2 - this.textRenderer.getWidth(this.title) / 2, this.y + 6, 0x404040, false);

        super.render(context, mouseX, mouseY, delta);

        if (!this.dungeonIds.isEmpty()) {
            int k = this.y + 16 + 1;
            int l = this.x + 5 + 5;
            this.renderScrollbar(context, i, j, this.dungeonIds);
            int m = 0;
            for (String dungeon : this.dungeonIds) {
                if (this.canScroll(this.dungeonIds.size()) && (m < this.indexStartOffset || m >= 7 + this.indexStartOffset)) {
                    ++m;
                    continue;
                }

                int n = k + 7;
                context.drawText(this.textRenderer, getDungeonName(dungeon, 78, 9), l, n, 0xE5E5E5, false);
                k += 20;
                ++m;
            }

            for (WidgetButtonPage widgetButtonPage : this.dungeons) {
                if (widgetButtonPage.isHovered()) {
                    widgetButtonPage.renderTooltip(context, mouseX, mouseY);
                }
                widgetButtonPage.visible = widgetButtonPage.index < this.dungeonIds.size();
            }
            RenderSystem.enableDepthTest();
        }
        if (this.doneButton.isHovered() && !this.doneButton.active && !StringUtils.isEmpty(this.dungeonType) && client.player != null
                && !InventoryHelper.hasRequiredItemStacks(client.player.getInventory(), ItemInit.REQUIRED_DUNGEON_COMPASS_CALIBRATION_ITEMS)) {
            context.drawTooltip(this.textRenderer, Text.translatable("compass.compass_screen.required"), mouseX, mouseY);
        }
    }

    private void renderScrollbar(DrawContext context, int x, int y, List<String> dungeonIds) {
        int i = dungeonIds.size() + 1 - 7;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int m = Math.min(113, this.indexStartOffset * k);
            if (this.indexStartOffset == i - 1) {
                m = 113;
            }
            context.drawTexture(TEXTURE, x + 94, y + 18 + m, 105.0f, 0.0f, 6, 27, 256, 256);
        } else {
            context.drawTexture(TEXTURE, x + 94, y + 18, 111.0f, 0.0f, 6, 27, 256, 256);
        }
    }

    private Text getDungeonName(String dungeonType, int length, int substringLength) {
        Text dungeonName = Text.translatable("dungeon." + dungeonType);

        if (this.client.textRenderer.getWidth(dungeonName) > length && substringLength != 0) {
            dungeonType = dungeonName.getString().substring(0, substringLength) + "..";
            return Text.of(dungeonType);
        } else {
            return dungeonName;
        }

    }

    private boolean canScroll(int listSize) {
        return listSize > 7;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int i = this.dungeonIds.size();
        if (this.canScroll(i)) {
            int j = i - 7;
            this.indexStartOffset = MathHelper.clamp((int) ((double) this.indexStartOffset - amount), 0, j);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int i = this.dungeonIds.size();
        if (this.scrolling) {
            int j = this.y + 18;
            int k = j + 139;
            int l = i - 7;
            float f = ((float) mouseY - (float) j - 13.5f) / ((float) (k - j) - 27.0f);
            f = f * (float) l + 0.5f;
            this.indexStartOffset = MathHelper.clamp((int) f, 0, l);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        if (this.canScroll(this.dungeonIds.size()) && mouseX > (double) (i + 94) && mouseX < (double) (i + 94 + 6) && mouseY > (double) (j + 18) && mouseY <= (double) (j + 18 + 139 + 1)) {
            this.scrolling = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateDoneButtonState() {
        this.doneButton.active = !StringUtils.isEmpty(this.dungeonType) && client.player != null
                && InventoryHelper.hasRequiredItemStacks(client.player.getInventory(), ItemInit.REQUIRED_DUNGEON_COMPASS_CALIBRATION_ITEMS);
    }

    private void onDone() {
        this.client.setScreen(null);
        this.client.player.playSound(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.BLOCKS, 1.0f, 1.0f);
        DungeonClientPacket.writeC2SSetDungeonCompassPacket(this.client, this.dungeonType);
    }

    private class WidgetButtonPage extends ButtonWidget {
        final int index;

        public WidgetButtonPage(int x, int y, int index, ButtonWidget.PressAction onPress) {
            super(x, y, 89, 20, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
            if (this.hovered) {
                Text text = Text.translatable("dungeon." + DungeonCompassScreen.this.dungeonIds.get(this.index + DungeonCompassScreen.this.indexStartOffset));
                if (client.textRenderer.getWidth(text) > 78) {
                    context.drawTooltip(textRenderer, text, mouseX, mouseY);
                }
            }
        }
    }

}
