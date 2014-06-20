package com.intellij.youtrack.editor;

import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.GraphicsUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class CommentsIndicator extends JLabel {
  public CommentsIndicator() {
    setVerticalAlignment(CENTER);
    setHorizontalAlignment(LEFT);
    setFont(new Font("Arial", Font.PLAIN, 11));
  }

  public void setCommentsNumber(int number) {
    if (number <= 0) {
      setText("");
      setBorder(null);
    } else {
      setText(String.valueOf(number));
      setBorder(new EmptyBorder(0, 3, 0, 3));
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (StringUtil.isEmpty(getText())) return;

    g.setColor(getBackground());
    GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
    g.fillRoundRect(0, 0, getWidth()-1, getHeight() - 1, 7, 7);


    super.paintComponent(g);
    final int h2 = getHeight() / 2;
    final int x0 = getWidth() - 8;
    final int y0 = h2 - 3;
    g.setColor(getForeground());
    g.fillPolygon(new int[]{x0, x0 + 4, x0}, new int[]{y0, y0 + 3, y0 + 6}, 3);
    config.restore();
  }

  @Override
  public Dimension getPreferredSize() {
    if (StringUtil.isEmpty(getText())) {
      return new Dimension(0, 0);
    }
    final Dimension size = super.getPreferredSize();
    size.width += 6;
    return size;
  }
}
