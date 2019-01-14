package jab.spigot.language;

/**
 * TODO: Document.
 *
 * @author Josh
 */
enum PoolType {
  RANDOM,
  SEQUENTIAL,
  SEQUENTIAL_REVERSED;

  public static PoolType getPoolType(String type) {
    PoolType returned = null;
    type = type.toUpperCase().trim();
    for (PoolType poolType : PoolType.values()) {
      if (poolType.name().equals(type)) {
        returned = poolType;
        break;
      }
    }
    return returned;
  }
}
