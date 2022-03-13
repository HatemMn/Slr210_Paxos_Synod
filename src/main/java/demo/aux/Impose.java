package demo.aux;

public class Impose {
	private int id;
	private int ballot;
	private int proposal;
	
	public int getId() {
		return id;
	}
	public int getBallot() {
		return ballot;
	}
	public int getProposal() {
		return proposal;
	}
	public Impose(int ballot, int proposal,int id) {
		this.id = id;
		this.ballot = ballot;
		this.proposal = proposal;
	}
}
