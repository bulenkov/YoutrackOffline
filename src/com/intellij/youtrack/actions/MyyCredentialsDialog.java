package com.intellij.youtrack.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.youtrack.util.CancellableConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Konstantin Bulenkov
 */
public class MyyCredentialsDialog extends DialogWrapper {
  private final Project myProject;
  private JTextField myServer;
  private JTextField myUsername;
  private JPasswordField myPassword;
  private JButton myTestButton;
  private JPanel myRoot;

  public MyyCredentialsDialog(@NotNull Project project) {
    super(project);
    myProject = project;
    setTitle("Login to YouTrack");
    myTestButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        testConnection();
      }
    });
    init();
  }

  public String getUrl() {
    return myServer.getText();
  }

  public String getPassword() {
    return new String(myPassword.getPassword());
  }

  public String getUser() {
    return myUsername.getText();
  }

  public boolean testConnection() {
//    final YoutrackSession session = new YoutrackSession(getUrl(), getUser(), getPassword());
//    TestConnectionTask task = new TestConnectionTask("Test connection") {
//      public void run(@NotNull ProgressIndicator indicator) {
//        indicator.setText("Connecting to " + getUrl() + "...");
//        indicator.setFraction(0);
//        indicator.setIndeterminate(true);
//        try {
//          myConnection = session.createCancellableConnection();
//          if (myConnection != null) {
//            Future<Exception> future = ApplicationManager.getApplication().executeOnPooledThread(myConnection);
//            while (true) {
//              try {
//                myException = future.get(100, TimeUnit.MILLISECONDS);
//                return;
//              }
//              catch (TimeoutException ignore) {
//                try {
//                  indicator.checkCanceled();
//                }
//                catch (ProcessCanceledException e) {
//                  myException = e;
//                  myConnection.cancel();
//                  return;
//                }
//              }
//              catch (Exception e) {
//                myException = e;
//                return;
//              }
//            }
//          }
//          else {
//            try {
//              testConnection();
//            }
//            catch (Exception e) {
//              e.printStackTrace();
//              myException = e;
//            }
//          }
//        }
//        catch (Exception e) {
//          myException = e;
//        }
//      }
//    };
//    ProgressManager.getInstance().run(task);
//    Exception e = task.myException;
//    if (e == null) {
//      Messages.showMessageDialog(myProject, "Connection is successful", "Connection", Messages.getInformationIcon());
//    }
//    else if (!(e instanceof ProcessCanceledException)) {
//      String message = e.getMessage();
//      if (e instanceof UnknownHostException) {
//        message = "Unknown host: " + message;
//      }
//      if (message == null) {
//        e.printStackTrace();
//        message = "Unknown error";
//      }
//      Messages.showErrorDialog(myProject, StringUtil.capitalize(message), "Error");
//    }
//    return e == null;
    return true;
  }


  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return myRoot;
  }

  private abstract class TestConnectionTask extends com.intellij.openapi.progress.Task.Modal {

    protected Exception myException;

    @Nullable
    protected CancellableConnection myConnection;

    public TestConnectionTask(String title) {
      super(MyyCredentialsDialog.this.myProject, title, true);
    }

    @Override
    public void onCancel() {
      if (myConnection != null) {
        myConnection.cancel();
      }
    }
  }

}
