package com.noveris.itemrestrictor.client.screen;

import com.noveris.itemrestrictor.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public final class RestrictionAdminScreen extends Screen {
    private static final int BG = 0xF01F1B0C, PANEL = 0xFF14110C, YELLOW = 0xFFFFD84D, ACTIVE = 0xFFE1B300, HOVER = 0xFFF2C94C, TEXT = 0xFFFFFBEB, MUTED = 0xFFC9BE9B, DANGER = 0xFFFF6B5E;
    private final String adminName;
    private final String feedback;
    private boolean addModal;
    private boolean allowlist;
    private EditBox itemInput;
    private int left, top, panelWidth, panelHeight;
    private int tab;
    private final List<String> itemRules = new ArrayList<>();
    private RestrictionAdminScreen(String adminName, String feedback, String itemsCsv) { super(Component.translatable("noveris_item_restrictor.screen.title")); this.adminName = adminName; this.feedback = feedback; if (!itemsCsv.isEmpty()) for (String id : itemsCsv.split(",")) if (!id.isBlank()) itemRules.add(id); }
    public static void open(String adminName, String feedback, String itemsCsv) { Minecraft.getInstance().setScreen(new RestrictionAdminScreen(adminName, feedback, itemsCsv)); }
    private int tabsY, tabWidth, addX, addY, addWidth, addHeight, modalLeft, modalTop, modalWidth, modalHeight;
    @Override protected void init() {
        Minecraft.getInstance().gameRenderer.shutdownEffect();
        panelWidth = Math.min(1970, width - 36); panelHeight = Math.min(740, height - 32); left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        tabsY = top + 138; tabWidth = (panelWidth - 36) / 3;
        addX = left + 18; addY = top + panelHeight - 56; addWidth = 210; addHeight = 24;
        if (addModal) { modalWidth = Math.min(760, width - 48); modalHeight = Math.min(250, height - 48); modalLeft = (width - modalWidth) / 2; modalTop = (height - modalHeight) / 2; itemInput = new EditBox(font, modalLeft + 28, modalTop + 76, modalWidth - 56, 28, Component.translatable("noveris_item_restrictor.screen.item_id")); itemInput.setBordered(false); itemInput.setHint(Component.translatable("noveris_item_restrictor.screen.item_hint")); addRenderableWidget(itemInput); itemInput.setFocused(true); }
    }
    private void addRule(String id, boolean allow) { NetworkHandler.sendAction(new NetworkHandler.Action(allow ? "allow_item" : "block_item", id)); }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Minecraft.getInstance().gameRenderer.shutdownEffect();
        g.fill(left, top, left + panelWidth, top + panelHeight, BG); border(g, left, top, panelWidth, panelHeight, YELLOW); g.fill(left + 4, top + 100, left + panelWidth - 4, top + 104, YELLOW);
        cornerMarks(g);
        g.drawString(font, "✚  CONTROLE DE ITENS", left + 16, top + 17, TEXT); g.drawString(font, "ADMINISTRADOR: " + adminName, left + panelWidth - 190, top + 17, MUTED);
        drawTab(g, 0, "ITENS", mouseX, mouseY); drawTab(g, 1, "JOGADORES", mouseX, mouseY); drawTab(g, 2, "MODS", mouseX, mouseY);
        if (tab == 0) drawButton(g, addX, addY, addWidth, addHeight, "+ ADICIONAR ITEM", mouseX, mouseY, false);
        g.drawString(font, "◆ SINCRONIZADO", left + panelWidth / 2 - 48, top + panelHeight - 20, 0xFFFFC928); g.drawString(font, "NOVERIS", left + panelWidth - 62, top + panelHeight - 20, TEXT);
        if (tab == 0) renderItems(g); else if (tab == 1) renderEmpty(g, "PERMISSÕES DE JOGADORES"); else renderEmpty(g, "MODS CARREGADOS");
        if (addModal) drawModal(g, mouseX, mouseY);
        super.render(g, mouseX, mouseY, partialTick);
    }
    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // NeoForge invokes this method automatically before render(). Do not call the vanilla blur.
        Minecraft.getInstance().gameRenderer.shutdownEffect();
        g.fill(0, 0, width, height, 0x52000000);
    }
    private void drawModal(GuiGraphics g, int mouseX, int mouseY) {
        // Opaque content scrim: no label from the underlying screen may remain visible.
        g.fill(0, 0, width, height, 0xF00D0C09);
        g.fill(modalLeft, modalTop, modalLeft + modalWidth, modalTop + modalHeight, BG);
        border(g, modalLeft, modalTop, modalWidth, modalHeight, YELLOW);
        g.drawString(font, Component.translatable("noveris_item_restrictor.screen.add_rule"), modalLeft + 28, modalTop + 24, TEXT);
        g.drawString(font, Component.translatable("noveris_item_restrictor.screen.item_id"), modalLeft + 28, modalTop + 52, MUTED);
        g.fill(modalLeft + 24, modalTop + 70, modalLeft + modalWidth - 24, modalTop + 112, PANEL);
        border(g, modalLeft + 24, modalTop + 70, modalWidth - 48, 42, YELLOW);
        int y = modalTop + modalHeight - 54;
        drawButton(g, modalLeft + 28, y, 180, 32, Component.translatable("noveris_item_restrictor.screen.cancel").getString(), mouseX, mouseY, true);
        drawButton(g, modalLeft + 220, y, 180, 32, Component.translatable("noveris_item_restrictor.screen.confirm").getString(), mouseX, mouseY, false);
        drawButton(g, modalLeft + 412, y, Math.max(180, modalWidth - 440), 32, allowlist ? "ALLOWLIST" : "BLOQUEIO TOTAL", mouseX, mouseY, false);
    }
    private void renderItems(GuiGraphics g) { g.drawString(font, "BUSCAR ITEM...", left + 18, top + 100, MUTED); g.fill(left + 18, top + 116, left + panelWidth - 18, top + 118, 0xFF5A4D26); g.drawString(font, "Itens registrados (" + itemRules.size() + ")", left + 22, top + 134, TEXT); if (itemRules.isEmpty()) g.drawString(font, "Nenhuma regra cadastrada.", left + 22, top + 160, MUTED); else for (int i = 0; i < Math.min(itemRules.size(), 8); i++) { int y = top + 160 + i * 28; g.fill(left + 18, y - 4, left + panelWidth - 18, y + 20, PANEL); g.drawString(font, itemRules.get(i), left + 28, y + 3, TEXT); g.drawString(font, "REMOVER", left + panelWidth - 92, y + 3, DANGER); } if (!feedback.isEmpty()) g.drawString(font, feedback, left + 22, top + panelHeight - 78, feedback.startsWith("ERRO") || feedback.startsWith("ITEM") ? DANGER : 0xFFFFC928); }
    private void renderEmpty(GuiGraphics g, String title) { g.drawString(font, title, left + 22, top + 104, TEXT); g.drawString(font, "Nenhuma alteração pendente.", left + 22, top + 132, MUTED); }
    private void drawTab(GuiGraphics g, int index, String label, int mouseX, int mouseY) { int x = left + 12 + index * (tabWidth + 6); boolean hover = inside(mouseX, mouseY, x, tabsY, tabWidth, 48); int color = tab == index ? ACTIVE : (hover ? HOVER : PANEL); g.fill(x, tabsY, x + tabWidth, tabsY + 48, color); border(g, x, tabsY, tabWidth, 48, YELLOW); g.drawCenteredString(font, label, x + tabWidth / 2, tabsY + 18, TEXT); }
    private void drawButton(GuiGraphics g, int x, int y, int w, int h, String label, int mouseX, int mouseY, boolean danger) { int color = inside(mouseX, mouseY, x, y, w, h) ? HOVER : (danger ? DANGER : PANEL); g.fill(x, y, x + w, y + h, color); border(g, x, y, w, h, danger ? DANGER : YELLOW); g.drawCenteredString(font, label, x + w / 2, y + (h - 8) / 2, TEXT); }
    private void cornerMarks(GuiGraphics g) { int c = YELLOW; int x1 = left + 9, x2 = left + panelWidth - 17, y1 = top + 9, y2 = top + panelHeight - 17; g.drawString(font, "+", x1, y1, c); g.drawString(font, "+", x2, y1, c); g.drawString(font, "+", x1, y2, c); g.drawString(font, "+", x2, y2, c); }
    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) { return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h; }
    private void submitAdd() { if (itemInput != null && !itemInput.getValue().isBlank()) addRule(itemInput.getValue().trim(), allowlist); }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { if (button == 0) { if (addModal) { int y = modalTop + modalHeight - 54; if (inside(mouseX, mouseY, modalLeft + 28, y, 180, 32)) { addModal = false; rebuildWidgets(); return true; } if (inside(mouseX, mouseY, modalLeft + 220, y, 180, 32)) { submitAdd(); return true; } if (inside(mouseX, mouseY, modalLeft + 412, y, Math.max(180, modalWidth - 440), 32)) { allowlist = !allowlist; return true; } } for (int i = 0; i < 3; i++) { int x = left + 12 + i * (tabWidth + 6); if (inside(mouseX, mouseY, x, tabsY, tabWidth, 48)) { tab = i; return true; } } if (tab == 0 && inside(mouseX, mouseY, addX, addY, addWidth, addHeight)) { addModal = true; rebuildWidgets(); return true; } if (tab == 0) for (int i = 0; i < Math.min(itemRules.size(), 8); i++) { int y = top + 160 + i * 28; if (inside(mouseX, mouseY, left + panelWidth - 110, y - 4, 100, 24)) { NetworkHandler.sendAction(new NetworkHandler.Action("remove_item", itemRules.get(i))); return true; } } } return super.mouseClicked(mouseX, mouseY, button); }
    private void border(GuiGraphics g, int x, int y, int w, int h, int c) { g.fill(x, y, x + w, y + 3, c); g.fill(x, y + h - 3, x + w, y + h, c); g.fill(x, y, x + 3, y + h, c); g.fill(x + w - 3, y, x + w, y + h, c); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { Minecraft.getInstance().setScreen(null); }
}
