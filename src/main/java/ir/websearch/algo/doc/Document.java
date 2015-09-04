package ir.websearch.algo.doc;

public class Document {
	
	public final static String ID_FIELD = "id";
	public final static String TITLE_FIELD = "title";
	public final static String ABSTRACT_FIELD = "abstract";
	public final static String TEXT_FIELD = "text";

	private final Integer id;
	private final String title;
	private final String abst;
	private final String text;
	
	public Integer getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getAbst() {
		return abst;
	}
	
	public String getText() {
		return text;
	}

	public static class Builder {
		private Integer id;
		private String title;
		private String abst;
		private String text;

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
		
		public Builder text(String text) {
			this.text = text;
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
		this.text = builder.text;
	}

}
