package Engine.UnitMods;

import java.util.List;

public interface UnitModList
{
  /**
   * @return All applicable modifiers for this and all greater levels of generality
   */
  List<UnitModifier> getModifiers();

  default void apply(UnitModifier unitModifier)
  {
    unitModifier.applyToUMLImpl(this);
  }

  void remove(UnitModifier unitModifier);
}
