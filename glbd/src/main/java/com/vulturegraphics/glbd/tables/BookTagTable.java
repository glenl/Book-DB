package com.vulturegraphics.glbd;

public class BookTagTable extends DBTable {
    public BookTagTable() {
        super("glb_book_tags");
    }

    @Override
    public String createTableSQL() {
        String sql_table[] = new String[] {
            "CREATE TABLE glb_book_tags (",
            "    book_id INTEGER NOT NULL REFERENCES glb_book (_id),",
            "    tag_id TEXT NOT NULL REFERENCES glb_tag (name),",
            "    PRIMARY KEY (book_id, tag_id)",
            "    ) WITHOUT ROWID ;"
        };
        StringBuilder sb = new StringBuilder();
        for (String s : sql_table) {
            sb.append(s + "\n");
        }
        return sb.toString();
    }
    
}
