package net.thedragonskull.qtefishingmod.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thedragonskull.qtefishingmod.QteFishingMod;
import net.thedragonskull.qtefishingmod.network.C2SQTEPacket;
import net.thedragonskull.qtefishingmod.network.PacketHandler;
import org.lwjgl.glfw.GLFW;

public class QteScreen extends Screen {

    private static final ResourceLocation BASE = new ResourceLocation(QteFishingMod.MOD_ID, "textures/gui/qte_bg.png");

    private String currentKey;

    public QteScreen(String initialKey) {
        super(Component.empty());
        this.currentKey = initialKey;
    }

    public void setCurrentKey(String key) {
        this.currentKey = key;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String keyString = InputConstants.getKey(keyCode, scanCode).getDisplayName().getString();

        if (keyString.matches("^[A-Z0-9]$")) {
            if (keyString.equalsIgnoreCase(currentKey)) {
                PacketHandler.sendToServer(new C2SQTEPacket(keyString));
            }

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);


        //TEXT
        int midWidth = this.width / 2;
        int midHeight = this.height / 2;

        guiGraphics.drawCenteredString(this.font, "¡Press the key!", midWidth, midHeight - 40, 0xFFFFFF);

        //FRAME
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int frameSize = 48;
        guiGraphics.blit(BASE, midWidth - frameSize / 2, midHeight - frameSize / 2, 0, 0, frameSize, frameSize, frameSize, frameSize);

        RenderSystem.disableBlend();

        //CHAR
        guiGraphics.pose().pushPose();

        float scale = 4.0f;
        int textWidth = this.font.width(currentKey);
        int textHeight = this.font.lineHeight;

        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.pose().translate(((float) midWidth / 4) - ((float) textWidth / 2.25), ((float) midHeight / 4) - ((float) textHeight / 2.5), 0);

        guiGraphics.drawString(this.font, currentKey, 0, 0, 0xFFFFFF, false);

        guiGraphics.pose().popPose();

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void init() {
        super.init();

        long windowHandle = Minecraft.getInstance().getWindow().getWindow();
        GLFW.glfwSetCursorPos(windowHandle, 0, 0);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

}
