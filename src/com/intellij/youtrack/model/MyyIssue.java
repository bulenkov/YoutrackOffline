package com.intellij.youtrack.model;

import com.intellij.CommonBundle;
import com.intellij.openapi.util.Clock;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.text.DateFormatUtil;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class MyyIssue {
  private final String id;
  private long created;
  private long updated;
  private MyyUser from;
  private String priority;
  private int commentsCount;
  private int votes;
  private String type;
  private String project;
  private String state;
  private String subsystem;
  private String summary;
  private String description;
  private Element xml;
  private String myPresentableTime;
  private String reporterFullName;
  private List<MyyComment> comments = new ArrayList<MyyComment>(0);
  private boolean read;

  public MyyIssue(Element xml) {
    this.xml = xml;
    id = xml.getAttributeValue("id");
    initFields();
    String value = xml.getAttributeValue("read");
    if (value != null) {
      read = "true".equals(value);
    }
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
    xml.setAttribute("read", String.valueOf(read));
  }

  private void initFields() {
    for (Element field : JDOMUtil.getChildren(xml, "field")) {
      String name = field.getAttributeValue("name");
      if ("projectShortName".equals(name)) {
        project = getValue(field);
      } else if ("summary".equals(name)) {
        summary = getValue(field);
      } else if ("description".equals(name)) {
        description = getValue(field);
      } else if ("created".equals(name)) {
        created = Long.parseLong(getValue(field));
      } else if ("updated".equals(name)) {
        updated = Long.parseLong(getValue(field));
      } else if ("reporterFullName".equals(name)) {
        reporterFullName = getValue(field);
      } else if ("commentsCount".equals(name)) {
        commentsCount = Integer.parseInt(getValue(field));
      } else if ("votes".equals(name)) {
        votes = Integer.parseInt(getValue(field));
      } else if ("Priority".equals(name)) {
        priority = getValue(field);
      } else if ("State".equals(name)) {
        state = getValue(field);
      } else if ("Subsystem".equals(name)) {
        subsystem = getValue(field);
      }
    }

    for (Element comment : JDOMUtil.getChildren(xml, "comment")) {
      comments.add(new MyyComment(comment.getAttributeValue("text"),
                                  comment.getAttributeValue("authorFullName"),
                                  Long.parseLong(comment.getAttributeValue("created"))));
    }
  }

  private String getValue(Element field) {
    return field.getChild("value").getValue();
  }

  public String getId() {
    return id;
  }

  public long getCreated() {
    return created;
  }

  public String getReporterFullName() {
    return reporterFullName;
  }

  public List<MyyComment> getComments() {
    return comments;
  }

  public long getUpdated() {
    return updated;
  }

  public MyyUser getFrom() {
    return from;
  }

  public String getPriority() {
    return priority;
  }

  public int getCommentsCount() {
    return commentsCount;
  }

  public int getVotes() {
    return votes;
  }

  public String getType() {
    return type;
  }

  public String getState() {
    return state;
  }

  public String getSubsystem() {
    return subsystem;
  }

  public String getSummary() {
    return summary;
  }

  public String getDescription() {
    return description == null ? "" : description;
  }

  public Element toXml() {
    return xml;
  }

  public boolean isResolved() {
    return Arrays.asList("Fixed", "Duplicate", "Won't Fix", "Obsolete", "Can't Reproduce").contains(state); //todo[kb] get from youtrack settings
  }

  public boolean isMajor() {
    return Arrays.asList("Show-Stopper", "Critical", "Major").contains(priority);
  }



  public String getPresentableTime() {
//    if (myPresentableTime != null) {
//      return myPresentableTime;
//    }
    long currentTime = Clock.getTime();

    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(currentTime);

    int currentYear = c.get(Calendar.YEAR);
    int currentDayOfYear = c.get(Calendar.DAY_OF_YEAR);

    c.setTimeInMillis(updated);

    int year = c.get(Calendar.YEAR);
    int dayOfYear = c.get(Calendar.DAY_OF_YEAR);

    boolean isToday = currentYear == year && currentDayOfYear == dayOfYear;
    if (isToday) {
      return DateFormatUtil.formatTime(updated);
    }

    boolean isYesterdayOnPreviousYear =
        (currentYear == year + 1) && currentDayOfYear == 1 && dayOfYear == c.getActualMaximum(Calendar.DAY_OF_YEAR);
    boolean isYesterday = isYesterdayOnPreviousYear || (currentYear == year && currentDayOfYear == dayOfYear + 1);

    if (isYesterday) {
      return CommonBundle.message("date.format.yesterday");
    }

    myPresentableTime = DateFormatUtil.formatDate(updated);
    return myPresentableTime;
  }

  public String getPriorityLetter() {
    return priority.substring(0, 1);
  }

  public boolean hasAttachment() {
    return getAttachmentsNumber() > 0;
  }

  public int getAttachmentsNumber() {
    Element attachments = xml.getChild("attachments");
    return attachments == null ? 0 : attachments.getChildren().size();
  }
}
