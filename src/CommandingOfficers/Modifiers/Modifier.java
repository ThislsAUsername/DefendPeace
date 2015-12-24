package CommandingOfficers.Modifiers;

import CommandingOfficers.Commander;

public class Modifier {
	public Commander target;
	public boolean done = false;
	private boolean applied = false;
	
	public void initTurn(){
		if (applied) {
			applyMod();
		} else {
			removeMod();
		}
	}
	
	private void applyMod(){
		// provided as a sample
		for(int i = 0; i < target.unitModels.length; i++) {
			if(target.unitModels[i].movePower == 6) {
				target.unitModels[i].movePower += 0;
			}
		}
	}
	private void removeMod(){}
}
