package Engine.StateTrackers;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Handles a list of lists of integer counts
 * @param <ListKeyType> Key to get/set an individual list
 * @param <CountKeyType> Key to get/set an individual value in one of the lists
 */
public class CountManager<ListKeyType, CountKeyType> implements Serializable
{
  private static final long serialVersionUID = 1L;

  private HashMap<ListKeyType, HashMap<CountKeyType, Integer>> countMap = new HashMap<>();

  /**
   * @return Whether there's a list currently for that key
   */
  public boolean hasCountFor(ListKeyType listKey)
  {
    return countMap.containsKey(listKey);
  }
  /**
   * Not really a public API; allows directly managing individual list contents, should the caller want that responsibility.
   * @return The internal representation of that list (created on-demand if necessary)
   */
  public HashMap<CountKeyType, Integer> getCountFor(ListKeyType listKey)
  {
    if( !countMap.containsKey(listKey) )
      countMap.put(listKey, new HashMap<CountKeyType, Integer>());
    return countMap.get(listKey);
  }
  /**
   * Does not modify internal state.
   * @return The requested count value.
   */
  public int getCountFor(ListKeyType listKey, CountKeyType countKey)
  {
    if( !hasCountFor(listKey) )
      return 0;
    HashMap<CountKeyType, Integer> instanceCounts = getCountFor(listKey);
    if( !instanceCounts.containsKey(countKey) )
      return 0;
    return instanceCounts.get(countKey);
  }
  /**
   * Adds one to the specific count value (and begins tracking said value if it was not tracked yet)
   */
  public void incrementCount(ListKeyType listKey, CountKeyType countKey)
  {
    HashMap<CountKeyType, Integer> instanceCounts = getCountFor(listKey);
    int prevCount = getCountFor(listKey, countKey);
    instanceCounts.put(countKey, prevCount + 1);
  }
  public void setCountFor(ListKeyType listKey, CountKeyType countKey, int newCount)
  {
    HashMap<CountKeyType, Integer> instanceCounts = getCountFor(listKey);
    instanceCounts.put(countKey, newCount);
  }
  /**
   * Stops tracking any counts for that list; {@link #hasCountFor(ListKeyType)} will now return false.
   */
  public void resetCountFor(ListKeyType listKey)
  {
    countMap.remove(listKey);
  }
  public void resetCountFor(ListKeyType listKey, CountKeyType countKey)
  {
    if( !countMap.containsKey(listKey) )
      return;
    HashMap<CountKeyType, Integer> instanceCounts = getCountFor(listKey);
    instanceCounts.remove(countKey);
  }
}
