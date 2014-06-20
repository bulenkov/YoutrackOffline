package com.intellij.youtrack.editor;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.youtrack.MyyIcons;
import com.intellij.youtrack.model.MyyIssue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class MyyIssueListCellRenderer extends JPanel implements ListCellRenderer {

  private final JLabel myPriority = new JLabel();
  private final JLabel myStatus = new JLabel();
  private final SimpleColoredComponent myId = new SimpleColoredComponent();
  private final JLabel mySummary = new JLabel();
  private final JTextArea myDescription = new JTextArea();
  private final JLabel myTime = new JLabel();
  private final SimpleTextAttributes ID = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, UIUtil.getListForeground(false));
  private final SimpleTextAttributes ID_SELECTED = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, UIUtil.getListForeground(true));
  private final SimpleTextAttributes ID_FIXED = new SimpleTextAttributes(SimpleTextAttributes.STYLE_STRIKEOUT, UIUtil.getListForeground(false));
  private final SimpleTextAttributes ID_FIXED_SELECTED = new SimpleTextAttributes(SimpleTextAttributes.STYLE_STRIKEOUT, UIUtil.getListForeground(true));
  private final CommentsIndicator myComments = new CommentsIndicator();


  public MyyIssueListCellRenderer() {
    super(new BorderLayout());
    myId.setOpaque(false);
    setBorder(new CustomLineBorder(new JBColor(Gray._220, Gray._85), 0, 0, 1, 0));
    //1. Left small line
    JPanel left = createNonOpaquePanel();
    JLabel bottomStub = new JLabel(" ");
    final String fontName = SystemInfo.isMac ? "Lucida Grande" : SystemInfo.isWindows ? "Arial" : "Verdana";
    Font font = new Font(fontName, Font.PLAIN, 10);
    myPriority.setFont(new Font(fontName, Font.BOLD, 12));
    myPriority.setBorder(new EmptyBorder(2, 6, 2, 2));
    bottomStub.setFont(font);
    left.add(myPriority, BorderLayout.NORTH);
    JPanel p = new JPanel();
    p.setOpaque(false);
    p.add(myStatus);
    myStatus.setMinimumSize(new Dimension(16, 16));
    left.add(p, BorderLayout.CENTER);
    left.add(bottomStub, BorderLayout.SOUTH);
    add(left, BorderLayout.WEST);

    //2. Central area contains other components
    JPanel central = createNonOpaquePanel();
    add(central, BorderLayout.CENTER);

    //3. Create header
    JPanel top = createNonOpaquePanel();
    myId.setFont(new Font(fontName, Font.PLAIN, 12));
    top.add(myId, BorderLayout.CENTER);
    top.add(myTime, BorderLayout.EAST);
    myTime.setFont(new Font(fontName, Font.PLAIN, 10));
    central.add(top, BorderLayout.NORTH);

    //4. Message body
    JPanel main = createNonOpaquePanel();
    JPanel text = createNonOpaquePanel();
    mySummary.setFont(new Font(fontName, Font.BOLD, 12));
    myDescription.setFont(new Font(fontName, Font.PLAIN, 12));
    myDescription.setAutoscrolls(false);

    myDescription.setEditable(false);
    myDescription.setLineWrap(true);
    myDescription.setOpaque(false);
    myDescription.setWrapStyleWord(true);
    main.add(text, BorderLayout.CENTER);
    text.add(mySummary, BorderLayout.NORTH);
    text.add(myDescription, BorderLayout.CENTER);
    central.add(main, BorderLayout.CENTER);

    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.add(myComments);
    central.add(panel, BorderLayout.EAST);
  }

  private JPanel createNonOpaquePanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    return panel;
  }

  @Override
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    init(((MyyIssue) value), isSelected, hasFocus());
    return this;
  }

  private void init(MyyIssue issue, final boolean selected, boolean focused) {
    setBackground(UIUtil.getListBackground(selected));
    myId.clear();
    myId.append(issue.getId() + " " + issue.getReporterFullName(), !issue.isResolved() ? selected ? ID_SELECTED : ID : selected ? ID_FIXED_SELECTED : ID_FIXED);
    myId.setIconOnTheRight(true);
    myId.setIcon(issue.hasAttachment() ? MyyIcons.ATTACHMENT : null);
    myTime.setText(issue.getPresentableTime());
    myTime.setForeground(selected ? UIUtil.getListForeground(true) : new JBColor(new Color(75, 107, 244), new Color(87, 120, 173)));
    myPriority.setIcon(issue.isMajor() ? MyyIcons.IMPORTANT : EmptyIcon.ICON_16);
    mySummary.setText("<html>" + issue.getSummary() + "</html>");
    myDescription.setText(StringUtil.unescapeXml(issue.getDescription()));
    myDescription.setForeground(selected ? UIUtil.getListForeground(selected) : new JBColor(Gray._130, Gray._110));
    mySummary.setForeground(selected ? UIUtil.getListForeground(selected) : new JBColor(new Color(0, 68, 105), new Color(160, 110, 0)));

    myComments.setCommentsNumber(issue.getCommentsCount());
    myComments.setForeground(UIUtil.getListBackground(selected));
    myComments.setBackground(selected ? UIUtil.getListForeground(selected) : Gray._180);

    if (issue.isRead()) {
      myStatus.setIcon(EmptyIcon.create(14));
    } else {
      myStatus.setIcon(new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
          GraphicsUtil.setupAAPainting(g);
          if (selected) {
            g.setColor(UIUtil.getListForeground(selected));
          } else {
            ((Graphics2D) g).setPaint(new GradientPaint(x + (c.getWidth() - 10) / 2, y + (c.getHeight() - 10) / 2, new Color(125, 172, 222), x + (c.getWidth() - 10) / 2 + 10, y + (c.getHeight() - 10) / 2 + 10, new Color(69, 86, 182)));
          }
          g.fillOval(x + (c.getWidth() - 10) / 2, y + (c.getHeight() - 10) / 2, 10, 10);
        }

        @Override
        public int getIconWidth() {
          return 14;
        }

        @Override
        public int getIconHeight() {
          return 14;
        }
      });
    }
  }
}
