package com.intellij.youtrack.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.youtrack.editor.MyyDataKeys;
import com.intellij.youtrack.model.MyyIssue;

/**
 * @author Konstantin Bulenkov
 */
public class MarkAsReadAction extends AnAction implements DumbAware {
  @Override
  public void actionPerformed(AnActionEvent e) {
    MyyIssue[] issues = e.getData(MyyDataKeys.ISSUES_ARRAY);
    for (MyyIssue issue : issues) {
      issue.setRead(true);
    }
  }

  @Override
  public void update(AnActionEvent e) {
    MyyIssue[] issues = e.getData(MyyDataKeys.ISSUES_ARRAY);
    if (issues != null) {
      for (MyyIssue issue : issues) {
        if (!issue.isRead()) {
          e.getPresentation().setEnabledAndVisible(true);
          return;
        }
      }
    }
    e.getPresentation().setEnabledAndVisible(false);
  }
}
