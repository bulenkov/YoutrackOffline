package com.intellij.youtrack.editor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.text.DateFormatUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.youtrack.MyyIcons;
import com.intellij.youtrack.model.MyyComment;
import com.intellij.youtrack.model.MyyIssue;
import com.intellij.youtrack.model.MyyStorage;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Konstantin Bulenkov
 */
public class MyyMessageBrowser extends JBPanel {
  private Project myProject;
  JTextPane myBrowser;

  public MyyMessageBrowser(Project project) {
    super(new BorderLayout());
    setBackground(JBColor.background());
    setCenterImage(MyyIcons.NOTHING);
    myProject = project;

  }

  public void showIssue(final MyyIssue issue) {
    if (myBrowser==null) {
      myBrowser = new JTextPane();
      myBrowser.setMargin(new Insets(0,0,0,0));
      final HTMLEditorKit editorKit = new HTMLEditorKit();
      myBrowser.setEditable(false);
      editorKit.getStyleSheet().addRule(UIUtil.displayPropertiesToCSS(UIUtil.getLabelFont(), UIUtil.getLabelForeground()));
      myBrowser.setEditorKit(editorKit);
      myBrowser.setContentType("text/html");
      myBrowser.addHyperlinkListener(new MyyHyperlinkListener(myProject));
      add(new JBScrollPane(myBrowser, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
      revalidate();
      repaint();
    }
    final String s = generateHtml(issue);
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        myBrowser.setText(s);
      }
    });
  }
  private static final Pattern STACKTRACE_LINE =
      Pattern.compile("[\t]*at [[_a-zA-Z0-9]+\\.]+[_a-zA-Z$0-9]+\\.([a-zA-Z$0-9_]+|<init>)\\(([[[A-Za-z0-9_]+\\.java:[\\d]+]]+|[Native\\sMethod]+|[Unknown\\sSource]+)\\)+[ [~]*\\[[a-zA-Z0-9\\.\\:/]\\]]*");

  private String generateHtml(MyyIssue issue) {
    String id = issue.getId();
    String summary = issue.getSummary();
    String description = html(StringUtil.unescapeXml(issue.getDescription()));

    try {
      String main = FileUtil.loadTextAndClose(MyyMessageBrowser.class.getResourceAsStream("issue.html"));
      String css = FileUtil.loadTextAndClose(MyyMessageBrowser.class.getResourceAsStream(UIUtil.isUnderDarcula() ? "style_dark.css" : "style.css"));
      String url = MyyStorage.getInstance(myProject).getIssueUrl(issue);
      id = "<a href='"+url+"'>" + id + "</a>";
      main = StringUtil.replace(main, new String[]{"{##STYLES}", "{##ID}", "{##Summary}", "{##Description}"}, new String[]{css, id, summary, description});
      if (issue.getCommentsCount() > 0) {
        String template = FileUtil.loadTextAndClose(MyyMessageBrowser.class.getResourceAsStream("comment.html"));
        String result = "";
        int i = 1;
        for (MyyComment comment : issue.getComments()) {
          result += StringUtil.replace(template, new String[]{"{##author}", "{##commentBody}","{##commentDate}", "{##OneOrTwo}"}, new String[]{comment.getAuthor(), comment.getText(), DateFormatUtil.formatDateTime(comment.getWhen()), String.valueOf(i)});
          i = i == 1 ? 2 : 1;
        }
        String comments = FileUtil.loadTextAndClose(MyyMessageBrowser.class.getResourceAsStream("comments.html"));
        comments = StringUtil.replace(comments, "{##comments}", result);
        main = StringUtil.replace(main, "{##comments}", comments);
      } else {
        main = StringUtil.replace(main, "{##comments}", "");
      }
      return main;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return "";
  }

  private String html(String description) {
    if (description == null) {
      return "";
    }
    StringBuffer buf = new StringBuffer();
    boolean listStarted = false;
    boolean preStarted = false;
    List<String> split = StringUtil.split(description, "\n");
    for (String s : split) {
      if (STACKTRACE_LINE.matcher(s).matches()) {
        if (!preStarted) {
          buf.append("<pre>");
          preStarted = true;
        }
        s = s.trim();
        int fqnStart = s.indexOf("at ") + 3;
        int linkStart = s.indexOf('(')+1;
        int linkEnd = s.indexOf(')');
        String line = "-1";
        int ind;
        if ((ind = s.indexOf(':', linkStart)) < linkEnd && ind > 0) {
          line = s.substring(s.indexOf(':') + 1, linkEnd);
        }
        String fqn = s.substring(fqnStart, linkStart);
        if (fqn.lastIndexOf('.') > 0) {
          fqn = fqn.substring(0, fqn.lastIndexOf('.'));
        }
        if (fqn.lastIndexOf('$') > 0) {
          fqn = fqn.substring(0, fqn.lastIndexOf('$'));
        }
        buf.append("  ");
        buf.append(s.substring(0, linkStart));
        buf.append("<a href='ide://" + fqn + "/" + line + "'>");
        buf.append(s.substring(linkStart, linkEnd));
        buf.append("</a>");
        buf.append(s.substring(linkEnd));
        buf.append("\n");
      } else {
        if (preStarted) {
          preStarted = false;
          buf.append("</pre>");
        }
        if (s.startsWith("- ")) {
          if (!listStarted) {
            buf.append("<ul>\n");
            listStarted = true;
          }
          buf.append("<li>").append(s.substring(2)).append("</li>\n");
        } else {
          if (listStarted) {
            listStarted = false;
            buf.append("</ul>\n");
          }
          buf.append(s).append("<br/>");
        }
      }
    }
    return buf.toString();
  }
}
