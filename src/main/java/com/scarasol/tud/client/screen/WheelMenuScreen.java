package com.scarasol.tud.client.screen;

import com.scarasol.tud.TudMod;
import com.scarasol.tud.client.render.WheelMenuRender;
import com.scarasol.tud.network.NetworkHandler;
import com.scarasol.tud.network.SwitchAmmoPacket;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Scarasol
 */
public class WheelMenuScreen extends Screen {

    private static final List<ItemStack> WHEEL_ITEMS = new ArrayList<>();

    public static void setWheelItems(List<ItemStack> stacks) {
        WHEEL_ITEMS.clear();
        WHEEL_ITEMS.addAll(stacks);
    }

    public static boolean hasWheelItems() {
        return !WHEEL_ITEMS.isEmpty();
    }

    private final List<ItemStack> items;
    private int selectedIndex = -1;

    private final float outerRadius = 90f;
    private final float innerRadius = 28f;
    private final float iconRadius = 58f;

    private final float aaPx = 1.2f;

    private final float lineWidthPx = 0.4f;

    private static final int BASE_COLOR = 0x591A1A1A;

    private static final int HIGHLIGHT_COLOR = 0x38B3B3B3;

    private static final int LINE_COLOR = 0x22E6E6E6;

    public WheelMenuScreen() {
        super(Component.empty());
        this.items = new ArrayList<>();
        this.items.addAll(WHEEL_ITEMS);
    }

    @Override
    protected void init() {
        selectedIndex = -1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        float cx = this.width / 2f;
        float cy = this.height / 2f;

        int n = items.size();
        if (n <= 0) {
            return;
        }

        WheelMenuRender.drawWheel(
                guiGraphics, cx, cy,
                outerRadius, innerRadius,
                n,
                selectedIndex,
                BASE_COLOR,
                HIGHLIGHT_COLOR,
                LINE_COLOR,
                lineWidthPx,
                aaPx
        );

        double sector = (Math.PI * 2.0) / n;
        for (int i = 0; i < n; i++) {
            double ang = -Math.PI / 2.0 + (i + 0.5) * sector;

            int ix = (int) Math.round(cx + Math.cos(ang) * iconRadius);
            int iy = (int) Math.round(cy + Math.sin(ang) * iconRadius);

            guiGraphics.renderItem(items.get(i), ix - 8, iy - 8);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        updateSelected(mouseX, mouseY);
    }

    private void updateSelected(double mouseX, double mouseY) {
        float cx = this.width / 2f;
        float cy = this.height / 2f;

        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > outerRadius || dist < innerRadius) {
            selectedIndex = -1;
            return;
        }

        int n = items.size();
        if (n <= 0) {
            selectedIndex = -1;
            return;
        }

        double ang = Math.atan2(dy, dx);
        ang = ang + Math.PI / 2.0;
        if (ang < 0) {
            ang += Math.PI * 2.0;
        }

        double sector = (Math.PI * 2.0) / n;
        int idx = (int) Math.floor(ang / sector);

        selectedIndex = (idx >= 0 && idx < n) ? idx : -1;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (selectedIndex >= 0 && selectedIndex < items.size()) {
                this.onClose();
                Minecraft.getInstance().setScreen(null);

            }
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        if (selectedIndex >= 0 && selectedIndex < items.size()) {
            ItemStack chosen = items.get(selectedIndex);
            Player player = Minecraft.getInstance().player;
            player.getMainHandItem().getOrCreateTag().putInt("TudCurrentAmmo", selectedIndex);
            IGunOperator.fromLivingEntity(player).initialData();
//                AttachmentPropertyManager.postChangeEvent(player, player.getMainHandItem());
            NetworkHandler.PACKET_HANDLER.sendToServer(new SwitchAmmoPacket(selectedIndex));

            TudMod.LOGGER.info("Chosen: {}", chosen);
        }
        super.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
