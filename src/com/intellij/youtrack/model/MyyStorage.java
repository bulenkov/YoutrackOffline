package com.intellij.youtrack.model;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.containers.SortedList;
import com.intellij.youtrack.editor.MyyFileEditor;
import com.intellij.youtrack.model.event.MyyStorageListener;
import com.intellij.youtrack.util.YoutrackSession;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Konstantin Bulenkov
 */
public class MyyStorage extends AbstractProjectComponent {
  private final HashMap<String, MyyUser> myUsers;
  private final HashMap<String, MyyIssue> myIssues;
  private final SortedList<String> mySortedIssues = new SortedList<String>(new Comparator<String>() {
    @Override
    public int compare(String o1, String o2) {
      long l = myIssues.get(o1).getUpdated() - myIssues.get(o2).getUpdated();
      return l == 0 ? 0 : l < 0 ? 1 : -1;
    }
  });
  private ActionCallback myWorking = new ActionCallback.Done();
  private List<MyyStorageListener> myListeners = new ArrayList<MyyStorageListener>();

  public MyyStorage(Project project) {
    super(project);
    myUsers = new HashMap<String, MyyUser>();
    myIssues = new HashMap<String, MyyIssue>();
  }

  public static MyyStorage getInstance(Project project) {
    return (MyyStorage) project.getComponent(MyyStorage.class.getName());
  }

  public Collection<MyyIssue> getAllIssues() {
    return myIssues.values();
  }

  public ActionCallback update() {
    if (isUpdating()) {
      return myWorking;
    }

    myWorking = new ActionCallback();

    refresh(myWorking);

    return myWorking;
  }

  public MyyUser getUser(String id) {
    if (myUsers.containsKey(id)) {
      return myUsers.get(id);
    } else {
      return myUsers.put(id, new MyyUser(id));
    }
  }

  private void refresh(final ActionCallback working) {
    Task.Backgroundable task = new Task.Backgroundable(myProject, "Updating issues from server", true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        YoutrackSession session = new YoutrackSession(myProject);
        long modificationStamp = PropertiesComponent.getInstance(myProject).getOrInitLong("MyyLastTimeUpdated", 0);
        try {
          long started = System.currentTimeMillis();
          List<MyyIssue> issues = session.getIssues(indicator, modificationStamp);
          setIssues(issues);
          PropertiesComponent.getInstance(myProject).setValue("MyyLastTimeUpdated", String.valueOf(started));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onCancel() {
        working.setDone();
      }

      @Override
      public void onSuccess() {
        working.setDone();
        save();
        fireIssuesChanged();
      }
    };
    ProgressManager.getInstance().run(task);
  }

  private void fireIssuesChanged() {
    for (MyyStorageListener listener : myListeners) {
      listener.storageUpdated();
    }
  }

  private void setIssues(List<MyyIssue> issues) {
    for (MyyIssue issue : issues) {
      myIssues.put(issue.getId(), issue);
    }
    mySortedIssues.clear();
    mySortedIssues.addAll(myIssues.keySet());
  }

  public boolean isUpdating() {
    return !myWorking.isDone();
  }

  public void addListener(final MyyStorageListener listener, Disposable parent) {
    myListeners.add(listener);
    Disposer.register(parent, new Disposable() {
      @Override
      public void dispose() {
        myListeners.remove(listener);
      }
    });
  }

  public MyyIssue getIssue(String id) {
    return myIssues.get(id);
  }

  public SortedList<String> getSortedIssues() {
    return mySortedIssues;
  }

  public void save() {
    if (MyyFileEditor.isLogged(myProject)) {
      String path = getIssuesXmlPath();
      final File file = new File(path);
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
              try {
                writeTo(file);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          });
        }
      });
    }
  }

  private String getIssuesXmlPath() {
    String url = new YoutrackSession(myProject).getUrl();
    if (url.toLowerCase().startsWith("http://")) {
      url = url.substring("http://".length());
    }
    if (url.toLowerCase().startsWith("https://")) {
      url = url.substring("https://".length());
    }
    return PathManager.getSystemPath() + File.separator
        + "myy" + File.separator
        + url.replace("\\", "/").replace("/", File.separator);
  }

  public void writeTo(File file) throws IOException {
    if (!file.exists()) {
      if (!file.mkdirs()) {
        throw new IOException("Can't create " + file.getPath());
      }
    }
    File issues = new File(file, "issues.xml");
    if (!issues.exists()) {
      issues.createNewFile();
    }
    Element xml = new Element("issues");
    for (MyyIssue issue : myIssues.values()) {
      Element element = issue.toXml();

      xml.addContent((Element) element.clone());
    }

    JDOMUtil.writeDocument(new Document(xml), issues, "\n");
  }

  public void load() {
    if (MyyFileEditor.isLogged(myProject)) {
      final File file = new File(getIssuesXmlPath() + File.separator + "issues.xml");
      if (file.exists()) {
        ApplicationManager.getApplication().runReadAction(new Runnable() {
          @Override
          public void run() {
            try {
              Document document = JDOMUtil.loadDocument(file);
              Element issues = document.getRootElement();
              ArrayList<MyyIssue> storedIssues = new ArrayList<MyyIssue>();
              for (Object child : issues.getChildren()) {
                if (child instanceof Element) {
                  storedIssues.add(new MyyIssue((Element) child));
                }
              }
              setIssues(storedIssues);
            } catch (JDOMException e) {
              e.printStackTrace();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
      }
    }
  }

  @Override
  public void projectOpened() {
    load();
    fireIssuesChanged();
  }

  @Override
  public void projectClosed() {
    if (MyyFileEditor.isLogged(myProject)) {
      String path = getIssuesXmlPath();
      final File file = new File(path);
      try {
        writeTo(file);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public String getIssueUrl(MyyIssue issue) {
    return new YoutrackSession(myProject).getUrl() + "/issue/" + issue.getId();
  }
}
