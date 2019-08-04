package UI;


public class GameOptionInt extends GameOption<Integer>
{
  public GameOptionInt(String name, int min, int max, int stride, int defaultValue)
  {
    super(name, buildArray(min, max, stride), 1);
    for( int i = 0; i < optionList.size(); ++i )
    {
      if( optionList.get(i) == defaultValue )
      {
        setSelectedOption(i);
      }
    }
  }

  private static Integer[] buildArray(int min, int max, int stride)
  {
    int numOptions = 1 + (max - min) / stride;
    Integer[] array = new Integer[numOptions];
    for( int i = 0; i < numOptions; i++ )
    {
      int value = min + (i*stride);
      array[i] = value;
    }
    return array;
  }
}
