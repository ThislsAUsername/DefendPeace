package Engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GameVersion implements Serializable
{
  private static final long serialVersionUID = 1L; // This class should always keep the same serialization format.
  
  int majorRev, minorRev, hotfix;
  public GameVersion()
  {
    majorRev = 0;
    minorRev = 1;
    hotfix = 0;
  }
  
  public boolean isEqual(GameVersion other)
  {
    boolean equal = true;
    equal &= (majorRev == other.majorRev);
    equal &= (minorRev == other.minorRev);
    equal &= (hotfix   == other.hotfix);
    return equal;
  }

  @Override
  public String toString()
  {
    return String.format("%s:%s:%s", majorRev, minorRev, hotfix); 
  }

  /**
   * Private method, same signature as in Serializable interface
   *
   * @param stream
   * @throws IOException
   */
  private void writeObject(ObjectOutputStream stream) throws IOException
  {
    stream.writeInt(majorRev);
    stream.writeInt(minorRev);
    stream.writeInt(hotfix);
  }

  /**
   * Private method, same signature as in Serializable interface
   *
   * @param stream
   * @throws IOException
   */
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
  {
    majorRev = stream.readInt();
    minorRev = stream.readInt();
    hotfix   = stream.readInt();
  }
}
