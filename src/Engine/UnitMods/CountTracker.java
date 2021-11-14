package Engine.UnitMods;

import java.io.Serializable;
import java.util.HashMap;

public class CountTracker<RootKeyType, InstanceKeyType> implements Serializable
{
  private static final long serialVersionUID = 1L;

  private HashMap<RootKeyType, HashMap<InstanceKeyType, Integer>> countMap = new HashMap<>();

  public boolean hasCountFor(RootKeyType root)
  {
    return countMap.containsKey(root);
  }
  public HashMap<InstanceKeyType, Integer> getCountFor(RootKeyType root)
  {
    if( !countMap.containsKey(root) )
      countMap.put(root, new HashMap<InstanceKeyType, Integer>());
    return countMap.get(root);
  }
  public int getCountFor(RootKeyType root, InstanceKeyType key)
  {
    if( !hasCountFor(root) )
      return 0;
    HashMap<InstanceKeyType, Integer> instanceCounts = getCountFor(root);
    if( !instanceCounts.containsKey(key) )
      return 0;
    return instanceCounts.get(key);
  }
  public void incrementCount(RootKeyType root, InstanceKeyType key)
  {
    HashMap<InstanceKeyType, Integer> instanceCounts = getCountFor(root);
    int prevCount = getCountFor(root, key);
    instanceCounts.put(key, prevCount + 1);
  }
  public void resetCountFor(RootKeyType co)
  {
    countMap.remove(co);
  }
}
