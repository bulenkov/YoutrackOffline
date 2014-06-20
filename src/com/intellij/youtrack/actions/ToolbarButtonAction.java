package com.intellij.youtrack.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Konstantin Bulenkov
 */
public abstract class ToolbarButtonAction extends AnAction implements CustomComponentAction, DumbAware {
  @Override
  public final void actionPerformed(AnActionEvent e) {
    if (!isOptionEnabled()) {
      enableOption();
    }
  }

  protected abstract void enableOption();

  public abstract boolean isOptionEnabled();

  public abstract String getText();

  @Override
  public JComponent createCustomComponent(Presentation presentation) {
    final JLabel label = new JLabel(getText()) {
      {
        setFont(new Font("Lucida Grande", Font.BOLD, 10));
        setBorder(new EmptyBorder(3, 7, 3, 7));
        setOpaque(false);
      }

      @Override
      protected void paintComponent(Graphics g) {
        GraphicsUtil.setupAAPainting(g);
        if (isOptionEnabled()) {
          g.setColor(new Color(133, 133, 134));
          g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight() / 2, getHeight() / 2);
          setForeground(Color.WHITE);
          super.paintComponent(g);
        } else {
          setForeground(UIUtil.getLabelForeground());
          super.paintComponent(g);
        }
      }
    };
    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        enableOption();
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            label.getParent().getParent().getParent().repaint();
          }
        });
      }
    });
    return label;
  }
}
