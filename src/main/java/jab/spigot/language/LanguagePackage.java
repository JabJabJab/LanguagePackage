package jab.spigot.language;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * LanguagePackage is a utility that provides the ability to substitute sections of a string
 * recursively. This allows for Strings to be dynamically edited, and defined anywhere within the
 * String to be injected with EntryFields. Adding to this is the ability to select what Language to
 * choose from, falling back to English if not defined.
 *
 * @author Jab
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class LanguagePackage {

  /** The standard 'line.separator' for most Java Strings. */
  public static final String NEW_LINE = "\n";

  static final Random random = new Random();

  /** The Map for LanguageFiles, assigned with their Languages. */
  private Map<Language, LanguageFile> mapLanguageFiles;

  /** The File Object for the directory where the LanguageFiles are stored. */
  private File directory;

  /**
   * The String name of the LanguagePackage. This is noted in the LanguageFiles as
   * "{{name}}_{{language_abbreviation}}.yml"
   */
  private String name;

  /**
   * Main constructor.
   *
   * @param directory The File Object for the directory where the LanguageFiles are stored.
   * @param name The String name of the LanguagePackage. This is noted in the LanguageFiles as
   *     "{{name}}_{{language_abbreviation}}.yml"
   */
  public LanguagePackage(@NotNull File directory, @NotNull String name) {
    mapLanguageFiles = new HashMap<>();
    setDirectory(directory);
    setPackageName(name);
  }

  /** Loads the LanguagePackage. */
  public void load() {
    String packageName = getPackageName();
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        String name = file.getName().toLowerCase();
        if (name.startsWith(packageName) && name.endsWith(".yml")) {
          Language language =
              Language.getLanguageWithAbbreviation(name.split("_")[1].split("\\.")[0]);
          LanguageFile languageFile = new LanguageFile(file, language);
          languageFile.load();
          mapLanguageFiles.put(language, languageFile);
        }
      }
    }
  }

  /**
   * Appends another language package.
   *
   * @param packageName The name of the package to append.
   */
  public void appendPackage(@NotNull String packageName) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        String name = file.getName().toLowerCase();
        if (name.startsWith(packageName) && name.endsWith(".yml")) {
          Language language =
              Language.getLanguageWithAbbreviation(name.split("_")[1].split("\\.")[0]);
          mapLanguageFiles.get(language).appendFile(file);
        }
      }
    }
  }

  /**
   * @param key The key of the field.
   * @param language The language to use.
   * @param fields Additional field(s) defined. (or overriding the language package)
   * @return Returns an array of TextComponents for the string.
   */
  public TextComponent[] getTexts(
      @NotNull String key, @NotNull Language language, EntryField... fields) {
    String string = getString(key, language, fields);
    if (string == null) return null;
    string = ChatColor.translateAlternateColorCodes('&', string);
    TextComponent[] textComponents;
    if (string.contains("[@")) {
      textComponents = getTexts(string);
    } else {
      textComponents = new TextComponent[1];
      textComponents[0] = new TextComponent(string);
    }
    return textComponents;
  }

  /**
   * Sends a processed String Message to a Player with a English Language, and additionally defined
   * EntryFields.
   *
   * @param player The Player to send the String message.
   * @param key The String identity of the entry to process.
   * @param entries The EntryList Array of any additional entries to process with the String
   *     message.
   */
  public void sendMessage(Player player, String key, EntryField... entries) {
    if (player == null) {
      throw new IllegalArgumentException("Player given is null.");
    }
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Key given is null or empty.");
    }
    if (!player.isOnline()) {
      return;
    }
    String result = this.getString(key, entries);
    if (result != null) {
      sendMessage(player, toStringArray(result));
    }
  }

  /**
   * Sends a processed String Message to a Player with a given Language, and additionally defined
   * EntryFields.
   *
   * @param player The Player to send the String message.
   * @param key The String identity of the entry to process.
   * @param language The Language to use for the Player.
   * @param entries The EntryList Array of any additional entries to process with the String
   *     message.
   */
  public void sendMessage(
      @NotNull Player player,
      @NotNull String key,
      @NotNull Language language,
      EntryField... entries) {
    if (player == null) {
      throw new IllegalArgumentException("Player given is null.");
    }
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Key given is null or empty.");
    }
    if (!player.isOnline()) {
      return;
    }
    String result = this.getString(key, language, entries);
    if (result != null) {
      sendMessage(player, toStringArray(result));
    }
  }

  /**
   * @param key The String identity of the entry to process.
   * @return Returns a processed String. If the English LanguageFile does not contain an entry for
   *     the given String identity, other LanguageFiles are checked. If none of the remaining
   *     LanguageFiles contains an entry, null is returned.
   */
  @NotNull
  public String getAnyString(String key) {
    Language language = Language.English;
    String value = getString(key, language);
    if (value == null) {
      for (Language languageNext : Language.values()) {
        value = getString(key, languageNext);
        if (value != null) {
          break;
        }
      }
    }
    return value;
  }

  /**
   * @param key The String identity of the entry to process.
   * @return Returns a processed String in the English LanguageFile. If the LanguageFile does not
   *     contain an entry, null is returned.
   */
  public String getString(@NotNull String key) {
    return getString(key, Language.English, new EntryField[] {});
  }

  /**
   * @param key The String identity of the entry to process.
   * @param language The Language to search for the String entry primarily.
   * @param entries Any additional EntryFields that add to or override the LanguagePackage library.
   * @return Returns a processed String in the given Language. If the LanguageFile does not contain
   *     an entry, null is returned.
   */
  public String getString(@NotNull String key, @NotNull Language language, EntryField... entries) {
    String value = getString(key, language);
    value = processString(value, this, language, entries);
    return value != null ? ChatColor.translateAlternateColorCodes('&', value) : null;
  }

  /**
   * @param key The String identity of the entry to process.
   * @param entries Any additional EntryFields that add to or override the LanguagePackage library.
   * @return Returns a processed String in the English LanguageFile. If the LanguageFile does not
   *     contain an entry, null is returned.
   */
  public String getString(@NotNull String key, EntryField... entries) {
    return getString(key, Language.English, entries);
  }

  /**
   * @param key The String identity of the entry to process.
   * @param language The Language to search for the String entry primarily.
   * @return Returns a processed String in the given Language. If the LanguageFile does not contain
   *     an entry, null is returned.
   */
  public String getString(@NotNull String key, @NotNull Language language) {
    String value = null;
    LanguageFile file = mapLanguageFiles.get(language);
    if (file != null) {
      value = file.get(key);
    }
    return value;
  }

  /**
   * @param key The String identity of the entry.
   * @param language The language specified.
   * @return Returns the unprocessed entry from a language file.
   */
  private String getFileString(@NotNull String key, @NotNull Language language) {
    LanguageFile fileDefault = mapLanguageFiles.get(Language.English);
    LanguageFile file = mapLanguageFiles.get(language);
    return file != null ? file.get(key) : fileDefault != null ? fileDefault.get(key) : null;
  }

  /**
   * @param key The String identity of the entry to process.
   * @return Returns a List of processed Strings.
   */
  public List<String> getAnyStringList(@NotNull String key) {
    return toList(getAnyString(key));
  }

  /**
   * @param key The String identity of the entry to process.
   * @return Returns a List of processed Strings.
   */
  public List<String> getStringList(@NotNull String key) {
    return toList(getString(key));
  }

  /**
   * @param key The String identity of the entry to process.
   * @param language The Language to search for the String entry primarily.
   * @param entries Any additional EntryFields that add to or override the LanguagePackage library.
   * @return Returns a List of processed Strings.
   */
  public List<String> getStringList(
      @NotNull String key, @NotNull Language language, EntryField... entries) {
    return toList(getString(key, language, entries));
  }

  /**
   * @param key The String identity of the entry to process.
   * @param entries Any additional EntryFields that add to or override the LanguagePackage library.
   * @return Returns a List of processed Strings.
   */
  public List<String> getStringList(@NotNull String key, EntryField... entries) {
    return toList(getString(key, entries));
  }

  /**
   * @param key The String identity of the entry to process.
   * @param language The Language to search for the String entry primarily.
   * @return Returns a List of processed Strings.
   */
  public List<String> getStringList(@NotNull String key, @NotNull Language language) {
    return toList(getString(key, language));
  }

  /** @return Returns the File Object of the directory where the LanguageFiles are located. */
  public File getDirectory() {
    return this.directory;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the File Object of the directory where the LanguageFiles are located.
   *
   * @param directory The File directory to set.
   */
  private void setDirectory(File directory) {
    this.directory = directory;
  }

  /**
   * @return Returns the String name of the LibraryPackage. This is noted in the LanguageFile's as
   *     "{{name}}_{{language_abbreviation}}.yml"
   */
  public String getPackageName() {
    return this.name;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the String name of the LibraryPackage. This is noted in the LanguageFile's as
   * "{{name}}_{{language_abbreviation}}.yml"
   *
   * @param name The String name to set.
   */
  private void setPackageName(String name) {
    this.name = name;
  }

  /**
   * Processes a String with the LanguagePackage library, using the given Language as an option. Any
   * EntryField's passed to this method override entries already defined in the LanguagePackage
   * provided.
   *
   * @param value The String to be processed.
   * @param languagePackage The LanguagePackage library to reference for any EntryField's not
   *     defined that are requested.
   * @param language The Language to use primarily. If EntryField's are not defined, and the
   *     LanguageFile referenced using the Language provided does not have a definition, then the
   *     LanguagePackage's English LanguageFile is checked for that definition.
   * @param entries The EntryField Array to add to or override the LanguagePackage library if
   *     passed.
   * @return Returns the processed String value.
   */
  public static String processString(
      String value, LanguagePackage languagePackage, Language language, EntryField... entries) {
    if (value == null) return null;
    if (languagePackage != null && language == null) language = Language.English;
    StringBuilder valueProcessed = new StringBuilder();
    boolean in = false;
    char[] chars = value.toCharArray();
    String key = "";
    String valNext;
    Map<String, String> mapCachedResults = new HashMap<>();
    for (int index = 0; index < chars.length; index++) {
      char charCurrent = chars[index];
      Character charNext = index < chars.length - 1 ? chars[index + 1] : null;
      if (in) {
        if (charCurrent == '}' && charNext != null && charNext == '}') {
          in = false;
          index += 1;
          key = key.trim();
          boolean skip = false;
          if (key.startsWith("if")) {
            String[] split = key.split(":");
            if (split.length >= 3) {
              String condition = split[1];
              Boolean result = calculateCondition(condition, languagePackage, language, entries);
              // If no variable is found, the entire if block is skipped.
              skip = result == null;
              if (!skip) {
                // IF : BOOLEAN : ACTION : ELSE_ACTION
                if (split.length == 4) {
                  key = result ? split[2] : split[3];
                }
                // IF : BOOLEAN : ACTION
                else if (split.length == 3) {
                  if (result) {
                    key = split[2];
                  } else {
                    skip = true;
                  }
                }
              }
            }
          }
          if (!skip) {
            valNext = mapCachedResults.get(key);
            if (valNext == null) {
              for (EntryField entryNext : entries) {
                if (entryNext != null && entryNext.isKey(key)) {
                  valNext = entryNext.getValue().toString();
                  mapCachedResults.put(key, valNext);
                  break;
                }
              }
            }
            if (valNext == null && languagePackage != null) {
              valNext = languagePackage.getString(key, language, entries);
              if (valNext != null) {
                mapCachedResults.put(key, valNext);
              }
            }
            if (valNext != null) {
              valueProcessed.append(valNext);
            } else {
              valueProcessed.append(key);
            }
          }
        } else {
          key += charCurrent;
        }
      } else {
        if (charCurrent == '{' && charNext != null && charNext == '{') {
          in = true;
          index += 1;
          key = "";
        } else {
          valueProcessed.append(charCurrent);
        }
      }
    }
    return valueProcessed.toString();
  }

  /**
   * Processes a string into a series of TextComponents.
   *
   * @param string The string to process.
   * @return Returns the processed string as TextComponents.
   */
  private static TextComponent[] getTexts(@NotNull String string) {
    int textIndex = 0;
    TextComponent[] textComponents = new TextComponent[1];
    textComponents[textIndex] = new TextComponent();
    boolean in = false;
    boolean inOperator = false;
    StringBuilder stringBuilder = null;
    String operator = null;
    String[] args = new String[0];
    char[] chars = string.toCharArray();
    for (int index = 0; index < chars.length; index++) {
      char charCurrent = chars[index];
      Character charNext = index + 1 >= chars.length ? null : chars[index + 1];
      if (charCurrent == '[' && charNext != null && charNext == '@') {
        in = true;
        inOperator = true;
        stringBuilder = new StringBuilder();
        index++;
        textIndex++;
        TextComponent[] textComponentsNew = new TextComponent[textComponents.length + 1];
        System.arraycopy(textComponents, 0, textComponentsNew, 0, textComponents.length);
        textComponents = textComponentsNew;
        continue;
      }
      if (in) {
        if (charCurrent == ']') {
          if (inOperator) {
            throw new IllegalArgumentException("Invalid operation  format for line: " + string);
          }
          // Save the last argument.
          String[] argsNew = new String[args.length + 1];
          System.arraycopy(args, 0, argsNew, 0, args.length);
          argsNew[args.length] = stringBuilder.toString();
          args = argsNew;
          stringBuilder = new StringBuilder();
          in = false;
          // Set the current text component using the operator and arguments given.
          textComponents[textIndex++] = createActionTextComponent(operator, args);
          // Increment the array.
          TextComponent[] textComponentsNew = new TextComponent[textComponents.length + 1];
          System.arraycopy(textComponents, 0, textComponentsNew, 0, textComponents.length);
          textComponentsNew[textComponents.length] = new TextComponent();
          textComponents = textComponentsNew;
          args = new String[0];
          continue;
        }
        if (inOperator) {
          if (charCurrent == ':') {
            inOperator = false;
            operator = stringBuilder.toString();
            stringBuilder = new StringBuilder();
            continue;
          }
          stringBuilder.append(charCurrent);
          continue;
        }
        if (charCurrent == ':') {
          // Save the next argument.
          String[] argsNew = new String[args.length + 1];
          System.arraycopy(args, 0, argsNew, 0, args.length);
          argsNew[args.length] = stringBuilder.toString();
          args = argsNew;
          stringBuilder = new StringBuilder();
          continue;
        }
        stringBuilder.append(charCurrent);
        continue;
      }
      // Add the next character.
      textComponents[textIndex].setText(textComponents[textIndex].getText() + charCurrent);
    }
    return textComponents;
  }

  /**
   * @param condition The string to process and calculate.
   * @param languagePackage The language-package instance.
   * @param language The language to process the result.
   * @param entries Additional field(s) above the language-package supplied upstream.
   * @return Returns true if all condition(s) pass the test. &br Returns false if the condition(s)
   *     do not pass the test. Returns null if no condition(s) can be located to test.
   */
  private static Boolean calculateCondition(
      String condition, LanguagePackage languagePackage, Language language, EntryField... entries) {
    condition = condition.trim();
    if (condition.contains("&&")) {
      String[] split = condition.split("&&");
      Boolean[] results = new Boolean[split.length];
      for (int index = 0; index < split.length; index++) {
        results[index] = calculateCondition(split[index], languagePackage, language, entries);
        // All of them have to be true to return true.
        if (results[index] == null || !results[index]) return false;
      }
    } else if (condition.contains("||")) {
      String[] split = condition.split(Pattern.quote("||"));
      Boolean[] results = new Boolean[split.length];
      for (int index = 0; index < split.length; index++) {
        results[index] = calculateCondition(split[index], languagePackage, language, entries);
        // Only one of them has to be true to return true.
        if (results[index] != null && results[index]) return true;
      }
    }
    boolean invert = false;
    Boolean result = null;
    if (condition.contains("==")) {
      String[] split = condition.split("==");
      String key = split[0].trim();
      // Calculate the boolean result.
      for (EntryField field : entries) {
        if (field.isKey(key)) {
          result = field.getValue().toString().trim().equalsIgnoreCase(split[1].trim());
          break;
        }
      }
    } else if (condition.contains("!=")) {
      String[] split = condition.split("!=");
      String key = split[0].trim();
      // Calculate the boolean result.
      for (EntryField field : entries) {
        if (field.isKey(key)) {
          invert = true;
          result = field.getValue().toString().trim().equalsIgnoreCase(split[1].trim());
          break;
        }
      }
    } else {
      invert = condition.startsWith("!");
      if (invert) condition = condition.substring(1);
      // Calculate the boolean result.
      for (EntryField field : entries) {
        if (field.isKey(condition)) {
          Object o = field.getValue();
          if (o == null) {
            result = false;
          } else if (o instanceof Boolean) {
            result = (Boolean) o;
          } else if (o instanceof Number) {
            result = ((Number) o).intValue() > 0;
          } else {
            String os = o.toString();
            result =
                os.equalsIgnoreCase("true")
                    || (!os.equalsIgnoreCase("false") && !os.equalsIgnoreCase("0"));
          }
          // We have found the variable.
          break;
        }
      }
      if (result == null) {
        String test = languagePackage.getString(condition, language, entries);
        if (test != null) {
          result =
              test.equalsIgnoreCase("true")
                  || (!test.equalsIgnoreCase("false") && !test.equalsIgnoreCase("0"));
        }
      }
    }
    // Invert the variable if the flag is set.
    if (result != null && invert) result = !result;
    return result;
  }

  /**
   * Creates a text component with the given operation & arguments.
   *
   * <h3>Argument types:</h3>
   *
   * <ul>
   *   <li><b>command</b> : Runs a command when clicked.
   *       <ul>
   *         <li><b>[0]</b> => command
   *         <li><b>[1]</b> => text
   *       </ul>
   *   <li><b>hover</b> : Sets text shown when hovering over the chat text.
   *       <ul>
   *         <li><b>[0]</b> => hover text
   *         <li><b>[1]</b> => chat text
   *       </ul>
   * </ul>
   *
   * @param operator The operation to perform for interacting with the text component.
   * @param args The arguments provided for the operation.
   * @return Returns a TextComponent with the ClickEvent set for it.
   * @throws NullPointerException Thrown if the operator given is null.
   * @throws IllegalArgumentException Thrown if the amount of arguments given are not equal to the
   *     required amount of arguments for the operator.
   */
  @NotNull
  private static TextComponent createActionTextComponent(@NotNull String operator, String... args) {
    operator = operator.toLowerCase().trim();
    if (operator.equals("command")) {
      if (args.length != 2) {
        throw new IllegalArgumentException(
            "The operator '@command' should only have 2 arguments. (" + args.length + " provided)");
      }
      TextComponent textComponent = new TextComponent(args[1]);
      textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, args[0]));
      return textComponent;
    } else if (operator.equals("hover")) {
      if (args.length != 2) {
        throw new IllegalArgumentException(
            "The operator '@hover' should only have 2 arguments. (" + args.length + " provided)");
      }
      TextComponent textComponent = new TextComponent(args[1]);
      textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, args[0]));
    }
    throw new IllegalArgumentException("The operator '@" + operator + " is unknown.");
  }

  /**
   * Converts any object given to a string.
   *
   * <p>NOTE: Lists are converted to a string with new-lines added.
   *
   * @param object The object to process.
   * @return Returns the object as a string.
   */
  public static String toAString(@NotNull Object object) {
    if (object instanceof List) {
      StringBuilder stringBuilder = new StringBuilder();
      for (Object entry : (List) object) {
        String line = entry.toString();
        if (stringBuilder.length() == 0) {
          stringBuilder.append(line);
        } else {
          stringBuilder.append(LanguagePackage.NEW_LINE).append(line);
        }
      }
      return stringBuilder.toString();
    } else {
      return object.toString();
    }
  }

  /**
   * Sends a String Array of messages to a Player.
   *
   * @param sender The Player receiving the messages.
   * @param lines The String Array of messages to send.
   */
  public static void sendMessage(CommandSender sender, String[] lines) {
    if (lines == null || lines.length == 0) return;
    if (sender == null) throw new IllegalArgumentException("CommandSender given is null.");
    sender.sendMessage(lines);
  }

  /**
   * Sends a List of String messages to a Player.
   *
   * @param sender The Player receiving the messages.
   * @param lines The List of String messages to send.
   */
  public static void sendMessage(CommandSender sender, List<String> lines) {
    if (sender == null) throw new IllegalArgumentException("CommandSender given is null.");
    String[] array = toStringArray(lines);
    if (array != null) {
      sender.sendMessage(array);
    }
  }

  /**
   * Broadcasts a List of String messages to all Players on a server.
   *
   * @param lines The List of String messages to send.
   */
  public static void broadcastMessages(List<String> lines) {
    if (lines == null || lines.size() == 0) return;
    for (String line : lines) {
      Bukkit.broadcastMessage(line);
    }
  }

  /**
   * @param string The String to partition with the '\n' operator.
   * @return Returns a List of Strings, partitioned by the '\n' operator.
   */
  public static List<String> toList(String string) {
    if (string == null) return null;
    List<String> list = new LinkedList<>();
    Collections.addAll(list, string.split(NEW_LINE));
    return list;
  }

  /**
   * Converts a List of Strings to a String Array.
   *
   * @param list The List to convert.
   * @return Returns a String Array of the String Lines in the List provided.
   */
  public static String[] toStringArray(List<String> list) {
    if (list == null) return null;
    String[] returned = new String[list.size()];
    for (int index = 0; index < list.size(); index++) {
      returned[index] = list.get(index);
    }
    return returned;
  }

  /**
   * @param string The String to partition with the '\n' operator.
   * @return Returns a String Array, partitioned by the '\n' operator.
   */
  public static String[] toStringArray(String string) {
    if (string == null) return null;
    return ChatColor.translateAlternateColorCodes('&', string).split(NEW_LINE);
  }

  /**
   * @param value The String to be processed.
   * @param entries The EntryField Array to add to or override the LanguagePackage. library if
   *     passed.
   * @return Returns the processed String.
   */
  public static String processString(String value, EntryField... entries) {
    return processString(value, null, Language.English, entries);
  }
}
