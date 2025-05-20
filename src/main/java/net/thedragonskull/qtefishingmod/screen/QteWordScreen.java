package net.thedragonskull.qtefishingmod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thedragonskull.qtefishingmod.QteFishingMod;
import net.thedragonskull.qtefishingmod.network.C2SQTEWordPacket;
import net.thedragonskull.qtefishingmod.network.PacketHandler;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class QteWordScreen extends Screen {

    private static final ResourceLocation BASE = new ResourceLocation(QteFishingMod.MOD_ID, "textures/gui/qte_bg.png");

    private String word;
    private int currentIndex;

    public QteWordScreen(String word) {
        super(Component.empty());
        this.word = word;
        this.currentIndex = 0;
    }

    public void setNewWord(String word) {
        this.word = word;
        this.currentIndex = 0;
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        String s = String.valueOf(codePoint).toUpperCase(Locale.ROOT);

        if (s.matches("^[A-Z0-9]$")) {
            PacketHandler.sendToServer(new C2SQTEWordPacket(s));
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        int midWidth = this.width / 2;
        int midHeight = this.height / 2;

        guiGraphics.drawCenteredString(this.font, "¡Type the word!", midWidth, midHeight - 40, 0xFFFFFF);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int frameSize = 48;
        guiGraphics.blit(BASE, midWidth - frameSize / 2, midHeight - frameSize / 2, 0, 0, frameSize, frameSize, frameSize, frameSize);

        RenderSystem.disableBlend();

        guiGraphics.pose().pushPose();

        float scale = 3.0f;
        guiGraphics.pose().scale(scale, scale, 1.0f);
        float xStart = midWidth / scale - ((float) font.width(word) / 2);

        for (int i = 0; i < word.length(); i++) {
            String letter = String.valueOf(word.charAt(i));
            int color = (i == currentIndex) ? 0xFF0000 : 0xFFFFFF;
            guiGraphics.drawString(this.font, letter, (int) xStart, 0, color, false);
            xStart += font.width(letter);
        }

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
