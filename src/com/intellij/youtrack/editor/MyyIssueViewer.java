package com.intellij.youtrack.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBLoadingPanel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.Alarm;
import com.intellij.youtrack.actions.ShowAllFilterAction;
import com.intellij.youtrack.actions.ShowMajorFilterAction;
import com.intellij.youtrack.actions.ShowUnresolvedFilterAction;
import com.intellij.youtrack.model.MyyIssue;
import com.intellij.youtrack.model.MyyStorage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Konstantin Bulenkov
 */
public class MyyIssueViewer extends JBLoadingPanel implements DataProvider, Disposable {

  private final Alarm myUpdater;
  private Project myProject;
  private final JBList myList;

  public MyyIssueViewer(final Project project, Disposable parent) {
    super(new BorderLayout(), parent);
    myProject = project;
    myUpdater = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, parent);
    myList = new JBList();
    MyyEditorSplitter splitter = new MyyEditorSplitter(project);
    final MyyMessageBrowser browser = new MyyMessageBrowser(project);
    myList.setFixedCellHeight(80);
    myList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        final MyyIssue issue = (MyyIssue) myList.getSelectedValue();
        if (issue != null) {
          myUpdater.cancelAllRequests();
          myUpdater.addRequest(new Runnable() {
            @Override
            public void run() {
              browser.showIssue(issue);
              if (!issue.isRead()) {
                myUpdater.addRequest(new Runnable() {
                  @Override
                  public void run() {
                    issue.setRead(true);
                    myList.repaint();
                    myUpdater.addRequest(new Runnable() {
                      @Override
                      public void run() {
                        MyyStorage.getInstance(project).save();
                      }
                    }, 60000);
                  }
                }, 3000); //yes, 3 sec to read
              }
            }
          }, 300);
        }
      }
    });
    myList.setCellRenderer(new MyyIssueListCellRenderer());
    final JBScrollPane scrollPane = new JBScrollPane(myList, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        myList.setFixedCellWidth(scrollPane.getVisibleRect().width - 30);
      }
    });
    myList.setExpandableItemsEnabled(false);
    ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu("MYY_VIEWER", (ActionGroup) ActionManager.getInstance().getAction("my.youtrack.context.menu"));
    myList.setComponentPopupMenu(popupMenu.getComponent());
    splitter.setFirstComponent(scrollPane);
    splitter.setSecondComponent(browser);
    add(splitter, BorderLayout.CENTER);
    JBPanel top = new JBPanel(new BorderLayout());
    AnAction action = ActionManager.getInstance().getAction("my.youtrack.main.toolbar");
    DefaultActionGroup group = new DefaultActionGroup(action, new ShowAllFilterAction(myList), new ShowUnresolvedFilterAction(myList), new ShowMajorFilterAction(myList));
    ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("MY_YOUTRACK", group, true);
    top.add(toolbar.getComponent(), BorderLayout.WEST);
    final SearchTextField field = new SearchTextField(true);
    field.getTextEditor().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        ((MyyIssuesModel) myList.getModel()).setTextSearch(field.getText());
      }
    });
    top.add(field, BorderLayout.EAST);
    add(top, BorderLayout.NORTH);

    startLoading();
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        final Ref<ListModel> model = Ref.create(myList.getModel());
        try {
          model.set(new MyyIssuesModel(myProject, MyyIssueViewer.this));
        } finally {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              myList.setModel(model.get());
              stopLoading();
            }
          });
        }
      }
    });
  }

  @Nullable
  @Override
  public Object getData(@NonNls String dataId) {
    if (PlatformDataKeys.PROJECT.is(dataId)) {
      return myProject;
    }
    if (MyyDataKeys.ISSUES_ARRAY.is(dataId)) {
      Object[] values = myList.getSelectedValues();
      MyyIssue[] issues = new MyyIssue[values.length];
      for (int i = 0; i < values.length; i++) {
        issues[i] = (MyyIssue) values[i];
      }
      return issues;
    }
    return null;
  }

  @Override
  public void dispose() {

  }
}
