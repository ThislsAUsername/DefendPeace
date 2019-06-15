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
    majorRev = 3; // For substantial, save-breaking changes to the game logic.
    minorRev = 0; // For added content or UI updates
    hotfix   = 0; // For bugfixes (hopefully to never see increment)
  }
  
  // TODO: Consider more nuanced validation here
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
