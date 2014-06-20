package com.intellij.youtrack.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.JavaPsiFacadeEx;
import com.intellij.psi.impl.compiled.ClsClassImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.BrowserHyperlinkListener;

import javax.swing.event.HyperlinkEvent;
import java.net.URI;

/**
 * @author Konstantin Bulenkov
 */
public class MyyHyperlinkListener extends BrowserHyperlinkListener {
  private Project myProject;

  public MyyHyperlinkListener(Project project) {
    myProject = project;
  }

  @Override
  protected void hyperlinkActivated(HyperlinkEvent e) {
    String s = e.getDescription();
    if (s != null && s.startsWith("ide://")) {
      try {
        final URI url = new URI(s);
        String fqn = url.getHost();

        if (!StringUtil.isEmpty(fqn)) {
          PsiClass psiClass = JavaPsiFacadeEx.getInstance(myProject).findClass(fqn, GlobalSearchScope.allScope(myProject));
          if (psiClass != null) {
            if (psiClass instanceof ClsClassImpl) {
              psiClass = ((ClsClassImpl) psiClass).getSourceMirrorClass();
              if (psiClass == null) return;
            }
            final VirtualFile file = psiClass.getContainingFile().getVirtualFile();
            if (file != null) {
              ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                  int line = -1;
                  try {
                    line = Integer.parseInt(url.getPath().substring(1)) - 1;
                  } catch (NumberFormatException e1) {
                  }
                  OpenFileDescriptor descriptor = line > -1 ? new OpenFileDescriptor(myProject, file, line, 0) : new OpenFileDescriptor(myProject, file);
                  descriptor.navigate(true);
                }
              });
            }

          }
        }
      } catch (Exception e1) {
        e1.printStackTrace();
      }
      return;
    }
    super.hyperlinkActivated(e);
  }
}
