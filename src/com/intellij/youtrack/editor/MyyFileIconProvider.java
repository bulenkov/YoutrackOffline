package com.intellij.youtrack.editor;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.youtrack.MyyIcons;
import com.intellij.youtrack.fs.MyyFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class MyyFileIconProvider implements FileIconProvider {
  @Nullable
  @Override
  public Icon getIcon(@NotNull VirtualFile file, @Iconable.IconFlags int flags, @Nullable Project project) {
    if (file instanceof MyyFileSystem.MyyVirtualFile) {
      return MyyIcons.YOUTRACK;
    }
    return null;
  }
}
