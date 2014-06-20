package com.intellij.youtrack.fs;

import com.intellij.ide.presentation.Presentation;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.DeprecatedVirtualFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Konstantin Bulenkov
 */
public class MyyFileSystem extends DeprecatedVirtualFileSystem {
  private final static MyyFileSystem instance = new MyyFileSystem();
  private MyyVirtualFile file;

  public MyyFileSystem() {
    file = new MyyVirtualFile();
  }

  public static MyyFileSystem getInstance() {
    return instance;
  }

  public MyyVirtualFile getFile() {
    return file;
  }

  @NotNull
  @Override
  public String getProtocol() {
    return "myy";
  }

  @Nullable
  @Override
  public VirtualFile findFileByPath(@NotNull @NonNls String path) {
    return getFile();
  }

  @Override
  public void refresh(boolean asynchronous) {

  }

  @Nullable
  @Override
  public VirtualFile refreshAndFindFileByPath(@NotNull String path) {
    return findFileByPath(path);
  }

  @Override
  protected void deleteFile(Object requestor, @NotNull VirtualFile vFile) throws IOException {
    throw new IOException("Not supported");
  }

  @Override
  protected void moveFile(Object requestor, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent) throws IOException {
    throw new IOException("Not supported");
  }

  @Override
  protected void renameFile(Object requestor, @NotNull VirtualFile vFile, @NotNull String newName) throws IOException {
    throw new IOException("Not supported");
  }

  @Override
  protected VirtualFile createChildFile(Object requestor, @NotNull VirtualFile vDir, @NotNull String fileName) throws IOException {
    throw new IOException("Not supported");
  }

  @NotNull
  @Override
  protected VirtualFile createChildDirectory(Object requestor, @NotNull VirtualFile vDir, @NotNull String dirName) throws IOException {
    throw new IOException("Not supported");
  }

  @Override
  protected VirtualFile copyFile(Object requestor, @NotNull VirtualFile virtualFile, @NotNull VirtualFile newParent, @NotNull String copyName) throws IOException {
    throw new IOException("Not supported");
  }

  @Presentation(icon = "/icons/youtrack.png")
  public class MyyVirtualFile extends VirtualFile {
    @NotNull
    @Override
    public String getName() {
      return "YouTrack";
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
      return MyyFileSystem.this;
    }

    @Override
    public String getPath() {
      return "YouTrack";
    }

    @Override
    public boolean isWritable() {
      return true;
    }

    @Override
    public boolean isDirectory() {
      return false;
    }

    @Override
    public boolean isValid() {
      return true;
    }

    @Override
    public VirtualFile getParent() {
      return null;
    }

    @Override
    public VirtualFile[] getChildren() {
      return EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
      throw new IOException("Not supported");
    }

    @NotNull
    @Override
    public byte[] contentsToByteArray() throws IOException {
      return new byte[0];
    }

    @Override
    public long getTimeStamp() {
      return 0;
    }

    @Override
    public long getLength() {
      return 0;
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive, @Nullable Runnable postRunnable) {

    }

    @Override
    public InputStream getInputStream() throws IOException {
      throw new IOException("Not supported");
    }

    @NotNull
    @Override
    public FileType getFileType() {
      return MyyFileType.INSTANCE;
    }

    @Override
    public long getModificationStamp() {
      return 0;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof MyyVirtualFile;
    }
  }
}
