package ir.websearch.algo;

public class Document {

	private final Integer id;
	private final String title;
	private final String abst;
	
	public Integer getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getAbst() {
		return abst;
	}

	public static class Builder {
		private Integer id;
		private String title;
		private String abst;

		public Builder id(Integer id) {
			this.id = id;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder abst(String abst) {
			this.abst = abst;
			return this;
		}

		public Document build() {
			return new Document(this);
		}
	}

	private Document(Builder builder) {
		this.id = builder.id;
		this.title = builder.title;
		this.abst = builder.abst;
	}
}
