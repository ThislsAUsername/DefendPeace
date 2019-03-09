package CommandingOfficers;

import java.io.Serializable;

public interface COMaker extends Serializable
{
  Commander create();
}