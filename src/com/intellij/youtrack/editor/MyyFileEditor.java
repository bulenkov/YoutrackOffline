package com.intellij.youtrack.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.youtrack.MyyLoginPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeListener;

/**
 * @author Konstantin Bulenkov
 */
public class MyyFileEditor extends UserDataHolderBase implements FileEditor, DumbAware {
  private final MyyLoginPanel myLogin;
  private final MyyIssueViewer myViewer;
  private Project myProject;

  public MyyFileEditor(Project project) {
    myProject = project;
    myLogin = new MyyLoginPanel(project);
    myViewer = new MyyIssueViewer(project, this);
    Disposer.register(this, myViewer);
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    if (isLogged(myProject)) {
      return myViewer;
    } else {
      return myLogin.getRoot();
    }
  }

  public static boolean isLogged(Project project) {
    return PropertiesComponent.getInstance(project).isTrueValue("MYY_LOGGED");
  }

  public static void setLogged(Project project, boolean logged) {
    PropertiesComponent.getInstance(project).setValue("MYY_LOGGED", String.valueOf(logged));
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    if (isLogged(myProject)) {
      return null;
    } else {
      for (JTextComponent textComponent : new JTextComponent[]{myLogin.getHost(), myLogin.getLogin(), myLogin.getPassword()}) {
        if (StringUtil.isEmpty(textComponent.getText())) {
          return textComponent;
        }
      }
    }
    return null;
  }

  @NotNull
  @Override
  public String getName() {
    return "Myy";
  }

  @NotNull
  @Override
  public FileEditorState getState(@NotNull FileEditorStateLevel level) {
    return new MyyEditorState();
  }

  @Override
  public void setState(@NotNull FileEditorState state) {

  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void selectNotify() {

  }

  @Override
  public void deselectNotify() {

  }

  @Override
  public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

  }

  @Override
  public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

  }

  @Nullable
  @Override
  public BackgroundEditorHighlighter getBackgroundHighlighter() {
    return null;
  }

  @Nullable
  @Override
  public FileEditorLocation getCurrentLocation() {
    return null;
  }

  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder() {
    return null;
  }

  @Override
  public void dispose() {

  }
}
