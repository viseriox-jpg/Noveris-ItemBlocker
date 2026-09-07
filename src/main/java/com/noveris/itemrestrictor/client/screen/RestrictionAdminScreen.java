package com.noveris.itemrestrictor.client.screen;

import com.noveris.itemrestrictor.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public final class RestrictionAdminScreen extends Screen {
    private static final int BG = 0xE80D0C09, PANEL = 0xFF17140E, YELLOW = 0xFFFFD84D, ACTIVE = 0xFFD6A800, HOVER = 0xFFF2C94C, TEXT = 0xFFFFFBE8, MUTED = 0xFFC9BE9B, DANGER = 0xFFFF6B5E;
    private final String adminName;
    private final String feedback;
    private int left, top, panelWidth, panelHeight;
    private int tab;
    private final List<String> itemRules = new ArrayList<>();
    private RestrictionAdminScreen(String adminName, String feedback) { super(Component.translatable("noveris_item_restrictor.screen.title")); this.adminName = adminName; this.feedback = feedback; }
    public static void open(String adminName, String feedback) { Minecraft.getInstance().setScreen(new RestrictionAdminScreen(adminName, feedback)); }
    private int tabsY, tabWidth, addX, addY, addWidth, addHeight;
    @Override protected void init() {
        Minecraft.getInstance().gameRenderer.shutdownEffect();
        panelWidth = Math.min(920, width - 36); panelHeight = Math.min(500, height - 32); left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        tabsY = top + 58; tabWidth = (panelWidth - 36) / 3;
        addX = left + 18; addY = top + panelHeight - 56; addWidth = 210; addHeight = 24;
    }
    private void addRule(String id, boolean allow) { NetworkHandler.send(new NetworkHandler.Action(allow ? "add_allowlist" : "add_block", id)); }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Minecraft.getInstance().gameRenderer.shutdownEffect();
        g.fill(left, top, left + panelWidth, top + panelHeight, BG); border(g, left, top, panelWidth, panelHeight, YELLOW); g.fill(left + 3, top + 42, left + panelWidth - 3, top + 44, YELLOW);
        cornerMarks(g);
        g.drawString(font, "✚  CONTROLE DE ITENS", left + 16, top + 17, TEXT); g.drawString(font, "ADMINISTRADOR: " + adminName, left + panelWidth - 190, top + 17, MUTED);
        drawTab(g, 0, "ITENS", mouseX, mouseY); drawTab(g, 1, "JOGADORES", mouseX, mouseY); drawTab(g, 2, "MODS", mouseX, mouseY);
        if (tab == 0) drawButton(g, addX, addY, addWidth, addHeight, "+ ADICIONAR ITEM", mouseX, mouseY, false);
        g.drawString(font, "◆ SINCRONIZADO", left + panelWidth / 2 - 48, top + panelHeight - 20, 0xFFFFC928); g.drawString(font, "NOVERIS", left + panelWidth - 62, top + panelHeight - 20, TEXT);
        if (tab == 0) renderItems(g); else if (tab == 1) renderEmpty(g, "PERMISSÕES DE JOGADORES"); else renderEmpty(g, "MODS CARREGADOS"); super.render(g, mouseX, mouseY, partialTick);
    }
    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // NeoForge invokes this method automatically before render(). Do not call the vanilla blur.
        Minecraft.getInstance().gameRenderer.shutdownEffect();
        g.fill(0, 0, width, height, 0x52000000);
    }
    private void renderItems(GuiGraphics g) { g.drawString(font, "BUSCAR ITEM...", left + 18, top + 100, MUTED); g.fill(left + 18, top + 116, left + panelWidth - 18, top + 118, 0xFF5A4D26); g.drawString(font, "Itens registrados", left + 22, top + 134, TEXT); g.drawString(font, "Use os comandos ou o botão + para criar regras.", left + 22, top + 154, MUTED); if (!feedback.isEmpty()) g.drawString(font, feedback, left + 22, top + 185, feedback.startsWith("ERRO") || feedback.startsWith("ITEM") ? DANGER : 0xFFFFC928); }
    private void renderEmpty(GuiGraphics g, String title) { g.drawString(font, title, left + 22, top + 104, TEXT); g.drawString(font, "Nenhuma alteração pendente.", left + 22, top + 132, MUTED); }
    private void drawTab(GuiGraphics g, int index, String label, int mouseX, int mouseY) { int x = left + 12 + index * (tabWidth + 6); boolean hover = inside(mouseX, mouseY, x, tabsY, tabWidth, 22); int color = tab == index ? ACTIVE : (hover ? HOVER : PANEL); g.fill(x, tabsY, x + tabWidth, tabsY + 22, color); border(g, x, tabsY, tabWidth, 22, YELLOW); g.drawCenteredString(font, label, x + tabWidth / 2, tabsY + 7, TEXT); }
    private void drawButton(GuiGraphics g, int x, int y, int w, int h, String label, int mouseX, int mouseY, boolean danger) { int color = inside(mouseX, mouseY, x, y, w, h) ? HOVER : (danger ? DANGER : PANEL); g.fill(x, y, x + w, y + h, color); border(g, x, y, w, h, danger ? DANGER : YELLOW); g.drawCenteredString(font, label, x + w / 2, y + 8, TEXT); }
    private void cornerMarks(GuiGraphics g) { int c = YELLOW; int x1 = left + 9, x2 = left + panelWidth - 17, y1 = top + 9, y2 = top + panelHeight - 17; g.drawString(font, "+", x1, y1, c); g.drawString(font, "+", x2, y1, c); g.drawString(font, "+", x1, y2, c); g.drawString(font, "+", x2, y2, c); }
    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) { return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h; }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { if (button == 0) { for (int i = 0; i < 3; i++) { int x = left + 12 + i * (tabWidth + 6); if (inside(mouseX, mouseY, x, tabsY, tabWidth, 22)) { tab = i; return true; } } if (tab == 0 && inside(mouseX, mouseY, addX, addY, addWidth, addHeight)) { addRule("minecraft:netherite_sword", false); return true; } } return super.mouseClicked(mouseX, mouseY, button); }
    private void border(GuiGraphics g, int x, int y, int w, int h, int c) { g.fill(x, y, x + w, y + 3, c); g.fill(x, y + h - 3, x + w, y + h, c); g.fill(x, y, x + 3, y + h, c); g.fill(x + w - 3, y, x + w, y + h, c); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { Minecraft.getInstance().setScreen(null); }
}
