package Engine.UnitMods;

import java.util.List;

public interface UnitModList
{
  /**
   * @return All applicable modifiers for this and all greater levels of generality
   */
  List<UnitModifier> getModifiers();

  void addUnitModifier(UnitModifier unitModifier);
  void removeUnitModifier(UnitModifier unitModifier);
}
