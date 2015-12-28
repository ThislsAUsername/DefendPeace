package Units.MoveTypes;

public class FootMech extends MoveType {

	// format is [weather][terrain]
	public int[][] moveCosts = {{1, 1, 1, 1, 1, 1, 1, 1, 1, 99, 99},
								{1, 1, 2, 1, 1, 1, 1, 1, 1, 99, 99},
								{1, 1, 1, 1, 1, 1, 1, 1, 1, 99, 99},
								{1, 1, 1, 1, 1, 1, 1, 1, 1, 99, 99}};
	public FootMech() {
		// TODO Auto-generated constructor stub
	}

}
