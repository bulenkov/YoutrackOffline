package com.intellij.youtrack.model;

/**
 * @author Konstantin Bulenkov
 */
public class MyyComment {
  private final String myText;
  private final String myAuthor;
  private final long myWhen;

  public MyyComment(String text, String author, long when) {
    myText = text;
    myAuthor = author;
    myWhen = when;
  }

  public String getText() {
    return myText;
  }

  public String getAuthor() {
    return myAuthor;
  }

  public long getWhen() {
    return myWhen;
  }
}
