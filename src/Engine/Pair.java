package Engine;

import java.io.Serializable;

/**
 * More-or-less stolen semantics from the 'javatuples' library, but homebrewed because I'm just that lazy
 * Simple struct to represent a pair of values
 */
public class Pair<K extends Serializable, V extends Serializable> implements Serializable
{
  private static final long serialVersionUID = 1L;

  public K key;
  public V val;
  private Pair(K key, V val)
  {
    this.key = key;
    this.val = val;
  }
  public static <K extends Serializable, V extends Serializable> Pair<K,V> from(K key, V val)
  {
    return new Pair<K,V>(key, val);
  }
}
