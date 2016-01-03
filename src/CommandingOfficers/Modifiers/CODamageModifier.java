package CommandingOfficers.Modifiers;

import Units.UnitModel;
import CommandingOfficers.Commander;

public class CODamageModifier implements COModifier
{
	private int attackModifier = 0;
	
	public CODamageModifier(int percentChange)
	{
		attackModifier = percentChange;
	}

	@Override
	public void apply(Commander commander) {
		for(UnitModel um : commander.unitModels)
		{
			if(um.weaponModels != null)
			{
				um.modifyDamageRatio(attackModifier);
			}
		}
	}

	@Override
	public void revert(Commander commander) {
		for(UnitModel um : commander.unitModels)
		{
			if(um.weaponModels != null)
			{
				um.modifyDamageRatio(-attackModifier);
			}
		}
	}
}
