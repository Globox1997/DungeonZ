package net.dungeonz.block.screen;

import org.apache.commons.lang3.StringUtils;

import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.network.DungeonClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.JigsawBlockScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class DungeonGateOpScreen extends Screen {

    private static final Text GATE_BLOCK_ID_TEXT = Text.translatable("dungeon.op_screen.gate_block_id");
    private static final Text GATE_PARTICLE_ID_TEXT = Text.translatable("dungeon.op_screen.gate_particle_id");
    private static final Text GATE_UNLOCK_ITEM_ID_TEXT = Text.translatable("dungeon.op_screen.gate_unlock_item_id");
    private final BlockPos dungeonGatePos;

    private ButtonWidget doneButton;
    private TextFieldWidget gateBlockIdTextFieldWidget;
    private TextFieldWidget gateParticleIdTextFieldWidget;
    private TextFieldWidget gateUnlockItemIdTextFieldWidget;

    private String defaultBlockId = "minecraft:chiseled_stone_bricks";
    private String defaultParticleId = "minecraft:scrape";
    private String defaultItemId = "";

    public DungeonGateOpScreen(BlockPos dungeonGatePos) {
        super(NarratorManager.EMPTY);
        this.dungeonGatePos = dungeonGatePos;
    }

    @Override
    protected void init() {
        if (client.world != null && client.world.getBlockEntity(this.dungeonGatePos) != null && client.world.getBlockEntity(this.dungeonGatePos) instanceof DungeonGateEntity) {
            DungeonGateEntity dungeonGateEntity = (DungeonGateEntity) client.world.getBlockEntity(this.dungeonGatePos);
            if (!dungeonGateEntity.getBlockState().getBlock().equals(Blocks.AIR)) {
                defaultBlockId = Registry.BLOCK.getId(dungeonGateEntity.getBlockState().getBlock()).toString();
            }
            if (dungeonGateEntity.getParticleEffect() != null) {
                defaultParticleId = dungeonGateEntity.getParticleEffect().asString();
            }
            if (dungeonGateEntity.getUnlockItem() != null) {
                defaultItemId = Registry.ITEM.getId(dungeonGateEntity.getUnlockItem()).toString();
            }
        }

        this.gateBlockIdTextFieldWidget = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 50, 300, 20, GATE_BLOCK_ID_TEXT);
        this.gateBlockIdTextFieldWidget.setMaxLength(128);
        this.gateBlockIdTextFieldWidget.setText(defaultBlockId);
        this.gateBlockIdTextFieldWidget.setChangedListener(pool -> this.updateDoneButtonState());
        this.addSelectableChild(this.gateBlockIdTextFieldWidget);

        this.gateParticleIdTextFieldWidget = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 85, 300, 20, GATE_PARTICLE_ID_TEXT);
        this.gateParticleIdTextFieldWidget.setMaxLength(128);
        this.gateParticleIdTextFieldWidget.setText(defaultParticleId);
        this.gateParticleIdTextFieldWidget.setChangedListener(name -> this.updateDoneButtonState());
        this.addSelectableChild(this.gateParticleIdTextFieldWidget);

        this.gateUnlockItemIdTextFieldWidget = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 120, 300, 20, GATE_UNLOCK_ITEM_ID_TEXT);
        this.gateUnlockItemIdTextFieldWidget.setMaxLength(128);
        this.gateUnlockItemIdTextFieldWidget.setText(defaultItemId);
        this.gateUnlockItemIdTextFieldWidget.setChangedListener(name -> this.updateDoneButtonState());
        this.addSelectableChild(this.gateUnlockItemIdTextFieldWidget);

        this.doneButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 75, 156, 150, 20, ScreenTexts.DONE, button -> {
            this.onDone();
        }));
        this.setInitialFocus(this.gateBlockIdTextFieldWidget);
        this.updateDoneButtonState();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.gateBlockIdTextFieldWidget.getText();
        String string2 = this.gateParticleIdTextFieldWidget.getText();
        String string3 = this.gateUnlockItemIdTextFieldWidget.getText();

        this.init(client, width, height);
        this.gateBlockIdTextFieldWidget.setText(string);
        this.gateParticleIdTextFieldWidget.setText(string2);
        this.gateUnlockItemIdTextFieldWidget.setText(string3);
    }

    @Override
    public void tick() {
        this.gateBlockIdTextFieldWidget.tick();
        this.gateParticleIdTextFieldWidget.tick();
        this.gateUnlockItemIdTextFieldWidget.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        JigsawBlockScreen.drawTextWithShadow(matrices, this.textRenderer, GATE_BLOCK_ID_TEXT, this.width / 2 - 153, 40, 0xA0A0A0);
        this.gateBlockIdTextFieldWidget.render(matrices, mouseX, mouseY, delta);
        JigsawBlockScreen.drawTextWithShadow(matrices, this.textRenderer, GATE_PARTICLE_ID_TEXT, this.width / 2 - 153, 75, 0xA0A0A0);
        this.gateParticleIdTextFieldWidget.render(matrices, mouseX, mouseY, delta);
        JigsawBlockScreen.drawTextWithShadow(matrices, this.textRenderer, GATE_UNLOCK_ITEM_ID_TEXT, this.width / 2 - 153, 110, 0xA0A0A0);
        this.gateUnlockItemIdTextFieldWidget.render(matrices, mouseX, mouseY, delta);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void updateDoneButtonState() {
        this.doneButton.active = !StringUtils.isEmpty(this.gateBlockIdTextFieldWidget.getText()) && !Registry.BLOCK.get(new Identifier(this.gateBlockIdTextFieldWidget.getText())).equals(Blocks.AIR);
    }

    private void onDone() {
        this.client.setScreen(null);
        DungeonClientPacket.writeC2SSetGateBlockPacket(client, this.gateBlockIdTextFieldWidget.getText(), this.gateParticleIdTextFieldWidget.getText(), this.gateUnlockItemIdTextFieldWidget.getText(),
                this.dungeonGatePos);
    }

}
