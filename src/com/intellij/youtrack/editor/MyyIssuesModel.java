package com.intellij.youtrack.editor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Alarm;
import com.intellij.util.containers.SortedList;
import com.intellij.youtrack.model.MyyIssue;
import com.intellij.youtrack.model.MyyStorage;
import com.intellij.youtrack.model.event.MyyStorageListener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * @author Konstantin Bulenkov
 */
public class MyyIssuesModel extends AbstractListModel {
  private final Alarm myAlarm;
  private volatile TypeFilter myFilter;
  private String myTextFilter;
  private MyyIssueViewer myViewer;

  public TypeFilter getTypeFilter() {
    return myFilter;
  }

  public static enum TypeFilter {ALL, UNRESOLVED, MAJOR}

  private final MyyStorage myStorage;
  private final SortedList<MyyIssue> myIssues = new SortedList<MyyIssue>(new Comparator<MyyIssue>() {
    @Override
    public int compare(MyyIssue o1, MyyIssue o2) {
      long l = o1.getUpdated() - o2.getUpdated();
      return l == 0 ? 0 : l < 0 ? 1 : -1;
    }
  });

  public MyyIssuesModel(Project project, MyyIssueViewer viewer) {
    myViewer = viewer;
    myStorage = MyyStorage.getInstance(project);
    myStorage.addListener(new MyyStorageListener() {
      @Override
      public void storageUpdated() {
        fireContentsChanged(MyyIssuesModel.this, -1, -1);
      }
    }, viewer);
    setTypeFilter(TypeFilter.UNRESOLVED);
    myAlarm = new Alarm(Alarm.ThreadToUse.SHARED_THREAD, viewer);
  }

  @Override
  public int getSize() {
    return myIssues.size();
  }

  @Override
  public MyyIssue getElementAt(int index) {
    return myIssues.get(index);
  }

  public void setTypeFilter(TypeFilter filter) {
    myFilter = filter;
    if (!StringUtil.isEmpty(getTextFilter())) {
      setTextSearch(getTextFilter());
      return;
    }
    switch (filter) {
      case ALL:
        myIssues.addAll(myStorage.getAllIssues());
        break;
      case UNRESOLVED:
        myIssues.clear();
        for (MyyIssue issue : myStorage.getAllIssues()) {
          if (!issue.isResolved()) {
            myIssues.add(issue);
          }
        }
        break;
      case MAJOR:
        myIssues.clear();
        for (MyyIssue issue : myStorage.getAllIssues()) {
          if (issue.isMajor() && !issue.isResolved()) {
            myIssues.add(issue);
          }
        }

        break;
    }

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        fireContentsChanged(this, -1, -1);
        myViewer.revalidate();
        myViewer.repaint();
      }
    });
  }

  public String getTextFilter() {
    return myTextFilter;
  }

  public void setTextSearch(final String textFilter) {
    myTextFilter = textFilter;
    myAlarm.addRequest(new Runnable() {
      @Override
      public void run() {
        final ArrayList<MyyIssue> issues = new ArrayList<MyyIssue>();
        for (MyyIssue issue : myStorage.getAllIssues()) {
          String filter = getTextFilter();
          if (!filter.equals(textFilter)) return;
          if (StringUtil.isEmpty(filter)) {
            setTypeFilter(myFilter);
            return;
          }
          if ((myFilter == TypeFilter.UNRESOLVED || myFilter == TypeFilter.MAJOR) && issue.isResolved()) continue;
          if (myFilter == TypeFilter.MAJOR && !issue.isMajor()) continue;
          filter = filter.toLowerCase();
          if (issue.getSummary().toLowerCase().contains(filter)
              || issue.getDescription().toLowerCase().contains(filter)
              || issue.getId().toLowerCase().contains(filter)
              || issue.getReporterFullName().toLowerCase().contains(filter)) {
            issues.add(issue);
          }
        }
        synchronized (MyyIssuesModel.this) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              myIssues.clear();
              myIssues.addAll(issues);
              fireContentsChanged(this, -1, -1);
            }
          });
        }
      }
    }, 300);
  }
}
