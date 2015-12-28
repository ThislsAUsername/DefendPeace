package Units.MoveTypes;

public class FootStandard extends MoveType {

	// format is [weather][terrain]
	public int[][] moveCosts = {{1, 1, 2, 1, 1, 1, 1, 1, 1, 99, 99},
								{1, 1, 3, 2, 1, 1, 1, 1, 1, 99, 99},
								{2, 1, 3, 1, 1, 1, 1, 1, 1, 99, 99},
								{1, 1, 2, 3, 1, 1, 1, 1, 2, 99, 99}};
	public FootStandard() {
		// TODO Auto-generated constructor stub
	}

}
