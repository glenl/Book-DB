package com.vulturegraphics.glbd;

public class BookTable extends DBTable {
    public BookTable() {
        super("glb_book");
    }

    @Override
    public String createTableSQL() {
        String sql_table[] = new String[] {
            "CREATE TABLE glb_book (",
            "   _id INTEGER PRIMARY KEY AUTOINCREMENT,",
            "   title TEXT NOT NULL",
            ") ;"
        };
        StringBuilder sb = new StringBuilder();
        for (String s : sql_table) {
            sb.append(s + "\n");
        }
        return sb.toString();
    }
    
}
