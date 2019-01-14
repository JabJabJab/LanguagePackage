package jab.spigot.language;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * TODO: Document.
 *
 * @author Josh
 */
@SuppressWarnings("WeakerAccess")
public class LanguageFile {

  private Map<String, Object> mapEntries;
  private File file;
  private Language language;

  /**
   * Main constructor.
   *
   * @param file The file handle.
   * @param language The language specified for the file.
   */
  public LanguageFile(@NotNull File file, @NotNull Language language) {
    this.mapEntries = new HashMap<>();
    setFile(file);
    setLanguage(language);
    if (language == Language.English) {
      setDefaults();
    }
  }

  /** Loads the LanguageFile. */
  public void load() {
    FileConfiguration config = getYaml(getFile());
    for (String key : config.getKeys(false)) {
      Object value;
      if (config.isConfigurationSection(key)) {
        ConfigurationSection section = config.getConfigurationSection(key);
        PoolType type = PoolType.SEQUENTIAL;
        if (section.contains("type")) {
          String sType = section.getString("type");
          type = PoolType.getPoolType(sType);
          if (type == null) {
            type = PoolType.SEQUENTIAL;
            System.out.println(
                "WARNING: ["
                    + key
                    + "] Invalid PoolType: "
                    + sType
                    + ". Using '"
                    + type.name()
                    + "' instead.");
          }
        }
        StringPool stringPool = new StringPool(type);
        List list = section.getList("pool");
        if (list.isEmpty()) {
          System.out.println("WARNING: [" + key + "] Pool is empty!");
        } else {
          for (Object o : list) {
            stringPool.add(LanguagePackage.toAString(o));
          }
        }
        value = stringPool;
      } else {
        value = LanguagePackage.toAString(config.get(key));
      }
      add(key, value);
    }
  }

  /**
   * Appends another language file's contents to the language file.
   *
   * @param file The file handle.
   */
  public void appendFile(@NotNull File file) {
    FileConfiguration config = getYaml(file);
    for (String key : config.getKeys(false)) {
      Object value;
      if (config.isConfigurationSection(key)) {
        ConfigurationSection section = config.getConfigurationSection(key);
        PoolType type = PoolType.SEQUENTIAL;
        if (section.contains("type")) {
          String sType = section.getString("type");
          type = PoolType.getPoolType(sType);
          if (type == null) {
            type = PoolType.SEQUENTIAL;
            System.out.println(
                "WARNING: ["
                    + key
                    + "] Invalid PoolType: "
                    + sType
                    + ". Using '"
                    + type.name()
                    + "' instead.");
          }
        }
        StringPool stringPool = new StringPool(type);
        List list = section.getList("pool");
        if (list.isEmpty()) {
          System.out.println("WARNING: [" + key + "] Pool is empty!");
        } else {
          for (Object o : list) {
            stringPool.add(LanguagePackage.toAString(o));
          }
        }
        value = stringPool;
      } else {
        value = LanguagePackage.toAString(config.get(key));
      }
      add(key, value);
    }
  }

  /**
   * @param key The id of the entry.
   * @return Returns the entry with the given id. If no entry is registered with the given id, null
   *     is returned.
   */
  public String get(@NotNull String key) {
    key = key.toLowerCase();
    if (mapEntries.containsKey(key)) {
      Object o = mapEntries.get(key);
      if (o instanceof StringPool) {
        return ((StringPool) o).roll();
      } else if (o instanceof String) {
        return (String) o;
      } else {
        return o.toString();
      }
    }
    return null;
  }

  /**
   * Adds an entry with the given id.
   *
   * @param key The ID to identify the entry.
   * @param entry The entry to add.
   */
  public void add(String key, Object entry) {
    key = key.toLowerCase();
    mapEntries.put(key, entry);
  }

  /** @return Returns the file handle. */
  public File getFile() {
    return this.file;
  }

  /**
   * Sets the file handle.
   *
   * @param file The file handle to set.
   */
  private void setFile(File file) {
    this.file = file;
  }

  /**
   * Reads a YAML configuration file.
   *
   * @param file The file handle to read.
   * @return Returns the loaded YAML configuration instance for the file.
   */
  private FileConfiguration getYaml(File file) {
    return YamlConfiguration.loadConfiguration(file);
  }

  /** @return Returns the Language. */
  public Language getLanguage() {
    return this.language;
  }

  private void setLanguage(Language language) {
    this.language = language;
  }

  private void setDefaults() {
    add("black", ChatColor.BLACK);
    add("blue", ChatColor.DARK_BLUE);
    add("green", ChatColor.DARK_GREEN);
    add("cyan", ChatColor.DARK_AQUA);
    add("aqua", ChatColor.DARK_AQUA);
    add("red", ChatColor.DARK_RED);
    add("purple", ChatColor.DARK_PURPLE);
    add("pink", ChatColor.LIGHT_PURPLE);
    add("gold", ChatColor.GOLD);
    add("gray", ChatColor.DARK_GRAY);
    add("light_gray", ChatColor.GRAY);
    add("light_blue", ChatColor.BLUE);
    add("light_green", ChatColor.GREEN);
    add("light_cyan", ChatColor.AQUA);
    add("light_aqua", ChatColor.AQUA);
    add("light_red", ChatColor.RED);
    add("light_purple", ChatColor.LIGHT_PURPLE);
    add("yellow", ChatColor.YELLOW);
    add("white", ChatColor.WHITE);
    add("magic", ChatColor.MAGIC);
    add("bold", ChatColor.BOLD);
    add("strike", ChatColor.STRIKETHROUGH);
    add("underline", ChatColor.UNDERLINE);
    add("italic", ChatColor.ITALIC);
    add("reset", ChatColor.RESET);
    add("color_code", ChatColor.COLOR_CHAR);
  }
}
