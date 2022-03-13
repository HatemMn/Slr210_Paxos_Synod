package demo.aux;

public class Gather {
	private Integer ballotp;
	private Integer impose;
	private Integer estimate;
	private Integer id;
	public Gather(Integer b, Integer im, Integer es, Integer id) {
		this.ballotp = b;
		this.estimate = es;
		this.impose = im;
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}

	public Integer getBallotp() {
		return ballotp;
	}
	public Integer getImpose() {
		return impose;
	}
	public Integer getEstimate() {
		return estimate;
	}
}
