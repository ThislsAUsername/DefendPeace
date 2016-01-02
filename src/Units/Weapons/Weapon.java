package Units.Weapons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Scanner;

import Units.Unit;
import Units.UnitModel;

public class Weapon {
	
	private static boolean chartSet = false;
	// format is [attacker][defender]
	private static int[][] damageChart;
	public WeaponModel model;
	public int ammo;
	
	public Weapon(WeaponModel model) {
		this.model = model;
		ammo = model.maxAmmo;
	}

	/**
	 * @return returns its base damage against that unit type
	 */
	public double getDamage(UnitModel.UnitEnum defender) {
		if (ammo == 0 || defender == null)
			return 0;
		return damageChart[model.getIndex()][defender.ordinal()];
	}
	/**
	 * @return returns its base damage against defender if the unit is in range,
	 */
	public double getDamage(int x, int y, Unit defender) {
		if (defender != null)
		{
			int dist = Math.abs(defender.y-y) + Math.abs(defender.x-x);
			if ((dist >= model.minRange) && (dist <= model.maxRange))
				return getDamage(defender.model.type);
		}
		return 0;
	}
	
	public void fire()
	{
	if (ammo > 0)
		ammo--;
	else
		System.out.println("Warning, trying to fire an empty gun!");
	}
	
	/**
	 * @return the amount of ammo reloaded
	 */
	public int reload()
	{
		int difference = model.maxAmmo-ammo;
		ammo = model.maxAmmo;
		return difference;
	}
	
	/**
	 * Tells the Weapon class to read in the INI file for its chart.
	 */
	public static void readINIdata()
	{
		if (!chartSet)
		{
			chartSet = true;
			Scanner scanner;
			try {
				scanner = new Scanner(new File("DamageChart.ini"));
			} catch (FileNotFoundException e) {
				scanner = getAlternateStream();
			}
			int numWeapons, numDefenders;
			numWeapons = scanner.nextInt();
			numDefenders = scanner.nextInt();
			damageChart = new int[numWeapons][numDefenders];
			for (int[] weapon :damageChart)
			{
				for (int i = 0; i < weapon.length; i++)
				{
					weapon[i] = scanner.nextInt();
				}
			}
			scanner.close();
		}
	}
	
	private static Scanner getAlternateStream()
	{
		System.out.println("DamageChart.ini not found. Attempting to generate new copy.");
		String defaultFileContents = "22	19\n55	45	12	5	1	1	14	15	25	5	25	0	0	7	30	0	0	0	0\n0	0	85	55	15	15	75	70	85	65	85	0	0	0	0	0	0	0	0\n65	55	18	6	1	1	20	32	35	6	35	0	0	9	35	0	0	0	0\n70	65	35	6	1	1	45	45	55	4	28	0	0	10	35	0	0	0	0\n25	25	85	55	15	15	75	70	85	65	85	0	0	0	0	1	5	10	1\n75	70	40	6	1	1	45	45	55	5	30	0	0	10	40	0	0	0	0\n30	30	105	85	55	45	105	105	105	105	105	0	0	0	0	10	45	35	10\n105	95	45	8	1	1	45	45	55	7	35	0	0	12	45	0	0	0	0\n35	35	125	105	75	55	125	115	125	115	125	0	0	0	0	15	50	40	15\n125	115	65	10	1	1	65	65	75	17	55	0	0	22	55	0	0	0	0\n90	85	80	70	45	40	70	75	80	75	80	0	0	0	0	40	65	55	60\n95	90	90	80	55	50	80	80	85	85	90	0	0	0	0	55	85	60	85\n105	105	60	25	10	5	50	50	55	45	55	65	75	120	120	0	0	0	0\n0	0	0	0	0	0	0	0	0	0	0	100	100	120	120	0	0	0	0\n0	0	0	0	0	0	0	0	0	0	0	55	100	100	100	0	0	0	0\n110	110	105	105	95	90	105	105	105	95	105	0	0	0	0	75	85	95	95\n0	0	55	55	25	20	60	65	65	25	65	0	0	0	0	25	55	25	25\n75	75	30	6	1	1	20	25	35	6	35	0	0	65	95	0	0	0	0\n95	90	90	80	55	50	80	80	85	85	90	0	0	0	0	50	95	95	95\n0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	90\n0	0	0	0	0	0	0	0	0	0	0	55	65	115	115	0	0	0	0\n0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	55	25	95	55";
		Scanner fileIn = new Scanner(new StringBufferInputStream(defaultFileContents));
		try {
			FileOutputStream fileOut = new FileOutputStream("DamageChart.ini");
			fileOut.write(defaultFileContents.getBytes());
			fileOut.close();
			try {
				fileIn = new Scanner(new File("DamageChart.ini"));
			} catch (FileNotFoundException e) {
				System.out.println("Your filesystem is really borked.");
			}
		} catch (SecurityException e1) {
			System.out.println("The Java runtime doesn't have priviledges to write its ini file out.");
		} catch (FileNotFoundException e1) {
			System.out.println("The Java runtime cannot write its ini file out.");
		} catch (IOException e1) {
			System.out.println("The Java runtime cannot write its ini file out.");
		}
		return fileIn;
	}
}
