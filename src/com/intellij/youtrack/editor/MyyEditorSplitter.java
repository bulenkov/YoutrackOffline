package com.intellij.youtrack.editor;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Konstantin Bulenkov
 */
public class MyyEditorSplitter extends JBSplitter {
  private static final String WIDTH_PROPERTY_NAME = "MyyMessagesListWidth";
  private int myMessagesWidth;
  private Project myProject;

  public MyyEditorSplitter(Project project) {
    super(false);
    myProject = project;
    myMessagesWidth = (int) PropertiesComponent.getInstance(project).getOrInitLong(WIDTH_PROPERTY_NAME, 350);
  }

  @Override
  public void addNotify() {
    super.addNotify();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        setProportion((float)myMessagesWidth / (float)getWidth());
        getFirstComponent().addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            PropertiesComponent.getInstance(myProject).setValue(WIDTH_PROPERTY_NAME, String.valueOf(Math.max(350, e.getComponent().getWidth())));
          }
        });
      }
    });
  }




}
