package demo.aux;

public class Gather {
	private int ballotp;
	private int impose;
	private int estimate;
	private int id;
	public Gather(int b, int im, int es, int id) {
		this.ballotp = b;
		this.estimate = es;
		this.impose = im;
		this.id = id;
	}
	@Override
	public String toString() {
		return "Gather [ballotp=" + ballotp + ", impose=" + impose + ", estimate=" + estimate + "]";
	}
	public int getBallotp() {
		return ballotp;
	}
	public int getImpose() {
		return impose;
	}
	public int getEstimate() {
		return estimate;
	}
}
