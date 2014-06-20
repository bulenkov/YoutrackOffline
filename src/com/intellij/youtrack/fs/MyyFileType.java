package com.intellij.youtrack.fs;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.youtrack.MyyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class MyyFileType implements FileType {
  public static final MyyFileType INSTANCE = new MyyFileType();

  private MyyFileType() {
  }

  @NotNull
  @Override
  public String getName() {
    return "My Youtrack";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "myy";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return MyyIcons.YOUTRACK;
  }

  @Override
  public boolean isBinary() {
    return false;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Nullable
  @Override
  public String getCharset(@NotNull VirtualFile file, byte[] content) {
    return null;
  }
}
