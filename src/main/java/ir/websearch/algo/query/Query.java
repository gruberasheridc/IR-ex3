package ir.websearch.algo.query;

public class Query {

	private final Integer id;
	private final String query;

	public Integer getId() {
		return id;
	}

	public String getQuery() {
		return query;
	}

	public static class Builder {
		private Integer id;
		private String query;

		public Builder id(Integer id) {
			this.id = id;
			return this;
		}

		public Builder query(String query) {
			this.query = query;
			return this;
		}

		public Query build() {
			return new Query(this);
		}
	}

	private Query(Builder builder) {
		this.id = builder.id;
		this.query = builder.query;
	}
}
