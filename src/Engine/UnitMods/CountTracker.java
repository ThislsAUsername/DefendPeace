package Engine.UnitMods;

import java.io.Serializable;
import java.util.HashMap;

public class CountTracker<RootKeyType, InstanceKeyType> implements Serializable
{
  private static final long serialVersionUID = 1L;

  private HashMap<RootKeyType, HashMap<InstanceKeyType, Integer>> buildCounts = new HashMap<>();

  public boolean hasCountFor(RootKeyType co)
  {
    return buildCounts.containsKey(co);
  }
  public HashMap<InstanceKeyType, Integer> getCountFor(RootKeyType co)
  {
    if( !buildCounts.containsKey(co) )
      buildCounts.put(co, new HashMap<InstanceKeyType, Integer>());
    return buildCounts.get(co);
  }
  public int getCountFor(RootKeyType co, InstanceKeyType coord)
  {
    HashMap<InstanceKeyType, Integer> coCounts = getCountFor(co);
    if( !coCounts.containsKey(coord) )
      coCounts.put(coord, 0);
    return coCounts.get(coord);
  }
  public void incrementCount(RootKeyType co, InstanceKeyType coord)
  {
    HashMap<InstanceKeyType, Integer> coCounts = getCountFor(co);
    int prevCount = getCountFor(co, coord);
    coCounts.put(coord, prevCount + 1);
  }
  public void resetCountFor(RootKeyType co)
  {
    buildCounts.remove(co);
  }
}
