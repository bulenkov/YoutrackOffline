package com.intellij.youtrack.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.youtrack.fs.MyyFileSystem;

/**
 * @author Konstantin Bulenkov
 */
public class OpenYouTrackAction extends AnAction implements DumbAware {
  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getProject();
    open(project);
  }

  public static void open(Project project) {
    final MyyFileSystem.MyyVirtualFile f = MyyFileSystem.getInstance().getFile();
    FileEditorManagerEx mgr = FileEditorManagerEx.getInstanceEx(project);
    mgr.openFile(f, true);
    mgr.getActiveWindow().doWhenDone(new AsyncResult.Handler<EditorWindow>() {
      @Override
      public void run(EditorWindow window) {
        window.setFilePinned(f, true);
      }
    });
  }

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setEnabledAndVisible(e.getProject() != null);
  }
}
