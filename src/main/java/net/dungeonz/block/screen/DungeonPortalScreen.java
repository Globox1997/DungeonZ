package net.dungeonz.block.screen;

import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;

import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.network.DungeonClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class DungeonPortalScreen extends HandledScreen<DungeonPortalScreenHandler> implements ScreenHandlerListener {

    private static final Identifier TEXTURE = new Identifier("dungeonz:textures/gui/dungeon_portal.png");
    private static final Text JOIN = Text.translatable("dungeon.task.join");
    private static final Text LEAVE = Text.translatable("dungeon.task.leave");

    // private CyclingButtonWidget<String> difficultySwitchButton;

    public DungeonPortalScreen.DungeonButton dungeonButton;
    public DungeonPortalScreen.DungeonDifficultyButton difficultyButton;
    private final PlayerEntity playerEntity;

    int backgroundHeight = 203;

    public DungeonPortalScreen(DungeonPortalScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.playerEntity = inventory.player;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        this.titleY = -10;
        ((DungeonPortalScreenHandler) this.handler).addListener(this);

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        final boolean playerIsInDungeonWorld = playerEntity.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD;
        Text buttonText = playerIsInDungeonWorld ? LEAVE : JOIN;

        this.dungeonButton = (DungeonPortalScreen.DungeonButton) this.addDrawableChild(new DungeonPortalScreen.DungeonButton(i + 88 - 26, j + 170, buttonText, (button) -> {
            if (button instanceof DungeonPortalScreen.DungeonButton && !((DungeonPortalScreen.DungeonButton) button).disabled) {
                DungeonClientPacket.writeC2SDungeonTeleportPacket(this.client, this.handler.getPos());
            }
        }));
        this.difficultyButton = (DungeonPortalScreen.DungeonDifficultyButton) this
                .addDrawableChild(new DungeonPortalScreen.DungeonDifficultyButton(i + ConfigInit.CONFIG.test3, j + ConfigInit.CONFIG.test4, buttonText, (button) -> {
                    if (button instanceof DungeonPortalScreen.DungeonDifficultyButton && !((DungeonPortalScreen.DungeonDifficultyButton) button).disabled) {
                        DungeonClientPacket.writeC2SChanceDifficultyPacket(this.client, this.handler.getPos());
                    }
                }));

        if (playerIsInDungeonWorld) {
            this.dungeonButton.setDisabled(false);
            this.difficultyButton.setDisabled(true);
        } else {
            if (this.handler.getDungeonPlayerUUIDs().size() > 0) {
                this.difficultyButton.setDisabled(true);
            } else {
                this.difficultyButton.setDisabled(false);
            }
            if (this.handler.getDungeonPlayerUUIDs().size() < this.handler.getMaxPlayerCount()) {
                this.dungeonButton.setDisabled(false);
            } else {
                this.dungeonButton.setDisabled(true);
            }
        }
        // System.out.println(this.handler.getDifficulty() + " : " + this.handler.getDifficulties() + " : " + (this.handler.getDifficulties().contains(this.handler.getDifficulty())));
        if (this.handler.getDifficulties().contains(this.handler.getDifficulty())) {
            this.difficultyButton.setText(Text.translatable("dungeonz.difficulty." + this.handler.getDifficulty()));
        } else {
            this.difficultyButton.setText(Text.translatable("dungeonz.difficulty." + this.handler.getDifficulties().get(0)));
        }
        // this.handler.getd
        // this.difficultyButton.setText(text);

        // System.out.println(this.handler.getDifficulties());

        // this.difficultySwitchButton = this.addDrawableChild(CyclingButtonWidget.builder(Difficulty::getTranslatableName).values((Difficulty[]) Difficulty.values()).initially(this.getDifficulty())
        // .build(j, 100, 150, 20, Text.translatable("options.difficulty"), (button, difficulty) -> {
        // this.currentDifficulty = difficulty;
        // }));

        // this.difficultySwitchButton =
        // this.addDrawableChild(CyclingButtonWidget.builder(Difficulty::getTranslatableName).values((Difficulty[])Difficulty.values()).initially(this.getDifficulty()).build(j, 100, 150, 20,
        // Text.translatable("options.difficulty"), (button, difficulty) -> {
        // this.currentDifficulty = difficulty;
        // }));
        this.handler.setDungeonPlayerUUIDs(List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
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

        // Dungeon player list
        int k = this.y + ConfigInit.CONFIG.test1;
        this.textRenderer.draw(matrices, Text.translatable("text.dungeonz.player_list", this.handler.getDungeonPlayerUUIDs().size(), this.handler.getMaxPlayerCount()), this.x + 5, this.y + 15,
                0x3F3F3F);
        for (int i = 0; i < this.handler.getDungeonPlayerUUIDs().size() && i < 10; i++) {
            String playerName = getPlayerName(this.handler.getDungeonPlayerUUIDs().get(i), 60, 10).getString();
            if (i == 9) {
                playerName = "...";
            }
            this.textRenderer.draw(matrices, playerName, this.x + 10, k, 0xFFFFFF);
            k += 13;
        }
        // Difficulty
        // this.textRenderer.draw(matrices, Text.translatable("dungeonz.difficulty"), this.x + ConfigInit.CONFIG.test2, this.y + 15, 0x3F3F3F);

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.x;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float) this.titleX, (float) this.titleY, 0x404040);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // @Override
    // protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
    // super.onMouseClick(slot, slotId, button, actionType);
    // }

    @Override
    public void onSlotUpdate(ScreenHandler var1, int var2, ItemStack var3) {
    }

    @Override
    public void onPropertyUpdate(ScreenHandler var1, int var2, int var3) {
    }

    public class DungeonButton extends ButtonWidget {
        private boolean disabled;

        public DungeonButton(int x, int y, Text text, ButtonWidget.PressAction onPress) {
            super(x, y, 52, 20, text, onPress);
            this.disabled = true;
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int j = 20;
            if (this.disabled) {
                j = 0;
            } else if (this.isHovered()) {
                j += 20;
            }
            this.drawTexture(matrices, this.x, this.y, 176, j, this.width, this.height);

            int o = this.active ? 0xFFFFFF : 0xA0A0A0;
            ClickableWidget.drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, o | MathHelper.ceil(this.alpha * 255.0f) << 24);
        }

        public void setDisabled(boolean disable) {
            this.disabled = disable;
        }

    }

    public class DungeonDifficultyButton extends ButtonWidget {
        private boolean disabled;
        private Text text;

        public DungeonDifficultyButton(int x, int y, Text text, ButtonWidget.PressAction onPress) {
            super(x, y, ConfigInit.CONFIG.test5, 20, text, onPress);
            this.text = text;
        }

        public void setDisabled(boolean disable) {
            this.disabled = disable;
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

            // RenderSystem.setShader(GameRenderer::getPositionTexShader);
            // RenderSystem.setShaderTexture(0, TEXTURE);
            // RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            // RenderSystem.enableBlend();
            // RenderSystem.defaultBlendFunc();
            // RenderSystem.enableDepthTest();
            // int j = 20;
            // if (this.disabled) {
            // j = 0;
            // } else if (this.isHovered()) {
            // j += 20;
            // }
            // this.drawTexture(matrices, this.x, this.y, 176, j, this.width, this.height);

            // int o = this.active ? 0xFFFFFF : 0xA0A0A0;
            // ClickableWidget.drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, o | MathHelper.ceil(this.alpha * 255.0f) << 24);
            // }

            // public void setDisabled(boolean disable) {
            // this.disabled = disable;
        }

    }

}
