package jab.spigot.language;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TODO: Document.
 *
 * @author Josh
 */
public class EntryField {

  private final String key;
  private Object value;

  public EntryField(@NotNull String key, @Nullable Object value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return this.key;
  }

  public Object getValue() {
    return this.value;
  }

  public void setValue(@Nullable Object value) {
    this.value = value;
  }

  public boolean isKey(String key) {
    return this.key.equals(key);
  }
}
