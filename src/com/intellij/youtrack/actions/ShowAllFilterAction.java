package com.intellij.youtrack.actions;

import com.intellij.ui.components.JBList;
import com.intellij.youtrack.editor.MyyIssuesModel;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class ShowAllFilterAction extends ToolbarButtonAction {
  private JBList myList;

  public ShowAllFilterAction(JBList list) {
    myList = list;
  }

  @Override
  protected void enableOption() {
    ListModel model = myList.getModel();
    if (model instanceof MyyIssuesModel) {
      ((MyyIssuesModel) model).setTypeFilter(MyyIssuesModel.TypeFilter.ALL);
    }
  }

  @Override
  public boolean isOptionEnabled() {
    ListModel model = myList.getModel();
    if (model instanceof MyyIssuesModel) {
      return ((MyyIssuesModel) model).getTypeFilter() == MyyIssuesModel.TypeFilter.ALL;
    }

    return false;
  }

  @Override
  public String getText() {
    return "All";
  }
}
