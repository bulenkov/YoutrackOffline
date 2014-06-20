package com.intellij.youtrack.editor;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;

/**
 * @author Konstantin Bulenkov
 */
public class MyyEditorState implements FileEditorState {
  @Override
  public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
    return false;
  }
}
