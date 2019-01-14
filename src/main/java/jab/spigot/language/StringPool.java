package jab.spigot.language;

import org.jetbrains.annotations.NotNull;

/**
 * TODO: Document.
 *
 * @author Josh
 */
public class StringPool {

  private String[] strings;
  private PoolType type;

  private int index;

  public StringPool(@NotNull PoolType type) {
    strings = new String[0];
    this.type = type;
    this.index = 0;
  }

  @Override
  public String toString() {
    return roll();
  }

  public String roll() {
    // No need to try calculating an empty string pool.
    String returned = null;
    if (!isEmpty()) {
      if (type == PoolType.RANDOM) {
        returned = strings[LanguagePackage.random.nextInt(strings.length)];
      } else if (type == PoolType.SEQUENTIAL) {
        returned = strings[index];
        if (index == strings.length - 1) {
          index = 0;
        } else {
          index++;
        }
      } else if (type == PoolType.SEQUENTIAL_REVERSED) {
        returned = strings[index];
        if (index == 0) {
          index = strings.length - 1;
        } else {
          index--;
        }
      }
    }
    return returned;
  }

  private boolean isEmpty() {
    return strings.length == 0;
  }

  public void add(@NotNull String string) {
    if (strings.length == 0) {
      strings = new String[] {string};
      return;
    }
    String[] newStrings = new String[strings.length + 1];
    System.arraycopy(strings, 0, newStrings, 0, strings.length);
    newStrings[strings.length] = string;
    strings = newStrings;
    if (type == PoolType.SEQUENTIAL_REVERSED) {
      index = strings.length - 1;
    } else {
      index = 0;
    }
  }

  public void clear() {
    strings = new String[0];
    index = 0;
  }

  public PoolType getType() {
    return this.type;
  }
}
