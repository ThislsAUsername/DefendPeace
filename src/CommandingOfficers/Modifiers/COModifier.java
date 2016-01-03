package CommandingOfficers.Modifiers;

import CommandingOfficers.Commander;

public interface COModifier {
	public abstract void apply(Commander commander);
	public abstract void revert(Commander commander);
}
