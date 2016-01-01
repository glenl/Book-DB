package com.vulturegraphics.glbd;

public class BookAuthorTable extends DBTable {
    public BookAuthorTable() {
        super("glb_book_author");
    }

    @Override
    public String createTableSQL() {
        String sql_table[] = new String[] {
            "CREATE TABLE glb_book_author (",
            "   book_id INTEGER NOT NULL REFERENCES glb_book (_id),",
            "   author_id INTEGER NOT NULL REFERENCES glb_author (_id),",
            "   PRIMARY KEY (book_id, author_id)",
            ") WITHOUT ROWID ;"
        };
        StringBuilder sb = new StringBuilder();
        for (String s : sql_table) {
            sb.append(s + "\n");
        }
        return sb.toString();
    }
    
}
