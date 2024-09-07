package UI;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;

import CommandingOfficers.Commander;
import UI.Art.SpriteArtist.ColorPalette;
import UI.Art.SpriteArtist.SpriteLibrary;

public class UIUtils
{
  public static final String DEFAULT_FACTION_NAME = "Thorn";

  // Define extra colors as needed.
  private static final Color VIOLET = new Color( 231, 123, 255 );
  private static final Color ROSE = Color.PINK;
  private static final Color CYAN = Color.CYAN;
  private static final Color ORANGE = Color.ORANGE;

  // Map Building colors.
  public static final Color[] defaultMapColors = { new Color(40, 40, 40), new Color(70, 70, 70), new Color(110, 110, 110), new Color(160, 160, 160),
      new Color(200, 200, 200), new Color(231, 231, 231) };
  private static Color[] pinkMapBuildingColors = { new Color(142, 26, 26), new Color(255, 219, 74), new Color(190, 90, 90), new Color(240, 140, 140),
      new Color(250, 190, 190), new Color(255, 245, 245) };
  private static Color[] cyanMapBuildingColors = { new Color(0, 105, 105), new Color(255, 219, 74), new Color(77, 157, 157), new Color(130, 200, 200),
      new Color(200, 230, 230), new Color(245, 255, 255) };
  private static Color[] orangeMapBuildingColors = { new Color(130, 56, 0), new Color(255, 237, 29), new Color(139, 77, 20), new Color(231, 139, 41),
      new Color(243, 186, 121), new Color(255, 234, 204) };
  private static Color[] purpleMapBuildingColors = { new Color(90, 14, 99), new Color(255, 207, 95), new Color(133, 65, 130), new Color(174, 115, 189),
    new Color(222, 171, 240), new Color(255, 231, 255) };

  // Map Unit colors.
  private static Color[] pinkMapUnitColors = { new Color(142, 26, 26), new Color(199, 62, 62), new Color(248, 100, 100), new Color(255, 136, 136),
      new Color(255, 175, 175), new Color(255, 201, 201) };
  private static Color[] cyanMapUnitColors = { new Color(0, 105, 105), new Color(0, 170, 170), new Color(0, 215, 215), new Color(0, 245, 245),
      new Color(121, 255, 255), new Color(195, 255, 255), };
  private static Color[] orangeMapUnitColors = { new Color(130, 56, 0), new Color(204, 103, 7), new Color(245, 130, 14), new Color(255, 160, 30),
      new Color(255, 186, 60), new Color(255, 225, 142), };
  private static Color[] purpleMapUnitColors = { new Color(90, 14, 99), new Color(132, 41, 148), new Color(181, 62, 198), new Color(201, 98, 223),
    new Color(231, 123, 255), new Color(243, 180, 255), };

  private static Map<Color, ColorPalette> buildingColorPalettes;
  private static Map<Color, ColorPalette> mapUnitColorPalettes;
  private static Map<Color, String> paletteNames;
  private static ArrayList<Faction> factions;


  /**
   * Parses the available palettes and faction unit images.
   * Both of the above are parsed from res/unit/faction.
   * For palettes:
   *   Reads in the first 6 colors on each row.
   *   Bottom colors are buildings, top are units.
   *   The key color is the last color on the top row.
   * For factions:
   *   The name of the folder is simply collected; no further work is done until units are drawn.
   */
  private static void initCosmetics()
  {
    if (null == mapUnitColorPalettes )
    {
      buildingColorPalettes = new LinkedHashMap<Color, ColorPalette>();
      mapUnitColorPalettes = new LinkedHashMap<Color, ColorPalette>();
      paletteNames = new LinkedHashMap<Color, String>();
      factions = new ArrayList<Faction>();

      // Create a mapping of game colors to the fine-tuned colors that will be used for map sprites.
      buildingColorPalettes.put(ROSE, new ColorPalette(pinkMapBuildingColors));
      buildingColorPalettes.put(CYAN, new ColorPalette(cyanMapBuildingColors));
      buildingColorPalettes.put(ORANGE, new ColorPalette(orangeMapBuildingColors));
      buildingColorPalettes.put(VIOLET, new ColorPalette(purpleMapBuildingColors));

      mapUnitColorPalettes.put(ROSE, new ColorPalette(pinkMapUnitColors));
      mapUnitColorPalettes.put(CYAN, new ColorPalette(cyanMapUnitColors));
      mapUnitColorPalettes.put(ORANGE, new ColorPalette(orangeMapUnitColors));
      mapUnitColorPalettes.put(VIOLET, new ColorPalette(purpleMapUnitColors));

      // Throw some color names in there for the defaults
      // toString() is not user-friendly
      paletteNames.put(ROSE, "rose");
      paletteNames.put(CYAN, "cyan");
      paletteNames.put(ORANGE, "orange");
      paletteNames.put(VIOLET, "violet");

      // We want to be able to use the normal units, as well as any others
      factions.add(new Faction(DEFAULT_FACTION_NAME,DEFAULT_FACTION_NAME));

      final File folder = new File(Engine.Driver.JAR_DIR + "res/unit/faction");

      if (folder.canRead())
      {
        PriorityQueue<TeamColorSpec> orderedPalettes = new PriorityQueue<TeamColorSpec>();
        for( final File fileEntry : folder.listFiles() )
        {
          // If it's a file, we assume it's a palette
          if( !fileEntry.isDirectory() )
          {
            String colorName = fileEntry.getAbsolutePath();
            if( colorName.contains(".png") )
            {
              try
              {
                BufferedImage bi = ImageIO.read(new File(colorName));

                // Grab the last color on the first row as our "banner" color
                Color key = new Color(bi.getRGB(bi.getWidth() - 1, 0));

                // Use the last color in the building row to order our colors.
                int ordinal = new Color(bi.getRGB(bi.getWidth() - 1, 1)).getRGB();
                Color[] cUnits = new Color[defaultMapColors.length];
                Color[] cStructs = new Color[defaultMapColors.length];
                for( int i = 0; i < defaultMapColors.length; i++ )
                {
                  cUnits[i] = new Color(bi.getRGB(i, 0));
                  cStructs[i] = new Color(bi.getRGB(i, 1));
                }
                orderedPalettes.offer(new TeamColorSpec(key, ordinal, new ColorPalette(cUnits), new ColorPalette(cStructs)));
                paletteNames.put(key, fileEntry.getName().replace(".png", ""));
              }
              catch (IOException ioex)
              {
                System.out.println("WARNING! Exception loading palette " + colorName);
              }
            }
          }
          // If it's a directory, we assume it's a set of map sprites, i.e. a faction.
          else if(fileEntry.canRead() && !fileEntry.getName().endsWith(DEFAULT_FACTION_NAME)) // However, we don't wanna add our default twice
          {
            String basis = DEFAULT_FACTION_NAME;
            String basisPath = fileEntry.getAbsolutePath() + "/basis.txt";
            if( new File(basisPath).canRead() )
            {
              try
              {
                BufferedReader br = new BufferedReader(new FileReader(basisPath));
                basis = br.readLine().trim();
                br.close();
              }
              catch (IOException ioex)
              {
                System.out.println("WARNING! Exception loading faction basis " + basisPath + ". Defaulting to sprites from " + DEFAULT_FACTION_NAME);
              }
            }

            factions.add(new Faction(fileEntry.getName(), basis));
          }
        }

        // Insert our now-ordered color palettes into the permanent collections in the correct order.
        while(!orderedPalettes.isEmpty())
        {
          TeamColorSpec tcs = orderedPalettes.poll();
          mapUnitColorPalettes.put(tcs.key, tcs.unitColors);
          buildingColorPalettes.put(tcs.key, tcs.buildingColors);
        }
      }
    }
  }

  public static Color[] getCOColors()
  {
    initCosmetics();
    return mapUnitColorPalettes.keySet().toArray(new Color[0]);
  }

  public static ColorPalette getBuildingColors(Color colorKey)
  {
    initCosmetics();
    ColorPalette palette = buildingColorPalettes.get(colorKey);
    if (null == palette) // Uh oh, the player's messing with us. Make stuff up so we don't crash.
    {
      buildingColorPalettes.put(colorKey, buildingColorPalettes.get(ROSE));
      palette = buildingColorPalettes.get(colorKey);
      System.out.println(String.format("WARNING!: Failed to retrieve building palette for color %s, defaulting to %s", colorKey, paletteNames.get(ROSE)));
    }
    return palette;
  }

  public static ColorPalette getMapUnitColors(Color colorKey)
  {
    initCosmetics();
    ColorPalette palette = mapUnitColorPalettes.get(colorKey);
    if (null == palette) // Uh oh, the player's messing with us. Make stuff up so we don't crash.
    {
      mapUnitColorPalettes.put(colorKey, mapUnitColorPalettes.get(ROSE));
      palette = mapUnitColorPalettes.get(colorKey);
      System.out.println(String.format("WARNING!: Failed to retrieve unit palette for color %s, defaulting to %s", colorKey, paletteNames.get(ROSE)));
    }
    return palette;
  }

  public static String getPaletteName(Color colorKey)
  {
    initCosmetics();
    if( !paletteNames.containsKey(colorKey) )
    {
      throw new NullPointerException("Cannot find name for Color " + colorKey);
    }
    return paletteNames.get(colorKey);
  }

  public static Faction[] getFactions()
  {
    initCosmetics();
    return factions.toArray(new Faction[0]);
  }

  public static String getCanonicalFactionName(COSpriteSpec spec)
  {
    if( Color.LIGHT_GRAY.equals(spec.color) )
      return "MISC";
    return getCanonicalFactionName(getPaletteName(spec.color), spec.faction.name);
  }
  public static String getCanonicalFactionName(String palette, String faction)
  {
    if ("red".equalsIgnoreCase(palette) && "frontier".equalsIgnoreCase(faction))
      return "Red Shirts";
    if ("red".equalsIgnoreCase(palette) && "star".equalsIgnoreCase(faction))
      return "Orange Star";
    if ("maroon".equalsIgnoreCase(palette) && "fire".equalsIgnoreCase(faction))
      return "Red Fire";
    if ("new rubinelle".equalsIgnoreCase(palette) && "wolves".equalsIgnoreCase(faction))
      return "New Rubinelle Army";
    return palette + ' ' + faction;
  }

  public static class Faction implements Serializable
  {
    private static final long serialVersionUID = 1L;
    public String name;
    public String basis;
    public boolean flip = false;

    public Faction(String pName, String pBasis)
    {
      name = pName;
      basis = pBasis;
    }
    public Faction(String pName)
    {
      this(pName, SpriteLibrary.DEFAULT_FACTION);
    }
    public Faction()
    {
      this(SpriteLibrary.DEFAULT_FACTION, SpriteLibrary.DEFAULT_FACTION);
    }
  }

  public static class COSpriteSpec implements Serializable
  {
    private static final long serialVersionUID = 1L;
    public final Faction faction;
    public final Color color;

    public COSpriteSpec(Faction faction, Color color)
    {
      this.faction = faction;
      this.color = color;
    }

    public static COSpriteSpec instance(Commander co)
    {
      COSpriteSpec spec = null;
      if( null != co )
        spec = new COSpriteSpec(co.faction, co.myColor);
      return spec;
    }

    @Override
    public String toString()
    {
      if( paletteNames.containsKey(color) )
        return getCanonicalFactionName(this);
      return faction.name;
    }

    /**
     * Attempts to pull the key color for this canon faction from loaded resources.
     * <p>Falls back to Rose
     */
    public static COSpriteSpec fromDisk(String col, String fac)
    {
      initCosmetics();
      COSpriteSpec spec = null;
      final Faction facInst = new Faction(fac);

      for( Entry<Color, String> pair : paletteNames.entrySet() )
      {
        if( pair.getValue().equalsIgnoreCase(col) )
        {
          spec = new COSpriteSpec(facInst, pair.getKey());
        }
      }

      if( null == spec )
        spec = new COSpriteSpec(facInst, ROSE);
      return spec;
    }
  }


  public static final COSpriteSpec MISC = new COSpriteSpec(new Faction("Misc"), Color.LIGHT_GRAY);
  //Ordered by "Thorn, then AWBW turn order"
  public static final COSpriteSpec RT = new COSpriteSpec(new Faction(), ROSE);
  public static final COSpriteSpec CO = new COSpriteSpec(new Faction("Ocean"), CYAN);
  public static final COSpriteSpec OS = COSpriteSpec.fromDisk("Red"   , "Star");
  public static final COSpriteSpec BM = COSpriteSpec.fromDisk("Blue"  , "Moon");
  public static final COSpriteSpec GE = COSpriteSpec.fromDisk("Green" , "Earth");
  public static final COSpriteSpec YC = COSpriteSpec.fromDisk("Yellow", "Comet");
  public static final COSpriteSpec BH = COSpriteSpec.fromDisk("Black" , "Hole");
  public static final COSpriteSpec RF = COSpriteSpec.fromDisk("Maroon", "Fire");
  public static final COSpriteSpec GS = COSpriteSpec.fromDisk("Grey"  , "Sky");
  public static final COSpriteSpec BD = COSpriteSpec.fromDisk("Brown" , "Desert");
  public static final COSpriteSpec AB = COSpriteSpec.fromDisk("Amber" , "Blaze");
  public static final COSpriteSpec JS = COSpriteSpec.fromDisk("Jade"  , "Sun");
  public static final COSpriteSpec CI = COSpriteSpec.fromDisk("Cobalt", "Ice");
  public static final COSpriteSpec PC = COSpriteSpec.fromDisk("Pink"  , "Cosmos");
  public static final COSpriteSpec TG = COSpriteSpec.fromDisk("Teal"  , "Galaxy");
  public static final COSpriteSpec PL = COSpriteSpec.fromDisk("Purple", "Lightning");
  public static final COSpriteSpec AR = COSpriteSpec.fromDisk("Acid"  , "Rain");
  public static final COSpriteSpec WN = COSpriteSpec.fromDisk("White" , "Nova");

  public static final COSpriteSpec BW = COSpriteSpec.fromDisk("Brenner's"     , "Wolves");
  public static final COSpriteSpec NW = COSpriteSpec.fromDisk("New Rubinelle" , "Wolves");
  public static final COSpriteSpec LA = COSpriteSpec.fromDisk("Lazurian"      , "Army");
  public static final COSpriteSpec IDS = COSpriteSpec.fromDisk("IDS"          , "Army");

  public static COSpriteSpec[] CANON_FACTIONS;
  public static SourceGames[] PORTRAIT_CHOICE_ORDER;

  public enum SourceGames
  {
    // In display order, because programming is hard™
    AW1(OS,  "1"),
    AW2(BM,  "2"),
    AW3(GE,  "3"),
    AW4(IDS, ""),
    DEFEND_PEACE(RT, ""),
    AWBW(CO, "BW"); // More? :v

    public final COSpriteSpec uiColorSpec;
    public final String discriminator;
    SourceGames(COSpriteSpec uiColorSpec, String discriminator)
    {
      this.uiColorSpec = new COSpriteSpec(new Faction(this.name()), uiColorSpec.color);
      this.discriminator = discriminator;
    }

    static // This block goes in here so the instances are constructed before it's executed. Null enums ain't fun.
    {
      CANON_FACTIONS = new COSpriteSpec[] { OS, BM, GE, YC, BH,
          // Custom
          RF, GS, BD, AB, JS, CI, PC, TG, PL, AR, WN, RT, CO, IDS, MISC,
          // DoR
          BW, NW, LA, };
      // In order of CO name discriminator resolution
      PORTRAIT_CHOICE_ORDER = new SourceGames[]
        {
            SourceGames.DEFEND_PEACE,
            SourceGames.AW3,
            SourceGames.AW2,
            SourceGames.AW1,
            SourceGames.AW4,
            SourceGames.AWBW,
        };
    }
  }

  private static class TeamColorSpec implements Comparable<TeamColorSpec>
  {
    final ColorPalette unitColors;
    final ColorPalette buildingColors;
    final Color key;
    final int ordinal;

    public TeamColorSpec(Color colorKey, int colorOrdinal, ColorPalette units, ColorPalette buildings)
    {
      key = colorKey;
      ordinal = colorOrdinal;
      unitColors = units;
      buildingColors = buildings;
    }

    @Override
    public int compareTo(TeamColorSpec other)
    {
      return ordinal - other.ordinal;
    }
  }
}
