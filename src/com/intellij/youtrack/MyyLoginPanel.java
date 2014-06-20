package com.intellij.youtrack;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.ui.Gray;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.youtrack.actions.OpenYouTrackAction;
import com.intellij.youtrack.editor.MyyFileEditor;
import com.intellij.youtrack.fs.MyyFileSystem;
import com.intellij.youtrack.util.CancellableConnection;
import com.intellij.youtrack.util.YoutrackSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Konstantin Bulenkov
 */
public class MyyLoginPanel {
  private JPanel myFieldsPanel;
  private JTextField myHost;
  private JTextField myLogin;
  private JPasswordField myPassword;
  private JButton myLoginButton;
  private JPanel myRoot;
  private JLabel myErrorLabel;
  private Project myProject;

  public MyyLoginPanel(Project project) {
    myProject = project;
    myLoginButton.setUI(new BasicButtonUI() {
      @Override
      public void paint(Graphics g, JComponent c) {
        GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
        ((Graphics2D) g).setPaint(new GradientPaint(0, 0, new Color(225, 123, 27), 0, c.getHeight(), new Color(219, 90, 9)));
        g.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 7, 7);

        if (c.hasFocus()) {
          g.setColor(new Color(255, 255, 255));
          g.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 7, 7);
          g.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, 5, 5);
        } else {
          g.setColor(Color.BLACK);
          g.drawRoundRect(0,0, c.getWidth()-1, c.getHeight()-1, 7, 7);
        }
        config.restore();
        super.paint(g, c);
      }
    });
    myLoginButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        doCheckLogin();
      }
    });

    KeyAdapter tab = new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.getComponent().transferFocus();
        }
      }
    };

    myHost.addKeyListener(tab);
    myLogin.addKeyListener(tab);
    KeyAdapter enter = new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          doCheckLogin();
        }
      }
    };
    myLoginButton.addKeyListener(enter);
    myPassword.addKeyListener(enter);


  }

  private void doCheckLogin() {
    YoutrackSession.saveCredentials(myProject, getHostName(), getLogin().getText(), new String(getPassword().getPassword()));
    myErrorLabel.setText("");
    testConnection();
  }

  private String getHostName() {
    String text = getHost().getText();
    if (text.endsWith("/")) {
      text = text.substring(0, text.length() - 1);
    }
    if (!text.contains("://")) {
      text = "http://" + text;
    }
    return text;
  }

  public boolean testConnection() {
    final YoutrackSession session = new YoutrackSession(myProject);
    TestConnectionTask task = new TestConnectionTask("Test connection") {
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setText("Connecting to " + getHostName() + "...");
        indicator.setFraction(0);
        indicator.setIndeterminate(true);
        try {
          myConnection = session.createCancellableConnection();
          if (myConnection != null) {
            Future<Exception> future = ApplicationManager.getApplication().executeOnPooledThread(myConnection);
            while (true) {
              try {
                myException = future.get(100, TimeUnit.MILLISECONDS);
                return;
              }
              catch (TimeoutException ignore) {
                try {
                  indicator.checkCanceled();
                }
                catch (ProcessCanceledException e) {
                  myException = e;
                  myConnection.cancel();
                  return;
                }
              }
              catch (Exception e) {
                myException = e;
                return;
              }
            }
          }
          else {
            try {
              testConnection();
            }
            catch (Exception e) {
              e.printStackTrace();
              myException = e;
            }
          }
        }
        catch (Exception e) {
          myException = e;
        }
      }
    };
    ProgressManager.getInstance().run(task);
    Exception e = task.myException;
    if (e == null) {
//      Messages.showMessageDialog(myProject, "Connection is successful", "Connection", Messages.getInformationIcon());
      MyyFileEditor.setLogged(myProject, true);
      final MyyFileSystem.MyyVirtualFile f = MyyFileSystem.getInstance().getFile();
      FileEditorManagerEx mgr = FileEditorManagerEx.getInstanceEx(myProject);
      mgr.closeFile(f);
      OpenYouTrackAction.open(myProject);

    }
    else if (!(e instanceof ProcessCanceledException)) {
      String message = e.getMessage();
      myErrorLabel.setText("Unknown host: " + message);
      if (message == null) {
        e.printStackTrace();
        myErrorLabel.setText("Unknown error");
      }
//      Messages.showErrorDialog(myProject, StringUtil.capitalize(message), "Error");
    }
    return e == null;
  }


  private void createUIComponents() {
    myFieldsPanel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g2) {
        Graphics2D g = (Graphics2D) g2;
        g.setColor(Color.WHITE);
        g.fillRect(0,0,getWidth(), getHeight());
        g.setPaint(Gray._0.withAlpha(10));
        g.fillRoundRect(0, 5, getWidth(), getHeight() - 5, 17, 17);
        GradientPaint paint = new GradientPaint(0, 0, new Color(65, 113, 193), getWidth(), getHeight()-10, new Color(18, 74, 177));
        g.setPaint(paint);
        g.fillRoundRect(3, 0, getWidth() - 6, getHeight() - 5, 9, 9);
        g.setColor(new Color(44, 96, 185));
        g.drawRoundRect(3, 0, getWidth() - 6, getHeight() - 5, 9, 9);
      }
    };
  }

  public JPanel getRoot() {
    return myRoot;
  }

  public JTextField getHost() {
    return myHost;
  }

  public JTextField getLogin() {
    return myLogin;
  }

  public JPasswordField getPassword() {
    return myPassword;
  }

  private abstract class TestConnectionTask extends com.intellij.openapi.progress.Task.Modal {

    protected Exception myException;

    @Nullable
    protected CancellableConnection myConnection;

    public TestConnectionTask(String title) {
      super(MyyLoginPanel.this.myProject, title, true);
    }

    @Override
    public void onCancel() {
      if (myConnection != null) {
        myConnection.cancel();
      }
    }
  }

}
