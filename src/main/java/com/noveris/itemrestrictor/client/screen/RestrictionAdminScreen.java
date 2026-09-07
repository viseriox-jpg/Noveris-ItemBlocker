package com.noveris.itemrestrictor.client.screen;

import com.noveris.itemrestrictor.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public final class RestrictionAdminScreen extends Screen {
    private static final int BG = 0xE80D0C09, PANEL = 0xFF17140E, YELLOW = 0xFFFFD84D, ACTIVE = 0xFFD6A800, TEXT = 0xFFFFFBE8, MUTED = 0xFFC9BE9B, DANGER = 0xFFFF6B5E;
    private final String adminName;
    private int left, top, panelWidth, panelHeight;
    private int tab;
    private final List<String> itemRules = new ArrayList<>();
    private RestrictionAdminScreen(String adminName) { super(Component.translatable("noveris_item_restrictor.screen.title")); this.adminName = adminName; }
    public static void open(String adminName) { Minecraft.getInstance().setScreen(new RestrictionAdminScreen(adminName)); }
    @Override protected void init() {
        panelWidth = Math.min(920, width - 36); panelHeight = Math.min(500, height - 32); left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        int y = top + 58, w = (panelWidth - 24) / 3;
        addRenderableWidget(Button.builder(Component.translatable("noveris_item_restrictor.screen.items"), b -> { tab = 0; clearWidgets(); init(); }).bounds(left + 12, y, w, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("noveris_item_restrictor.screen.players"), b -> { tab = 1; clearWidgets(); init(); }).bounds(left + 18 + w, y, w, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("noveris_item_restrictor.screen.mods"), b -> { tab = 2; clearWidgets(); init(); }).bounds(left + 24 + w * 2, y, w, 22).build());
        if (tab == 0) addRenderableWidget(Button.builder(Component.literal("+ ADICIONAR ITEM"), b -> addRule("minecraft:netherite_sword", false)).bounds(left + 18, top + panelHeight - 56, 180, 24).build());
    }
    private void addRule(String id, boolean allow) { NetworkHandler.send(new NetworkHandler.Action(allow ? "add_allowlist" : "add_block", id)); }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick); g.fill(left, top, left + panelWidth, top + panelHeight, BG); border(g, left, top, panelWidth, panelHeight, YELLOW); g.fill(left + 3, top + 42, left + panelWidth - 3, top + 44, YELLOW);
        g.drawString(font, "✚  CONTROLE DE ITENS", left + 16, top + 17, TEXT); g.drawString(font, "ADMINISTRADOR: " + adminName, left + panelWidth - 190, top + 17, MUTED);
        g.drawString(font, "◆ SINCRONIZADO", left + panelWidth / 2 - 48, top + panelHeight - 20, 0xFFFFC928); g.drawString(font, "NOVERIS", left + panelWidth - 62, top + panelHeight - 20, TEXT);
        if (tab == 0) renderItems(g); else if (tab == 1) renderEmpty(g, "PERMISSÕES DE JOGADORES"); else renderEmpty(g, "MODS CARREGADOS"); super.render(g, mouseX, mouseY, partialTick);
    }
    private void renderItems(GuiGraphics g) { g.drawString(font, "BUSCAR ITEM...", left + 18, top + 100, MUTED); g.fill(left + 18, top + 116, left + panelWidth - 18, top + 118, 0xFF5A4D26); g.drawString(font, "Itens registrados", left + 22, top + 134, TEXT); g.drawString(font, "Use os comandos ou o botão + para criar regras.", left + 22, top + 154, MUTED); }
    private void renderEmpty(GuiGraphics g, String title) { g.drawString(font, title, left + 22, top + 104, TEXT); g.drawString(font, "Nenhuma alteração pendente.", left + 22, top + 132, MUTED); }
    private void border(GuiGraphics g, int x, int y, int w, int h, int c) { g.fill(x, y, x + w, y + 3, c); g.fill(x, y + h - 3, x + w, y + h, c); g.fill(x, y, x + 3, y + h, c); g.fill(x + w - 3, y, x + w, y + h, c); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { Minecraft.getInstance().setScreen(null); }
}
