package net.dungeonz.block.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;

import net.dungeonz.init.DimensionInit;
import net.dungeonz.network.DungeonClientPacket;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class DungeonPortalScreen extends HandledScreen<DungeonPortalScreenHandler> implements ScreenHandlerListener {

    private static Identifier TEXTURE = new Identifier("dungeonz:textures/gui/dungeon_portal.png");
    private static final Identifier ICONS = new Identifier("dungeonz:textures/gui/dungeon_icons.png");
    private static final Text JOIN = Text.translatable("dungeon.task.join");
    private static final Text LEAVE = Text.translatable("dungeon.task.leave");

    public DungeonDifficultyButton difficultyButton;
    private DungeonButton dungeonButton;
    private DungeonSliderButton effectButton;
    private DungeonSliderButton privateButton;
    private final PlayerEntity playerEntity;

    private int backgroundWidth = 256;
    private int backgroundHeight = 222;

    public DungeonPortalScreen(DungeonPortalScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.playerEntity = inventory.player;
        TEXTURE = handler.getBackgroundId() != null ? handler.getBackgroundId() : TEXTURE;
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width / 2 - this.backgroundWidth / 2);
        this.y = (this.height / 2 - this.backgroundHeight / 2);

        ((DungeonPortalScreenHandler) this.handler).addListener(this);

        final boolean playerIsInDungeonWorld = playerEntity.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD;
        Text buttonText = playerIsInDungeonWorld ? LEAVE : JOIN;

        this.dungeonButton = this.addDrawableChild(new DungeonButton(this.x + this.backgroundWidth / 2 - 26, this.y + this.backgroundHeight - 28, buttonText, (button) -> {
            if (button.active) {
                DungeonClientPacket.writeC2SDungeonTeleportPacket(this.client, this.handler.getPos());
                button.active = false;
            }
        }));
        this.difficultyButton = this.addDrawableChild(new DungeonDifficultyButton(this.x + 144, this.y + 36, Text.of(""), (button) -> {
            if (button.active) {
                DungeonClientPacket.writeC2SChangeDifficultyPacket(this.client, this.handler.getPos());
            }
        }));
        this.effectButton = this.addDrawableChild(new DungeonSliderButton(this.x + 144, this.y + 63, (button) -> {
            if (button.active) {
                ((DungeonSliderButton) button).cycleEnabled();
                this.handler.setDisableEffects(((DungeonSliderButton) button).isEnabled());
                DungeonClientPacket.writeC2SChangeEffectsPacket(client, this.handler.getPos(), ((DungeonSliderButton) button).isEnabled());
            }
        }));
        this.privateButton = this.addDrawableChild(new DungeonSliderButton(this.x + 144, this.y + 79, (button) -> {
            if (button.active) {
                ((DungeonSliderButton) button).cycleEnabled();
                DungeonClientPacket.writeC2SChangePrivateGroupPacket(client, this.handler.getPos(), ((DungeonSliderButton) button).isEnabled());
            }
        }));

        this.effectButton.enabled = !this.handler.getDisableEffects();
        this.privateButton.enabled = this.handler.getPrivateGroup();
        if (playerIsInDungeonWorld) {
            this.dungeonButton.active = true;
            this.difficultyButton.active = false;
            this.effectButton.active = false;
            this.privateButton.active = false;
        } else {
            if (this.handler.getDungeonPlayerUUIDs().size() > 0) {
                this.difficultyButton.active = false;
                this.effectButton.active = false;
                this.privateButton.active = false;
            } else {
                this.difficultyButton.active = true;
                this.effectButton.active = true;
                this.privateButton.active = true;
            }
            if ((this.handler.getDungeonPlayerUUIDs().size() + this.handler.getDeadDungeonPlayerUUIDs().size()) < this.handler.getMaxPlayerCount()
                    && InventoryHelper.hasRequiredItemStacks(this.playerEntity.getInventory(), this.handler.getRequiredItemStacks())
                    && !this.handler.getDeadDungeonPlayerUUIDs().contains(this.playerEntity.getUuid())) {
                this.dungeonButton.active = true;
            } else {
                this.dungeonButton.active = false;
            }
            // Here it will need party addon compat and check at the DungeonPortalBlock if the dungeon is private before teleportation
            if (this.dungeonButton.active && this.privateButton.enabled && this.handler.getDungeonPlayerUUIDs().size() > 0) {
                this.dungeonButton.active = false;
            }
        }
        if (this.handler.getDifficulties().contains(this.handler.getDifficulty())) {
            this.difficultyButton.setText(Text.translatable("dungeonz.difficulty." + this.handler.getDifficulty()));
        } else {
            this.difficultyButton.setText(Text.translatable("dungeonz.difficulty." + this.handler.getDifficulties().get(0)));
        }

    }

    private Text getPlayerName(UUID playerId, int length, int substringLength) {
        if (this.client.getNetworkHandler().getPlayerListEntry(playerId) != null) {
            String playerName = this.client.getNetworkHandler().getPlayerListEntry(playerId).getProfile().getName();
            if (this.client.textRenderer.getWidth(playerName) > length && substringLength != 0) {
                playerName = playerName.substring(0, substringLength) + "..";
            }
            return Text.of(playerName);
        }
        return Text.translatable("text.dungeonz.empty_name");
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        // Title
        this.textRenderer.draw(matrices, this.title, this.x + this.backgroundWidth / 2 - this.textRenderer.getWidth(this.title) / 2, this.y + 8, 0x404040);

        // Dungeon player list
        int k = this.y + 37;
        this.textRenderer.draw(matrices,
                Text.translatable("text.dungeonz.player_list", this.handler.getDungeonPlayerUUIDs().size() + this.handler.getDeadDungeonPlayerUUIDs().size(), this.handler.getMaxPlayerCount()),
                this.x + 8, this.y + 24, 0x3F3F3F);
        for (int i = 0; i < this.handler.getDungeonPlayerUUIDs().size() && i < 13; i++) {
            String playerName = getPlayerName(this.handler.getDungeonPlayerUUIDs().get(i), 102, 15).getString();
            if (i == 12) {
                playerName = "...";
                if (this.isPointWithinBounds(13, k, 16, 7, mouseX, mouseY)) {
                    List<Text> otherPlayerNames = new ArrayList<Text>();
                    for (int u = 12; u < this.handler.getDungeonPlayerUUIDs().size(); u++) {
                        otherPlayerNames.add(getPlayerName(this.handler.getDungeonPlayerUUIDs().get(u), 102, 15));
                    }
                    this.renderTooltip(matrices, otherPlayerNames, mouseX, mouseY);
                }
            }
            this.textRenderer.draw(matrices, playerName, this.x + 13, k, 0xFFFFFF);
            k += 13;
        }
        // Required items
        this.textRenderer.draw(matrices, Text.translatable("text.dungeonz.required"), this.x + 139, this.y + 100, 0x3F3F3F);
        RenderSystem.setShaderTexture(0, ICONS);
        this.drawTexture(matrices, this.x + 142 + this.textRenderer.getWidth(Text.translatable("text.dungeonz.required")), this.y + 97,
                52 + (InventoryHelper.hasRequiredItemStacks(this.playerEntity.getInventory(), this.handler.getRequiredItemStacks()) ? 0 : 14), 0, 14, 14);

        if (this.handler.getRequiredItemStacks().size() > 0) {
            int l = 0;
            for (int i = 0; i < this.handler.getRequiredItemStacks().size(); i++) {
                this.itemRenderer.renderInGuiWithOverrides(this.handler.getRequiredItemStacks().get(i), this.x + 144 + l, this.y + 112);
                this.itemRenderer.renderGuiItemOverlay(this.textRenderer, this.handler.getRequiredItemStacks().get(i), this.x + 144 + l, this.y + 112);
                if (this.isPointWithinBounds(144 + l, 112, 16, 16, mouseX, mouseY)) {
                    this.renderTooltip(matrices, this.handler.getRequiredItemStacks().get(i).getName(), mouseX, mouseY);
                }
                l += 18;
            }
        } else {
            this.textRenderer.draw(matrices, Text.translatable("text.dungeonz.nothing_required"), this.x + 144, this.y + 112, 0x3F3F3F);
        }
        // Possible loot
        this.textRenderer.draw(matrices, Text.translatable("text.dungeonz.possible"), this.x + 139, this.y + 134, 0x3F3F3F);
        if (this.handler.getPossibleLootDifficultyItemStackMap().size() > 0 && this.handler.getPossibleLootDifficultyItemStackMap().containsKey(this.handler.getDifficulty())
                && this.handler.getPossibleLootDifficultyItemStackMap().get(this.handler.getDifficulty()).size() > 0) {
            int l = 0;
            int o = 0;
            for (int i = 0; i < this.handler.getPossibleLootDifficultyItemStackMap().get(this.handler.getDifficulty()).size() && i < 10; i++) {
                this.itemRenderer.renderInGuiWithOverrides(this.handler.getPossibleLootDifficultyItemStackMap().get(this.handler.getDifficulty()).get(i), this.x + 144 + l, this.y + o + 146);
                this.itemRenderer.renderGuiItemOverlay(this.textRenderer, this.handler.getPossibleLootDifficultyItemStackMap().get(this.handler.getDifficulty()).get(i), this.x + 144 + l,
                        this.y + o + 146);

                if (this.isPointWithinBounds(144 + l, o + 146, 16, 16, mouseX, mouseY)) {
                    this.renderTooltip(matrices, this.handler.getPossibleLootDifficultyItemStackMap().get(this.handler.getDifficulty()).get(i).getName(), mouseX, mouseY);
                }
                l += 18;
                if (i == 4) {
                    l = 0;
                    o = 18;
                }
            }
        }
        this.textRenderer.draw(matrices, Text.translatable("dungeonz.difficulty"), this.x + 139, this.y + 24, 0x3F3F3F);
        this.textRenderer.draw(matrices, Text.translatable("text.dungeonz.effects"), this.x + 169, this.y + 65, 0x3F3F3F);
        this.textRenderer.draw(matrices, Text.translatable("text.dungeonz.private"), this.x + 169, this.y + 81, 0x3F3F3F);

        // RenderSystem.setShaderTexture(0, ICONS);
        // this.drawTexture(matrices, this.x + 144, this.y + 63, 0, 60, 20, 12);
        // this.drawTexture(matrices, this.x + 144, this.y + 79, 0, 60, 20, 12);

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onSlotUpdate(ScreenHandler var1, int var2, ItemStack var3) {
    }

    @Override
    public void onPropertyUpdate(ScreenHandler var1, int var2, int var3) {
    }

    public class DungeonButton extends ButtonWidget {

        public DungeonButton(int x, int y, Text text, ButtonWidget.PressAction onPress) {
            super(x, y, 52, 20, text, onPress);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, ICONS);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int j = 20;
            if (!this.active) {
                j = 0;
            } else if (this.isHovered()) {
                j = 40;
            }
            this.drawTexture(matrices, this.x, this.y, 0, j, this.width, this.height);

            int o = this.active ? 0xFFFFFF : 0xA0A0A0;
            ClickableWidget.drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, o | MathHelper.ceil(this.alpha * 255.0f) << 24);

            if (!this.active && this.isHovered()) {
                Text text = null;
                if (DungeonPortalScreen.this.handler.getCooldown() > 0) {
                    int cooldown = DungeonPortalScreen.this.handler.getCooldown();
                    int seconds = cooldown % 60;
                    int minutes = cooldown / 60 % 60;
                    int hours = cooldown / 60 / 60;
                    text = Text.translatable("text.dungeonz.dungeon_cooldown_time", hours, minutes, seconds);
                } else if ((DungeonPortalScreen.this.handler.getDungeonPlayerUUIDs().size() + DungeonPortalScreen.this.handler.getDeadDungeonPlayerUUIDs().size()) >= DungeonPortalScreen.this.handler
                        .getMaxPlayerCount()) {
                    text = Text.translatable("text.dungeonz.dungeon_full");
                } else if (client.player != null && !DungeonPortalScreen.this.handler.getDeadDungeonPlayerUUIDs().isEmpty()
                        && DungeonPortalScreen.this.handler.getDeadDungeonPlayerUUIDs().contains(client.player.getUuid())) {
                    text = Text.translatable("text.dungeonz.dead_player");
                } else if (!InventoryHelper.hasRequiredItemStacks(client.player.getInventory(), DungeonPortalScreen.this.handler.getRequiredItemStacks())) {
                    text = Text.translatable("text.dungeonz.missing");
                }
                if (text != null) {
                    DungeonPortalScreen.this.renderTooltip(matrices, text, mouseX, mouseY);
                }
            }
        }

    }

    public class DungeonDifficultyButton extends ButtonWidget {
        private Text text;

        public DungeonDifficultyButton(int x, int y, Text text, ButtonWidget.PressAction onPress) {
            super(x, y, 60, 20, text, onPress);
            this.text = text;
        }

        public void setText(Text text) {
            this.text = text;
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            TextRenderer textRenderer = minecraftClient.textRenderer;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
            int i = this.getYImage(this.isHovered());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            this.drawTexture(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
            this.drawTexture(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
            this.renderBackground(matrices, minecraftClient, mouseX, mouseY);
            int j = this.active ? 0xFFFFFF : 0xA0A0A0;
            ClickableWidget.drawCenteredText(matrices, textRenderer, this.text, this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0f) << 24);
        }

    }

    public class DungeonSliderButton extends ButtonWidget {
        private boolean enabled = false;

        public DungeonSliderButton(int x, int y, ButtonWidget.PressAction onPress) {
            super(x, y, 20, 12, Text.of(""), onPress);
        }

        public void cycleEnabled() {
            this.enabled = !this.enabled;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, ICONS);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int i = 60;
            if (this.enabled) {
                i = 72;
            }
            int j = 0;
            if (!this.active) {
                j = 40;
            } else if (this.isHovered()) {
                j = 20;
            }
            this.drawTexture(matrices, this.x, this.y, j, i, this.width, this.height);
        }

    }

}
