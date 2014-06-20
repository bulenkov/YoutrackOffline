package com.intellij.youtrack.editor;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.youtrack.model.MyyIssue;

/**
 * @author Konstantin Bulenkov
 */
public interface MyyDataKeys {
  DataKey<MyyIssue[]> ISSUES_ARRAY = DataKey.create("MYY_ISSUES_ARRAY");
}
