package com.maybank.appsense.gui.panel;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.List;

/** Minimal bar-chart sparkline painted directly on the panel. */
public final class SparklinePanel extends JPanel {

    private static final Color BAR_COLOR = new Color(0xd45f5f);
    private List<Integer> values = Collections.emptyList();

    public SparklinePanel() {
        setPreferredSize(new Dimension(
                com.maybank.appsense.gui.GuiConstant.SPARKLINE_WIDTH,
                com.maybank.appsense.gui.GuiConstant.SPARKLINE_HEIGHT));
        setOpaque(false);
    }

    public void setValues(List<Integer> vals) {
        values = vals == null ? Collections.emptyList() : vals;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values.isEmpty()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(BAR_COLOR);

        int w = getWidth();
        int h = getHeight();
        int n = values.size();
        int max = values.stream().mapToInt(Integer::intValue).max().orElse(1);
        if (max == 0) {
            g2.dispose();
            return;
        }
        int barW = Math.max(1, w / n);
        for (int i = 0; i < n; i++) {
            int barH = (int) ((double) values.get(i) / max * h);
            g2.fillRect(i * barW, h - barH, barW - 1, barH);
        }
        g2.dispose();
    }
}
