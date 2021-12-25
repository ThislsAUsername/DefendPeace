package Engine.StateTrackers;

import java.io.Serializable;
import java.util.HashMap;

public class CountManager<ListKeyType, CountKeyType> implements Serializable
{
  private static final long serialVersionUID = 1L;

  private HashMap<ListKeyType, HashMap<CountKeyType, Integer>> countMap = new HashMap<>();

  public boolean hasCountFor(ListKeyType listKey)
  {
    return countMap.containsKey(listKey);
  }
  public HashMap<CountKeyType, Integer> getCountFor(ListKeyType listKey)
  {
    if( !countMap.containsKey(listKey) )
      countMap.put(listKey, new HashMap<CountKeyType, Integer>());
    return countMap.get(listKey);
  }
  public int getCountFor(ListKeyType listKey, CountKeyType countKey)
  {
    if( !hasCountFor(listKey) )
      return 0;
    HashMap<CountKeyType, Integer> instanceCounts = getCountFor(listKey);
    if( !instanceCounts.containsKey(countKey) )
      return 0;
    return instanceCounts.get(countKey);
  }
  public void incrementCount(ListKeyType listKey, CountKeyType countKey)
  {
    HashMap<CountKeyType, Integer> instanceCounts = getCountFor(listKey);
    int prevCount = getCountFor(listKey, countKey);
    instanceCounts.put(countKey, prevCount + 1);
  }
  public void resetCountFor(ListKeyType listKey)
  {
    countMap.remove(listKey);
  }
}
