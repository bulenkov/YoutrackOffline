package com.intellij.youtrack.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.youtrack.model.MyyStorage;

/**
 * @author Konstantin Bulenkov
 */
public class UpdateIssuesFromServerAction extends AnAction implements DumbAware {

  @Override
  public void actionPerformed(AnActionEvent e) {
    MyyStorage.getInstance(e.getProject()).update();
  }

  @Override
  public void update(AnActionEvent e) {
    Project project = e.getProject();
    e.getPresentation().setEnabled(project != null && !MyyStorage.getInstance(project).isUpdating());
  }
}
