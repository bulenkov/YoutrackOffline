package com.intellij.youtrack.actions;

import com.intellij.ui.components.JBList;
import com.intellij.youtrack.editor.MyyIssuesModel;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
  public class ShowMajorFilterAction extends ToolbarButtonAction {
    private JBList myList;

    public ShowMajorFilterAction(JBList list) {
      myList = list;
    }

    @Override
    protected void enableOption() {
      ListModel model = myList.getModel();
      if (model instanceof MyyIssuesModel) {
        ((MyyIssuesModel) model).setTypeFilter(MyyIssuesModel.TypeFilter.MAJOR);
      }
    }

    @Override
    public boolean isOptionEnabled() {
      ListModel model = myList.getModel();
      if (model instanceof MyyIssuesModel) {
        return ((MyyIssuesModel) model).getTypeFilter() == MyyIssuesModel.TypeFilter.MAJOR;
      }

      return false;
    }

    @Override
    public String getText() {
      return "Major";
    }
  }